package com.mpakam.dao;

import javax.transaction.Transactional;

import com.mpakam.model.Stock;
import com.mpakam.model.Strategy;

public interface StrategyDao {
	@Transactional
	Strategy findBy(int i);
	Long save(Strategy stock);
	void update(Strategy cart);
}
