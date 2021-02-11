package com.mpakam;

import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.mpakam.service.IMonitoredStockService;

@SpringBootTest
public class TestMonitoredStockService {

	@Autowired
	IMonitoredStockService mStockSvc;
	
	@Test
	public void test_loadMonitoredStockHistory() {
		try {
			mStockSvc.loadMonitoredStockHistory();
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
