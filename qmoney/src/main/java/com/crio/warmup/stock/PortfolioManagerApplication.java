
package com.crio.warmup.stock;


import com.crio.warmup.stock.dto.*;
import com.crio.warmup.stock.log.UncaughtExceptionHandler;
import com.crio.warmup.stock.portfolio.PortfolioManager;
import com.crio.warmup.stock.portfolio.PortfolioManagerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.web.client.RestTemplate;


public class PortfolioManagerApplication {






final static String TOKEN = "f97cd24323c2d3ac6227161812e4787c8ebe3e23";

  public static String getToken(){
    return TOKEN;
  }

  public static RestTemplate restTemplate = new RestTemplate();
  public static PortfolioManager portfolioManager = PortfolioManagerFactory.getPortfolioManager(restTemplate);

  // TODO: CRIO_TASK_MODULE_REST_API
  //  Find out the closing price of each stock on the end_date and return the list
  //  of all symbols in ascending order by its close value on end date.

  // Note:
  // 1. You may have to register on Tiingo to get the api_token.
  // 2. Look at args parameter and the module instructions carefully.
  // 2. You can copy relevant code from #mainReadFile to parse the Json.
  // 3. Use RestTemplate#getForObject in order to call the API,
  //    and deserialize the results in List<Candle>

  public static List<String> mainReadQuotes(String[] args) throws IOException, URISyntaxException {
    List<PortfolioTrade> trades = readTradesFromJson(args[0]);
    RestTemplate restTemplate = new RestTemplate();
    List<TotalReturnsDto> tests = new ArrayList<TotalReturnsDto>();
    for(PortfolioTrade pt : trades){
      String url = "https://api.tiingo.com/tiingo/daily/" + pt.getSymbol() + "/prices?startDate=" + pt.getPurchaseDate().toString() +
                  "&endDate=" + args[1] + "&token=f97cd24323c2d3ac6227161812e4787c8ebe3e23";
      TiingoCandle[] results = restTemplate.getForObject(url,TiingoCandle[].class);
      if(results != null){
        tests.add(new TotalReturnsDto(pt.getSymbol(),results[results.length - 1].getClose()));
      }
    } 
    Collections.sort(tests, TotalReturnsDto.closingComparator);
    List<String> stocks = new ArrayList<String>();
    for(TotalReturnsDto trd:tests){
      stocks.add(trd.getSymbol());
    }
    return stocks;
    //  return Collections.emptyList();
  }

  

  // TODO:
  //  After refactor, make sure that the tests pass by using these two commands
  //  ./gradlew test --tests PortfolioManagerApplicationTest.readTradesFromJson
  //  ./gradlew test --tests PortfolioManagerApplicationTest.mainReadFile
  public static List<PortfolioTrade> readTradesFromJson(String filename) throws IOException, URISyntaxException {
  //  File file = resolveFileFromResources(filename);
   ObjectMapper objectMapper = getObjectMapper();
   PortfolioTrade[] trade = objectMapper.readValue(resolveFileFromResources(filename),PortfolioTrade[].class);
   List<PortfolioTrade> trades = Arrays.asList(trade);
   //   return Collections.emptyList();
   return trades;
  }


  // TODO:
  //  Build the Url using given parameters and use this function in your code to cann the API.
  public static String prepareUrl(PortfolioTrade trade, LocalDate endDate, String token) {
     
     return "https://api.tiingo.com/tiingo/daily/" + trade.getSymbol() + "/prices?startDate=" + trade.getPurchaseDate().toString() +
     "&endDate=" + endDate + "&token=" + token;
  }

  public static List<String> mainReadFile(String[] args) throws IOException, URISyntaxException {
   File file = resolveFileFromResources(args[0]);
   ObjectMapper objectMapper = getObjectMapper();
   PortfolioTrade[] trades = objectMapper.readValue(file,PortfolioTrade[].class);
   List<String> symbols = new ArrayList<>();
   for(PortfolioTrade pt : trades){
     symbols.add(pt.getSymbol());
   }
   return symbols;
 }


  private static void printJsonObject(Object object) throws IOException {
   Logger logger = Logger.getLogger(PortfolioManagerApplication.class.getCanonicalName());
   ObjectMapper mapper = new ObjectMapper();
   logger.info(mapper.writeValueAsString(object));
 }

 private static File resolveFileFromResources(String filename) throws URISyntaxException {
   return Paths.get(
       Thread.currentThread().getContextClassLoader().getResource(filename).toURI()).toFile();
 }

 private static ObjectMapper getObjectMapper() {
   ObjectMapper objectMapper = new ObjectMapper();
   objectMapper.registerModule(new JavaTimeModule());
   return objectMapper;
 }


  public static List<String> debugOutputs() {

   String valueOfArgument0 = "trades.json";
   String resultOfResolveFilePathArgs0 = "/home/crio-user/workspace/samirsujan121-ME_QMONEY_V2/qmoney/bin/main/" + valueOfArgument0;
   String toStringOfObjectMapper = "com.fasterxml.jackson.databind.ObjectMapper@1573f9fc";
   String functionNameFromTestFileInStackTrace = "PortfolioManagerApplicationTest.mainReadFile()";
   String lineNumberFromTestFileInStackTrace = "29:1";


  return Arrays.asList(new String[]{valueOfArgument0, resultOfResolveFilePathArgs0,
      toStringOfObjectMapper, functionNameFromTestFileInStackTrace,
      lineNumberFromTestFileInStackTrace});
}
  // TODO: CRIO_TASK_MODULE_CALCULATIONS
  //  Now that you have the list of PortfolioTrade and their data, calculate annualized returns
  //  for the stocks provided in the Json.
  //  Use the function you just wrote #calculateAnnualizedReturns.
  //  Return the list of AnnualizedReturns sorted by annualizedReturns in descending order.

