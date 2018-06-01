package com.mpakam.dao;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

import com.mpakam.model.StockTickData;

public interface StockTickDataDao extends GenericDao<StockTickData>{

	void saveAll(List<StockTickData> tickerList);
	
	LinkedList<StockTickData> findCurrentSessionByStockNum(int stocknum);
}
