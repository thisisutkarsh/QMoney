
package com.crio.warmup.stock.portfolio;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.SECONDS;

import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.web.client.RestTemplate;

public class PortfolioManagerImpl implements PortfolioManager {

  private RestTemplate restTemplate;

  // Caution: Do not delete or modify the constructor, or else your build will
  // break!
  // This is absolutely necessary for backward compatibility
  protected PortfolioManagerImpl(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  // TODO: CRIO_TASK_MODULE_REFACTOR
  // 1. Now we want to convert our code into a module, so we will not call it from
  // main anymore.
  // Copy your code from Module#3
  // PortfolioManagerApplication#calculateAnnualizedReturn
  // into #calculateAnnualizedReturn function here and ensure it follows the
  // method signature.
  // 2. Logic to read Json file and convert them into Objects will not be required
  // further as our
  // clients will take care of it, going forward.

  // Note:
  // Make sure to exercise the tests inside PortfolioManagerTest using command
  // below:
  // ./gradlew test --tests PortfolioManagerTest

  // CHECKSTYLE:OFF
  @Override
  public List<AnnualizedReturn> calculateAnnualizedReturn(List<PortfolioTrade> portfolioTrades, LocalDate endDate) {

    List<AnnualizedReturn> allTradesAnnualizedReturns = new ArrayList<AnnualizedReturn>();
    for (PortfolioTrade trade : portfolioTrades) {
      List<Candle> candles = new ArrayList<Candle>();
      try {
        candles = getStockQuote(trade.getSymbol(), trade.getPurchaseDate(), endDate);
      } catch (Exception e) {
        e.printStackTrace();
      }
      double buyPrice = getOpeningPriceOnStartDate(candles);
      double sellPrice = getClosingPriceOnEndDate(candles);
      Double totalReturns = getTotalReturns(buyPrice, sellPrice);
      double totalYears = getTotalYears(trade.getPurchaseDate(), endDate);
      double exponent = 1.0 / totalYears;
      double annualizedReturn = Math.pow((1 + totalReturns), exponent) - 1.0;
      AnnualizedReturn annualizedReturnObj = new AnnualizedReturn(trade.getSymbol(), annualizedReturn, totalYears);
      allTradesAnnualizedReturns.add(annualizedReturnObj);
    }
    Collections.sort(allTradesAnnualizedReturns, getComparator());
    return allTradesAnnualizedReturns;
  }

  public static double getTotalReturns(Double buyPrice, Double sellPrice) {
    return (sellPrice - buyPrice) / buyPrice;
  }

  public static double getTotalYears(LocalDate startDate, LocalDate endDate) {
    long dateDiff = ChronoUnit.DAYS.between(startDate, endDate);
    Long l = Long.valueOf(dateDiff);
    double totalYears = l.doubleValue();
    return totalYears / 365.0;
  }

  static Double getOpeningPriceOnStartDate(List<Candle> candles) {
    Collections.sort(candles, (x, y) -> x.getDate().compareTo(y.getDate()));
    return candles.get(0).getOpen();
  }

  public static Double getClosingPriceOnEndDate(List<Candle> candles) {
    Collections.sort(candles, (x, y) -> x.getDate().compareTo(y.getDate()));
    return candles.get(candles.size() - 1).getClose();
  }

  private Comparator<AnnualizedReturn> getComparator() {
    return Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed();
  }

  // CHECKSTYLE:OFF

  // TODO: CRIO_TASK_MODULE_REFACTOR
  // Extract the logic to call Tiingo third-party APIs to a separate function.
  // Remember to fill out the buildUri function and use that.

  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to) throws JsonProcessingException {
    return null;
  }

  protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
    String token = "a55716d2ff7cbbe9b320009298da04a0a01c1773";
    String uriTemplate = "https:api.tiingo.com/tiingo/daily/$SYMBOL/prices?"
        + "startDate=$STARTDATE&endDate=$ENDDATE&token=$APIKEY";
    return uriTemplate;
  }
}
