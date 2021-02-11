package com.mpakam;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.junit.jupiter.api.Test;
import org.patriques.input.timeseries.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.mpakam.app.config.EnvironmentConfig;
import com.mpakam.dao.BacktestStockOrderDao;
import com.mpakam.dao.CustomerDao;
import com.mpakam.dao.CustomerTickerTrackerDao;
import com.mpakam.dao.MonitoredStockDao;
import com.mpakam.dao.RenkoChartDao;
import com.mpakam.dao.StockAlertDao;
import com.mpakam.dao.StockDao;
import com.mpakam.dao.StockHlDataDao;
import com.mpakam.dao.StockQuoteDao;
import com.mpakam.dao.StockTickDataDao;
import com.mpakam.dao.StrategyDao;
import com.mpakam.dao.StrategyStockQuoteDao;
import com.mpakam.dao.TechAnalysisAtrDao;
import com.mpakam.model.BacktestStockOrder;
import com.mpakam.model.CustomerTickerTracker;
import com.mpakam.model.MonitoredStock;
import com.mpakam.model.RenkoChartBox;
import com.mpakam.model.Stock;
import com.mpakam.model.StockHlData;
import com.mpakam.model.StockQuote;
import com.mpakam.model.StockTickData;
import com.mpakam.model.StrategyStockQuote;
import com.mpakam.model.TechAnalysisAtr;
import com.mpakam.scheduler.ScheduledTasks;
import com.mpakam.service.EmailService;
import com.mpakam.service.IStockHlDataService;
import com.mpakam.service.IStockQuoteService;
import com.mpakam.service.IStockTickDataService;
import com.mpakam.service.strategy.IStrategyService;
import com.mpakam.service.strategy.RenkoChartStrategyService;
import com.mpakam.util.BigDecimalUtil;
import com.mpakam.util.IEXTradingService;
import com.mpakam.util.IQuoteDataProviderService;
import com.mpakam.util.StooqHistoryLoaderUtilService;
import com.mpakam.util.UIToolsService;

//@RunWith(SpringJUnit4ClassRunner.class)
//@SpringBootTest(classes=TradingAlertsApplication.class)
//@TestPropertySource(locations="classpath:application-test.properties")
public class TradingAlertsApplicationTests {
	
	private final Logger log = LoggerFactory.getLogger(this.getClass());


	@Autowired
	IStockQuoteService quoteService;
	
	@Autowired
	IQuoteDataProviderService dataProvider;
	
	@Autowired
	StockDao stockDao;
	
	@Autowired
	StockQuoteDao stockQuoteDao;
	
	@Autowired
	UIToolsService uiSvc;
	
	@Autowired
	IStrategyService strategySvc;
	
	@Autowired
	StrategyStockQuoteDao strategyStockDao;
	
	@Autowired
	CustomerTickerTrackerDao trackerDao;
	
	@Autowired
	CustomerDao custDao;
	
	@Autowired
	StrategyDao strategyDao;	
	
	@Autowired
	ScheduledTasks tasksDao;
	
	@Autowired
	StockAlertDao saDao;
	
	@Autowired
	IStockTickDataService stockTickSvc;
	
	@Autowired
	IStockHlDataService hlSvc;
	
	@Autowired
	IEXTradingService iexService;
	
	@Autowired
	StockHlDataDao hlDao;
	
	@Autowired
	MonitoredStockDao mStockDao;
	
	@Autowired
	StockTickDataDao tickerDao;
	
	@Autowired
	BigDecimalUtil bigDUtil;
	
	@Autowired
	EnvironmentConfig eConfig;
	
	@Autowired
	StooqHistoryLoaderUtilService stooqSvc;
	
	@Autowired
	StrategyStockQuoteDao stratStockQuoteDao;
	
	@Autowired
	EmailService emailSvc;
	
	@Autowired
	BacktestStockOrderDao backtestDao;
	
	@Autowired
	RenkoChartStrategyService renkoSvc;
	
	@Autowired
	RenkoChartDao renkoChartDao;
	
	@Autowired
	TechAnalysisAtrDao atrDao;
	
	
    @Test
    public void runSampleTest() {
    	System.out.println("Hello sweet success");
    	System.out.println(Interval.getByTime(30));
    }
    
    @Test
    public void retrieveStockQuotes() throws Exception {
    	Stock stock = stockDao.findBy(7);
    	dataProvider.retrieveCandleData(stock).forEach(s->{
    		System.out.println(s.getStockQuoteId()+"-"+s.getQuoteDatetime());
    		});

    }
    
    @Test
    @Transactional
    public void testRunScheduler() throws Exception{
    	List<CustomerTickerTracker> tickerList = trackerDao.findByCustomerId(5);
    	tickerList.forEach(t->{try {
			quoteService.analyzeStock(t.getStock());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			try {
			Thread.sleep(30*1000); // wait for 10 seconds
			quoteService.analyzeStock(t.getStock());
			}catch(Exception e2) {
				e2.printStackTrace();
			}
		}});
    }
    
