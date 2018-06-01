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
import com.mpakam.dao.CustomerDao;
import com.mpakam.dao.CustomerTickerTrackerDao;
import com.mpakam.dao.MonitoredStockDao;
import com.mpakam.dao.StockAlertDao;
import com.mpakam.dao.StockDao;
import com.mpakam.dao.StockHlDataDao;
import com.mpakam.dao.StockQuoteDao;
import com.mpakam.dao.StockTickDataDao;
import com.mpakam.dao.StrategyDao;
import com.mpakam.dao.StrategyStockQuoteDao;
import com.mpakam.model.BacktestStockOrder;
import com.mpakam.model.CustomerTickerTracker;
import com.mpakam.model.MonitoredStock;
import com.mpakam.model.Stock;
import com.mpakam.model.StockHlData;
import com.mpakam.model.StockQuote;
import com.mpakam.model.StockTickData;
import com.mpakam.model.StrategyStockQuote;
import com.mpakam.scheduler.ScheduledTasks;
import com.mpakam.service.EmailService;
import com.mpakam.service.IStockHlDataService;
import com.mpakam.service.IStockQuoteService;
import com.mpakam.service.IStockTickDataService;
import com.mpakam.service.strategy.IStrategyService;
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
public class BackTestingStrategies {
	
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
	

