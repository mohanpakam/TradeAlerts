package com.mpakam.dao;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.transaction.Transactional;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.mpakam.app.config.EnvironmentConfig;
import com.mpakam.model.Stock;
import com.mpakam.model.StockHlData;
import com.mpakam.util.ListUtil;

@Repository
@Transactional
public class StockHlDataDaoImpl extends AbstractGenericDao<StockHlData> implements  StockHlDataDao {
	
	public static HashMap<Stock, StockHlData> cache = new HashMap<>();
	
	@Autowired
	private SessionFactory sessionFactory;
	
	@Autowired
	EnvironmentConfig eConfig;
	
	private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	@Override
	public StockHlData findLastByStock(Stock s) {
		if(!cache.containsKey(s)) {
			Query query = sessionFactory.getCurrentSession().getNamedQuery("StockHlData.findLastByStock")
				.setInteger("stocknum", s.getStocknum())
				.setInteger("interval", eConfig.getTICK_INTERVAL());
			List<StockHlData> list = (List<StockHlData>)(query.list());
			if(list.size()>0) {
				cache.put(s, list.get(0));
			}else
				return null;
		}
		return  cache.get(s);
	}

	@Transactional
	@Override
	public void saveAll(List<StockHlData> list) {
		list.stream().forEach(hl->{
			if(hl !=null) {
				hl.setRecordedTimestamp(LocalDateTime.now());//Recoridng the timestamp when HL was recorded
			if(hl.getHlDatetime() != null)
				hl.setHlDatetime(hl.getHlDatetime().withSecond(0));//seconds doesn't matter
			else
				hl.setHlDatetime(hl.getHlDatetime());//seconds doesn't matter
			save(hl);
			}
		});
	}
	
	@Override
	public Serializable save(StockHlData entity) {
		entity.setStockHlDataId((int)super.save(entity));
		cache.put(entity.getStock(), entity);
		return entity.getStockHlDataId();
	}


	@Override
	public LinkedList<StockHlData> findAllEntriesAfter(int stocknum, LocalDateTime startTime) {
		String startTimeStr= startTime.format(formatter);
//		System.out.println("Start time String is "  + startTimeStr);
		Query query = sessionFactory.getCurrentSession().getNamedQuery("StockHlData.findAllEntriesAfter")
				.setInteger("stocknum", stocknum)
				.setTimestamp("startTime", Date.from(startTime.atZone(ZoneId.systemDefault()).toInstant()));
		return  (LinkedList<StockHlData>)ListUtil.getLinkedListInstance(query.list());
	}

	@Override
	public int deleteForAllStock(Stock s) {
		Query query = sessionFactory.getCurrentSession().getNamedQuery("StockHlData.deleteAllForStock")
		.setInteger("stocknum", s.getStocknum());
		cache.remove(s);
		return query.executeUpdate();		
	}
}
