package com.mpakam.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mpakam.dao.StockAlertDao;
import com.mpakam.model.StockAlert;
import com.mpakam.model.StockQuote;

@Service
public class StockAlertService {
	
	@Autowired
	StockAlertDao stockAlertDao;
	
	public void createAlert(StockQuote currentQuote, int buyOrSell) {
		StockAlert sq = new StockAlert();		
		sq.setBuySellSignal(buyOrSell);//1= Buy; -1= SELL
//		sq.setCustomer(c);
		sq.setStock(currentQuote.getStock());
		sq.setStockQuote(currentQuote);
		sq.setStockPrice(currentQuote.getClose());
		sq.setMonitored(0);
		stockAlertDao.save(sq);
	}
}
