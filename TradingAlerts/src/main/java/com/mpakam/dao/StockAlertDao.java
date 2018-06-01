package com.mpakam.dao;

import java.util.List;

import com.mpakam.model.StockAlert;

public interface StockAlertDao  extends GenericDao<StockAlert>{

	public List<StockAlert> getTop100Alerts();
	
	public List<StockAlert> getActiveAlerts();
	
	public List<StockAlert> getActiveStockAlertByStocknum(int stocknum);
	public List<StockAlert> retrieveNewAlerts();

	void markAsSentAlert(StockAlert sa);
	
}
