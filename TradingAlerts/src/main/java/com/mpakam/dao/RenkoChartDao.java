package com.mpakam.dao;

import java.util.LinkedList;

import com.mpakam.model.RenkoChartBox;
import com.mpakam.model.Stock;

public interface RenkoChartDao  extends GenericDao<RenkoChartBox>{
	public RenkoChartBox findLastByStocknum(Stock s);
	public LinkedList<RenkoChartBox> findAllByStocknum(int stocknum);
}
