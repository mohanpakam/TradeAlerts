package com.mpakam.dao;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

import javax.transaction.Transactional;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.mpakam.constants.Interval;
import com.mpakam.model.Stock;
import com.mpakam.model.StockQuote;
import com.mpakam.util.BigDecimalUtil;
import com.mpakam.util.ListUtil;

@Repository
@Transactional 
public class StockQuoteDaoImp implements StockQuoteDao {
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	public static HashMap<Stock, StockQuote> quotesCache= new HashMap<>();
	
	@Autowired
	private SessionFactory sessionFactory;
	
	@Autowired
	private BigDecimalUtil bigDUtil;
	
	@Transactional
	public int save(StockQuote stockQuote) {
		if(!bigDUtil.isValid(stockQuote.getOpen()) 
				|| !bigDUtil.isValid(stockQuote.getClose())
				|| !bigDUtil.isValid(stockQuote.getHigh())
				|| !bigDUtil.isValid(stockQuote.getLow()))
			throw new RuntimeException("one of OHLC is invalid " 
					+stockQuote.getStock().getTicker() +" @ " 
					+stockQuote.getQuoteDatetime());
		stockQuote.setRecordedTimestamp(LocalDateTime.now());
		int returnValue=(int) sessionFactory.getCurrentSession().save(stockQuote);
		stockQuote.setStockQuoteId(returnValue);
		quotesCache.put(stockQuote.getStock(), stockQuote);
		return returnValue;
	}
	
	@Override
	public LinkedList<StockQuote> findAllByStock(Stock stock) {
		Query query = sessionFactory.getCurrentSession().getNamedQuery("StockQuote.findAllByStock")
				.setInteger("stockNum", stock.getStocknum())
				.setInteger("interval", stock.getInterval());
		return  (LinkedList<StockQuote>)ListUtil.getLinkedListInstance(query.list());
	}

	@Override
	public void update(StockQuote stockQuote) {
		sessionFactory.getCurrentSession().update(stockQuote);
		return;
	}

	@Override
	public void save(Set<StockQuote> stockQuoteList) {
		/*stockQuoteList.sort(new Comparator<StockQuote>() {
			@Override
			public int compare(StockQuote o1, StockQuote o2) {
				return o1.getQuoteDatetime().compareTo(o2.getQuoteDatetime());
			}
		});*/
		stockQuoteList.forEach(p->save(p));
	}

	@Transactional
	@Override
	public StockQuote findLastStockQuote(Stock stock) {
		if(!quotesCache.containsKey(stock)) {
			log.debug("Retrieving the stock quote data from db " + stock.getTicker());
		String queryString = "FROM StockQuote WHERE stocknum = :stocknum and interval=:interval order by quoteDatetime desc";
		StockQuote result= (StockQuote) sessionFactory.getCurrentSession()
								.createQuery(queryString)
								.setMaxResults(1)
								.setParameter("stocknum", stock.getStocknum())
								.setParameter("interval", stock.getInterval())
								.uniqueResult();
			if(result != null)
				quotesCache.put(stock, result);
			else
				return null;
		}
		return quotesCache.get(stock);
	}

	@Override
	@Transactional
	public void cleanUp() {
		Query query = sessionFactory.getCurrentSession().createSQLQuery(
				"CALL cleanup()");
		query.getFirstResult();
	}
	
	@Override
	public Set<StockQuote> findAllSetByStock(Stock stock) {
		Query query = sessionFactory.getCurrentSession().getNamedQuery("StockQuote.findAllByStock")
				.setInteger("stockNum", stock.getStocknum())
				.setInteger("interval", stock.getInterval());
		return  new TreeSet<StockQuote>(query.list());
	}
	
	@Override
	public Set<StockQuote> findAllSetByStock(Stock stock, Interval i) {
		Query query = sessionFactory.getCurrentSession().getNamedQuery("StockQuote.findAllByStock")
				.setInteger("stockNum", stock.getStocknum())
				.setInteger("interval", i.getInterval());
		return  new TreeSet<StockQuote>(query.list());
	}
	
	@Override
	public Set<StockQuote> findAllDailySetByStock(Stock stock) {
		return findAllSetByStock(stock, Interval.DAILY);
	}
	@Override
	public Set<StockQuote> findAllWeeklySetByStock(Stock stock) {
		return findAllSetByStock(stock, Interval.WEEKLY);
	}
	
	@Override
	public Set<StockQuote> findAllMonthlySetByStock(Stock stock) {
		return findAllSetByStock(stock, Interval.MONTHLY);
	}
	
	@Transactional
	@Override
	public StockQuote findStockQuoteByQuoteDate(Stock stock, final LocalDateTime quoteDate) {
			log.debug("Retrieving the stock quote data from db " + stock.getTicker());
		String queryString = "FROM StockQuote WHERE stocknum = :stocknum and interval=:interval and quoteDatetime=:dateTime order by quoteDatetime desc";
		StockQuote result= (StockQuote) sessionFactory.getCurrentSession()
								.createQuery(queryString)
								.setMaxResults(1)
								.setParameter("stocknum", stock.getStocknum())
								.setParameter("interval", stock.getInterval())
								.setParameter("dateTime", quoteDate)
								.uniqueResult();
			if(result != null)
				quotesCache.put(stock, result);
			else
				return null;
		return quotesCache.get(stock);
	}

}