    @Test
    public void analyzeStockTest() throws Exception{
    	Stock s =new Stock();
    	s.setInterval(30);
    	s.setStocknum(7);
    	s.setStockName("BA");
    	s.setTicker("BA");
    	quoteService.analyzeStock(s);
    }
    
    
    @Test
    public void retrieveLatestStockQuote() {
    	Stock s = stockDao.findBy(7);
    	StockQuote sq=stockQuoteDao.findLastStockQuote(s);
    	System.out.println(sq!=null?sq.getStockQuoteId():"No Records exists");
    }
    
    @Test
    @Transactional
    public void retrieveLatestStrategyStockQuote() {
    	CustomerTickerTracker custTrkr = trackerDao.findById(3);
		
		LinkedList<StrategyStockQuote> quoteList = 
				strategyStockDao.retrieveQuotesByStockNumStrategyId(custTrkr.getStock(),
						custTrkr.getStrategyId());
		
		uiSvc.convertToHACandleData(quoteList);
		uiSvc.stochRsiFromHA(quoteList);
    }
    @Test
    @Transactional
    public void retrieveQuotesByCustTrackerId() {
    	CustomerTickerTracker custTrkr = trackerDao.findById(7);
    	LinkedList<StrategyStockQuote> quoteList = 
    			strategyStockDao.retrieveQuotesByStockNumStrategyId(custTrkr.getStock(),
						custTrkr.getStrategyId());
    }
    
    @Test
    public void testInterval() {
    	System.out.println(Interval.getByTime(30));
    }
    
    @Test
    public void runDailyAnalysis() throws Exception {
    	tasksDao.analyzeDailyAlerts();
    }
    
    @Test
    @Transactional
    public void runDailyForStock() throws Exception {
    	quoteService.analyzeDailyStocks();
    }
    
    @Test
    public void run15MinStock() throws Exception{
    	tasksDao.analyze15MinAlerts();
    }
    
    @Test
    public void run30MinStock() throws Exception{
    	tasksDao.analyze30MinAlerts();
    }
    
    @Test
    @Transactional
    public void runMoniteredStockAlertTest() throws Exception{
    	System.out.println("Monitored Stock Alert count is " + saDao.getActiveStockAlertByStocknum(821).size());
    }
    
    @Test
    public void testPriceRetrievalFromIEX() {
    	stockTickSvc.saveCurrentPriceForAllMonitoredStocks();
    }
    

    
    @Test
    public void batchDailyDataTest() throws Exception {
    	quoteService.batchDailyAnalyzeStock();
    }
    
    @Test
    public void loadBatchDailyDataTest() throws Exception {
    	quoteService.initialBatchAnalyzeStock();
    }
    
    @Test
    public void syncBatchPrice() {
    	stockTickSvc.saveCurrentPriceForAllMonitoredStocks();
    }
    
    @Test
    @Transactional
    public void testMinuteHLDataForApple() throws Exception {
    	List<MonitoredStock> mStockList = mStockDao.retreiveByStockNum(206);
		hlDao.saveAll(iexService.retrieveBatchStockHL(mStockList));
    }
    
    @Test
    public void testMinuteHLDataForAllMonitored() throws Exception {
    	List<MonitoredStock> mStockList = mStockDao.retrievegetActivelyMonitoredStocks();
		hlDao.saveAll(iexService.retrieveBatchStockHL(mStockList));
    }
    
    @Test
    @Transactional
    public void testHighLowQry() {
    	
    	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm");
		LocalDateTime dateTime = LocalDateTime
				.parse("20180214 09:30", formatter);
		
    	LinkedList<StockHlData> data =hlDao.findAllEntriesAfter(524,dateTime);
    	data.stream().forEach(s->{
    		System.out.println(s.getHlDatetime() + " High: " + s.getHigh() +" Low: "+s.getLow());	
    	});
    }
    
    @Test
    public void prepareStockQuoteFromIEX() throws InterruptedException, ExecutionException {

    	List<MonitoredStock> list = mStockDao.retreiveByStockNum(956); //CMG
        quoteService.iexAnalyzeStock(list);
    }
    
    @Test
    @Transactional
    public void retrieveHlData() throws Exception {
    	hlSvc.getStockHlData();
    }
    
    @Test
    @Transactional
    public void batchDailyAnalyzeStock() throws Exception {
    	quoteService.batchDailyAnalyzeStock();
    }
    
    @Test
    public void testLogging() {
    	log.debug("Hello - new file");
    }
    
    @Transactional
    @Test
    public void retrieveCurrentRecs() {
    	tickerDao.findCurrentSessionByStockNum(206).forEach(p->{
    		System.out.println(p.getTickDatetime() + ":" + p.getPrice());
    	});;
    }
    
