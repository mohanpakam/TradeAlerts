package com.mpakam.service.strategy;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.transaction.Transactional;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mpakam.dao.BacktestStockOrderDao;
import com.mpakam.dao.MonitoredStockDao;
import com.mpakam.dao.StockAlertDao;
import com.mpakam.dao.StockDao;
import com.mpakam.dao.StrategyStockQuoteDao;
import com.mpakam.model.BacktestStockOrder;
import com.mpakam.model.Customer;
import com.mpakam.model.MonitoredStock;
import com.mpakam.model.Stock;
import com.mpakam.model.StockAlert;
import com.mpakam.model.StockQuote;
import com.mpakam.model.Strategy;
import com.mpakam.model.StrategyStockQuote;
import com.mpakam.service.EmailService;



/**
 * Strategy: 
 * Long
 * 	Open	: when EMA9 crosses over Kijun and a green HA candle appears
 * 	close	: when a red candle closes below Kijun.
 * 
 * Short
 * 	Open	: when EMA9 crosses below Kijun and a  
 *  
 * @author lucky
 *
 */
@Service
public class HAIchimokuEMAKijunXService implements IStrategyService{

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	final int STRATEGYID=1;
	final BigDecimal periodLength = new BigDecimal(14);
	final int stochPeriodLength = 14;
	final int K_LENGTH=3;
	final int D_LENGTH=3;
	final BigDecimal ZERO= new BigDecimal(0).setScale(4);
	final BigDecimal HUNDRED = new BigDecimal(100).setScale(4);
	final BigDecimal ONE = new BigDecimal(1).setScale(4);
	final MathContext mc = new MathContext(4, RoundingMode.HALF_UP);
	final Strategy s= new Strategy();
	public static Customer c = new Customer();
	{
		s.setStrategyId(STRATEGYID);
		c.setCustomerid(1);
		c.setEmailId("mohaneee221@gmail.com");
	}
	
	@Autowired
	StrategyStockQuoteDao strategyStockQuoteDao;
	
	@Autowired
	StockAlertDao stockAlertDao;
	
	@Autowired
	EmailService emailService;
	
	@Autowired
	MonitoredStockDao mStockDao;
	
	@Autowired
	StockDao stockDao;
	