  // Note:
  // 1. You may need to copy relevant code from #mainReadQuotes to parse the Json.
  // 2. Remember to get the latest quotes from Tiingo API.




  // TODO:
  //  Ensure all tests are passing using below command
  //  ./gradlew test --tests ModuleThreeRefactorTest
  static Double getOpeningPriceOnStartDate(List<Candle> candles) {
     return candles.get(0).getOpen();
  }


  public static Double getClosingPriceOnEndDate(List<Candle> candles) {
     return candles.get(candles.size() - 1).getClose();
  }


  public static List<Candle> fetchCandles(PortfolioTrade trade, LocalDate endDate, String token) {
    RestTemplate restTemplate = new RestTemplate();
    String url = String.format("https://api.tiingo.com/tiingo/daily/%s/prices?" + "startDate=%s&endDate=%s&token=%s",
                                  trade.getSymbol(),trade.getPurchaseDate(),endDate,TOKEN);
    TiingoCandle[] stockStartToEnd = restTemplate.getForObject(url,TiingoCandle[].class);
    List<Candle> ans = Arrays.asList(stockStartToEnd);
    return ans;
  }


  public static List<AnnualizedReturn> mainCalculateSingleReturn(String[] args)
      throws IOException, URISyntaxException {
    // File file = resolveFileFromResources(args[0]);
    // ObjectMapper objectMapper = getObjectMapper();
    List<PortfolioTrade> trades = readTradesFromJson(args[0]);
    LocalDate endDate = LocalDate.parse(args[1]);
    System.out.println(trades);
    // RestTemplate restTemplate = new RestTemplate();
    List<AnnualizedReturn> annualizedReturns = new ArrayList<>();
    for(PortfolioTrade pt:trades){
      // String url = String.format("https://api.tiingo.com/tiingo/daily/%s/prices?" + "startDate=%s&endDate=%s&token=%s",
      //                             pt.getSymbol(),pt.getPurchaseDate(),endDate,TOKEN);
      List<Candle> stockStartToEnd = fetchCandles(pt,endDate,TOKEN);
      if(stockStartToEnd != null){
        // TiingoCandle stockStartDate = stockStartToEnd[0];
        // TiingoCandle stockEndDate = stockStartToEnd[stockStartToEnd.length - 1];
        Double buyPrice = getOpeningPriceOnStartDate(stockStartToEnd);
        Double sellPrice = getClosingPriceOnEndDate(stockStartToEnd);
        annualizedReturns.add(calculateAnnualizedReturns(endDate,pt,buyPrice,sellPrice));
      }
      else{
        annualizedReturns.add(new AnnualizedReturn(pt.getSymbol(),Double.NaN,Double.NaN));
      }
    }
    Collections.sort(annualizedReturns,AnnualizedReturn.sortByAnnualizedReturns);
    System.out.println(annualizedReturns);
    return annualizedReturns;
    // return Collections.emptyList();
  }

  // TODO: CRIO_TASK_MODULE_CALCULATIONS
  //  Return the populated list of AnnualizedReturn for all stocks.
  //  Annualized returns should be calculated in two steps:
  //   1. Calculate totalReturn = (sell_value - buy_value) / buy_value.
  //      1.1 Store the same as totalReturns
  //   2. Calculate extrapolated annualized returns by scaling the same in years span.
  //      The formula is:
  //      annualized_returns = (1 + total_returns) ^ (1 / total_num_years) - 1
  //      2.1 Store the same as annualized_returns
  //  Test the same using below specified command. The build should be successful.
  //     ./gradlew test --tests PortfolioManagerApplicationTest.testCalculateAnnualizedReturn

  public static AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate,
      PortfolioTrade trade, Double buyPrice, Double sellPrice) {
      LocalDate startDate = trade.getPurchaseDate();
      Double totalReturns = (sellPrice - buyPrice) / buyPrice;
      Double total_num_years = startDate.until(endDate,ChronoUnit.DAYS)/365.24;
      Double annualized_returns = Math.pow((1 + totalReturns),(1/total_num_years)) - 1;
      return new AnnualizedReturn(trade.getSymbol(), annualized_returns,totalReturns);
  }
























  // TODO: CRIO_TASK_MODULE_REFACTOR
  //  Once you are done with the implementation inside PortfolioManagerImpl and
  //  PortfolioManagerFactory, create PortfolioManager using PortfolioManagerFactory.
  //  Refer to the code from previous modules to get the List<PortfolioTrades> and endDate, and
  //  call the newly implemented method in PortfolioManager to calculate the annualized returns.

  // Note:
  // Remember to confirm that you are getting same results for annualized returns as in Module 3.

  public static List<AnnualizedReturn> mainCalculateReturnsAfterRefactor(String[] args)
      throws Exception {
       File file = resolveFileFromResources(args[0]);
       LocalDate endDate = LocalDate.parse(args[1]);
      //  String contents = readFileAsString(file);
       ObjectMapper objectMapper = getObjectMapper();
       PortfolioTrade[] portfolioTrades = objectMapper.readValue(file,PortfolioTrade[].class);
       return portfolioManager.calculateAnnualizedReturn(Arrays.asList(portfolioTrades), endDate);
  }


  public static void main(String[] args) throws Exception {
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    ThreadContext.put("runId", UUID.randomUUID().toString());



    // printJsonObject(mainCalculateSingleReturn(args));


    printJsonObject(mainCalculateReturnsAfterRefactor(args));
  }
}