    @Test
    @Transactional
    @Rollback(false)
    /*
     * Strategy1 - BTO when its uptrend. sell on Red
     * 	STO when in downtrend.  buy on green
     * 
     */
    public void backTestStrategy1() throws Exception{
    	Stock st = stockDao.findBy(890);
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
        				if(trend >=0 ) {// This ensures that we are only capturing the highest high when we were in uptrend but a sell signal
        					String quoteStr = "; Current High: " +quotes.get(index-1).getXhigh().doubleValue() + "; Prev High: " +prevHighestHigh;
        					if(prevHighestHigh > quotes.get(index-1).getXhigh().doubleValue()) {
        						log.debug(quote.getStockQuote().getStock().getTicker() +" : Donot SELL- - Up Trend "  + quote.getStockQuote().getQuoteDatetime() + quoteStr);
        					}else {
        						log.debug(quote.getStockQuote().getStock().getTicker() +" : SELL- Sell - Down Trend "  + quote.getStockQuote().getQuoteDatetime() + quoteStr + " StopLoss: "+quotes.get(index-1).getXhigh());
        						trend = -1;
        						if(prevOrder == null) {
        							prevOrder=createNewBackTestOrder(quote, signal,quotes.get(index-1).getXhigh());
        						}
        					}
    						prevHighestHigh = quotes.get(index-1).getXhigh().doubleValue(); //Capturing previous high
            			}
        				if(prevOrder!=null) {// Making sure that we are not closing in the same Txn
        					//&& prevOrder.getStrategyStockQuote().getStrategyStockQuoteId() != quote.getStrategyStockQuoteId()
							orderList.add(closeBackTestOrder(quote,prevOrder));
							prevOrder = null;
						}
        			}else if (prevHighestHigh == 0) {
        				prevHighestHigh = currentHigh; // Initial Setting
        			}    			
        		}else if(signal == 1) { //Buy
        			if(prevLowestLow != 0 ) {
        				if(trend <=0) {
        					String quoteStr = "; Current Low: " + quotes.get(index-1).getXlow().doubleValue() + "; Prev Low: " +prevLowestLow;
        					if(prevLowestLow < quotes.get(index-1).getXlow().doubleValue()) {
        						log.debug(quote.getStockQuote().getStock().getTicker() + " : BUY - Up Trend "  + quote.getStockQuote().getQuoteDatetime() + quoteStr + " StopLoss:" +quotes.get(index-1).getXlow());
        						trend = 1;
        						if(prevOrder == null) {
        							prevOrder=createNewBackTestOrder(quote,signal,quotes.get(index-1).getXlow());
        						}
        					}else {
        						log.debug(quote.getStockQuote().getStock().getTicker() +" : Donot BUY - Down- Trend "  + quote.getStockQuote().getQuoteDatetime() +quoteStr);
        					}
        					prevLowestLow = quotes.get(index-1).getXlow().doubleValue(); //Capturing previous high
        				}
        				if(prevOrder!=null ) { // Making sure that we are not closing in the same Txn
        					//&& prevOrder.getStrategyStockQuote().getStrategyStockQuoteId() != quote.getStrategyStockQuoteId()
							orderList.add(closeBackTestOrder(quote,prevOrder));
							prevOrder = null;
						}
        			}else if (prevLowestLow == 0) {
        				prevLowestLow = currentLow;
        			}
        		}else {
        			if(trend == 1 && currentLow <prevLowestLow && prevLowestLow>0) {//Still in up-trend, check if current low is below the PrevLowestlow
        				String quoteStr = "; Current Low: " + currentLow + "; Prev Low: " +prevLowestLow;
        				log.debug(quote.getStockQuote().getStock().getTicker() + " Change in Trend to DOWN" + quote.getStockQuote().getQuoteDatetime() + quoteStr);
        				trend = -1;
        			}else if(trend == -1 && currentHigh > prevHighestHigh && prevHighestHigh>0) {//Still in Down-trend, check if current high is above the Prevhighesthigh
        				String quoteStr = "; Current High: " + currentHigh + "; Prev High: " +prevHighestHigh;
        				log.debug(quote.getStockQuote().getStock().getTicker() + " Change in Trend to UP" + quote.getStockQuote().getQuoteDatetime() + quoteStr);
        				trend = 1;
        			}
        			if(prevOrder!=null) {
        				BacktestStockOrder stopLossedOrder =checkForStopLoss(quote, prevOrder);
        				if(stopLossedOrder!= null) {
        					orderList.add(stopLossedOrder);
        					trend=(trend==1)?-1:1;
        					log.debug(quote.getStockQuote().getStock().getTicker() +" Hit the stoploss @ "+ quote.getStockQuote().getQuoteDatetime());
            				prevOrder=null;
        				}
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
    	double totalProfit=0;
    	for(BacktestStockOrder p: orderList) {
    		double profitOrLoss = p.getProfitLoss().doubleValue();
    		log.debug(p.getOrderType() + " Open Price:"+ p.getOpenPrice() + " @ "+ p.getOpenDatetime());
    		log.debug(p.getOrderType() + " Close Price:"+ p.getClosePrice() + " @ "+ p.getCloseDatetime());
    		log.debug( p.getOrderType() + (profitOrLoss<=0?" LOSS":" PROFIT") +" : "+ p.getProfitLoss());
    		totalProfit+=profitOrLoss;
    	}
    	log.debug(st.getTicker() + " - Total Profit is" + totalProfit);
    }
    
    private BacktestStockOrder createNewBackTestOrder(StrategyStockQuote quote, int signal, BigDecimal stopLossPrice) {
			BacktestStockOrder order = new BacktestStockOrder();
			order.setEntryDatetime(LocalDateTime.now());
			order.setOpenPrice(quote.getStockQuote().getClose());
			order.setOpenDatetime(quote.getStockQuote().getQuoteDatetime());
			order.setStock(quote.getStockQuote().getStock());
			//order.setStrategyStockQuote(quote);
			order.setStrategyId(1); // HeikenAshi
			order.setStopLossPrice(stopLossPrice);
			order.setOrderType((signal==1)?"BTO":"STO"); //BTO - Buy to Open; STO - Sell to Open
			return order;
    }
    
    private BacktestStockOrder checkForStopLoss(StrategyStockQuote quote, BacktestStockOrder order) {
    	if(order == null)
    		return null;
//    	log.debug("BacktestOrder:"+order.toString());
//    	log.debug("StrategyStock:"+quote.toString());
    	if(order.getOrderType().equals("BTO") && order.getStopLossPrice().compareTo(quote.getStockQuote().getClose() )>=0) {
    			return closeBackTestOrder(quote, order);
    		
    	}else if (order.getOrderType().equals("STO") && (order.getStopLossPrice().compareTo(quote.getStockQuote().getClose() )<=0)) {
    			return closeBackTestOrder(quote, order);
    	}
    	return null;
    }
    
    private BacktestStockOrder checkForMovingStopLoss(StrategyStockQuote quote, BacktestStockOrder order) {
    	if(order == null)
    		return null;
//    	log.debug("BacktestOrder:"+order.toString());
//    	log.debug("StrategyStock:"+quote.toString());
    	if(order.getOrderType().equals("BTO") ) {
    		if(order.getStopLossPrice().compareTo(quote.getStockQuote().getClose() )>=0)
    			return closeBackTestOrder(quote, order);
    		else {
    			order.setStopLossPrice(quote.getXlow()); // Setting the moving the stoploss
    		}
    		
    	}else if (order.getOrderType().equals("STO") ) {
    		if(order.getStopLossPrice().compareTo(quote.getStockQuote().getClose() )<=0)
    			return closeBackTestOrder(quote, order);
    		else {
    			order.setStopLossPrice(quote.getXhigh()); // Setting the moving the stoploss
    		}
    	}
    	return null;
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
    @Transactional
    @Rollback(false)
    /*
     * StopLoss is considered change in Trend
     * 
     */
    public void backTestStrategy2() throws Exception{
    	Stock st = stockDao.findBy(906);
    	List<Stock> sList = new ArrayList<>();
    	sList.add(st);
//    	List<Stock> sList =stockDao.findAll();
    	
    	
    	sList.forEach(s->{
    		LinkedList<BacktestStockOrder> orderList = new LinkedList<>();
    		LinkedList<StrategyStockQuote> quotes = stratStockQuoteDao.getAllByStockNum(s);
        	StrategyStockQuote lastStockQuote=null;
        	
        	BacktestStockOrder prevOrder = null;
        	int prevSignal =0;
        	int trend =0;
        	
        	BigDecimal currentStopLoss = BigDecimal.ZERO; //Use this inconjuction with signal
        	for(int index =0;index<quotes.size();index++) {
        		StrategyStockQuote quote  = quotes.get(index);
        		int signal =strategySvc.checkForSignalForTrend(quote, lastStockQuote);
        		if(lastStockQuote == null) {
        			lastStockQuote = quote;
        			continue;
        		}
        		
        		if(prevSignal!=0 &&  prevSignal == signal) // Eliminates duplicate signals
        			continue;
        		if(trend == 0 && signal!=0) // no trend to start w.
        			trend = signal;
        		if(signal == -1) { //Sell
        			if(prevOrder == null && trend == signal) { ///STO
        				currentStopLoss =quotes.get(index-1).getXhigh();
        				prevOrder=createNewBackTestOrder(quote,signal,currentStopLoss);
        			}else if(prevOrder != null && prevOrder.getOrderType().equals("BTO")) {// prev order is BTO, close it
        				orderList.add(closeBackTestOrder(quote,prevOrder)); // Closing to booking the profit
						prevOrder = null;
        			}
        		}else if(signal == 1) { // Buy
        			if(prevOrder == null && trend == signal) { //BTO
        				currentStopLoss =quotes.get(index-1).getXlow();
        				prevOrder=createNewBackTestOrder(quote,signal,currentStopLoss);
        			}else if(prevOrder != null && prevOrder.getOrderType().equals("STO")) { // prev order is STO, close it
        				orderList.add(closeBackTestOrder(quote,prevOrder));// Closing to booking the profit
						prevOrder = null;
        			}
        		}else {//Check for stop losses
        			if(prevOrder!=null) { // If thre is an Open Order
        				BacktestStockOrder stopLossedOrder =checkForStopLoss(quote, prevOrder);
        				if(stopLossedOrder!= null) {
        					orderList.add(stopLossedOrder);
        					trend=(trend==1)?-1:1; // Change the Trend
        					log.debug(quote.getStockQuote().getStock().getTicker() +" : " + stopLossedOrder.getOrderType() + " Hit the stoploss @ "+ quote.getStockQuote().getQuoteDatetime());
            				prevOrder=null;
        				}
        			}
        		}
        		lastStockQuote = quote;
        		prevSignal=signal;
        	}
        	double totalProfit=0;
        	for(BacktestStockOrder p: orderList) {
        		double profitOrLoss = p.getProfitLoss().doubleValue();
        		log.debug(p.getOrderType() + " Open Price:"+ p.getOpenPrice() + " @ "+ p.getOpenDatetime());
        		log.debug(p.getOrderType() + " Close Price:"+ p.getClosePrice() + " @ "+ p.getCloseDatetime());
        		log.debug( p.getOrderType() + (profitOrLoss<=0?" LOSS":" PROFIT") +" : "+ p.getProfitLoss());
        		totalProfit+=profitOrLoss;
        	}
        	log.error(s.getTicker() + " - Total Profit is" + totalProfit);	
    	});
    }
    
    
    @Test
    @Transactional
    @Rollback(false)
    /*
     * StopLoss is considered change in Trend
     * take profit @ $2 & take loss @ $1.
     * 
     */
    public void backTestStrategy3() throws Exception{
    	Stock st = stockDao.findBy(890);
    	List<Stock> sList = new ArrayList<>();
    	sList.add(st);
    	LinkedList<BacktestStockOrder> orderList = new LinkedList<>();
    	
    	BigDecimal takeProfit = new BigDecimal(20);
    	
    	sList.forEach(s->{
    		LinkedList<StrategyStockQuote> quotes = stratStockQuoteDao.getAllByStockNum(s);
        	StrategyStockQuote lastStockQuote=null;
        	
        	BacktestStockOrder prevOrder = null;
        	int prevSignal =0;
        	int trend =0;
        	
        	BigDecimal currentStopLoss = BigDecimal.ZERO; //Use this inconjuction with signal
        	for(int index =0;index<quotes.size();index++) {
        		StrategyStockQuote quote  = quotes.get(index);
        		int signal =strategySvc.checkForSignalForTrend(quote, lastStockQuote);
        		if(lastStockQuote == null) {
        			lastStockQuote = quote;
        			continue;
        		}
        		
        		if(prevSignal!=0 &&  prevSignal == signal) // Eliminates duplicate signals
        			continue;
        		if(trend == 0 && signal!=0) // no trend to start w.
        			trend = signal;
        		if(signal == -1) { //Sell
        			if(prevOrder == null && trend == signal) { ///STO
        				currentStopLoss =quotes.get(index-1).getXhigh();
        				prevOrder=createNewBackTestOrder(quote,signal,currentStopLoss);
        			}else if(prevOrder != null && prevOrder.getOrderType().equals("BTO")) {// prev order is BTO, close it
        				orderList.add(closeBackTestOrder(quote,prevOrder)); // Closing to booking the profit
						prevOrder = null;
        			}
        		}else if(signal == 1) { // Buy
        			if(prevOrder == null && trend == signal) { //BTO
        				currentStopLoss =quotes.get(index-1).getXlow();
        				prevOrder=createNewBackTestOrder(quote,signal,currentStopLoss);
        			}else if(prevOrder != null && prevOrder.getOrderType().equals("STO")) { // prev order is STO, close it
        				orderList.add(closeBackTestOrder(quote,prevOrder));// Closing to booking the profit
						prevOrder = null;
        			}
        		}else {//Check for stop losses
        			if(prevOrder!=null) { // If thre is an Open Order
        				BacktestStockOrder stopLossedOrder =checkForStopLoss(quote, prevOrder);
        				if(stopLossedOrder!= null) {
        					orderList.add(stopLossedOrder);
        					trend=(trend==1)?-1:1; // Change the Trend
        					log.debug(quote.getStockQuote().getStock().getTicker() +" : " + stopLossedOrder.getOrderType() + " Hit the stoploss @ "+ quote.getStockQuote().getQuoteDatetime());
            				prevOrder=null;
        				}else {//Take Profit
        					BacktestStockOrder takeProfitOrder =checkForProfit(quote, prevOrder,takeProfit);
        					if(takeProfitOrder!= null) {
        						orderList.add(takeProfitOrder);
        						trend=(trend==1)?-1:1; // Change the Trend
        						log.debug(quote.getStockQuote().getStock().getTicker() +" : " + takeProfitOrder.getOrderType() + " Hit the stoploss @ "+ quote.getStockQuote().getQuoteDatetime());
        						prevOrder=null;
        					}
        				}
        			}
        		}
        		lastStockQuote = quote;
        		prevSignal=signal;
        	}
    	});
    	double totalProfit=0;
    	for(BacktestStockOrder p: orderList) {
    		double profitOrLoss = p.getProfitLoss().doubleValue();
    		log.debug(p.getOrderType() + " Open Price:"+ p.getOpenPrice() + " @ "+ p.getOpenDatetime());
    		log.debug(p.getOrderType() + " Close Price:"+ p.getClosePrice() + " @ "+ p.getCloseDatetime());
    		log.debug( p.getOrderType() + (profitOrLoss<=0?" LOSS":" PROFIT") +" : "+ p.getProfitLoss());
    		totalProfit+=profitOrLoss;
    	}
    	log.debug(st.getTicker() + " - Total Profit is" + totalProfit);
    }
    private BacktestStockOrder checkForProfit(StrategyStockQuote quote, BacktestStockOrder order, BigDecimal takeProfit) {
    	if(order == null)
    		return null;
//    	log.debug("BacktestOrder:"+order.toString());
//    	log.debug("StrategyStock:"+quote.toString());
    	if(order.getOrderType().equals("BTO") && quote.getStockQuote().getClose().compareTo(order.getOpenPrice().add(takeProfit))>=0) {
    		return closeBackTestOrder(quote, order);
    		
    	}else if (order.getOrderType().equals("STO") && quote.getStockQuote().getClose().compareTo(order.getOpenPrice().subtract(takeProfit)) <=0) {
    		return closeBackTestOrder(quote, order);
    	}
    	return null;
    }
    
    @Test
    @Transactional
    @Rollback(false)
    /*
     * Moving stoploss to the previous candle's low for BTO, high for STO
     * 
     * 
     */
    public void backTestStrategy4() throws Exception{
//    	Stock st = stockDao.findBy(880);
//    	List<Stock> sList = new ArrayList<>();
//    	sList.add(st);
    	List<Stock> sList =stockDao.findAll();
    	
    	sList.forEach(s->{
    		LinkedList<BacktestStockOrder> orderList = new LinkedList<>();
    		LinkedList<StrategyStockQuote> quotes = stratStockQuoteDao.getAllByStockNum(s);
        	StrategyStockQuote lastStockQuote=null;
        	
        	BacktestStockOrder prevOrder = null;
        	int prevSignal =0;
        	int trend =0;
        	
        	BigDecimal currentStopLoss = BigDecimal.ZERO; //Use this inconjuction with signal
        	for(int index =0;index<quotes.size();index++) {
        		StrategyStockQuote quote  = quotes.get(index);
        		int signal =strategySvc.checkForSignalForTrend(quote, lastStockQuote);
        		if(lastStockQuote == null) {
        			lastStockQuote = quote;
        			continue;
        		}
        		
        		if(prevSignal!=0 &&  prevSignal == signal) // Eliminates duplicate signals
        			continue;
        		if(trend == 0 && signal!=0) // no trend to start w.
        			trend = signal;
        		if(signal == -1) { //Sell
        			if(prevOrder == null && trend == signal) { ///STO
        				currentStopLoss =quotes.get(index-1).getXhigh();
        				prevOrder=createNewBackTestOrder(quote,signal,currentStopLoss);
        			}else if(prevOrder != null && prevOrder.getOrderType().equals("BTO")) {// prev order is BTO, close it
        				orderList.add(closeBackTestOrder(quote,prevOrder)); // Closing to booking the profit
						prevOrder = null;
        			}
        		}else if(signal == 1) { // Buy
        			if(prevOrder == null && trend == signal) { //BTO
        				currentStopLoss =quotes.get(index-1).getXlow();
        				prevOrder=createNewBackTestOrder(quote,signal,currentStopLoss);
        			}else if(prevOrder != null && prevOrder.getOrderType().equals("STO")) { // prev order is STO, close it
        				orderList.add(closeBackTestOrder(quote,prevOrder));// Closing to booking the profit
						prevOrder = null;
        			}
        		}else {//Check for stop losses
        			if(prevOrder!=null) { // If thre is an Open Order
        				BacktestStockOrder stopLossedOrder =checkForMovingStopLoss(quote, prevOrder);
        				if(stopLossedOrder!= null) {
        					orderList.add(stopLossedOrder);
        					trend=(trend==1)?-1:1; // Change the Trend
        					log.debug(quote.getStockQuote().getStock().getTicker() +" : " + stopLossedOrder.getOrderType() + " Hit the stoploss @ "+ quote.getStockQuote().getQuoteDatetime());
            				prevOrder=null;
        				}
        			}
        		}
        		lastStockQuote = quote;
        		prevSignal=signal;
        	}
        	double totalProfit=0;
        	for(BacktestStockOrder p: orderList) {
        		double profitOrLoss = p.getProfitLoss().doubleValue();
        		log.debug(p.getOrderType() + " Open Price:"+ p.getOpenPrice() + " @ "+ p.getOpenDatetime());
        		log.debug(p.getOrderType() + " Close Price:"+ p.getClosePrice() + " @ "+ p.getCloseDatetime());
        		log.debug( p.getOrderType() + (profitOrLoss<=0?" LOSS":" PROFIT") +" : "+ p.getProfitLoss());
        		totalProfit+=profitOrLoss;
        	}
        	log.error(s.getTicker() + " - Total Profit is" + totalProfit);	
    	});
    }

}

