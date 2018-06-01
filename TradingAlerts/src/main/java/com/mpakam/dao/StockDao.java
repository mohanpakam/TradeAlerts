package com.mpakam.dao;

import java.io.Serializable;

import javax.transaction.Transactional;

import com.mpakam.model.CustomerTickerTracker;
import com.mpakam.model.Stock;

public interface StockDao  extends GenericDao<Stock>{
	@Transactional
	Stock findBy(int i);
	Stock findBySymbol(String symbol);
}
