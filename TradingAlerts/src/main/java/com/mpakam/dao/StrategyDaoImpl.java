package com.mpakam.dao;

import javax.transaction.Transactional;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.mpakam.model.Strategy;

@Repository
@Transactional 
public class StrategyDaoImpl implements StrategyDao {
	
	@Autowired
	private SessionFactory sessionFactory;
	
	@Transactional
	@Override
	public Strategy findBy(int strategyId) {
		String queryString = "FROM Strategy WHERE strategyId = :id";
		return (Strategy) sessionFactory.getCurrentSession()
								.createQuery(queryString)
								.setParameter("id", strategyId)
								.uniqueResult();
	}

	@Override
	public Long save(Strategy strategy) {
		return (Long) sessionFactory.getCurrentSession().save(strategy);
		
	}

	@Override
	public void update(Strategy strategy) {
		sessionFactory.getCurrentSession().update(strategy);
		return;
	}

}
