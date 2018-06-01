package com.mpakam.service.strategy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.TreeSet;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mpakam.app.config.EnvironmentConfig;
import com.mpakam.dao.BacktestStockOrderDao;
import com.mpakam.dao.RenkoChartDao;
import com.mpakam.dao.StockDao;
import com.mpakam.dao.StockQuoteDao;
import com.mpakam.dao.TechAnalysisAtrDao;
import com.mpakam.model.BacktestStockOrder;
import com.mpakam.model.RenkoChartBox;
import com.mpakam.model.Stock;
import com.mpakam.model.StockQuote;
import com.mpakam.model.TechAnalysisAtr;
import com.mpakam.service.StockAlertService;
import com.mpakam.service.TechAnalysisAtrService;

/*
 * Strategy - Buy Call - Above EMA and Green close -Buy & first Red is close
 * Buy Put - Below EMA and Red Close - Buy & first Green is close
 *  
 */
@Service
public class RenkoChartStrategyService {

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	public static int STRATEGYID=5;
	
	@Autowired
	RenkoChartDao renkoDao;
	
	@Autowired
	EnvironmentConfig eConfig;
	
	@Autowired
	BacktestStockOrderDao backtestOrderDao;
	
	@Autowired
	StockDao stockDao;
	
	@Autowired
	TechAnalysisAtrDao atrDao;
	
	@Autowired
	StockQuoteDao stockQuoteDao;
	
	@Autowired
	TechAnalysisAtrService atrSvc;
	
	@Autowired
	StockAlertService saSvc;
	
	@Transactional
	public void executeStrategy(TreeSet<StockQuote> quotes) throws Exception {
		for(StockQuote quote:quotes){
			// Find the last Renko Chart and also get the box size, if a new box needs to be added, check for EMA.
			RenkoChartBox lastBox = renkoDao.findLastByStocknum(quote.getStock());
			
			TechAnalysisAtr atr=atrSvc.calculateATR(quote);// Calculate the ATR			
			
			LinkedList<RenkoChartBox> newRcList = null;
			if(lastBox == null) {// Create a new renko box
				newRcList=addNewBox(quote,null, atr.getAverageTrueRange());
			}else {
				//Check if renko box needs to be added with last rc close price and current stock quote close price
				RenkoChartBox lastRc = lastBox;
				BigDecimal lastEma = lastRc.getEma14Price();
				
				log.debug("Last RC close price is " + lastRc.getClosePrice() +"; EMA price is " + lastEma);
				BigDecimal boxSize = atr.getAverageTrueRange();
				if(quote.getClose().compareTo(lastRc.getClosePrice().add(boxSize))>0 ) {// Current close price is greater than prev rc close price + box size, create a new one
					newRcList=addNewBox(quote,lastRc, boxSize);
				}else if(quote.getClose().compareTo(lastRc.getClosePrice().subtract(boxSize))<0) { // Current close price is less than prev rc close - box size, create a new one
					newRcList=addNewBox(quote,lastRc, boxSize);
				}
			}	
		}
	}
	
