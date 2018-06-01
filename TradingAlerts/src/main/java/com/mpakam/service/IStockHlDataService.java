package com.mpakam.service;

import java.util.List;

import com.mpakam.model.Stock;
import com.mpakam.model.StockHlData;

public interface IStockHlDataService {

	public void getStockHlData() throws Exception;

	void saveAll(Stock s, List<StockHlData> list);
	
	public void deleteAllForStock(Stock s);

	void saveAll(List<StockHlData> list);
	
}
