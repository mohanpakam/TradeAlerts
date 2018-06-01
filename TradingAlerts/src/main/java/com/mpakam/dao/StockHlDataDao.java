package com.mpakam.dao;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

import com.mpakam.model.Stock;
import com.mpakam.model.StockHlData;

public interface StockHlDataDao  extends GenericDao<StockHlData>{
		
	StockHlData findLastByStock(Stock s);
	
	void saveAll(List<StockHlData> list);
	
	LinkedList<StockHlData> findAllEntriesAfter(int stocknum, LocalDateTime startTime);
	
	int deleteForAllStock(Stock s);
}
