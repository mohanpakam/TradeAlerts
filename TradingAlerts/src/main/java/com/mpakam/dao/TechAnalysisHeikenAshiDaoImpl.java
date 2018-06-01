package com.mpakam.dao;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedList;

import javax.transaction.Transactional;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.mpakam.app.config.EnvironmentConfig;
import com.mpakam.model.Stock;
import com.mpakam.model.TechAnalysisAtr;
import com.mpakam.model.TechAnalysisHeikenashi;
import com.mpakam.util.ListUtil;

@Repository
@Transactional
public class TechAnalysisHeikenAshiDaoImpl extends AbstractGenericDao<TechAnalysisHeikenashi> implements  TechAnalysisHeikenAshiDao {
	
	public static HashMap<Stock, TechAnalysisHeikenashi> cache = new HashMap<>();
	
	@Autowired
	private SessionFactory sessionFactory;
	
	@Autowired
	EnvironmentConfig eConfig;
	
	private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	@Override
	public LinkedList<TechAnalysisHeikenashi> retrieveLastXQuotesByStockNum(Stock stock) {
		Query query = sessionFactory.getCurrentSession().getNamedQuery("TechAnalysisHeikenashi.retrieveLastXQuotesByStockNum")
					.setInteger("stockNum", stock.getStocknum());
		LinkedList<TechAnalysisHeikenashi> resultList=(LinkedList<TechAnalysisHeikenashi>)ListUtil.getLinkedListInstance(query.list());
		return resultList;
	}

}
