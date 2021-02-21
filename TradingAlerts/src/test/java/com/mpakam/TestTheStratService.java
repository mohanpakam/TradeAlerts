package com.mpakam;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.mpakam.dao.MonitoredStockDao;
import com.mpakam.model.MonitoredStock;
import com.mpakam.service.TechAnalysisTheStratService;

@SpringBootTest
public class TestTheStratService {
	
	
	@Autowired
	private MonitoredStockDao mStockDao;
	
	@Autowired
	private TechAnalysisTheStratService stratSvc;

	@Test
	public void test_backTestMonitoredStock() {
		List<MonitoredStock> list = mStockDao.retrievegetActivelyMonitoredStocks();
		MonitoredStock ms = list.get(0); // not a good practice.
		stratSvc.backTestMonitoredStock(ms);
	}
}
