package com.mpakam.service.strategy;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDateTime;
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
import com.mpakam.dao.TechAnalysisHeikenAshiDao;
import com.mpakam.model.BacktestStockOrder;
import com.mpakam.model.MonitoredStock;
import com.mpakam.model.Stock;
import com.mpakam.model.StockAlert;
import com.mpakam.model.StockQuote;
import com.mpakam.model.Strategy;
import com.mpakam.model.StrategyStockQuote;
import com.mpakam.model.TechAnalysisHeikenashi;
import com.mpakam.service.EmailService;


@Service
public class HeikenAshiService{

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	final BigDecimal periodLength = new BigDecimal(14);
	final int stochPeriodLength = 14;
	final int K_LENGTH=3;
	final int D_LENGTH=3;
	final BigDecimal ZERO= new BigDecimal(0).setScale(4);
	final BigDecimal HUNDRED = new BigDecimal(100).setScale(4);
	final BigDecimal ONE = new BigDecimal(1).setScale(4);
	final MathContext mc = new MathContext(4, RoundingMode.HALF_UP);
	final Strategy s= new Strategy();
	
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
	
	@Autowired
	TechAnalysisHeikenAshiDao haDao;
	

	@Transactional
	public int generateHA(Set<StockQuote> quotes) {
		int trend =0;
		int quoteCounter=0; // for initial loads
		
		Stock stock=((TreeSet<StockQuote>)quotes).first().getStock();
		LinkedList<TechAnalysisHeikenashi> lastStrategyQuotes=haDao.retrieveLastXQuotesByStockNum(stock);
		
		TechAnalysisHeikenashi lastStrategyQuote = 
				(lastStrategyQuotes == null || lastStrategyQuotes.size()==0)?null: lastStrategyQuotes.get(0);
		boolean lastItemExists = lastStrategyQuote!=null;

		for (StockQuote stockQuote : quotes) {
			BigDecimal xOpenPrev = new BigDecimal(0);
			BigDecimal xClosePrev = new BigDecimal(0);
			
			TechAnalysisHeikenashi haObj = new TechAnalysisHeikenashi();
			haObj.setStockQuote(stockQuote);
			
			if (lastItemExists || lastStrategyQuote != null) {
				xOpenPrev = lastStrategyQuote.getXopen();
				xClosePrev = lastStrategyQuote.getXclose();
				quoteCounter=15; // for continued loads, we just need to start caculating the RSI instead of initial load code
			} else {
				xClosePrev = stockQuote.getClose();
				xOpenPrev = stockQuote.getOpen();
			}
			
			BigDecimal xClose = (stockQuote.getOpen().add(stockQuote.getHigh()).add(stockQuote.getLow()).add(stockQuote.getClose())).divide(new BigDecimal(4));
			BigDecimal xOpen = (xOpenPrev.add(xClosePrev)).divide(new BigDecimal(2));
			BigDecimal xHigh = stockQuote.getHigh().max(xClosePrev.max(xOpenPrev));
			BigDecimal xLow = stockQuote.getLow().min(xClosePrev.min(xOpenPrev));
			
			haObj.setXclose(xClose);
			haObj.setXopen(xOpen);
			haObj.setXhigh(xHigh);
			haObj.setXlow(xLow);
			
			if(quoteCounter<14) {
				quoteCounter++;
			}else if(quoteCounter == 14) {
				quoteCounter++;
			}else {
				BigDecimal gains=new BigDecimal(0);
				BigDecimal losses=new BigDecimal(0);

		        BigDecimal change = haObj.getXclose().subtract(lastStrategyQuote.getXclose(),mc); 
		        
		        gains=gains.add(ZERO.max(change));
	            losses = losses.add(ZERO.max(change.negate()));

			}
			log.debug("Saving the Heiken Ashi data ");
			haObj.setHaId((int)haDao.save(haObj));
			lastStrategyQuote = haObj;
		} 
		return trend;
	}
}