    @Transactional
    @Test
    public void testOpenCloseLogic() {
    	int stocknum = 223;
    	LinkedList<StockTickData> tickDataList = tickerDao.findCurrentSessionByStockNum(stocknum);
    	StockHlData lastItem =  hlDao.findLastByStock(stockDao.findBy(stocknum));
    	LocalDateTime openTime = lastItem.getHlDatetime().plusMinutes(1);
    	LocalDateTime closeTime = lastItem.getHlDatetime().plusMinutes(2);
    	
    	BigDecimal open = BigDecimal.ZERO;
    	BigDecimal close = BigDecimal.ZERO;
    	List<StockTickData> tickDataOpt =tickDataList.parallelStream().filter(p->p.getTickDatetime().compareTo(openTime) >= 0 && p.getTickDatetime().compareTo(closeTime) <=0).collect(Collectors.toList());
    	if(tickDataOpt.size()>0) {
    		int i =0;
    		open = tickDataOpt.get(i).getPrice();

    		for(;!bigDUtil.isValid(open) && i<tickDataOpt.size();) {
    			open = tickDataOpt.get(++i).getPrice();
    		}
    		int closeI =tickDataOpt.size()-1;
    		close = tickDataOpt.get(closeI).getPrice();
    		
    		for(;!bigDUtil.isValid(close) && closeI>=0;) {
    			close = tickDataOpt.get(--closeI).getPrice();
    		}
    		
//    		log.debug("using the open @ "+ tickDataOptOpen.get().getTickDatetime());
    	}else {
    		open = BigDecimal.ZERO;
    		close = BigDecimal.ZERO;
//    		throw new RuntimeException("no open tick data found for "+ s.getTicker());
    	}
    	
    	/*//Retrieving the Close Tick        	
    	List<StockTickData> tickDataOptClose =tickDataList.parallelStream().filter(p->(p.getTickDatetime().compareTo(closeTime) <= 0 &&
    			p.getTickDatetime().compareTo(openTime) > 0))
    			.collect(Collectors.toList()); //The Next Minute
    	*/	
    	System.out.println("Open is " + open + "Close is " + close);
    }
    
    @Test
    public void loadHistoryPath() throws IOException {
    	List<Stock> stockList = stockDao.findAll();
    	Map<String, String> pathMap = new HashMap<>();

    	Files.walk(Paths.get(eConfig.getSTOOQ_FILEPATH())).
    	filter(Files::isRegularFile).
    	forEach(p->{
    		System.out.println("Fiel name " +p.getFileName());
    		pathMap.put(p.getFileName().toString().split("\\.")[0].toUpperCase(),p.toAbsolutePath().toString() );
    	});;
    	try {
    	stockList.parallelStream().forEach(s->{
    		System.out.println("Ticker name:" + s.getTicker() + " path is " + pathMap.get(s.getTicker()));
    		if(pathMap.containsKey(s.getTicker()))
    			s.setStooqFilePath(pathMap.get(s.getTicker()));
    		stockDao.merge(s);
    	});
    	}catch(Exception e) {
    		e.printStackTrace();
    	}
    }

    @Test
    public void loadHistoryForMonitoredStocks() throws IOException, InterruptedException, ExecutionException {
    	List<MonitoredStock> list =mStockDao.retrievegetActivelyMonitoredStocks();
    	list.parallelStream().forEach(mStock->{
    		Stock s = mStock.getStock();
    		hlSvc.deleteAllForStock(s);
    		hlSvc.saveAll(s,stooqSvc.parseStockHLDataFromHistory(s));
    	});
    	quoteService.iexAnalyzeStock(list);
    }
    
    @Test
    public void processMonitoredStocks() throws IOException, InterruptedException, ExecutionException {
    	List<MonitoredStock> list =mStockDao.retrievegetActivelyMonitoredStocks();
    	quoteService.iexAnalyzeStock(list);
    }

    @Test
    public void parseStockHLDataFromHistory() throws InterruptedException, ExecutionException {    
    	int stocknum = 991;
    	Stock s = stockDao.findBy(stocknum);
    	hlSvc.deleteAllForStock(s);
    	hlSvc.saveAll(s,stooqSvc.parseStockHLDataFromHistory(s));
    	List<MonitoredStock> mStockList = mStockDao.retreiveByStockNum(stocknum);
    	quoteService.iexAnalyzeStock(mStockList );
    }
    
    @Test
    public void loadIntraDayForStocks() throws IOException, InterruptedException, ExecutionException {
//    	List<MonitoredStock> list =mStockDao.retrievegetActivelyMonitoredStocks();
    	List<MonitoredStock> mStockList = mStockDao.retreiveByStockNum(880);
    	hlSvc.saveAll(stooqSvc.processIntraDayFile(eConfig.getSTOOQ_INTRADAY_FILEPATH()));
    	quoteService.iexAnalyzeStock(mStockList);
    }
    
