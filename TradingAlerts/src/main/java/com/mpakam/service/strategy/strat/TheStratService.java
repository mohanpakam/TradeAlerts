package com.mpakam.service.strategy.strat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mpakam.constants.BackTestOrder;
import com.mpakam.constants.CandleColor;
import com.mpakam.constants.StratDirection;
import com.mpakam.constants.StratCandleIdentifier;
import com.mpakam.dao.BacktestStockOrderDao;
import com.mpakam.exception.BackTestOrderCreatedException;
import com.mpakam.model.BacktestStockOrder;
import com.mpakam.model.StockQuote;
import com.mpakam.model.TechAnalysisStrat;

@Service
public class TheStratService {
	
	private static List<BacktestStockOrder> orders = new ArrayList<BacktestStockOrder>();
	
	@Autowired
	private BacktestStockOrderDao orderDao;

	/*
	 * a. 122 RevStrat2
	// b. 123 RevStrat3
	// c. 1-2
	// d. 1-3
	// e. 2-2
	// f. 2-3
	// g. 3-2
	 */
		
	public BackTestOrder backTestStrat(TechAnalysisStrat todayStrat,LinkedList<TechAnalysisStrat> stratList,
			StockQuote weeklySq, StockQuote monthlySq,Set<StockQuote> dailySQs) {
		TechAnalysisStrat yesterdayStrat = yesterdayStrat(todayStrat,stratList);
		TechAnalysisStrat dayBeforeYesterdayStrat = dayBeforeYesterdayStrat(todayStrat,stratList);
		
		if(todayStrat.getCandleId() == StratCandleIdentifier.TWO.getStratId()) {
			//Calls strategies for 2
			try {
				//check 122
				checkFor122(todayStrat,yesterdayStrat,dayBeforeYesterdayStrat,weeklySq,monthlySq);
			}catch(BackTestOrderCreatedException e) {
				
			}
			
		}else if(todayStrat.getCandleId() == StratCandleIdentifier.THREE.getStratId()) {
			//Calls strategies for 3
		}
		
		//Close
		orders.forEach(o->{
			if(o.getClosePrice() == null)
				closeBacktestOrder(o,todayStrat.getStockQuote());
		});
		
		return BackTestOrder.NONE;
	}
	
	private void closeBacktestOrder(BacktestStockOrder bso, StockQuote sq) {
		if(bso.getOrderType().equals("BTO")) {
			if(sq.getLow().floatValue() <= bso.getStopLossPrice().floatValue()) {
				float stopLossF = bso.getStopLossPrice().floatValue();
				float openF = sq.getOpen().floatValue();
				BigDecimal stopLoss = bso.getStopLossPrice();
				if(stopLossF >= openF) {
					stopLoss = sq.getOpen();
				}
				closeBackTestOrder(bso,sq.getQuoteDatetime(),stopLoss);
			}
		}else {
			if(sq.getHigh().floatValue() >= bso.getStopLossPrice().floatValue()) {
				float stopLossF = bso.getStopLossPrice().floatValue();
				float openF = sq.getOpen().floatValue();
				BigDecimal stopLoss = bso.getStopLossPrice();
				if(stopLossF <= openF) {
					stopLoss = sq.getOpen();
				}
				closeBackTestOrder(bso,sq.getQuoteDatetime(),stopLoss);
			}
		}
	}
	
	private BacktestStockOrder closeBackTestOrder(BacktestStockOrder order, LocalDateTime quoteDatetime,BigDecimal stopLoss) {
    	order.setCloseDatetime(quoteDatetime);
    	order.setClosePrice(stopLoss);
    	BigDecimal profitOrLoss  =BigDecimal.ZERO;
    	if(order.getOrderType().equals("BTO")) {
    		profitOrLoss = order.getClosePrice().subtract(order.getOpenPrice());
    	}else {
    		profitOrLoss = order.getOpenPrice().subtract(order.getClosePrice());
    	}
    	order.setProfitLoss(profitOrLoss);
    	orderDao.saveOrUpdate(order);
    	return order;
    }
	
