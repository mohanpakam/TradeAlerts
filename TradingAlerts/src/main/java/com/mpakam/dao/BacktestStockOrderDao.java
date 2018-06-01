package com.mpakam.dao;

import java.time.LocalDateTime;
import java.util.List;

import com.mpakam.model.BacktestStockOrder;
import com.mpakam.model.Stock;

public interface BacktestStockOrderDao extends GenericDao<BacktestStockOrder>{
	BacktestStockOrder findOpenOrder(Stock s);
	List<BacktestStockOrder> getLatestOrders(LocalDateTime time);
	BacktestStockOrder findOpenOrder(Stock s, int strategyId);
}
