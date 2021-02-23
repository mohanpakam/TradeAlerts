package com.mpakam.service;

import java.util.LinkedList;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mpakam.constants.CandleColor;
import com.mpakam.constants.StratDirection;
import com.mpakam.constants.StratCandleIdentifier;
import com.mpakam.dao.MonitoredStockDao;
import com.mpakam.dao.StockQuoteDao;
import com.mpakam.dao.TechAnalysisStratDao;
import com.mpakam.model.MonitoredStock;
import com.mpakam.model.StockQuote;
import com.mpakam.model.TechAnalysisStrat;
import com.mpakam.service.strategy.strat.TheStratService;

@Service
public class TechAnalysisTheStratService {
	
	@Autowired
	private StockQuoteDao quoteDao;
	
	@Autowired
	MonitoredStockDao mStockDao;
	
	@Autowired
	private HigherTimeFrameStockQuoteService higherTFSvc;
	
	@Autowired
	private TechAnalysisStratDao stratDao;
	
	@Autowired
	private TheStratService stratSvc;
	
	//TODO: 
	//1. Calculate the Candle IDs and save them 
	//2. Set alerts for the following Strategies if not backtesting.
	// a. 122 RevStrat2
	// b. 123 RevStrat3
	// c. 1-2
	// d. 1-3
	// e. 2-2
	// f. 2-3
	// g. 3-2
	//3. considers the higher Time Frames to determine the above Strats
	public TechAnalysisStrat createStrat(StockQuote currentQuote, StockQuote lastQuote) {
		
		//TODO: 
		/*1. Saves the Strat entries for all days
		 * 2. creates and udpates the Week with in the same Week
		 * 3. creates and udpates the Monthly with in the same Month 
		 */

		TechAnalysisStrat strat = new TechAnalysisStrat();
		strat.setStockQuote(currentQuote);
		strat.setDirectionId(StratDirection.NONE.getDirectionId());
		CandleColor candleColor = stratSvc.getCandleColor(currentQuote);
		
		strat.setCandleColor(candleColor.getColorId());
		if(lastQuote == null) {
			strat.setCandleId(StratCandleIdentifier.ONE.getStratId());
			return strat;
		}
		
		// def insideBar = high < high[1] and low > low[1];
		// def outsideBar = high > high[1] and low < low[1];
		// plot barType = if insideBar then 1 else if outsideBar then 3 else 2;
		int highComp = currentQuote.getHigh().compareTo(lastQuote.getHigh());
		int lowComp = currentQuote.getLow().compareTo(lastQuote.getLow());
		String stratId = null;

		// Current high is less than or equal to previous high
		// Current Low is greater than or equal to preiouv low then 1
		if (highComp <= 0 && lowComp >= 0) {
			strat.setCandleId(StratCandleIdentifier.ONE.getStratId());
		}
		// Current high is greater than to previous high
		// Current Low is lower than to previous low then 3
		else if (highComp == 1 && lowComp == -1) {
			strat.setCandleId(StratCandleIdentifier.THREE.getStratId());
		} else {
//			String color = highComp == 1?"U":"D";
			strat.setDirectionId(highComp == 1?StratDirection.UP.getDirectionId():StratDirection.DOWN.getDirectionId());
			strat.setCandleId(StratCandleIdentifier.TWO.getStratId());
		}
		System.out.println(currentQuote.getStock().getTicker() + "-" + currentQuote.getQuoteDatetime() +":"
				+ StratCandleIdentifier.valueOfLabel(strat.getCandleId()).name() +"-" 
				+ StratDirection.valueOfLabel(strat.getDirectionId()).name() +"-" 
				+ CandleColor.valueOfLabel(strat.getCandleColor()).name());
		return strat;
	}
	
	public void backTestMonitoredStock(MonitoredStock ms) {
		//TODO:
		//1. Retrieves the Stock Quotes for Daily, creates Weekly, Monthly 
		//2. Assigns candle ID for Each
		//3 . Checks for Various conditions and creates backtest orders
		
		// Retrieves all Stock quotes that may include Daily, Weekly and Monthly.
		Set<StockQuote> dailySQs = quoteDao.findAllDailySetByStock(ms.getStock());
		
		LinkedList<TechAnalysisStrat> stratList = new LinkedList<TechAnalysisStrat>();
		
		//Create Stock Quotes for Daily Check if Weekly, Monthly exists
		StockQuote lastSq = null;
		for(StockQuote sq: dailySQs) {
			//Try to get the Weekly SQs
			StockQuote weeklySQ = higherTFSvc.getWeekly(sq, dailySQs);
			StockQuote monthlySQ = higherTFSvc.getMonthly(sq, dailySQs);
			TechAnalysisStrat strat = createStrat(sq,lastSq);
			stratDao.save(strat); //TODO: This would need to be further refined.
			stratList.add(strat);
			stratSvc.backTestStrat(strat,stratList,weeklySQ,monthlySQ,dailySQs);
			lastSq=sq;
		}
		
	}
	
}
