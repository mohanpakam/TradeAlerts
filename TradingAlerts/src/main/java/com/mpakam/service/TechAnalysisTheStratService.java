package com.mpakam.service;

import org.springframework.stereotype.Service;

import com.mpakam.model.StockQuote;
import com.mpakam.model.TechAnalysisStrat;

@Service
public class TechAnalysisTheStratService {
	
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
	public TechAnalysisStrat analyze(StockQuote currentQuote, StockQuote lastQuote) {
		
		TechAnalysisStrat strat = new TechAnalysisStrat();
		strat.setStockQuote(currentQuote);
		strat.setStratId(getStratId(currentQuote,lastQuote));
		
		return strat;
	}
	
	private String getStratId(StockQuote currentQuote, StockQuote lastQuote) {
		
		String candleColor = currentQuote.getOpen().compareTo(currentQuote.getClose()) != -1 ? "r" : "g";
		
		if(lastQuote == null) {
			return "1" + candleColor;
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
			stratId = "1" + candleColor;
		}
		// Current high is greater than to previous high
		// Current Low is lower than to previous low then 3
		else if (highComp == 1 && lowComp == -1) {

			stratId = "3" + candleColor;
		} else {
			String color = highComp == 1?"U":"D";
					
			stratId = "2" + color + candleColor;
		}
		System.out.println("CQ- High:"+currentQuote.getHigh() + ";Low:"+currentQuote.getLow() + ";Open:"+currentQuote.getOpen() + ";Close:"+ currentQuote.getLow());
		System.out.println("LQ- High:"+lastQuote.getHigh() + ";Low:"+lastQuote.getLow() );
		System.out.println(currentQuote.getStock().getTicker() +"-"+currentQuote.getQuoteDatetime() + "Strat ID:" + stratId);
		return stratId;
	}
}
