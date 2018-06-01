package com.mpakam.service.strategy;

import java.util.Set;

import com.mpakam.model.StockQuote;
import com.mpakam.model.StrategyStockQuote;

public interface IStrategyService {

	public int executeStrategy(Set<StockQuote> quotes);

	int checkForSignal(StrategyStockQuote currentQuote, StrategyStockQuote prevQuote);

	int checkForSignalForTrend(StrategyStockQuote currentQuote, StrategyStockQuote prevQuote);
	
}
