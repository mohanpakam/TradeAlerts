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
import com.mpakam.model.TechAnalysisIchimoku;
import com.mpakam.util.ListUtil;

@Repository
@Transactional
public class TechAnalysisIchimokuDaoImpl extends AbstractGenericDao<TechAnalysisIchimoku> implements  TechAnalysisIchimokuDao {
	
	public static HashMap<Stock, StockHlData> cache = new HashMap<>();
	
	@Autowired
	private SessionFactory sessionFactory;
	
	@Autowired
	EnvironmentConfig eConfig;
	
	private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

}
