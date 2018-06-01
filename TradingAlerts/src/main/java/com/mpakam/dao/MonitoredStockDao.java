package com.mpakam.dao;

import java.util.List;

import com.mpakam.model.MonitoredStock;

public interface MonitoredStockDao  extends GenericDao<MonitoredStock>{

	List<MonitoredStock> retrievegetActivelyMonitoredStocks();
	
	int cleanUpAndSave(MonitoredStock mStock);

	List<MonitoredStock> retreiveByStockNum(int stockNum);
	
	List<MonitoredStock> retrievegetActivelyMonitoredStocksByTime(int time);
	
}