    @Test
    public void checkForSuperTrend() throws Exception{
    	Stock s = stockDao.findBy(1176);
    	LinkedList<StrategyStockQuote> quotes = stratStockQuoteDao.getAllByStockNum(s);
    	StrategyStockQuote lastStockQuote=null;
    	double prevHighestHigh=0;
    	double prevLowestLow=0;
    	// register high only after low and low only after high
    	int trend =0;
    	int index = 0;
    	for(StrategyStockQuote quote: quotes) {
    		int signal =strategySvc.checkForSignalForTrend(quote, lastStockQuote);
    		if(lastStockQuote == null) {
    			lastStockQuote = quote;
    			continue;
    		}
    		double currentHigh = lastStockQuote.getXhigh().doubleValue();
    		double currentLow= lastStockQuote.getXlow().doubleValue();

    		boolean highUpTrend = currentHigh > prevHighestHigh;
    		
    		if(signal == -1) { //Sell
    			if(prevHighestHigh != 0) {
    				if(trend >=0 ) {// This ensures that we areonly capturing the highest high when we were in uptrend but a sell signal
    					String quoteStr = "; Current High: " +quotes.get(index-1).getXhigh().doubleValue() + "; Prev High: " +prevHighestHigh;
    					if(prevHighestHigh > quotes.get(index-1).getXhigh().doubleValue()) {
    						System.out.println("Donot SELL- - Up Trend "  + quote.getStockQuote().getQuoteDatetime() + quoteStr);
    					}else {
    						System.out.println("SELL- signal "  + quote.getStockQuote().getQuoteDatetime() + quoteStr);
    						trend = -1;
    					}
    					prevHighestHigh = quotes.get(index-1).getXhigh().doubleValue(); //Capturing previous high
        			}else {
//        				System.out.println("False Negative " + quote.getStockQuote().getQuoteDatetime());
        			}
    			}else if (prevHighestHigh == 0) {
    				prevHighestHigh = currentHigh; // Initial Setting
    			}    			
    		}else if(signal == 1) { //Buy
    			if(prevLowestLow != 0 ) {
    				
    				prevHighestHigh = currentHigh;
    				if(trend <=0) {
    					String quoteStr = "; Current Low: " + quotes.get(index-1).getXlow().doubleValue() + "; Prev Low: " +prevLowestLow;
    					if(prevLowestLow < quotes.get(index-1).getXlow().doubleValue()) {
    						System.out.println("BUY - signal "  + quote.getStockQuote().getQuoteDatetime() + quoteStr);
    						trend = 1;
    					}else {
    						System.out.println("Donot BUY - Down- Trend "  + quote.getStockQuote().getQuoteDatetime() +quoteStr);
    					}
    					prevLowestLow = quotes.get(index-1).getXlow().doubleValue(); //Capturing previous high
    				}else {
//        				System.out.println("False Positive " + quote.getStockQuote().getQuoteDatetime());
        			}
    			}else if (prevLowestLow == 0) {
    				prevLowestLow = currentLow;
    			}
    		} 
    		lastStockQuote = quote;
    		index++;
    	}
    }
    
    @Test
    @Transactional
    public void loadInitialTrend() throws Exception{
    	Stock st = stockDao.findBy(884);
    	List<Stock> sList = new ArrayList<>();
    	sList.add(st);
    	BacktestStockOrder order = null;
    	sList.forEach(s->{
    		LinkedList<StrategyStockQuote> quotes = stratStockQuoteDao.getAllByStockNum(s);
        	StrategyStockQuote lastStockQuote=null;
        	double prevHighestHigh=0;
        	double prevLowestLow=0;

        	int prevSignal =0;
        	int trend =0;
        	int index = 0;
        	for(StrategyStockQuote quote: quotes) {
        		int signal =strategySvc.checkForSignalForTrend(quote, lastStockQuote);
        		if(lastStockQuote == null) {
        			lastStockQuote = quote;
        			continue;
        		}
        		double currentHigh = lastStockQuote.getXhigh().doubleValue();
        		double currentLow= lastStockQuote.getXlow().doubleValue();
        		if(prevSignal!=0 &&  prevSignal == signal)
        			continue;
        		if(signal == -1) { //Sell
        			if(prevHighestHigh != 0) {
        				if(trend >=0 ) {// This ensures that we areonly capturing the highest high when we were in uptrend but a sell signal
        					String quoteStr = "; Current High: " +quotes.get(index-1).getXhigh().doubleValue() + "; Prev High: " +prevHighestHigh;
        					if(prevHighestHigh > quotes.get(index-1).getXhigh().doubleValue()) {
        						System.out.println(quote.getStockQuote().getStock().getTicker() +" : Donot SELL- - Up Trend "  + quote.getStockQuote().getQuoteDatetime() + quoteStr);
        					}else {
        						System.out.println(quote.getStockQuote().getStock().getTicker() +" : SELL- Sell - Down Trend "  + quote.getStockQuote().getQuoteDatetime() + quoteStr + " StopLoss: "+quotes.get(index-1).getXhigh());
        						trend = -1;
        					}
    						prevHighestHigh = quotes.get(index-1).getXhigh().doubleValue(); //Capturing previous high
            			}
        			}else if (prevHighestHigh == 0) {
        				prevHighestHigh = currentHigh; // Initial Setting
        			}    			
        		}else if(signal == 1) { //Buy
        			if(prevLowestLow != 0 ) {
        				if(trend <=0) {
        					String quoteStr = "; Current Low: " + quotes.get(index-1).getXlow().doubleValue() + "; Prev Low: " +prevLowestLow;
        					if(prevLowestLow < quotes.get(index-1).getXlow().doubleValue()) {
        						System.out.println(quote.getStockQuote().getStock().getTicker() + " : BUY - Up Trend "  + quote.getStockQuote().getQuoteDatetime() + quoteStr + " StopLoss:" +quotes.get(index-1).getXlow());
        						trend = 1;
        					}else {
        						System.out.println(quote.getStockQuote().getStock().getTicker() +" : Donot BUY - Down- Trend "  + quote.getStockQuote().getQuoteDatetime() +quoteStr);
        					}
        					prevLowestLow = quotes.get(index-1).getXlow().doubleValue(); //Capturing previous high
        					//prevHighestHigh = currentHigh;
        				}
        			}else if (prevLowestLow == 0) {
        				prevLowestLow = currentLow;
        			}
        		}else {
        			if(trend == 1 && currentLow <prevLowestLow && prevLowestLow>0) {//Still in up-trend, check if current low is below the PrevLowestlow
        				String quoteStr = "; Current Low: " + currentLow + "; Prev Low: " +prevLowestLow;
        				System.out.println(quote.getStockQuote().getStock().getTicker() + " Change in Trend to DOWN" + quote.getStockQuote().getQuoteDatetime() + quoteStr);
        				trend = -1;
        			}else if(trend == -1 && currentHigh > prevHighestHigh && prevHighestHigh>0) {//Still in Down-trend, check if current high is above the Prevhighesthigh
        				String quoteStr = "; Current High: " + currentHigh + "; Prev High: " +prevHighestHigh;
        				System.out.println(quote.getStockQuote().getStock().getTicker() + " Change in Trend to UP" + quote.getStockQuote().getQuoteDatetime() + quoteStr);
        				trend = 1;
        			}
        		}
        		if(signal!=0)
        			prevSignal = signal;
        		lastStockQuote = quote;
        		index++;
        	}
        	s.setTrend(trend);
    		s.setHighestHigh(new BigDecimal(prevHighestHigh));
    		s.setLowestLow(new BigDecimal(prevLowestLow));
        	stockDao.save(s);
    	});
    }
    
