package com.mpakam.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.mpakam.dao.BacktestStockOrderDao;
import com.mpakam.dao.StockAlertDao;
import com.mpakam.model.BacktestStockOrder;
import com.mpakam.model.StockAlert;
import com.mpakam.model.StockQuote;

@Service
public class EmailService {

	@Autowired
	private JavaMailSender javaMailSender;
	
	@Autowired
	private StockAlertDao saDao;
	
	@Autowired
	private BacktestStockOrderDao backtestDao;

	@Transactional
	public void sendEmail() {
		List<StockAlert> saList = saDao.retrieveNewAlerts();
		StringBuffer content = new StringBuffer();
		for(StockAlert sa:saList) {
			StockQuote currentQuote = sa.getStockQuote();			
			content.append(currentQuote.getQuoteDatetime() + "\t" );
			//+ "Stoch RSI: "+ currentQuote.getStochRsiD() + "\t"
			content.append(((sa.getBuySellSignal() == 1)?"BUY ":"SELL ") +"\t" +currentQuote.getStock().getTicker() + "\t @ "+ currentQuote.getClose().doubleValue()+"\n") ;
			saDao.markAsSentAlert(sa);
		}
		System.out.println(content);
		sendMail("Trade - Alert @ " + LocalDateTime.now(), content.toString(), "mohaneee221@gmail.com");
	}
	
	@Transactional
	public void sendEmail(LocalDateTime time) {
		
		if(time!=null) {
			String content = sendBacktestOrderSummary(time);
			if(content !=null && !content.isEmpty()) {
				System.out.println(content);
				sendMail("Trade - Alert @ " + LocalDateTime.now(), content, "mohaneee221@gmail.com");
			}
		}
	}
	
	public String sendBacktestOrderSummary(LocalDateTime quoteTime) {
		
		StringBuffer closingOrders=new StringBuffer("Closing Orders:\n");
		closingOrders.append("========================\n");
		StringBuffer openingOrders=new StringBuffer("Opening Orders:\n");
		openingOrders.append("========================\n");
		List<BacktestStockOrder> orderList = backtestDao.getLatestOrders(quoteTime);
		if(orderList.size()==0)
			return null;
		orderList.forEach(p->{
//    		StrategyStockQuote currentQuote = p.getStrategyStockQuote();
			LocalDateTime orderQuoteTime = p.getOpenDatetime();
    		BigDecimal stopLoss = p.getStopLossPrice();
    		if(p.getOpenDatetime().equals(quoteTime)) {
    			openingOrders.append(orderQuoteTime + "\t" );
    			//+ "Stoch RSI: "+ currentQuote.getStochRsiD() + "\t"
    			openingOrders.append(p.getOrderType() +"\t" + p.getStock().getTicker() + "\t @ "+ p.getOpenPrice()
    					+"\tStopLoss: "+stopLoss+"\n") ;
    		}else {
    			closingOrders.append(orderQuoteTime + "\t" );
    			boolean stopLossHit =false;
    			//+ "Stoch RSI: "+ currentQuote.getStochRsiD() + "\t"
    			/*if(((p.getOrderType().equals("BTO") && currentQuote.getStockQuote().getClose().compareTo(stopLoss)<=0) || 
    					p.getOrderType().equals("STO") && currentQuote.getStockQuote().getClose().compareTo(stopLoss)>=0)) {
    				stopLossHit = true;
    			}*/
    			if(stopLossHit)
    				closingOrders.append("**STOPLOSS:\t");
    			closingOrders.append((p.getOrderType().equals("BTO")?"STC":"BTC") +"\t" 
    				+p.getStock().getTicker() + "\t @ " 
    					+ p.getClosePrice()
    					+"\tStopLoss: "+stopLoss+"\n") ;
    		}
    		p.setAlerted(1);//Alert sent
//    		System.out.println(p.toString());
    		backtestDao.merge(p);
    	});
		return closingOrders.toString() +"\n"+openingOrders.toString();
	}
	

	private void sendMail(String subject, String text, String emailId) {

		System.out.println("Sending email...");
		
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(emailId);
		message.setFrom("trade.alerts4u@gmail.com");
		message.setSubject(subject);
		message.setText(text);
		try {
			javaMailSender.send(message);
		}catch(Exception e) {
			e.printStackTrace();
		}
		System.out.println("Email Sent!");
	}

}