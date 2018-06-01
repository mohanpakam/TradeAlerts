package com.mpakam.service;

import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.transaction.Transactional;

import com.mpakam.model.MonitoredStock;
import com.mpakam.model.Stock;

public interface IStockQuoteService {


	@Transactional
	public void analyzeStock(Stock symbol) throws Exception;
	
	/*
	 * this api cleanups all the records older than 5 days 
	 * except for the daily stockquotes. For daily, it will be 
	 * older than 75 days.
	 */
	@Transactional
	void cleanupOldData();
	
	@Transactional
	public void analyzeDailyStocks() throws Exception;

	public void analyzeStock(List<MonitoredStock> list) throws InterruptedException, ExecutionException;

	void initialBatchAnalyzeStock() throws Exception;

	void batchDailyAnalyzeStock() throws Exception;

	void iexAnalyzeStock(List<MonitoredStock> list) throws InterruptedException, ExecutionException;

	void iexRenkoChartAnalyzeStock(List<MonitoredStock> list) throws InterruptedException, ExecutionException;

	void analyzeStockFromAlphaVantage(Stock symbol) throws Exception;

}
