package com.mpakam.dao;

import java.util.List;

import javax.transaction.Transactional;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.mpakam.model.CustomerTickerTracker;

@Repository
@Transactional 
public class CustomerTickerTrackerDaoImp extends AbstractGenericDao<CustomerTickerTracker> implements CustomerTickerTrackerDao {

	@Autowired
	private SessionFactory sessionFactory;
	
	@Override
	public List<CustomerTickerTracker> findByCustomerId(int customerId) {
		String queryString = "FROM CustomerTickerTracker WHERE customerId = :customerId";
		return (List<CustomerTickerTracker>) sessionFactory.getCurrentSession()
								.createQuery(queryString)
								.setParameter("customerId", customerId)
								.list();
	}

	@Override
	public Long save(CustomerTickerTracker customerTkr) {
		return (Long)sessionFactory.getCurrentSession().save(customerTkr);
	}
	
	public List<CustomerTickerTracker> findByTicker(int stockNum){
		String queryString = "FROM CustomerTickerTracker WHERE stocknum = :stocknum";
		return (List<CustomerTickerTracker>) sessionFactory.getCurrentSession()
								.createQuery(queryString)
								.setParameter("stocknum", stockNum)
								.list();
	}
}