	private void checkFor122(TechAnalysisStrat todayStrat, TechAnalysisStrat yesterdayStrat,
			TechAnalysisStrat dayBeforeYesterdayStrat, StockQuote weeklySq, StockQuote monthlySq) {
		if(yesterdayStrat.getCandleId() !=  StratCandleIdentifier.TWO.getStratId())
			return; //its not 22
		if(dayBeforeYesterdayStrat.getCandleId() !=  StratCandleIdentifier.ONE.getStratId())
			return; //its not 122
		// Check for Sell - Is this a reversal into FTFC
		// 1. yesterday's low is the trigger.
		// 2. Is yesterday's low is lower than weekly's open to cause it to go red?
		// 3. Sell with a stoploss at yesterday's high
		float wOpen= weeklySq.getOpen().floatValue();		
		float yLow = yesterdayStrat.getStockQuote().getLow().floatValue();
		float tLow =  todayStrat.getStockQuote().getLow().floatValue();
		
		if(yLow<=wOpen &&// Yesterdays low is less than or equal to Weekly Open
				yesterdayStrat.getDirectionId() == StratDirection.UP.getDirectionId() &&//Yesterday's 2 was Up.
				todayStrat.getDirectionId() == StratDirection.DOWN.getDirectionId() &&//Today's 2 is down
				tLow <= yLow // Broke yesterday's low
				) {
			BacktestStockOrder bso = createNewBackTestOrder(todayStrat.getStockQuote(),BackTestOrder.SELL, yesterdayStrat.getStockQuote().getHigh());
			throw new BackTestOrderCreatedException(bso);
		}
		
		// Check for Buy - Is this a reversal into FTFC
		// 1. Yesterdays's high is the trigger
		// 2. Yesterday is 2 to downside and today is 2 to the upside reversal. 
		// 3. Is yesterday's high is higher than weekly's open to cause it to go green?
		// 4. Buy with a stoploss at yesterday's low
		
		float yHigh= yesterdayStrat.getStockQuote().getLow().floatValue();
		float tHigh=  todayStrat.getStockQuote().getHigh().floatValue();

		if(yHigh >= wOpen &&
				yesterdayStrat.getDirectionId() == StratDirection.DOWN.getDirectionId() && //Yesterday's 2 was Up.
				todayStrat.getDirectionId() == StratDirection.UP.getDirectionId() &&//Today's 2 is down
				tHigh>=yHigh // Broke Yesterday's High
				) {
			BacktestStockOrder bso = createNewBackTestOrder(todayStrat.getStockQuote(),BackTestOrder.BUY, yesterdayStrat.getStockQuote().getLow());
			throw new BackTestOrderCreatedException(bso);
		}		
	}

	private TechAnalysisStrat yesterdayStrat(TechAnalysisStrat strat,LinkedList<TechAnalysisStrat> stratList){
		int idx = stratList.indexOf(strat);
		return stratList.get(idx > 1?idx-1:idx);
	}
	
	private TechAnalysisStrat dayBeforeYesterdayStrat(TechAnalysisStrat strat,LinkedList<TechAnalysisStrat> stratList){
		int idx = stratList.indexOf(strat);
		return stratList.get(idx > 2?idx-2:idx);
	}
	
	public CandleColor getCandleColor(StockQuote currentQuote) {
		return currentQuote.getOpen().compareTo(currentQuote.getClose()) != -1 ? CandleColor.RED: CandleColor.GREEN;
	}
	
	private BacktestStockOrder createNewBackTestOrder(StockQuote quote, BackTestOrder signal, BigDecimal stopLossPrice) {
		BacktestStockOrder order = new BacktestStockOrder();
		order.setEntryDatetime(LocalDateTime.now());
		order.setOpenPrice(quote.getClose());
		order.setOpenDatetime(quote.getQuoteDatetime());
		order.setStock(quote.getStock());
		order.setStrategyId(1); // HeikenAshi
		order.setStopLossPrice(stopLossPrice);
		order.setOrderType((signal==BackTestOrder.BUY)?"BTO":"STO"); //BTO - Buy to Open; STO - Sell to Open
		orders.add(order);
		orderDao.save(order);
		return order;
}
}