	@Autowired
	BacktestStockOrderDao backtestOrderDao;
	
	
	@Override
	@Transactional
	public int executeStrategy(Set<StockQuote> quotes) {
		int trend =0;
		LinkedList<BigDecimal> lastNRsi= new LinkedList<BigDecimal>();
		LinkedList<StrategyStockQuote> smaStockRsiList = new LinkedList<StrategyStockQuote>(); 
		int quoteCounter=0; // for initial loads
		TreeSet<StrategyStockQuote> initialRsiQuotes= new TreeSet<StrategyStockQuote>();
		Stock stock=((TreeSet<StockQuote>)quotes).first().getStock();
		int stockNum  =stock.getStocknum();
		LinkedList<StrategyStockQuote> lastStrategyQuotes=strategyStockQuoteDao.retrieveLastXQuotesByStockNumStrategyId(stock, STRATEGYID);
		
		StrategyStockQuote lastStrategyQuote = 
				(lastStrategyQuotes == null || lastStrategyQuotes.size()==0)?
						null: lastStrategyQuotes.get(0);
		boolean lastItemExists = lastStrategyQuote!=null;
		boolean sendEmails=true;
		if(lastItemExists) {
			int size = lastStrategyQuotes.size();
			for(int i=size-1;i>=0;i--) {
				lastNRsi.addLast(lastStrategyQuotes.get(i).getRsi());
			}
		}else
			sendEmails=false;
		for (StockQuote stockQuote : quotes) {
			//System.out.println("Quote TimeSTamp: "+stockQuote.getQuoteDatetime());
			BigDecimal xOpenPrev = new BigDecimal(0);
			BigDecimal xClosePrev = new BigDecimal(0);
			
			StrategyStockQuote strategyStockQuote = new StrategyStockQuote();
			strategyStockQuote.setStockQuote(stockQuote);			
			strategyStockQuote.setStrategy(s);
			strategyStockQuote.setSendEmail(sendEmails);
			
			if (lastItemExists || lastStrategyQuote != null) {
				xOpenPrev = lastStrategyQuote.getXopen();
				xClosePrev = lastStrategyQuote.getXclose();
				quoteCounter=15; // for continued loads, we just need to start caculating the RSI instead of initial load code
			} else {
				//System.out.println("Initial settingup of stock" + stockQuote.getStock().getTicker());
				initialRsiQuotes.add(strategyStockQuote);
				xClosePrev = stockQuote.getClose();
				xOpenPrev = stockQuote.getOpen();
			}
			
			BigDecimal xClose = (stockQuote.getOpen().add(stockQuote.getHigh()).add(stockQuote.getLow()).add(stockQuote.getClose())).divide(new BigDecimal(4));
			BigDecimal xOpen = (xOpenPrev.add(xClosePrev)).divide(new BigDecimal(2));
			BigDecimal xHigh = stockQuote.getHigh().max(xClosePrev.max(xOpenPrev));
			BigDecimal xLow = stockQuote.getLow().min(xClosePrev.min(xOpenPrev));
			
			strategyStockQuote.setXclose(xClose);
			strategyStockQuote.setXopen(xOpen);
			strategyStockQuote.setXhigh(xHigh);
			strategyStockQuote.setXlow(xLow);
			
			if(quoteCounter<14) {
				if(!initialRsiQuotes.isEmpty()) {
					//Collecting first 14 records
					initialRsiQuotes.add(strategyStockQuote);
				}
				quoteCounter++;
			}else if(quoteCounter == 14) {
				quoteCounter++;
				//we now have 14 previous records to calculate the RSI
				initialRsiQuotes.add(strategyStockQuote);
				calculateInitialStochRsi(initialRsiQuotes);
			}else {
				BigDecimal gains=new BigDecimal(0);
				BigDecimal losses=new BigDecimal(0);
				BigDecimal avgUp = lastStrategyQuote.getAvgGain();
				BigDecimal avgDown = lastStrategyQuote.getAvgLoss();
				//System.out.println("Before-Avgup" + avgUp +"AvgDown-"+avgDown);
		        BigDecimal change = strategyStockQuote.getXclose().subtract(lastStrategyQuote.getXclose(),mc); 
		        
		        gains=gains.add(ZERO.max(change));
	            losses = losses.add(ZERO.max(change.negate()));
		        
				//avgUp = ((avgUp * (periodLength - 1)) + gains) / (periodLength);
	            avgUp = (avgUp.multiply(periodLength.subtract(ONE)).add(gains)).divide( periodLength, mc);
				//avgDown = ((avgDown * (periodLength - 1)) + losses)/ (periodLength);
				avgDown= (avgDown.multiply(periodLength.subtract(ONE)).add(losses)).divide( periodLength, mc);
				strategyStockQuote.setAvgGain(avgUp);
				strategyStockQuote.setAvgLoss(avgDown);
				//stockQuote.setRsi(new BigDecimal(100 - (100 / (1 + (avgUp / avgDown)))));
				//System.out.println("Gains:"+gains + "losses:"+losses+"Change:"+change);
				//System.out.println("After-Avgup" + avgUp +"AvgDown-"+avgDown);
				if(!(avgDown.compareTo(ZERO) == 0))
					strategyStockQuote.setRsi(HUNDRED.subtract(HUNDRED.divide(ONE.add(avgUp.divide(avgDown, mc)),mc)));
				else
					strategyStockQuote.setRsi(ZERO);
					
				if(strategyStockQuote.getRsi() != null) {
					addRsiToList(lastNRsi, strategyStockQuote.getRsi());
					strategyStockQuote.setHighrsi(getMaxRSIn(lastNRsi, strategyStockQuote.getRsi()));
					strategyStockQuote.setLowrsi(getMinRSIn(lastNRsi, strategyStockQuote.getRsi()));
//					strategyStockQuote.setStochRsi(calculateStochRsi(strategyStockQuote.getLowrsi(),strategyStockQuote.getHighrsi(),strategyStockQuote.getRsi()));
					calculateStochRsi(strategyStockQuote,lastStrategyQuotes);
					addStochRsiToList(strategyStockQuote,lastStrategyQuotes,smaStockRsiList);
				}
				
//				determineTrend(stockQuote,lastItem);
//				sendAlert(stockQuote);//TODO: uncomment
				//System.out.println("AvgUp:"+ avgUp +" AvgDown:"+avgDown+" RsiK:"+strategyStockQuote.getStochRsiK() +" RsiD:"+strategyStockQuote.getStochRsiD() );
			}
			strategyStockQuote.setStrategyStockQuoteId((int)strategyStockQuoteDao.save(strategyStockQuote));
//			trend=checkForSignal(strategyStockQuote, lastStrategyQuote);
			try {
//				trend=superTrend(strategyStockQuote, lastStrategyQuote);
				trend=simpleTrend(strategyStockQuote, lastStrategyQuote);
			} catch (Exception e) {
				trend = 0;
				e.printStackTrace();
			}
			lastStrategyQuote = strategyStockQuote;
			
		}
//		createAlert(lastStrategyQuote, trend); // commented out 
		return trend;
	}
	