    @Test
    @Transactional
    public void backTest() throws Exception{
    	Stock st = stockDao.findBy(884);
    	List<Stock> sList = new ArrayList<>();
    	sList.add(st);
    	LinkedList<BacktestStockOrder> orderList = new LinkedList<>();
    	
    	sList.forEach(s->{
    		LinkedList<StrategyStockQuote> quotes = stratStockQuoteDao.getAllByStockNum(s);
        	StrategyStockQuote lastStockQuote=null;
        	double prevHighestHigh=0;
        	double prevLowestLow=0;
        	BacktestStockOrder prevOrder = null;
        	int prevSignal =0;
        	int trend =0;
        	int index = 0;
        	for(StrategyStockQuote quote: quotes) {
        		int signal =strategySvc.checkForSignalForTrend(quote, lastStockQuote);
        		if(lastStockQuote == null) {
        			lastStockQuote = quote;
        			continue;
        		}	
        		double currentHigh = lastStockQuote.getXhigh().doubleValue();
        		double currentLow= lastStockQuote.getXlow().doubleValue();
        		if(prevSignal!=0 &&  prevSignal == signal)
        			continue;
        		if(signal == -1) { //Sell
        			if(prevHighestHigh != 0) {
        				if(trend >=0 ) {// This ensures that we areonly capturing the highest high when we were in uptrend but a sell signal
        					String quoteStr = "; Current High: " +quotes.get(index-1).getXhigh().doubleValue() + "; Prev High: " +prevHighestHigh;
        					if(prevHighestHigh > quotes.get(index-1).getXhigh().doubleValue()) {
        						System.out.println(quote.getStockQuote().getStock().getTicker() +" : Donot SELL- - Up Trend "  + quote.getStockQuote().getQuoteDatetime() + quoteStr);
        						if(prevOrder!=null) {
        							orderList.add(closeBackTestOrder(quote,prevOrder));
        							prevOrder = null;
        						}
        					}else {
        						System.out.println(quote.getStockQuote().getStock().getTicker() +" : SELL- Sell - Down Trend "  + quote.getStockQuote().getQuoteDatetime() + quoteStr + " StopLoss: "+quotes.get(index-1).getXhigh());
        						trend = -1;
        						if(prevOrder == null) {
        							prevOrder=createNewBackTestOrder(quote, signal,quotes.get(index-1).getXhigh());
        						}
        					}
    						prevHighestHigh = quotes.get(index-1).getXhigh().doubleValue(); //Capturing previous high
            			}
        			}else if (prevHighestHigh == 0) {
        				prevHighestHigh = currentHigh; // Initial Setting
        			}    			
        		}else if(signal == 1) { //Buy
        			if(prevLowestLow != 0 ) {
        				if(trend <=0) {
        					String quoteStr = "; Current Low: " + quotes.get(index-1).getXlow().doubleValue() + "; Prev Low: " +prevLowestLow;
        					if(prevLowestLow < quotes.get(index-1).getXlow().doubleValue()) {
        						System.out.println(quote.getStockQuote().getStock().getTicker() + " : BUY - Up Trend "  + quote.getStockQuote().getQuoteDatetime() + quoteStr + " StopLoss:" +quotes.get(index-1).getXlow());
        						trend = 1;
        						if(prevOrder == null) {
        							prevOrder=createNewBackTestOrder(quote,signal,quotes.get(index-1).getXlow());
        						}
        					}else {
        						System.out.println(quote.getStockQuote().getStock().getTicker() +" : Donot BUY - Down- Trend "  + quote.getStockQuote().getQuoteDatetime() +quoteStr);
        						if(prevOrder!=null) {
        							orderList.add(closeBackTestOrder(quote,prevOrder));
        							prevOrder = null;
        						}
        					}
        					prevLowestLow = quotes.get(index-1).getXlow().doubleValue(); //Capturing previous high
        				}
        			}else if (prevLowestLow == 0) {
        				prevLowestLow = currentLow;
        			}
        		}else {
        			if(trend == 1 && currentLow <prevLowestLow && prevLowestLow>0) {//Still in up-trend, check if current low is below the PrevLowestlow
        				String quoteStr = "; Current Low: " + currentLow + "; Prev Low: " +prevLowestLow;
        				System.out.println(quote.getStockQuote().getStock().getTicker() + " Change in Trend to DOWN" + quote.getStockQuote().getQuoteDatetime() + quoteStr);
        				trend = -1;
        			}else if(trend == -1 && currentHigh > prevHighestHigh && prevHighestHigh>0) {//Still in Down-trend, check if current high is above the Prevhighesthigh
        				String quoteStr = "; Current High: " + currentHigh + "; Prev High: " +prevHighestHigh;
        				System.out.println(quote.getStockQuote().getStock().getTicker() + " Change in Trend to UP" + quote.getStockQuote().getQuoteDatetime() + quoteStr);
        				trend = 1;
        			}
        			if(prevOrder!=null && checkForStopLoss(quote, prevOrder)== null) {
        				System.out.println(quote.getStockQuote().getStock().getTicker() +" Hit the stoploss @ "+ quote.getStockQuote().getQuoteDatetime());
        				prevOrder=null;
        			}
        		}

        		if(signal!=0)
        			prevSignal = signal;
        		lastStockQuote = quote;
        		index++;
        	}
        	s.setTrend(trend);
    		s.setHighestHigh(new BigDecimal(prevHighestHigh));
    		s.setLowestLow(new BigDecimal(prevLowestLow));
        	stockDao.save(s);
    	});
    	
    	orderList.forEach(p->{
    		System.out.println(p.getOrderType() + " Open Price:"+ p.getOpenPrice() + " @ "+ p.getOpenDatetime());
    		System.out.println(p.getOrderType() + " Close Price:"+ p.getClosePrice() + " @ "+ p.getCloseDatetime());
    		System.out.println( p.getOrderType() + " - Profile or Lost : "+ p.getProfitLoss());
    	});
    }
    
