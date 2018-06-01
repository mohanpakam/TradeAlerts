package com.mpakam.dao;

import java.util.LinkedList;

import com.mpakam.model.Stock;
import com.mpakam.model.StrategyStockQuote;

public interface StrategyStockQuoteDao extends GenericDao<StrategyStockQuote>{
	LinkedList<StrategyStockQuote> retrieveLastXQuotesByStockNumStrategyId(Stock stock, int strategyNum);
	LinkedList<StrategyStockQuote> retrieveQuotesByStockNumStrategyId(Stock stock, int strategyNum);
	LinkedList<StrategyStockQuote> getAllByStockNum(Stock stock);
	StrategyStockQuote retrieveLastQuotesByStockNumStrategyId(Stock stock, int strategyNum);
}
