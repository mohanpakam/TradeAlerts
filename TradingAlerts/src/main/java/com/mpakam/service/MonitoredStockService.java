package com.mpakam.service;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mpakam.dao.MonitoredStockDao;
import com.mpakam.model.MonitoredStock;

@Service
public class MonitoredStockService  implements IMonitoredStockService{

	@Autowired
	MonitoredStockDao mStockDao;
	
	@Autowired
	IStockQuoteService quoteSvc;
	
	@Override
	public void loadMonitoredStockHistory() throws InterruptedException, ExecutionException {
		List<MonitoredStock> list =mStockDao.retrievegetActivelyMonitoredStocks();
        quoteSvc.analyzeStock(list);
	}
	
	@Override
	public void loadBatchMonitoredStockHistory() throws InterruptedException, ExecutionException {
		List<MonitoredStock> list =mStockDao.retrievegetActivelyMonitoredStocks();
        quoteSvc.iexAnalyzeStock(list);
	}
	

}
