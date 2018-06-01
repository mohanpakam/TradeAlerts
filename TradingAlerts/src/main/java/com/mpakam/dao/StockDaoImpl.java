package com.mpakam.dao;

import java.io.Serializable;

import javax.transaction.Transactional;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.mpakam.model.Stock;

@Repository
@Transactional
public class StockDaoImpl extends AbstractGenericDao<Stock> implements  StockDao {
	
	@Autowired
	private SessionFactory sessionFactory;
	
	@Transactional
	@Override
	public Stock findBy(int stocknum) {
		String queryString = "FROM Stock WHERE stocknum = :stocknum";
		return (Stock) sessionFactory.getCurrentSession()
								.createQuery(queryString)
								.setParameter("stocknum", stocknum)
								.uniqueResult();
	}

	@Override
	@Transactional
	public Stock findBySymbol(String symbol) {
		String queryString = "FROM Stock WHERE ticker = :symbol";
		return (Stock) sessionFactory.getCurrentSession()
								.createQuery(queryString)
								.setParameter("symbol", symbol)
								.uniqueResult();
	}

}