    private BacktestStockOrder createNewBackTestOrder(StrategyStockQuote quote, int signal, BigDecimal stopLossPrice) {
			BacktestStockOrder order = new BacktestStockOrder();
			order.setEntryDatetime(LocalDateTime.now());
			order.setOpenPrice(quote.getStockQuote().getOpen());
			order.setOpenDatetime(quote.getStockQuote().getQuoteDatetime());
			order.setStock(quote.getStockQuote().getStock());
			order.setStrategyId(1);//Heiken Ashi
			order.setStopLossPrice(stopLossPrice);
			order.setOrderType((signal==1)?"BTO":"STO"); //BTO - Buy to Open; STO - Sell to Open
			return order;
    }
    
    private BacktestStockOrder checkForStopLoss(StrategyStockQuote quote, BacktestStockOrder order) {
    	if(order == null)
    		return null;
    	if(order.getOrderType().equals("BTO") && order.getStopLossPrice().compareTo(quote.getStockQuote().getClose() )>=0) {
    		closeBackTestOrder(quote, order);
    		return null;
    	}else if (order.getOrderType().equals("BTO") && order.getStopLossPrice().compareTo(quote.getStockQuote().getClose() )<=0) {
    		closeBackTestOrder(quote, order);
    		return null;
    	}
    	return order;
    }
    
    private BacktestStockOrder closeBackTestOrder(StrategyStockQuote quote, BacktestStockOrder order) {
    	order.setCloseDatetime(quote.getStockQuote().getQuoteDatetime());
    	order.setClosePrice(quote.getStockQuote().getOpen());
    	BigDecimal profitOrLoss  =BigDecimal.ZERO;
    	if(order.getOrderType().equals("BTO")) {
    		profitOrLoss = order.getClosePrice().subtract(order.getOpenPrice());
    	}else {
    		profitOrLoss = order.getOpenPrice().subtract(order.getClosePrice());
    	}
    	order.setProfitLoss(profitOrLoss);
    	return order;
    }
    
    @Test
    public void sendEmails() {
    	emailSvc.sendEmail();
    }
    
    @Test
    public void run5Min() throws Exception {
    	tasksDao.analyze5MinAlerts(LocalDateTime.now());
    }
    
