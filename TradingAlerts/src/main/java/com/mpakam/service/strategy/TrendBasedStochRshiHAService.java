package com.mpakam.service.strategy;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mpakam.dao.MonitoredStockDao;
import com.mpakam.dao.StockAlertDao;
import com.mpakam.dao.StrategyStockQuoteDao;
import com.mpakam.model.Customer;
import com.mpakam.model.MonitoredStock;
import com.mpakam.model.Stock;
import com.mpakam.model.StockAlert;
import com.mpakam.model.StockQuote;
import com.mpakam.model.Strategy;
import com.mpakam.model.StrategyStockQuote;
import com.mpakam.service.EmailService;

@Service
public class TrendBasedStochRshiHAService {

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
	final Customer c = new Customer();
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
				
//				sendAlert(stockQuote);//TODO: uncomment
				//System.out.println("AvgUp:"+ avgUp +" AvgDown:"+avgDown+" RsiK:"+strategyStockQuote.getStochRsiK() +" RsiD:"+strategyStockQuote.getStochRsiD() );
			}
			trend=checkForSignal(strategyStockQuote, lastStrategyQuote);
			
			lastStrategyQuote = strategyStockQuote;
			strategyStockQuote.setStrategyStockQuoteId((int)strategyStockQuoteDao.save(strategyStockQuote));
			
		}
		sendEmail(lastStrategyQuote, trend);
		return trend;
	}
	
	/*
	 * This determines the lowest low and highest high and sets a trend
	 */
	private int determineTrend(StrategyStockQuote strategyStockQuote, StrategyStockQuote lastStrategyQuote, int buyOrSell) {
		//TODO:check if the lastStrategyQuote is null
		
		//TODO:set the highest high and lowest low. Based on buyOrSell, determine whether we need to update the low or heigh value.
	
		return 0;
	}

	private void addStochRsiToList(StrategyStockQuote currentStrategyQuote, LinkedList<StrategyStockQuote> lastStrategyQuotes, LinkedList<StrategyStockQuote> lastStochRsi) {
		
		if(lastStrategyQuotes != null && lastStrategyQuotes.size() > 0 && lastStochRsi.size()<2) {
				lastStochRsi.addLast(lastStrategyQuotes.get(1));
				lastStochRsi.addLast(lastStrategyQuotes.get(0));
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
	
	private int checkForSignal(StrategyStockQuote currentQuote, StrategyStockQuote prevQuote) {
		
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
		if(prevQuote.getStochRsiD().doubleValue() >=75 && 
				currentQuote.getStochRsiK().doubleValue()<= currentQuote.getStochRsiD().doubleValue()) {
			if(currentQuote.getXopen().compareTo(currentQuote.getXclose()) == 1	) {//xopen >xclose
				//System.out.println("Sell Signal " +currentQuote.getStockQuote().getStock().getTicker());
				buyOrSell=-1;
			}
		}
		
		int trend =determineTrend(currentQuote,prevQuote, buyOrSell);
		//TODO: use the trend and buyOrSell indicator to determine whether to send email or not.
		
		return buyOrSell;
		/*if (buyOrSell != 0) {
			sendEmail(currentQuote, buyOrSell);
		}*/
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
	
	private void sendEmail(StrategyStockQuote currentQuote, int buyOrSell) {
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
		//System.out.println("Buy Signal " + currentQuote.getStockQuote().getStock().getTicker());
		String text = "Identified on " + currentQuote.getStockQuote().getQuoteDatetime() + "\t" + "Stoch RSI: "
				+ currentQuote.getStochRsiD() + "\t";
		String subject = ((buyOrSell == 1)?"BUY ":"SELL ") + currentQuote.getStockQuote().getStock().getTicker() + " @ "
				+ currentQuote.getStockQuote().getClose().doubleValue();
		//if(currentQuote.getSendEmail())
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
