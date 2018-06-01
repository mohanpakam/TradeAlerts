package com.mpakam.dao;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

import javax.transaction.Transactional;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.mpakam.model.StockAlert;
import com.mpakam.util.ListUtil;

@Repository
@Transactional 
public class StockAlertDaoImp extends AbstractGenericDao<StockAlert> implements StockAlertDao {
	
	@Autowired
	private SessionFactory sessionFactory;

	public List<StockAlert> getTop100Alerts() {

		Query query = sessionFactory.getCurrentSession().getNamedQuery("StockAlert.getOpenAlerts");
		return (LinkedList<StockAlert>) ListUtil.getLinkedListInstance(query.list());

	}

	@Override
	public List<StockAlert> getActiveAlerts() {
		Query query = sessionFactory.getCurrentSession().getNamedQuery("StockAlert.getActiveAlerts");
		return (LinkedList<StockAlert>) ListUtil.getLinkedListInstance(query.list());
	}

	@Override
	public List<StockAlert> getActiveStockAlertByStocknum(int stocknum) {
		Query query = sessionFactory.getCurrentSession().getNamedQuery("StockAlert.getActiveStockAlertByStockNum")
				.setInteger("stocknum",stocknum);
		return (LinkedList<StockAlert>) ListUtil.getLinkedListInstance(query.list());
	}
	
	@Override
	public Serializable save(StockAlert sa) {
		sa.setSentStatus(0); //Always setting the sent Status to 0;
		return super.save(sa);
	}

	@Override
	public List<StockAlert> retrieveNewAlerts() {
		Query query = sessionFactory.getCurrentSession().getNamedQuery("StockAlert.retrieveNewAlerts");
		return (LinkedList<StockAlert>) ListUtil.getLinkedListInstance(query.list());
	}
	
	@Override
	public void markAsSentAlert(StockAlert sa) {
		sa.setSentStatus(1);
		sa.setSentTimeStamp(LocalDateTime.now());
		super.merge(sa);
	}
	

}
