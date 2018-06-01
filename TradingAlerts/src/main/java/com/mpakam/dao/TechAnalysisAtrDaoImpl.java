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
import com.mpakam.util.ListUtil;

@Repository
@Transactional 
public class TechAnalysisAtrDaoImpl  extends AbstractGenericDao<TechAnalysisAtr> implements TechAnalysisAtrDao {
	
	public static HashMap<Stock, TechAnalysisAtr> cache= new HashMap<>();

	@Autowired
	private SessionFactory sessionFactory;
	
	@Override
	public TechAnalysisAtr retrieveLastByStockNum(Stock stock) {
		if(!cache.containsKey(stock)) {
			Query query = sessionFactory.getCurrentSession().getNamedQuery("TechAnalysisAtr.retrieveLastByStockNum")
					.setInteger("stockNum", stock.getStocknum());
			LinkedList<TechAnalysisAtr> resultList=(LinkedList<TechAnalysisAtr>)ListUtil.getLinkedListInstance(query.list());
			if(resultList.size()>0) {
				TechAnalysisAtr atr=resultList.get(0);
				cache.put(stock, atr);
				return atr;
			}else
				return null;
		}else {
			return cache.get(stock);
		}
	}
	
	@Override
	public Serializable save(TechAnalysisAtr entity) {
		entity.setAtrId((int)super.save(entity));
		cache.put(entity.getStockQuote().getStock(), entity);
		return entity.getAtrId();
	}
	
	@Override
	public LinkedList<TechAnalysisAtr> retrieveAtrByStockQuote(StockQuote stockQ) {
		Query query = sessionFactory.getCurrentSession().getNamedQuery("TechAnalysisAtr.retrieveAtrByStockQuote")
				.setInteger("stockQuoteId", stockQ.getStockQuoteId());
		return  (LinkedList<TechAnalysisAtr>)ListUtil.getLinkedListInstance(query.list());
	}

}