    @Test
    public void retrieveAllOrders() throws Exception {
    	LocalDateTime quoteTime  =LocalDateTime.parse("2018-03-07T15:55:00");
    	
    	StringBuffer closingOrders=new StringBuffer("Closing Orders:\n");
		closingOrders.append("========================\n");
		StringBuffer openingOrders=new StringBuffer("Opening Orders:\n");
		openingOrders.append("========================\n");
//		backtestDao.getLatestOrders(quoteTime).forEach(p->{
		for(BacktestStockOrder p:backtestDao.getLatestOrders(quoteTime)) {
    		StrategyStockQuote currentQuote = null; //p.getStrategyStockQuote();//TODO:
    		BigDecimal stopLoss = p.getStopLossPrice();
    		if(p.getOpenDatetime().equals(quoteTime)) {	
    			openingOrders=openingOrders.append(currentQuote.getStockQuote().getQuoteDatetime() + "\t" );
    			//System.out.println("Open Quote : close price:" +currentQuote.getStockQuote().getClose().doubleValue());
    			openingOrders=openingOrders.append(p.getOrderType() +"\t" +currentQuote.getStockQuote().getStock().getTicker() + "\t @ "+ currentQuote.getStockQuote().getClose().doubleValue()
    					+"\tStopLoss: "+stopLoss+"\n") ;
    			//System.out.println(openingOrders);
    		}else {
//    			System.out.println("quoteTime:"+quoteTime +" p.getOpenDatetime() : "+p.getOpenDatetime() + "inside close quote");
    			closingOrders=closingOrders.append(currentQuote.getStockQuote().getQuoteDatetime() + "\t" );
    			boolean stopLossHit =false;
    			//+ "Stoch RSI: "+ currentQuote.getStochRsiD() + "\t"
    			if(((p.getOrderType().equals("BTO") && currentQuote.getStockQuote().getClose().compareTo(stopLoss)<=0) || 
    					p.getOrderType().equals("STO") && currentQuote.getStockQuote().getClose().compareTo(stopLoss)>=0)) {
    				stopLossHit = true;
    			}
    			if(stopLossHit)
    				closingOrders=closingOrders.append("**STOPLOSS:\t");
    			closingOrders=closingOrders.append((p.getOrderType().equals("BTO")?"STC":"BTC") +"\t" 
    				+currentQuote.getStockQuote().getStock().getTicker() + "\t @ " 
    					+ currentQuote.getStockQuote().getClose().doubleValue()
    					+"\tStopLoss: "+stopLoss+"\n") ;
    			
    			//System.out.println(closingOrders);
    		}
    		System.out.println(p.toString());
    	}
		System.out.println("final:"+closingOrders.toString() +"\n"+openingOrders.toString());
    }
    
    @Test
    public void testRenkoStrategy() throws Exception {
		Stock s = stockDao.findBy(1205);
		List<StockQuote> quotes = stockQuoteDao.findAllByStock(s);
		TreeSet<StockQuote> quoteTreeset = new TreeSet();
		for(StockQuote sq: quotes) {
			quoteTreeset.add(sq);
		}
		renkoSvc.executeStrategy(quoteTreeset);
		return ;
    }
    
