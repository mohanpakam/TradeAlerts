package com.mpakam.dao;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import javax.transaction.Transactional;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.mpakam.model.MonitoredStock;
import com.mpakam.util.ListUtil;

@Repository
@Transactional 
public class MonitoredStockDaoImpl extends AbstractGenericDao<MonitoredStock> implements  MonitoredStockDao {
	
	@Autowired
	private SessionFactory sessionFactory;
	
	@Override
	public List<MonitoredStock> retrievegetActivelyMonitoredStocks() {
		Query query = sessionFactory.getCurrentSession().getNamedQuery("MonitoredStock.getActivelyMonitoredStocks");
		return  (LinkedList<MonitoredStock>)ListUtil.getLinkedListInstance(query.list());
	}
	@Override
	public List<MonitoredStock> retreiveByStockNum(int stockNum) {
		Query query = sessionFactory.getCurrentSession().getNamedQuery("MonitoredStock.retreiveByStockNum")
				.setInteger("stockNum", stockNum);
		return  (LinkedList<MonitoredStock>)ListUtil.getLinkedListInstance(query.list());
	}
	
	@Override
	public List<MonitoredStock> retrievegetActivelyMonitoredStocksByTime(int time) {
		Query query = sessionFactory.getCurrentSession().getNamedQuery("MonitoredStock.retrievegetActivelyMonitoredStocksByTime")
				.setInteger("time", time);
		return  (LinkedList<MonitoredStock>)ListUtil.getLinkedListInstance(query.list());
	}

	@Override
	public int cleanUpAndSave(MonitoredStock mStock) {
		deleteByStockNum(mStock);
		return (int) save(mStock);
	}
	
	private void deleteByStockNum(MonitoredStock mStock) {
		retreiveByStockNum(mStock.getStock().getStocknum()).forEach(p->{
			delete(mStock);
		});
	}
}
