package com.mpakam.dao;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.transaction.Transactional;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.mpakam.model.BacktestStockOrder;
import com.mpakam.model.Stock;
import com.mpakam.model.StockHlData;
import com.mpakam.util.ListUtil;

@Repository
@Transactional 
public class BacktestStockOrderDaoImpl extends AbstractGenericDao<BacktestStockOrder> implements BacktestStockOrderDao  {
	
	@Autowired
	private SessionFactory sessionFactory;

	@Override
	public BacktestStockOrder findOpenOrder(Stock s) {
		Query query = sessionFactory.getCurrentSession().getNamedQuery("BacktestStockOrder.getOpenOrderForStock")
				.setInteger("strategyId", 1)
				.setInteger("stockNum", s.getStocknum());
		List<BacktestStockOrder> list = query.list();
		return  (list!=null && list.size()>0)?list.get(0):null;
	}
	
	@Override
	public BacktestStockOrder findOpenOrder(Stock s, int strategyId) {
		Query query = sessionFactory.getCurrentSession().getNamedQuery("BacktestStockOrder.getOpenOrderForStock")
				.setInteger("strategyId", strategyId)
				.setInteger("stockNum", s.getStocknum());
		List<BacktestStockOrder> list = query.list();
		return  (list!=null && list.size()>0)?list.get(0):null;
	}


	@Override
	public List<BacktestStockOrder> getLatestOrders(LocalDateTime time) {

		Query query = sessionFactory.getCurrentSession().getNamedQuery("BacktestStockOrder.getLatestOrders");
//				.setTimestamp("quoteTime", Date.from(time.atZone(ZoneId.systemDefault()).toInstant()));
		return  (LinkedList<BacktestStockOrder>)ListUtil.getLinkedListInstance(query.list());
	}
	
	@Override
	public Serializable save(BacktestStockOrder entity) {
		entity.setAlerted(0);//0 is not alerts, 1 means alerted.
		return super.save(entity);
	}
	
}
