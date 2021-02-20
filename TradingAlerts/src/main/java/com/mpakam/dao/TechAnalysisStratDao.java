package com.mpakam.dao;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;

import javax.transaction.Transactional;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.mpakam.model.Stock;
import com.mpakam.model.StockQuote;
import com.mpakam.model.TechAnalysisAtr;
import com.mpakam.model.TechAnalysisStrat;
import com.mpakam.util.ListUtil;

@Repository
@Transactional 
public class TechAnalysisStratDao  extends AbstractGenericDao<TechAnalysisStrat>{
	
	public static HashMap<Stock, TechAnalysisStrat> cache= new HashMap<>();

	@Autowired
	private SessionFactory sessionFactory;
	
	public Serializable save(TechAnalysisStrat entity) {
		entity.setSqStratId((int)super.save(entity));
		cache.put(entity.getStockQuote().getStock(), entity);
		return entity.getSqStratId();
	}
	
	public LinkedList<TechAnalysisStrat> retrieveStratByStockQuote(StockQuote stockQ) {
		Query query = sessionFactory.getCurrentSession().getNamedQuery("TechAnalysisStrat.retrieveStratByStockQuote")
				.setInteger("stockQuoteId", stockQ.getStockQuoteId());
		return  (LinkedList<TechAnalysisStrat>)ListUtil.getLinkedListInstance(query.list());
	}
	
	

}