	/**
	 * After adding the box, also check the EMA.
	 * @param sq
	 * @return
	 * @throws Exception 
	 */
	private LinkedList<RenkoChartBox> addNewBox(StockQuote sq, RenkoChartBox lastRc, BigDecimal boxSize) throws Exception {
		LinkedList<RenkoChartBox> rcList = new LinkedList<RenkoChartBox>();
//		BigDecimal boxSize = sq.getStock().getRenkoBoxSize();
//		BigDecimal boxSize = atr.getAverageTrueRange();
		RenkoChartBox currentBox = null;

		if (lastRc == null) {
			RenkoChartBox rc = new RenkoChartBox();
			rc.setClosePrice(sq.getClose());
			rc.setOpenPrice(sq.getOpen());
			rc.setEma14Price(sq.getClose());
			rc.setStockQuote(sq);
			rc.setStock(sq.getStock());
			rc.setEntryDatetime(LocalDateTime.now());
			rc.setRenkoBoxId((int) renkoDao.save(rc));
			rcList.add(rc);
			currentBox = rc;
		}else { // We have previous box, add one box for every last Box closePrice +box size
			while(true) {
				RenkoChartBox rc = new RenkoChartBox();
				if(sq.getClose().compareTo(lastRc.getClosePrice().add(boxSize))>0) // green
					rc.setClosePrice(lastRc.getClosePrice().add(boxSize));
				else
					rc.setClosePrice(lastRc.getClosePrice().subtract(boxSize)); // red
				rc.setOpenPrice(lastRc.getClosePrice());
				rc.setEma14Price(calculateEma(sq.getClose(), lastRc)); // Calculate eMa 14.
				rc.setStockQuote(sq);
				rc.setStock(sq.getStock());
				rc.setEntryDatetime(LocalDateTime.now());
				rc.setRenkoBoxId((int) renkoDao.save(rc));
				rcList.add(rc);
				currentBox= rc;
				System.out.println("Creating the renko chart ");

				if(sq.getClose().compareTo(rc.getClosePrice().add(boxSize))<0 || sq.getClose().compareTo(lastRc.getClosePrice().subtract(boxSize))>0)
					break;

				lastRc=rc;
			}
		}
		checkForTrend2(currentBox, lastRc);
		
		return rcList;
	}
	
	private BigDecimal calculateEma(BigDecimal currentClosePrice, RenkoChartBox lastRc) {
		double emaLength =  eConfig.getEmaLength();
		double k = (double)2/(emaLength+1.0);
		//EMA = Price(t) * k + EMA(y) * (1 - k)
		double lastEma = lastRc.getEma14Price().doubleValue();
		double currentClosePriceD = currentClosePrice.doubleValue();
		return new BigDecimal(currentClosePriceD*k+lastEma*(1-k));
	}
	
