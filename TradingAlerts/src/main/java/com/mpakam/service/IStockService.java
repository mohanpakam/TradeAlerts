package com.mpakam.service;

import java.util.List;
import java.util.TreeSet;

import javax.transaction.Transactional;

import com.mpakam.model.Stock;
import com.mpakam.model.StockQuote;

public interface IStockService {

	/*
	 * do a lookup to the 
	 */
	
	void calculateHeikenAshi(List<StockQuote> quotesList);
	
	void calculateStochRsi(List<StockQuote> quotesList);

	@Transactional
	TreeSet<StockQuote> syncBySymbol(Stock symbol) throws Exception;

}
