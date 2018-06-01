package com.mpakam.service;

import java.util.concurrent.ExecutionException;

public interface IMonitoredStockService {

	void loadMonitoredStockHistory() throws InterruptedException, ExecutionException;

	void loadBatchMonitoredStockHistory() throws InterruptedException, ExecutionException;
	
}
