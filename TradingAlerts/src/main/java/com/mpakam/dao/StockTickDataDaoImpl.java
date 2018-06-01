package com.mpakam.dao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.transaction.Transactional;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.mpakam.model.StockTickData;
import com.mpakam.scheduler.ScheduledTasks;
import com.mpakam.util.BigDecimalUtil;
import com.mpakam.util.ListUtil;

@Repository
@Transactional 
public class StockTickDataDaoImpl  extends AbstractGenericDao<StockTickData> implements StockTickDataDao {

	private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);
	
	@Autowired
	private SessionFactory sessionFactory;
	
	@Autowired
	private BigDecimalUtil bigDUtil;


	@Override
	public void saveAll(List<StockTickData> tickerList) {
		tickerList.forEach(t->{
			if(t== null)
				return;
			if( t.getTickDatetime() == null) {
				System.out.println("tick Date time is null for " + t.getStock().getTicker() + " time now: " + LocalDateTime.now());
				t.setTickDatetime(LocalDateTime.now().withSecond(0));
			}else
				t.setTickDatetime(t.getTickDatetime().withSecond(0));
			if(bigDUtil.isValid(t.getPrice()))
					save(t);
			else
				log.debug("Invalid tick price for " + t.getStock().getTicker());
		});
		
	}

	@Override
	public LinkedList<StockTickData> findCurrentSessionByStockNum(int stocknum) {
		LocalDate timeNow = LocalDate.now().minusDays(3);//3 days to ensure a Monday run will be smooth after Last Friday's close
		Query query = sessionFactory.getCurrentSession().getNamedQuery("StockTickData.findCurrentSessionByStockNum")
					.setInteger("stocknum", stocknum)
					.setTimestamp("todaySessionStart", Date.from(timeNow.atStartOfDay(ZoneId.systemDefault()).toInstant()));
			return  (LinkedList<StockTickData>)ListUtil.getLinkedListInstance(query.list());
	}
	
}
