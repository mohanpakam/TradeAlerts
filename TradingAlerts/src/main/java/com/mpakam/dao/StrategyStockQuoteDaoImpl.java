package com.mpakam.dao;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.transaction.Transactional;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.mpakam.model.Stock;
import com.mpakam.model.StrategyStockQuote;
import com.mpakam.util.ListUtil;

@Repository
@Transactional 
public class StrategyStockQuoteDaoImpl  extends AbstractGenericDao<StrategyStockQuote> implements StrategyStockQuoteDao {
	
	public static HashMap<Stock, StrategyStockQuote> cache= new HashMap<>();

	@Autowired
	private SessionFactory sessionFactory;
	
	@Override
	public LinkedList<StrategyStockQuote> retrieveLastXQuotesByStockNumStrategyId(Stock stock, int strategyNum) {
		Query query = sessionFactory.getCurrentSession().getNamedQuery("StrategyStockQuote.getTopXByStockNum")
				.setInteger("strategyId", strategyNum)
				.setInteger("stockNum", stock.getStocknum())
				.setInteger("interval", stock.getInterval());
//		return  ListUtil.mapInstance((ArrayList<StrategyStockQuote>)query.list());
		return  (LinkedList<StrategyStockQuote>)ListUtil.getLinkedListInstance(query.list());
	}
	
	@Override
	public LinkedList<StrategyStockQuote> retrieveQuotesByStockNumStrategyId(Stock stock, int strategyNum) {
		Query query = sessionFactory.getCurrentSession().getNamedQuery("StrategyStockQuote.getAllByStockNumStrategyId")
				.setInteger("strategyId", strategyNum)
				.setInteger("stockNum", stock.getStocknum())
				.setInteger("interval", stock.getInterval());
//		return  ListUtil.mapInstance((ArrayList<StrategyStockQuote>)query.list());
		LinkedList<StrategyStockQuote> list=(LinkedList<StrategyStockQuote>)ListUtil.getLinkedListInstance(query.list());
		list.sort(( StrategyStockQuote o1,  StrategyStockQuote o2)->o1.getStrategyStockQuoteId() - o2.getStrategyStockQuoteId());
		return  list;
	}
	
	@Override
	public LinkedList<StrategyStockQuote> getAllByStockNum(Stock stock) {
		Query query = sessionFactory.getCurrentSession().getNamedQuery("StrategyStockQuote.getAllByStockNum")
				.setInteger("stockNum", stock.getStocknum())
				.setInteger("interval", stock.getInterval());
//		return  ListUtil.mapInstance((ArrayList<StrategyStockQuote>)query.list());
		LinkedList<StrategyStockQuote> list=(LinkedList<StrategyStockQuote>)ListUtil.getLinkedListInstance(query.list());
		list.sort(( StrategyStockQuote o1,  StrategyStockQuote o2)->o1.getStrategyStockQuoteId() - o2.getStrategyStockQuoteId());
		return  list;
	}
	
	@Override
	public StrategyStockQuote retrieveLastQuotesByStockNumStrategyId(Stock stock, int strategyNum) {
		if(!cache.containsKey(stock)) {
			Query query = sessionFactory.getCurrentSession().getNamedQuery("StrategyStockQuote.getLastQuoteByStockNum")
				.setInteger("strategyId", strategyNum)
				.setInteger("stockNum", stock.getStocknum())
				.setInteger("interval", stock.getInterval());
			List<StrategyStockQuote> list =(List<StrategyStockQuote>)query.list();
			if(list.size() >0) {
				cache.put(stock, list.get(0)) ;
			}else
				return null;
		}
		return cache.get(stock);
	}	
	
	@Override
	public Serializable save(StrategyStockQuote entity) {
		entity.setStrategyStockQuoteId((int)super.save(entity));
		cache.put(entity.getStockQuote().getStock(), entity);
		return entity.getStrategyStockQuoteId();
	}
	

}