	public int checkForTrend(RenkoChartBox currentBox, RenkoChartBox previousBox) throws Exception {
		if(previousBox == null)
			return 0; // no previous box, no trend.
		int trend =0;
		Stock s=currentBox.getStockQuote().getStock();
		BacktestStockOrder prevOrder = backtestOrderDao.findOpenOrder(s, STRATEGYID);
		
		if(prevOrder !=null && prevOrder.getCloseDatetime() != null) {
			prevOrder=null; // this is to ensure same DB Txn records are not pulled again.
		}
		
		//Check for UpTrend
		/*
		 * Green currentBox.close is greater than EMA price
		 */
//		boolean previousGreenBox =previousBox.getOpenPrice().compareTo(previousBox.getClosePrice())<0;
		boolean greenBox = currentBox.getOpenPrice().compareTo(currentBox.getClosePrice())<0;
		
		log.debug( currentBox.getStockQuote().getQuoteDatetime() + " - GreenBox:"+greenBox + "; Previous Green Box?:" + currentBox.getEma14Price() + "; close price: " + currentBox.getClosePrice());
		
		if(greenBox) {
			if(prevOrder!=null && prevOrder.getOrderType().equals("STO")) { // Close the previous Sell to Open Order irrespective of EMA.
				//Close the Back Test Order
				log.debug("Closing the STO order");
				closeBackTestOrder(currentBox,prevOrder); // closing the existing order
				prevOrder=null;
			}
			
			if( currentBox.getClosePrice().compareTo(currentBox.getEma14Price())>0) { // Green Box & EMA 14 below green box. BUY //previousGreenBox &&
				if(prevOrder == null) { // there is no existing order.
					log.debug("Opening BTO order");
					createNewBackTestOrder(currentBox, 1 , previousBox.getClosePrice());
					trend =1;
				}
			}
		}
		
		//Check for DownTrend
		/*
		 * RED currentBox.close is less than EMA price
		 */
		if(!greenBox) { // Green Box
			if(prevOrder!=null && prevOrder.getOrderType().equals("BTO")) { // Close the previous Sell to Open Order irrespective of EMA.
				//Close the Back Test Order
				log.debug("Closing the BTO order");
				closeBackTestOrder(currentBox,prevOrder); // closing the existing order
				prevOrder=null;
			}
			
			if(currentBox.getClosePrice().compareTo(currentBox.getEma14Price())<0) { // Red Box & EMA 14 below green box. BUY //!previousGreenBox && 
				if(prevOrder == null) { // there is no existing order.
					log.debug("Opening STO order");
					createNewBackTestOrder(currentBox, -1 , previousBox.getClosePrice());
					trend =-1;
				}
			}
		}
		return trend;
	}
	
	
	/**
	 * sends out an alert for only changes colors
	 * @param currentBox
	 * @param previousBox
	 * @return
	 * @throws Exception
	 */
	public int checkForTrend2(RenkoChartBox currentBox, RenkoChartBox previousBox) throws Exception {
		if(previousBox == null)
			return 0; // no previous box, no trend.
		int trend =0;
		Stock s=currentBox.getStockQuote().getStock();
		BacktestStockOrder prevOrder = backtestOrderDao.findOpenOrder(s, STRATEGYID);
		
		if(prevOrder !=null && prevOrder.getCloseDatetime() != null) {
			prevOrder=null; // this is to ensure same DB Txn records are not pulled again.
		}
		
		//Check for UpTrend
		/*
		 * Green currentBox.close is greater than EMA price
		 */
		boolean previousGreenBox =previousBox.getOpenPrice().compareTo(previousBox.getClosePrice())<0;
		boolean greenBox = currentBox.getOpenPrice().compareTo(currentBox.getClosePrice())<0;
		
		log.debug( currentBox.getStockQuote().getQuoteDatetime() + " - GreenBox:"+greenBox + "; Previous Green Box?:" + currentBox.getEma14Price() + "; close price: " + currentBox.getClosePrice());
		
		if(greenBox != previousGreenBox) {
			saSvc.createAlert(currentBox.getStockQuote(), greenBox?1:-1);
		}
		return trend;
	}
	
	private BacktestStockOrder createNewBackTestOrder(RenkoChartBox box, int signal, BigDecimal stopLossPrice) {
		
		BacktestStockOrder order = new BacktestStockOrder();
		order.setEntryDatetime(LocalDateTime.now());
		order.setOpenPrice(box.getClosePrice());
		order.setOpenDatetime(box.getStockQuote().getQuoteDatetime());
		order.setStock(box.getStockQuote().getStock());
		order.setStrategyId(STRATEGYID);
		order.setStopLossPrice(stopLossPrice);
		order.setOrderType((signal==1)?"BTO":"STO"); //BTO - Buy to Open; STO - Sell to Open
		order.setBacktestStockOrderId((int)backtestOrderDao.save(order));
		log.debug("New backtest order " + order.getOpenDatetime() + " : "+order.getOrderType());
		return order;
	}
	
	private BacktestStockOrder closeBackTestOrder(RenkoChartBox currentBox, BacktestStockOrder order) {
		order.setCloseDatetime(currentBox.getStockQuote().getQuoteDatetime());
		order.setClosePrice(currentBox.getClosePrice());
		BigDecimal profitOrLoss = BigDecimal.ZERO;
		if (order.getOrderType().equals("BTO")) {
			profitOrLoss = order.getClosePrice().subtract(order.getOpenPrice());
		} else {
			profitOrLoss = order.getOpenPrice().subtract(order.getClosePrice());
		}
		order.setProfitLoss(profitOrLoss);
		backtestOrderDao.merge(order);
		
		log.debug("Closing backtest order " + order.getOpenDatetime() + " : "+order.getOrderType() + " @ "+order.getCloseDatetime());

		return order;
	}
	
}