	private void addStochRsiToList(StrategyStockQuote currentStrategyQuote, LinkedList<StrategyStockQuote> lastStrategyQuotes, LinkedList<StrategyStockQuote> lastStochRsi) {
		
		if(lastStrategyQuotes != null && lastStrategyQuotes.size() > 0 && lastStochRsi.size()<2) {
				if(lastStrategyQuotes.size()==1)
					lastStochRsi.addLast(lastStrategyQuotes.get(0));
				else {
					lastStochRsi.addLast(lastStrategyQuotes.get(1));
					lastStochRsi.addLast(lastStrategyQuotes.get(0));
				}
		}
		
		if(lastStochRsi.size() >= K_LENGTH) {
			lastStochRsi.removeFirst();
		}
		
		lastStochRsi.addLast(currentStrategyQuote);
		BigDecimal rawK=new BigDecimal(0);
		for(int i=0;i<lastStochRsi.size();i++ ) {
			rawK = rawK.add(lastStochRsi.get(i).getStochRsi());
			//System.out.println(i+" RawK is" + rawK);
		}
		currentStrategyQuote.setStochRsiK(rawK.divide(new BigDecimal(lastStochRsi.size()),mc));
		//System.out.println("RStoch Size is"+lastStochRsi.size()+"Smoothed Stoch RSIK is" + currentStrategyQuote.getStochRsiK());
		BigDecimal smoothedD = new BigDecimal(0);
		for(int i=0;i<lastStochRsi.size();i++ ) {
			smoothedD = smoothedD.add(lastStochRsi.get(i).getStochRsiK());
		}
		currentStrategyQuote.setStochRsiD(smoothedD.divide(new BigDecimal(lastStochRsi.size()),mc));
		//System.out.println(" StockRsiD is" + currentStrategyQuote.getStochRsiD());
	}	
	
	@Override
	public int checkForSignal(StrategyStockQuote currentQuote, StrategyStockQuote prevQuote) {
		
		if(currentQuote.getStockQuote().getInterval() == 480)
			return checkForDailySignal(currentQuote, prevQuote);
		
		int buyOrSell=0;//-1-sell;+1-Buy
		if(prevQuote == null || prevQuote.getStochRsiD() == null || 
				currentQuote== null || currentQuote.getStochRsiK() == null)
			return 0;
		//Check for Buy Signal
		if(currentQuote.getStochRsiD().doubleValue() <=25 && currentQuote.getStochRsiK().doubleValue()>= currentQuote.getStochRsiD().doubleValue()) { //TODO: changed with HyperTrend 25 to 40
			if(currentQuote.getXopen().compareTo(currentQuote.getXclose()) ==- 1	) {//xopen >xclose
				//System.out.println("Buy Signal " +currentQuote.getStockQuote().getStock().getTicker());
				buyOrSell=1;
			}
		}
		
		//Check for Sell Signal
		if(prevQuote.getStochRsiD().doubleValue() >=75 && 
				currentQuote.getStochRsiK().doubleValue()<= currentQuote.getStochRsiD().doubleValue()) { //TODO: changed with HyperTrend 75 to 60
			if(currentQuote.getXopen().compareTo(currentQuote.getXclose()) == 1	) {//xopen >xclose
				//System.out.println("Sell Signal " +currentQuote.getStockQuote().getStock().getTicker());
				buyOrSell=-1;
			}
		}
		return buyOrSell;
		/*if (buyOrSell != 0) {
			sendEmail(currentQuote, buyOrSell);
		}*/
	}
	
	@Override
	public int checkForSignalForTrend(StrategyStockQuote currentQuote, StrategyStockQuote prevQuote) {
		
		if(currentQuote.getStockQuote().getInterval() == 480)
			return checkForDailySignal(currentQuote, prevQuote);
		
		int buyOrSell=0;//-1-sell;+1-Buy
		if(prevQuote == null || prevQuote.getStochRsiD() == null || 
				currentQuote== null || currentQuote.getStochRsiK() == null)
			return 0;
		//Check for Buy Signal
		if(currentQuote.getStochRsiD().doubleValue() <=25 && currentQuote.getStochRsiK().doubleValue()>= currentQuote.getStochRsiD().doubleValue()) {
			if(currentQuote.getXopen().compareTo(currentQuote.getXclose()) ==- 1	) {//xopen >xclose
				//System.out.println("Buy Signal " +currentQuote.getStockQuote().getStock().getTicker());
				buyOrSell=1;
			}
		}

		//Check for Sell Signal
		if(currentQuote.getStochRsiD().doubleValue() >=75 && 
				currentQuote.getStochRsiK().doubleValue()<= currentQuote.getStochRsiD().doubleValue()) {
			if(currentQuote.getXopen().compareTo(currentQuote.getXclose()) == 1	) {//xopen >xclose
				//System.out.println("Sell Signal " +currentQuote.getStockQuote().getStock().getTicker());
				buyOrSell=-1;
			}
		}
		return buyOrSell;
		/*if (buyOrSell != 0) {
			sendEmail(currentQuote, buyOrSell);
		}*/
	}
	
