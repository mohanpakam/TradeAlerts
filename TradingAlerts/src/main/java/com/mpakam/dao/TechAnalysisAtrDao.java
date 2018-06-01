package com.mpakam.dao;

import java.util.LinkedList;

import com.mpakam.model.Stock;
import com.mpakam.model.StockQuote;
import com.mpakam.model.StrategyStockQuote;
import com.mpakam.model.TechAnalysisAtr;

public interface TechAnalysisAtrDao extends GenericDao<TechAnalysisAtr>{
	TechAnalysisAtr retrieveLastByStockNum(Stock stock);

	LinkedList<TechAnalysisAtr> retrieveAtrByStockQuote(StockQuote stockQ);
}
