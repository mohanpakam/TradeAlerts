package com.mpakam.dao;

import java.util.LinkedList;

import com.mpakam.model.Stock;
import com.mpakam.model.TechAnalysisHeikenashi;

public interface TechAnalysisHeikenAshiDao  extends GenericDao<TechAnalysisHeikenashi>{

	LinkedList<TechAnalysisHeikenashi> retrieveLastXQuotesByStockNum(Stock stock);
}
