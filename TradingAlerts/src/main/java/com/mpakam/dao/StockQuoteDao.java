package com.mpakam.dao;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.Set;

import javax.transaction.Transactional;

import com.mpakam.constants.Interval;
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
	
	Set<StockQuote> findAllDailySetByStock(Stock stock);
	Set<StockQuote> findAllWeeklySetByStock(Stock stock);
	Set<StockQuote> findAllMonthlySetByStock(Stock stock);
	Set<StockQuote> findAllSetByStock(Stock stock, Interval i);
	public StockQuote findStockQuoteByQuoteDate(Stock stock, final LocalDateTime dateTime);
}
