package com.mpakam.dao;

import java.util.List;

import com.mpakam.model.CustomerTickerTracker;


public interface CustomerTickerTrackerDao  extends GenericDao<CustomerTickerTracker>{

	List<CustomerTickerTracker> findByCustomerId(int username);
	Long save(CustomerTickerTracker customerTkr);
	List<CustomerTickerTracker> findByTicker(int stockNum);
	
}
