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
import com.mpakam.constants.TheStrat;
import com.mpakam.constants.StratCandleIdentifier;
import com.mpakam.dao.BacktestStockOrderDao;
import com.mpakam.exception.BackTestOrderCreatedException;
import com.mpakam.model.BacktestStockOrder;
import com.mpakam.model.StockQuote;
import com.mpakam.model.TechAnalysisStrat;
import com.mpakam.service.HigherTimeFrameStockQuoteService;
import com.mpakam.service.TechAnalysisTheStratService;

@Service
public class TheStratService {
	
	private static List<BacktestStockOrder> orders = new ArrayList<BacktestStockOrder>();
	
	@Autowired
	private BacktestStockOrderDao orderDao;

	@Autowired
	private HigherTimeFrameStockQuoteService hfSvc;
	
	@Autowired
	private TechAnalysisTheStratService strat;
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
		boolean backTestOrder = false;
		
		if(todayStrat.getCandleId() == StratCandleIdentifier.TWO.getStratId()) {
			//Calls strategies for 2
			try {
				if(yesterdayStrat.getCandleId() ==  StratCandleIdentifier.TWO.getStratId()) {
					twoCandleStrat(TheStrat._22,todayStrat,yesterdayStrat,weeklySq,monthlySq, dailySQs);
					if(dayBeforeYesterdayStrat.getCandleId() ==  StratCandleIdentifier.ONE.getStratId())
						//check 122
						twoCandleStrat(TheStrat._122,todayStrat,yesterdayStrat,weeklySq,monthlySq, dailySQs);
				}else if(yesterdayStrat.getCandleId() ==  StratCandleIdentifier.ONE.getStratId()) {
					twoCandleStrat(TheStrat._12,todayStrat,yesterdayStrat,weeklySq,monthlySq, dailySQs);
				}else if(yesterdayStrat.getCandleId() ==  StratCandleIdentifier.THREE.getStratId()) {
					twoCandleStrat(TheStrat._32,todayStrat,yesterdayStrat,weeklySq,monthlySq, dailySQs);
				}				
			}catch(BackTestOrderCreatedException e) {
				backTestOrder= true;
			}
		}else if(todayStrat.getCandleId() == StratCandleIdentifier.THREE.getStratId()) {
			//Calls strategies for 3
			/*
			 * _23(23),
				_13(13),
			 */
			try { 
				//123
				if(yesterdayStrat.getCandleId() ==  StratCandleIdentifier.TWO.getStratId()) {
					twoCandleStrat(TheStrat._23,todayStrat,yesterdayStrat,weeklySq,monthlySq, dailySQs);
					if(dayBeforeYesterdayStrat.getCandleId() ==  StratCandleIdentifier.ONE.getStratId())
						twoCandleStrat(TheStrat._123,todayStrat,yesterdayStrat,weeklySq,monthlySq, dailySQs);
				}else if(yesterdayStrat.getCandleId() ==  StratCandleIdentifier.ONE.getStratId()) {
					twoCandleStrat(TheStrat._13,todayStrat,yesterdayStrat,weeklySq,monthlySq, dailySQs);
				}
			}catch(BackTestOrderCreatedException e) {
				backTestOrder= true;
			}
		}
		
		if (!backTestOrder) {
			// Close
			orders.forEach(o -> {
				if (o.getClosePrice() == null)
					closeBacktestOrder(o, todayStrat.getStockQuote(), weeklySq, monthlySq, dailySQs);
			});
		}
		