    @Test
    public void checkRenkoLogic() throws Exception {
    	List<MonitoredStock> mStockList = mStockDao.retrievegetActivelyMonitoredStocks();
    	mStockList.parallelStream().forEach(s->{
    		List<RenkoChartBox> boxList =renkoChartDao.findAllByStocknum(s.getStock().getStocknum());
        	
        	RenkoChartBox prevBox = null;
        	for(RenkoChartBox currentBox:boxList) {
        		if(prevBox == null) {
        			prevBox =currentBox;
        			continue;
        		}
        		try {
					renkoSvc.checkForTrend(currentBox, prevBox);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        		prevBox= currentBox;
        	}
    	});
    	
    }
    
    @Test
    public void checkRenkoLogicForStock() throws Exception {
//    	List<MonitoredStock> mStockList = mStockDao.retrievegetActivelyMonitoredStocks();
    	List<MonitoredStock> mStockList = mStockDao.retreiveByStockNum(1205);
    	mStockList.parallelStream().forEach(s->{
    		List<RenkoChartBox> boxList =renkoChartDao.findAllByStocknum(s.getStock().getStocknum());
        	
        	RenkoChartBox prevBox = null;
        	for(RenkoChartBox currentBox:boxList) {
        		if(prevBox == null) {
        			prevBox =currentBox;
        			continue;
        		}
        		try {
					renkoSvc.checkForTrend(currentBox, prevBox);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        		prevBox= currentBox;
        	}
    	});
    	
    }
    
    @Test
    public void iexRenkoChartAnalyzeStock() throws Exception {
    	List<MonitoredStock> mStockList = mStockDao.retrievegetActivelyMonitoredStocks();
    	
    	mStockList.parallelStream().forEach(mStock->{
    		Stock s = mStock.getStock();
    		hlSvc.deleteAllForStock(s);
    		hlSvc.saveAll(s,stooqSvc.parseStockHLDataFromHistory(s));
    	});
    	
    	quoteService.iexRenkoChartAnalyzeStock(mStockList);
    }
    
    @Test
    public void analyzeIexRenkoChartAnalyzeStock() throws Exception {
    	List<MonitoredStock> mStockList = mStockDao.retrievegetActivelyMonitoredStocks();
    	
    	quoteService.iexRenkoChartAnalyzeStock(mStockList);
    }
    
    @Test
    public void analyzeIexRenkoChartAnalyzeForStock() throws Exception {
    	List<MonitoredStock> mStockList = mStockDao.retreiveByStockNum(1760);
    	quoteService.iexRenkoChartAnalyzeStock(mStockList);
    }
    
    @Test
    public void iexRenkoChartAnalyzeStockForStock() throws Exception {
    	List<MonitoredStock> mStockList = mStockDao.retreiveByStockNum(1205);
    	
    	mStockList.parallelStream().forEach(mStock->{
    		Stock s = mStock.getStock();
    		hlSvc.deleteAllForStock(s);
    		hlSvc.saveAll(s,stooqSvc.parseStockHLDataFromHistory(s));
    	});
    	
    	quoteService.iexRenkoChartAnalyzeStock(mStockList);
    }
    
    @Test
    public void calculateATR() throws Exception{
    	Stock s = stockDao.findBy(1205);
		
    	BigDecimal trueRange=null;
    	
		TechAnalysisAtr prevAtr = atrDao.retrieveLastByStockNum(s);
		
		List<StockQuote> quotes = stockQuoteDao.findAllByStock(s); //TODO: This is not needed in actual code
		
		if(prevAtr !=null ) {
			if(quotes.size()>0)
				trueRange=calculateTR(quotes.get(quotes.size()-1), prevAtr.getStockQuote()); //TODO: This needs to be modified as welel.
		}else {
			//Initial ATR Calculation
			TreeSet<StockQuote> quoteTreeset = new TreeSet();
			for(StockQuote sq: quotes) {
				quoteTreeset.add(sq);
			}
			//http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:average_true_range_atr
			StockQuote cQuote=null;
			StockQuote pQuote=null;
			
			BigDecimal[] trueRangeList=new BigDecimal[14];
			int sqCounter=0;
			double prevAtrD=0;
			for(StockQuote sq:quotes) {
				cQuote=sq;
				double averageTrueRange=0;
				if(pQuote == null) {
					pQuote=sq;
					trueRange=sq.getHigh().subtract(sq.getLow());
				}else {
					trueRange=calculateTR(cQuote, pQuote);
					pQuote=cQuote;
				}
				if(sqCounter == 13) { //14th recursion.
					//Calculate the ATR for the 1st time.
					BigDecimal allTrValue=BigDecimal.ZERO;
					for(BigDecimal bd:trueRangeList) {
						if(bd != null)
							allTrValue=allTrValue.add(bd.abs());
					}
					averageTrueRange = allTrValue.doubleValue()/14;
				}else if(sqCounter <14) {
					trueRangeList[sqCounter]=trueRange;
				}else { //sqCounter=14 or above
					//Calculate the ATR
					//Current ATR = [(Prior ATR x 13) + Current TR] / 14
//					averageTrueRange=((prevAtr.multiply(new BigDecimal(13))).add(trueRange)).divide(new BigDecimal(14));
					averageTrueRange=(prevAtrD*13 + trueRange.doubleValue())/14;
				}
				TechAnalysisAtr atr=new TechAnalysisAtr();
				atr.setTrueRange(trueRange);
				atr.setStockQuote(sq);
				atr.setAverageTrueRange(new BigDecimal(averageTrueRange));
				prevAtrD=averageTrueRange;
				atrDao.save(atr);
				sqCounter++;
			}
		}
    }
    
    private BigDecimal calculateTR(StockQuote cQuote, StockQuote pQuote) {
    	BigDecimal hl = cQuote.getHigh().subtract(cQuote.getLow()).abs();
    	BigDecimal hCp= cQuote.getHigh().subtract(pQuote.getClose()).abs();
    	BigDecimal lCp= cQuote.getLow().subtract(pQuote.getClose()).abs(); 
    	BigDecimal largestTR=null;
    	if(hl.compareTo(hCp)>=0) {
    		largestTR=hl;
    	}else
    		largestTR=hCp;
    	
    	if(lCp.compareTo(largestTR)>0)
    		largestTR=lCp;
    	return largestTR;
    }
    
    @Transactional
    @Test
    public void checkForLastItem() {
    	Stock s= stockDao.findBy(1657);
    	for(int i=0;i<10;i++) {
    		System.out.println("Stock Qutoe is " + stockQuoteDao.findLastStockQuote(s));
    	}
    }
    
    @Transactional
    @Test
    public void checkForLastItemRenkoChartBox() {
    	Stock s= stockDao.findBy(1657);
    	for(int i=0;i<10;i++) {
    		System.out.println("Stock Qutoe is " + renkoChartDao.findLastByStocknum(s));
    	}
    }
    
    @Transactional
    @Test
    public void checkForLastItemStockHl() {
    	Stock s= stockDao.findBy(1657);
    	for(int i=0;i<10;i++) {
    		System.out.println("Stock Qutoe is " + hlDao.findLastByStock(s));
    	}
    }
    
    @Transactional
    @Test
    public void loadHl() throws Exception {
    	List<MonitoredStock> mStockList = mStockDao.retreiveByStockNum(1716);
//    	hlSvc.retrieveBatchStockHL(mStockList);
    	iexService.retrieveBatchStockHL(mStockList).forEach(p->{
    		System.out.println(p.getHlDatetime() +" - " + p.getOpen());
    	});;
    }
    
}