	public int setupInitialSuperTrend(Stock s) throws Exception{
    	LinkedList<StrategyStockQuote> quotes = strategyStockQuoteDao.getAllByStockNum(s);
    	StrategyStockQuote lastStockQuote=null;
    	double prevHighestHigh=0;
    	double prevLowestLow=0;
    	// register high only after low and low only after high
    	int trend =0;
    	int index = 0;
    	for(StrategyStockQuote quote: quotes) {
    		int signal =checkForSignalForTrend(quote, lastStockQuote);
    		if(lastStockQuote == null) {
    			lastStockQuote = quote;
    			continue;
    		}
    		double currentHigh = lastStockQuote.getXhigh().doubleValue();
    		double currentLow= lastStockQuote.getXlow().doubleValue();
    		
    		if(signal == -1) { //Sell
    			if(prevHighestHigh != 0) {
    				if(trend >=0 ) {// This ensures that we areonly capturing the highest high when we were in uptrend but a sell signal
    					String quoteStr = "; Current High: " +quotes.get(index-1).getXhigh().doubleValue() + "; Prev High: " +prevHighestHigh;
    					if(prevHighestHigh > quotes.get(index-1).getXhigh().doubleValue()) {
    						System.out.println(s.getTicker() + " - Donot SELL- - Up Trend "  + quote.getStockQuote().getQuoteDatetime() + quoteStr);
    					}else {
    						System.out.println(s.getTicker() + " - SELL- Sell - Down Trend "  + quote.getStockQuote().getQuoteDatetime() + quoteStr);
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
    						System.out.println(s.getTicker() + " - BUY - Up Trend "  + quote.getStockQuote().getQuoteDatetime() + quoteStr);
    						trend = 1;
    					}else {
    						System.out.println(s.getTicker() + " - Donot BUY - Down- Trend "  + quote.getStockQuote().getQuoteDatetime() +quoteStr);
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
    	System.out.println("Setting up the final trend " + trend);
    	s.setHighestHigh(new BigDecimal(prevHighestHigh));
    	s.setLowestLow(new BigDecimal(prevLowestLow));
    	s.setTrend(trend);
    	stockDao.merge(s);
    	return trend;
    }
	
	public int setupInitialSimpleTrend(Stock s) throws Exception {

		LinkedList<BacktestStockOrder> orderList = new LinkedList<>();
		LinkedList<StrategyStockQuote> quotes = strategyStockQuoteDao.getAllByStockNum(s);
		StrategyStockQuote lastStockQuote = null;

		BacktestStockOrder prevOrder = null;
		int prevSignal = 0;
		int trend = 0;

		BigDecimal currentStopLoss = BigDecimal.ZERO; // Use this inconjuction with signal
		for (int index = 0; index < quotes.size(); index++) {
			StrategyStockQuote quote = quotes.get(index);
			int signal = checkForSignalForTrend(quote, lastStockQuote);
			if (lastStockQuote == null) {
				lastStockQuote = quote;
				continue;
			}

			if (prevSignal != 0 && prevSignal == signal) // Eliminates duplicate signals
				continue;
			if (trend == 0 && signal != 0) // no trend to start w.
				trend = signal;
			if (signal == -1) { // Sell
				if (prevOrder == null && trend == signal) { /// STO
					currentStopLoss = quotes.get(index - 1).getXhigh();
					prevOrder = createNewBackTestOrder(quote, signal, currentStopLoss);
				} else if (prevOrder != null && prevOrder.getOrderType().equals("BTO")) {// prev order is BTO, close it
					orderList.add(closeBackTestOrder(quote, prevOrder)); // Closing to booking the profit
					prevOrder = null;
				}
			} else if (signal == 1) { // Buy
				if (prevOrder == null && trend == signal) { // BTO
					currentStopLoss = quotes.get(index - 1).getXlow();
					prevOrder = createNewBackTestOrder(quote, signal, currentStopLoss);
				} else if (prevOrder != null && prevOrder.getOrderType().equals("STO")) { // prev order is STO, close it
					orderList.add(closeBackTestOrder(quote, prevOrder));// Closing to booking the profit
					prevOrder = null;
				}
			} else {// Check for stop losses
				if (prevOrder != null) { // If thre is an Open Order
					BacktestStockOrder stopLossedOrder = checkForStopLoss(quote, prevOrder);
					if (stopLossedOrder != null) {
						orderList.add(stopLossedOrder);
						trend = (trend == 1) ? -1 : 1; // Change the Trend
						log.debug(quote.getStockQuote().getStock().getTicker() + " : " + stopLossedOrder.getOrderType()
								+ " Hit the stoploss @ " + quote.getStockQuote().getQuoteDatetime());
						prevOrder = null;
					}
				}
			}
			lastStockQuote = quote;
			prevSignal = signal;
		}
		double totalProfit = 0;
		for (BacktestStockOrder p : orderList) {
			double profitOrLoss = p.getProfitLoss().doubleValue();
			log.debug(p.getOrderType() + " Open Price:" + p.getOpenPrice() + " @ " + p.getOpenDatetime());
			log.debug(p.getOrderType() + " Close Price:" + p.getClosePrice() + " @ " + p.getCloseDatetime());
			log.debug(p.getOrderType() + (profitOrLoss <= 0 ? " LOSS" : " PROFIT") + " : " + p.getProfitLoss());
			totalProfit += profitOrLoss;
		}
		log.error(s.getTicker() + " - Total Profit is" + totalProfit);
		System.out.println("Setting up the final trend " + trend);
		s.setTrend(trend);
		stockDao.merge(s);
		return trend;
	}
	
	private BacktestStockOrder createNewBackTestOrder(StrategyStockQuote quote, int signal, BigDecimal stopLossPrice) {
		
		BacktestStockOrder order = new BacktestStockOrder();
		order.setEntryDatetime(LocalDateTime.now());
		order.setOpenPrice(quote.getStockQuote().getClose());
		order.setOpenDatetime(quote.getStockQuote().getQuoteDatetime());
		order.setStock(quote.getStockQuote().getStock());
		order.setStrategyId(STRATEGYID);
		order.setStopLossPrice(stopLossPrice);
		order.setOrderType((signal==1)?"BTO":"STO"); //BTO - Buy to Open; STO - Sell to Open
		order.setBacktestStockOrderId((int)backtestOrderDao.save(order));
		log.debug("New backtest order " + order.getOpenDatetime() + " : "+order.getOrderType());
		createAlert(quote, order);
		return order;
	}

	private BacktestStockOrder checkForStopLoss(StrategyStockQuote quote, BacktestStockOrder order) {
		if (order == null)
			return null;
		// log.debug("BacktestOrder:"+order.toString());
		// log.debug("StrategyStock:"+quote.toString());
		if (order.getOrderType().equals("BTO")
				&& order.getStopLossPrice().compareTo(quote.getStockQuote().getClose()) >= 0) {
			return closeBackTestOrder(quote, order);

		} else if (order.getOrderType().equals("STO")
				&& (order.getStopLossPrice().compareTo(quote.getStockQuote().getClose()) <= 0)) {
			return closeBackTestOrder(quote, order);
		}
		return null;
	}
	
	private BacktestStockOrder closeBackTestOrder(StrategyStockQuote quote, BacktestStockOrder order) {
		order.setCloseDatetime(quote.getStockQuote().getQuoteDatetime());
		order.setClosePrice(quote.getStockQuote().getOpen());
		BigDecimal profitOrLoss = BigDecimal.ZERO;
		if (order.getOrderType().equals("BTO")) {
			profitOrLoss = order.getClosePrice().subtract(order.getOpenPrice());
		} else {
			profitOrLoss = order.getOpenPrice().subtract(order.getClosePrice());
		}
		order.setProfitLoss(profitOrLoss);
		order.setAlerted(0);
		backtestOrderDao.merge(order);
		
		log.debug("Closing backtest order " + order.getOpenDatetime() + " : "+order.getOrderType() + " @ "+order.getCloseDatetime());
		
		createAlert(quote, order);
		return order;
	}
	
	//TODO: Add strong/Weak  buy or Sell
	public int superTrend(StrategyStockQuote currentQuote, StrategyStockQuote lastStockQuote) throws Exception{
    	Stock s = currentQuote.getStockQuote().getStock();
    	double prevHighestHigh=(s.getHighestHigh() == null)?0:s.getHighestHigh().doubleValue();
    	double prevLowestLow=(s.getLowestLow() == null )?0:s.getLowestLow().doubleValue();
    	
    	int trend =0;
    	if(s.getTrend() == 0 ) {
//    		trend=setupInitialSuperTrend(s);
    		trend=setupInitialSimpleTrend(s); // using simple trend
    	}else
    		trend =currentQuote.getStockQuote().getStock().getTrend();
    	
    	// register high only after low and low only after high
    	int signal =checkForSignalForTrend(currentQuote, lastStockQuote);
//    	int trend =currentQuote.getStockQuote().getStock().getTrend();
    	int prevTrend = trend;
    	{
    		if(lastStockQuote == null) {
    			return 0; // if there is no last quote, no trend
    		}
    		double currentHigh = lastStockQuote.getXhigh().doubleValue();
    		double currentLow= lastStockQuote.getXlow().doubleValue();
    		
    		if(signal == -1) { //Sell
    			if(prevHighestHigh != 0) {
    				if(trend >=0 ) {// This ensures that we areonly capturing the highest high when we were in uptrend but a sell signal
    					String quoteStr = "; Current High: " +lastStockQuote.getXhigh().doubleValue() + "; Prev High: " +prevHighestHigh;
    					if(prevHighestHigh > lastStockQuote.getXhigh().doubleValue()) {
    						System.out.println(s.getTicker() + " - Donot SELL- - Up Trend "  + currentQuote.getStockQuote().getQuoteDatetime() + quoteStr);
    					}else {
    						System.out.println(s.getTicker() + " - SELL- Sell - Down Trend "  + currentQuote.getStockQuote().getQuoteDatetime() + quoteStr);
    						trend = -1;
    					}
    					prevHighestHigh = lastStockQuote.getXhigh().doubleValue(); //Capturing previous high
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
    					String quoteStr = "; Current Low: " + lastStockQuote.getXlow().doubleValue() + "; Prev Low: " +prevLowestLow;
    					if(prevLowestLow < lastStockQuote.getXlow().doubleValue()) {
    						System.out.println(s.getTicker() + " - BUY - Up Trend "  + currentQuote.getStockQuote().getQuoteDatetime() + quoteStr);
    						trend = 1;
    					}else {
    						System.out.println(s.getTicker() + " - Donot BUY - Down- Trend "  + currentQuote.getStockQuote().getQuoteDatetime() +quoteStr);
    					}
    					prevLowestLow = lastStockQuote.getXlow().doubleValue(); //Capturing previous high
    				}else {
//        				System.out.println("False Positive " + quote.getStockQuote().getQuoteDatetime());
        			}
    			}else if (prevLowestLow == 0) {
    				prevLowestLow = currentLow;
    			}
    		}
    	}
    	System.out.println("Setting up the final trend " + trend);
    	if(prevTrend!=trend){
    		s.setTrend(trend);
    		s.setHighestHigh(new BigDecimal(prevHighestHigh));
    		s.setLowestLow(new BigDecimal(prevLowestLow));
        	stockDao.merge(s);
        	return trend;
    	}else
    		return 0;
    }

	public int simpleTrend(StrategyStockQuote currentQuote, StrategyStockQuote lastStockQuote) throws Exception {
		LinkedList<BacktestStockOrder> orderList = new LinkedList<>();

		Stock s=currentQuote.getStockQuote().getStock();
		BacktestStockOrder prevOrder = backtestOrderDao.findOpenOrder(s);
		if(prevOrder !=null && prevOrder.getCloseDatetime() != null) {
//			log.debug("Previous Backtest order is " + prevOrder.getBacktestStockOrderId() + " @ " + prevOrder.getOpenDatetime() + " @ "+ prevOrder.getCloseDatetime());
			prevOrder=null; // this is to ensure same DB Txn records are not pulled again.
		}

		int trend = currentQuote.getStockQuote().getStock().getTrend();
		if(trend == 0) {
			trend=setupInitialSimpleTrend(s); // using simple trend
		}
		
		int prevTrend = trend;

		BigDecimal currentStopLoss = BigDecimal.ZERO; // Use this in conjuction with signal
		int signal = checkForSignalForTrend(currentQuote, lastStockQuote);
		if (lastStockQuote == null) {
			return 0; // no trend without previous quote.
		}

		if (trend == 0 && signal != 0) // no trend to start w.
			trend = signal;
		if (signal == -1) { // Sell
			if (prevOrder == null && trend == signal) { /// STO
				currentStopLoss = lastStockQuote.getXhigh();
				prevOrder = createNewBackTestOrder(currentQuote, signal, currentStopLoss);
			} else if (prevOrder != null && prevOrder.getOrderType().equals("BTO")) {// prev order is BTO, close it
				closeBackTestOrder(currentQuote, prevOrder);
				prevOrder = null;
			}
		} else if (signal == 1) { // Buy
			if (prevOrder == null && trend == signal) { // BTO
				currentStopLoss = lastStockQuote.getXlow();
				prevOrder = createNewBackTestOrder(currentQuote, signal, currentStopLoss);
			} else if (prevOrder != null && prevOrder.getOrderType().equals("STO")) { // prev order is STO, close it
				closeBackTestOrder(currentQuote, prevOrder);// Closing to booking the profit
				prevOrder = null;
			}
		} else {// Check for stop losses
			if (prevOrder != null) { // If thre is an Open Order
				BacktestStockOrder stopLossedOrder = checkForStopLoss(currentQuote, prevOrder);
				if (stopLossedOrder != null) {
					orderList.add(stopLossedOrder);
					trend = (trend == 1) ? -1 : 1; // Change the Trend
					log.debug(
							currentQuote.getStockQuote().getStock().getTicker() + " : " + stopLossedOrder.getOrderType()
									+ " Hit the stoploss @ " + currentQuote.getStockQuote().getQuoteDatetime());
					prevOrder = null;
				}
			}
		}		
		if(prevTrend != trend) {
			s.setTrend(trend);
			stockDao.merge(s);
			return trend; // return trend only when there is a change in trend so that an alert can be sent out.
		}
		return 0;
	}

	private int checkForDailySignal(StrategyStockQuote currentQuote, StrategyStockQuote prevQuote) {
		int buyOrSell=0;//-1-sell;+1-Buy
		if(prevQuote == null || prevQuote.getStochRsiD() == null || 
				currentQuote== null || currentQuote.getStochRsiK() == null)
			return 0;
		//Check for Buy Signal
		if(currentQuote.getStochRsiD().doubleValue() <=40 && currentQuote.getStochRsiK().doubleValue()>= currentQuote.getStochRsiD().doubleValue()) {
			if(currentQuote.getXopen().compareTo(currentQuote.getXclose()) ==- 1	) {//xopen >xclose
				//System.out.println("Buy Signal " +currentQuote.getStockQuote().getStock().getTicker());
				buyOrSell=1;
			}
		}
		
		//Check for Sell Signal
		if(prevQuote.getStochRsiD().doubleValue() >=60 && 
				currentQuote.getStochRsiK().doubleValue()<= currentQuote.getStochRsiD().doubleValue()) {
			if(currentQuote.getXopen().compareTo(currentQuote.getXclose()) == 1	) {//xopen >xclose
				//System.out.println("Sell Signal " +currentQuote.getStockQuote().getStock().getTicker());
				buyOrSell=-1;
			}
		}
		return buyOrSell;
	}
	
	private int checkForDaySignal(Stock stock) {
		List<MonitoredStock> mStockList = mStockDao.retreiveByStockNum(stock.getStocknum());
		if(mStockList !=null && mStockList.size() >0) {
			MonitoredStock mStock =mStockList.get(0);
			return mStock.getTrennd();
		}
		return 0;
	}
	
	//TODO: Send one Email per run instead of multple emails
	//TODO: Change the text email to Thyme based email (http://www.thymeleaf.org/doc/articles/springmail.html)
	private void createAlert(StrategyStockQuote currentQuote, int buyOrSell) {
		if (buyOrSell == 0)
			return;
		int interval=currentQuote.getStockQuote().getInterval();
		int dayTrend =0;
		if(interval <480 && 
				stockAlertDao.getActiveStockAlertByStocknum(currentQuote.getStockQuote().getStock().getStocknum()).size() == 0) {
			dayTrend=checkForDaySignal(currentQuote.getStockQuote().getStock());
			if(dayTrend != buyOrSell && dayTrend !=0) //TODO: Check to ensure that dayTrend set to either buy or sell, if nothing, send it anyways
				return; //donot send email.
		}
//		//System.out.println("Buy Signal " + currentQuote.getStockQuote().getStock().getTicker());
//		String text = "Identified on " + currentQuote.getStockQuote().getQuoteDatetime() + "\t" + "Stoch RSI: "
//				+ currentQuote.getStochRsiD() + "\t";
//		String subject = ((buyOrSell == 1)?"BUY ":"SELL ") + currentQuote.getStockQuote().getStock().getTicker() + " @ "
//				+ currentQuote.getStockQuote().getClose().doubleValue();
//		//if(currentQuote.getSendEmail())
//			emailService.sendMail(subject, text, "mohaneee221@gmail.com");
		StockAlert sq = new StockAlert();
		sq.setBuySellSignal(buyOrSell);
		sq.setCustomer(c);
		sq.setStock(currentQuote.getStockQuote().getStock());
		sq.setStrategyStockQuote(currentQuote);
		sq.setStockPrice(currentQuote.getStockQuote().getClose());
		sq.setMonitored(0);
		stockAlertDao.save(sq);
	}
	
	private void createAlert(StrategyStockQuote currentQuote, BacktestStockOrder order) {
		StockAlert sq = new StockAlert();
		int buyOrSell =0;
		if(order.getCloseDatetime()!=null) // Is this from Closing Backtest Order?
			buyOrSell=order.getOrderType().equals("BTO")?-1:1; // Closing Alert
		else
			buyOrSell=order.getOrderType().equals("BTO")?1:-1; // Opening Alert
		
		sq.setBuySellSignal(buyOrSell);//1= Buy; -1= SELL
		sq.setCustomer(c);
		sq.setStock(currentQuote.getStockQuote().getStock());
		sq.setStrategyStockQuote(currentQuote);
		sq.setStockQuote(currentQuote.getStockQuote());
		sq.setStockPrice(currentQuote.getStockQuote().getClose());
		sq.setMonitored(0);
		stockAlertDao.save(sq);
	}
	
	public void calculateInitialStochRsi(TreeSet<StrategyStockQuote> quotesList) {		
		StrategyStockQuote[] quotesArray=(StrategyStockQuote[]) quotesList.toArray(new StrategyStockQuote[quotesList.size()]);
		BigDecimal gains=new BigDecimal(0);
		gains.setScale(6);
		BigDecimal losses=new BigDecimal(0);
		losses.setScale(6);
		BigDecimal avgUp = new BigDecimal(0);
		BigDecimal avgDown = new BigDecimal(0);
		
		for (int bar = 0; bar <14; bar++) {
            BigDecimal change = quotesArray[bar+1].getXclose().subtract(quotesArray[bar].getXclose());
            gains=gains.add(ZERO.max(change));
            losses = losses.add(ZERO.max(change.negate()));
//            //System.out.println("Initial load Gains:"+gains + " losses:"+losses+" Change:"+change);
        }
		avgUp = gains.divide(periodLength, mc);
        avgDown = losses.divide(periodLength, mc);
        //quotesList.get(quotesList.size()-1).setAvgGain(avgUp);
        quotesList.last().setAvgGain(avgUp);
        quotesList.last().setAvgLoss(avgDown);
        //stockQuote.setRsi(new BigDecimal(100 - (100 / (1 + (avgUp / avgDown)))));
        quotesList.last().setRsi(HUNDRED.subtract(HUNDRED.divide(ONE.add(avgUp.divide(avgDown, mc)), mc)));
        //System.out.println("AvgUp:"+ avgUp +" AvgDown:"+avgDown+" Rsi:"+quotesList.last().getRsi());
	}
	
	private BigDecimal getMinRSIn(LinkedList<BigDecimal> minRSIn, BigDecimal currentRsi) {
		BigDecimal minRsin=currentRsi;
		
		for(BigDecimal d: minRSIn) {
			minRsin=minRsin.min(d);
		}
		//System.out.println("MinRSI is " + minRsin);
		return minRsin;
	}
	
	private BigDecimal getMaxRSIn(LinkedList<BigDecimal> maxRSIn, BigDecimal currentRsi) {
		BigDecimal maxRsin=currentRsi;
		for(BigDecimal d: maxRSIn) {
			maxRsin=maxRsin.max(d);
		}
		//System.out.println("MaxRSI is " + maxRsin);
		return maxRsin;
	}
	private void addRsiToList(LinkedList<BigDecimal> lastNRsi, BigDecimal rsi) {
		if(lastNRsi.size() >=stochPeriodLength) {
			lastNRsi.removeFirst();
		}
		lastNRsi.addLast(rsi);
	}
	
	/*
	 * Read more: StochRSI https://www.investopedia.com/terms/s/stochrsi.asp#ixzz55BlKNVzg 
	 */
	private void calculateStochRsi(StrategyStockQuote quote, LinkedList<StrategyStockQuote> quotes) {
			//BigDecimal minRsi,BigDecimal maxRsi, BigDecimal currentRsi) {
		//StochRSI = (RSI - Lowest Low RSI) / (Highest High RSI - Lowest Low RSI)
		BigDecimal maxRsi= quote.getHighrsi();
		BigDecimal minRsi= quote.getLowrsi();
		BigDecimal currentRsi= quote.getRsi();
		BigDecimal stochRsiRawK =(!maxRsi.equals(minRsi))? ((currentRsi.subtract(minRsi))
								.divide(maxRsi.subtract(minRsi), mc)).multiply(HUNDRED, mc):new BigDecimal(0);
		//System.out.println("MaxRsi-" +maxRsi +"MinRsi-"+minRsi +"currentRsi-"+currentRsi +"stochRsiRawK-"+stochRsiRawK);
		quote.setStochRsi(stochRsiRawK);
	}	
	
}
