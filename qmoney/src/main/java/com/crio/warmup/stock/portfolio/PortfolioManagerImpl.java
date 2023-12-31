
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



  final static String TOKEN = "f97cd24323c2d3ac6227161812e4787c8ebe3e23";
  private RestTemplate restTemplate;


  // Caution: Do not delete or modify the constructor, or else your build will break!
  // This is absolutely necessary for backward compatibility
  protected PortfolioManagerImpl(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }


  //TODO: CRIO_TASK_MODULE_REFACTOR
  // 1. Now we want to convert our code into a module, so we will not call it from main anymore.
  //    Copy your code from Module#3 PortfolioManagerApplication#calculateAnnualizedReturn
  //    into #calculateAnnualizedReturn function here and ensure it follows the method signature.
  // 2. Logic to read Json file and convert them into Objects will not be required further as our
  //    clients will take care of it, going forward.
  public List<AnnualizedReturn> calculateAnnualizedReturn(List<PortfolioTrade> portfolioTrades,
      LocalDate endDate){
        AnnualizedReturn annualizedReturn;
        List<AnnualizedReturn> annualizedReturns = new ArrayList<>();
        for(PortfolioTrade pt:portfolioTrades){
          annualizedReturn = getAnnualizedReturn(pt,endDate);
          annualizedReturns.add(annualizedReturn);
        }
        Collections.sort(annualizedReturns,getComparator());
        return annualizedReturns;
      }

  // Note:
  // Make sure to exercise the tests inside PortfolioManagerTest using command below:
  // ./gradlew test --tests PortfolioManagerTest

  //CHECKSTYLE:OFF




  private AnnualizedReturn getAnnualizedReturn(PortfolioTrade pt, LocalDate endDate) {
    String symbol = pt.getSymbol();
    AnnualizedReturn annualizedReturn;
    LocalDate startDate = pt.getPurchaseDate();
    try{
      List<Candle> stockStartToEnd = getStockQuote(symbol, startDate, endDate);

      Candle stockStartDate = stockStartToEnd.get(0);
      Candle stockEndDate = stockStartToEnd.get(stockStartToEnd.size()-1);

      Double buyPrice = stockStartDate.getOpen();
      Double endPrice = stockEndDate.getClose();

      Double totalReturns = (endPrice - buyPrice)/buyPrice;
      Double total_num_years = startDate.until(endDate,ChronoUnit.DAYS)/365.24;
      Double annualized_returns = Math.pow((1 + totalReturns),(1/total_num_years)) - 1;
      annualizedReturn = new AnnualizedReturn(symbol,annualized_returns,totalReturns);
    }
    catch(JsonProcessingException ex){
      annualizedReturn = new AnnualizedReturn(symbol,Double.NaN,Double.NaN);
    }
    return annualizedReturn;
  }






  private Comparator<AnnualizedReturn> getComparator() {
    return Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed();
  }

  //CHECKSTYLE:OFF

  // TODO: CRIO_TASK_MODULE_REFACTOR
  //  Extract the logic to call Tiingo third-party APIs to a separate function.
  //  Remember to fill out the buildUri function and use that.


  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to)
      throws JsonProcessingException {
    if(from.compareTo(to) >= 0){
      throw new RuntimeException();
    }
    String url = buildUri(symbol,from,to);
    TiingoCandle[] stockStartToEnd = restTemplate.getForObject(url,TiingoCandle[].class);
    if(stockStartToEnd == null){
      return new ArrayList<Candle>();
    }
    else{
      List<Candle> candleList = Arrays.asList(stockStartToEnd);
      return candleList;
    }
  }

  protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
      String uriTemplate = "https://api.tiingo.com/tiingo/daily/$SYMBOL/prices?"
            + "startDate=$STARTDATE&endDate=$ENDDATE&token=$APIKEY";
      String url = uriTemplate.replace("$APIKEY",TOKEN).replace("$STARTDATE",startDate.toString())
      .replace("$ENDDATE",endDate.toString()).replace("$SYMBOL",symbol);
      return url;
  }



}
