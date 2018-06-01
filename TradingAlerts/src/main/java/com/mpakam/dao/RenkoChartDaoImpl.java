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

import com.mpakam.model.RenkoChartBox;
import com.mpakam.model.Stock;
import com.mpakam.model.StockHlData;
import com.mpakam.util.ListUtil;

@Repository
@Transactional
public class RenkoChartDaoImpl extends AbstractGenericDao<RenkoChartBox> implements  RenkoChartDao {
	
	public static HashMap<Stock, RenkoChartBox> cache= new HashMap<>();
	
	@Autowired
	private SessionFactory sessionFactory;
	
	@Override
	public RenkoChartBox findLastByStocknum(Stock s) {
		if(!cache.containsKey(s)) {
			Query query = sessionFactory.getCurrentSession().getNamedQuery("RenkoChartBox.findLastByStocknum")
					.setInteger("stocknum", s.getStocknum());
			List<RenkoChartBox> list = (List<RenkoChartBox>)(query.list());
			if(list.size() >0 ) {
				cache.put(s, list.get(0));
			}else
				return null;
		}
		return cache.get(s);
	}
	
	@Override
	public Serializable save(RenkoChartBox entity) {
		entity.setRenkoBoxId((int)super.save(entity));
		cache.put(entity.getStock(), entity);
		return entity.getRenkoBoxId();
	}

	@Override
	public LinkedList<RenkoChartBox> findAllByStocknum(int stocknum) {
		Query query = sessionFactory.getCurrentSession().getNamedQuery("RenkoChartBox.findAllByStocknum")
				.setInteger("stocknum", stocknum);
		return  (LinkedList<RenkoChartBox>)ListUtil.getLinkedListInstance(query.list());
	}
}