		return BackTestOrder.NONE;
	}
	
	private void closeBacktestOrder(BacktestStockOrder bso, StockQuote sq,
			StockQuote weeklySq,StockQuote monthlySq,
			Set<StockQuote> dailySQs) {
		
		stopLossCloseOrder(bso,sq);
		closeOnLastWeekBreak(bso,sq,weeklySq,dailySQs);
	}
	
	public void closeOnLastWeekBreak(BacktestStockOrder bso, StockQuote sq, StockQuote weeklySq,
			Set<StockQuote> dailySQs) {
		// Check for Weekly Reversals
		StockQuote lastWeek = hfSvc.getPreviousWeek(sq, dailySQs);
		float lastWeekHigh = lastWeek.getHigh().floatValue();
		float lastWeekLow = lastWeek.getLow().floatValue();
		if (bso.getOrderType().equals("BTO") && lastWeekLow > sq.getLow().floatValue()) {
			closeBackTestOrder(bso, sq.getQuoteDatetime(), lastWeek.getLow());
		} else if (bso.getOrderType().equals("STO") && lastWeekHigh < sq.getHigh().floatValue()) {
			closeBackTestOrder(bso, sq.getQuoteDatetime(), lastWeek.getHigh());
		}
	}
	
	private void stopLossCloseOrder(BacktestStockOrder bso, StockQuote sq) {
		if(bso.getOrderType().equals("BTO")) {
			if(sq.getLow().floatValue() < bso.getStopLossPrice().floatValue()) {
				float stopLossF = bso.getStopLossPrice().floatValue();
				float openF = sq.getOpen().floatValue();
				BigDecimal stopLoss = bso.getStopLossPrice();
				if(stopLossF >= openF) {
					stopLoss = sq.getOpen();
				}
				closeBackTestOrder(bso,sq.getQuoteDatetime(),stopLoss);
			}
		}else {
			if(sq.getHigh().floatValue() > bso.getStopLossPrice().floatValue()) {
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
	
	private BacktestStockOrder closeBackTestOrder(BacktestStockOrder order,
			LocalDateTime quoteDatetime,
			BigDecimal closePrice) {
    	order.setCloseDatetime(quoteDatetime);
    	order.setClosePrice(closePrice);
    	BigDecimal profitOrLoss  =BigDecimal.ZERO;
    	if(order.getOrderType().equals("BTO")) {
    		profitOrLoss = order.getClosePrice().subtract(order.getOpenPrice());
    	}else {
    		profitOrLoss = order.getOpenPrice().subtract(order.getClosePrice());
    	}
    	order.setProfitLoss(profitOrLoss.subtract(BigDecimal.valueOf(0.01)));
    	orderDao.saveOrUpdate(order);
    	return order;
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
	
	public BacktestStockOrder createNewBackTestOrder(StockQuote quote, StockQuote ystrDayquote, BackTestOrder signal,TheStrat stratNum) {
		BacktestStockOrder order = new BacktestStockOrder();
		order.setEntryDatetime(LocalDateTime.now());
		BigDecimal openPrice = signal==BackTestOrder.BUY ?ystrDayquote.getHigh():ystrDayquote.getLow();
		// Open Price for the back order has to be within Todays open
		if(signal==BackTestOrder.BUY) {
			if(openPrice.floatValue() < quote.getOpen().floatValue())
				openPrice = quote.getOpen();
			else
				openPrice = openPrice.add(BigDecimal.valueOf(0.01));
		}else {
			if(openPrice.floatValue() > quote.getOpen().floatValue())
				openPrice = quote.getOpen();
			else
				openPrice = openPrice.subtract(BigDecimal.valueOf(0.01));
		}
		order.setOpenPrice(openPrice); // One cent 
		order.setOpenDatetime(quote.getQuoteDatetime());
		order.setStock(quote.getStock());
		order.setStrategyId(stratNum.getStratNum()); // HeikenAshi
		order.setStopLossPrice((signal==BackTestOrder.BUY)?ystrDayquote.getLow():ystrDayquote.getHigh());
		order.setOrderType((signal==BackTestOrder.BUY)?"BTO":"STO"); //BTO - Buy to Open; STO - Sell to Open
		orders.add(order);
		orderDao.save(order);
		return order;
	}
	
	private StratDirection getWeeklyTrend(StockQuote currentW, StockQuote prevW) {
		//Determine the Weekly Trend based on last week and current week.
		TechAnalysisStrat currentWStrat =  strat.createStrat(currentW, prevW);
		return StratDirection.valueOfLabel(currentWStrat.getDirectionId());
	}
	
	private void twoCandleStrat(TheStrat strat,TechAnalysisStrat todayStrat, TechAnalysisStrat yesterdayStrat,
			StockQuote weeklySq, StockQuote monthlySq, Set<StockQuote> dailySQs) {
		StockQuote prevW = hfSvc.getPreviousWeek(todayStrat.getStockQuote(), dailySQs);
		
		// Check for Sell - Is this a reversal into FTFC
		// 1. yesterday's low is the trigger.
		// 2. Is yesterday's low is lower than weekly's open to cause it to go red?
		// 3. Sell with a stoploss at yesterday's high
		float wOpen= weeklySq.getOpen().floatValue();		
		float yLow = yesterdayStrat.getStockQuote().getLow().floatValue();
		float tLow =  todayStrat.getStockQuote().getLow().floatValue();
		
		StringBuilder sb = new StringBuilder();
		sb.append("SELL-");
		
		if((yLow<wOpen || tLow<wOpen) ) {
			if(checkYesterday(yesterdayStrat, strat,StratDirection.UP)) {
				if(checkToday(todayStrat, strat, StratDirection.DOWN )){
					StratDirection direction = getWeeklyTrend(weeklySq,prevW);
					if(direction == StratDirection.DOWN) {
						BacktestStockOrder bso = createNewBackTestOrder(todayStrat.getStockQuote(),
								yesterdayStrat.getStockQuote(), BackTestOrder.SELL, strat);
						throw new BackTestOrderCreatedException(bso);
					}else
						sb.append("Weekly:"+direction);
				}else
					sb.append("Today:"+StratDirection.valueOfLabel(todayStrat.getDirectionId()));	
				
			}else
				sb.append("Yesterday:"+StratDirection.valueOfLabel(yesterdayStrat.getDirectionId()));
		}else {
			sb.append("Main Criteria fail");
		}
		
		// Check for Buy - Is this a reversal into FTFC
		// 1. Yesterdays's high is the trigger
		// 2. Yesterday is 2 to downside and today is 2 to the upside reversal. 
		// 3. Is yesterday's high is higher than weekly's open to cause it to go green?
		// 4. Buy with a stoploss at yesterday's low
		
		float yHigh= yesterdayStrat.getStockQuote().getLow().floatValue();
		float tHigh=  todayStrat.getStockQuote().getHigh().floatValue();
		sb.append(";BUY-");
		

		if((yHigh > wOpen ||  tHigh>wOpen ) ) {
			if(checkYesterday(yesterdayStrat, strat,StratDirection.DOWN)) {
				if(checkToday(todayStrat, strat, StratDirection.UP )) {
//					(strat != TheStrat._123 && todayStrat.getDirectionId() == StratDirection.UP.getDirectionId()) || strat == TheStrat._123) {
					StratDirection direction = getWeeklyTrend(weeklySq,prevW);
					if(direction == StratDirection.UP) {
						BacktestStockOrder bso = createNewBackTestOrder(todayStrat.getStockQuote(),
								yesterdayStrat.getStockQuote(), BackTestOrder.BUY, strat);
						throw new BackTestOrderCreatedException(bso);
					}else
						sb.append("Weekly:"+direction);
				}else
					sb.append("Today:"+StratDirection.valueOfLabel(todayStrat.getDirectionId()));
				
			}else
				sb.append("Yesterday:"+StratDirection.valueOfLabel(yesterdayStrat.getDirectionId()));
		}else {
			sb.append("Main Criteria fail");
		}
		
		System.out.println("Scenario: "+strat +"-"+todayStrat.getStockQuote().getQuoteDatetime() +":" + sb);
	}
	
	private boolean checkYesterday(TechAnalysisStrat yesterdayStrat, TheStrat strat, StratDirection direction) {
		int yesterdayCandleId = strat.getYesterdayCandleId();
		return (yesterdayCandleId == 2 && yesterdayStrat.getDirectionId() == direction.getDirectionId()) || yesterdayCandleId !=2; 
	}
	
	private boolean checkToday(TechAnalysisStrat todayStrat, TheStrat strat , StratDirection direction) {
		
		int todayCandleId = strat.getTodayCandleId();
		return (todayCandleId == 2 && todayStrat.getDirectionId() ==  direction.getDirectionId()) || todayCandleId != 2;
	}
}