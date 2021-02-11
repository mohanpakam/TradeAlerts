package com.mpakam.dao;

import java.util.LinkedList;
import java.util.Set;

import javax.transaction.Transactional;

import com.mpakam.model.Stock;
import com.mpakam.model.StockQuote;

public interface StockQuoteDao {

	
	int save(StockQuote stockQuote);
	void update(StockQuote stockQuote);
	
	void save(Set<StockQuote> stockQuoteList);
	@Transactional
	StockQuote findLastStockQuote(Stock stock);
	
	@Transactional
	void cleanUp();
	
	LinkedList<StockQuote> findAllByStock(Stock stock);
	
	Set<StockQuote> findAllSetByStock(Stock stock);
	
	
	
}
