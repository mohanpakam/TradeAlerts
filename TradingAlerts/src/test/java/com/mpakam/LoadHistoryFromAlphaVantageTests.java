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
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.patriques.input.timeseries.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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

import pl.zankowski.iextrading4j.api.stocks.Chart;
import pl.zankowski.iextrading4j.api.stocks.DailyChart;
import pl.zankowski.iextrading4j.api.stocks.Price;
import pl.zankowski.iextrading4j.client.IEXTradingClient;
import pl.zankowski.iextrading4j.client.rest.request.stocks.BatchChartRequestBuilder;
import pl.zankowski.iextrading4j.client.rest.request.stocks.BatchPriceRequestBuilder;
import pl.zankowski.iextrading4j.client.rest.request.stocks.ChartRange;
import pl.zankowski.iextrading4j.client.rest.request.stocks.DailyChartRequestBuilder;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes=TradingAlertsApplication.class)
@TestPropertySource(locations="classpath:application-test.properties")
public class LoadHistoryFromAlphaVantageTests {
	
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
    
  //Initial load of stock quotes from scatch. 
    @Transactional
    @Test
    public void loadHistoricData() {
    	List<Stock> stocks=  stockDao.findAll();
    	stocks.forEach(p->{
    			
        		try {
            		System.out.println("analyzing for stock - " + p.getStockName());
					quoteService.analyzeStockFromAlphaVantage(p);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    	});
    	
    }    
}

