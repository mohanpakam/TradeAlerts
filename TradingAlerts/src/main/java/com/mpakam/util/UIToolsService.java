package com.mpakam.util;

import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.mpakam.model.CustomerTickerTracker;
import com.mpakam.model.MonitoredStock;
import com.mpakam.model.StockAlert;
import com.mpakam.model.StrategyStockQuote;

@Service
public class UIToolsService {

	public Object[][] convertToHACandleData(LinkedList<StrategyStockQuote> quotes){
		Object[][] returnData = new Object[quotes.size()][5];
		int i=0;
		for(StrategyStockQuote quote:quotes) {
			returnData[i][0]=quote.getStockQuote().getQuoteDatetime().toString();
			returnData[i][1]=quote.getXlow();
			returnData[i][2]=quote.getXopen();
			returnData[i][3]=quote.getXclose();
			returnData[i][4]=quote.getXhigh();
			i++;
		}
		return returnData;
	}
	
	public Object[][] stochRsiFromHA(LinkedList<StrategyStockQuote> quotes){
		Object[][] returnData = new Object[quotes.size()][3];
		int i=0;
		for(StrategyStockQuote quote:quotes) {
			returnData[i][0]=quote.getStockQuote().getQuoteDatetime().toString();
			returnData[i][1]=(quote.getStochRsiK()==null)?0:quote.getStochRsiK();
			returnData[i][2]=quote.getStochRsiD()==null?0:quote.getStochRsiD();
			i++;
		}
		return returnData;
	}
	
	public Object[][] stocksList(List<CustomerTickerTracker> trackerList){
		Object[][] returnData = new Object[trackerList.size()][4];
		int i=0;
		for(CustomerTickerTracker ticker: trackerList) {
			returnData[i][0]=ticker.getStock().getTicker();
			returnData[i][1]=ticker.getStock().getStockName();
//			returnData[i][2]="<a href=\""+"/heikenAshi?custTrackerId="+ticker.getQuoteTickId()+"\">Chart</a>";
			returnData[i][2]="<a href=\""+"/heikenAshi?custTrackerId="+ticker.getStock().getStocknum()+"&interval="+ticker.getStock().getInterval()+"\">IntraDay Chart</a>";
//			returnData[i][3]="<a href=\""+"/analyze?stockNum="+ticker.getStock().getStocknum()+"\">Sync Now</a>";
			returnData[i][3]="<a href=\""+"/analyze?stockNum="+ticker.getStock().getStocknum()+"&interval="+ticker.getStock().getInterval()+"\">Sync Now</a>";
			
			i++;
		}
		return returnData;
	}
	
	public Object[][] monitoredStockList(List<MonitoredStock> mStockList){
		Object[][] returnData = new Object[mStockList.size()][8];
		int i=0;
		for(MonitoredStock mStock: mStockList) {
			returnData[i][0]=mStock.getStock().getTicker();
			returnData[i][1]=mStock.getStock().getStockName();
			returnData[i][2]=mStock.getAddedDate();
			returnData[i][3]=mStock.getAddedBy()==0?"SYSTEM":"MANUAL";
			returnData[i][4]="<a href=\""+"/newHeikenAshi?stockNum="+mStock.getStock().getStocknum()+"&interval=480\">Daily Chart</a>";
			returnData[i][5]="<a href=\""+"/newHeikenAshi?stockNum="+mStock.getStock().getStocknum()+"&interval="+mStock.getInterval()+"\">IntraDay Chart</a>";
			returnData[i][6]="<a href=\""+"/analyze?stockNum="+mStock.getStock().getStocknum()+"&interval="+mStock.getInterval()+"\">Sync Now</a>";
			returnData[i][7]=mStock.getTrennd()==1?"BULLISH":"BEARISH";
//			System.out.println(returnData[i]);
			i++;
		}
		return returnData;
	}
	
	public Object[][] stockAlertList(List<StockAlert> saList){
		Object[][] returnData = new Object[saList.size()][9];
		int i=0;
		for(StockAlert stockAlert: saList) {
			returnData[i][0]=stockAlert.getStock().getTicker();
			returnData[i][1]=stockAlert.getStock().getStockName();
			returnData[i][2]=stockAlert.getStrategyStockQuote().getStockQuote().getQuoteDatetime().toString();
			int interval=stockAlert.getStrategyStockQuote().getStockQuote().getInterval();
			returnData[i][3]=(interval == 480)?"DAILY":interval+"min";
			returnData[i][4]=stockAlert.getBuySellSignal() == 1? "BUY":"SELL";
			if(!stockAlert.isMonitored())
				returnData[i][5]="<a href=\""+"/addStockAlertForMonitor?alertId="+stockAlert.getStockAlertId()+"\">Monitor</a>";
			else
				returnData[i][5]="<a href=\""+"/removeStocAlertkForMonitor?alertId="+stockAlert.getStockAlertId()+"\">Ignore</a>";
			returnData[i][6]="<a href=\""+"/newHeikenAshi?stockNum="+stockAlert.getStock().getStocknum()+"&interval=480\">Daily Chart</a>";
			returnData[i][7]="<a href=\""+"/newHeikenAshi?stockNum="+stockAlert.getStock().getStocknum()+"&interval="+stockAlert.getStrategyStockQuote().getStockQuote().getInterval()+"\">IntraDay Chart</a>";
			returnData[i][8]="<a href=\""+"/analyze?stockNum="+stockAlert.getStock().getStocknum()+"&interval="+stockAlert.getStock().getInterval()+"\">Sync Now</a>";
//			System.out.println(returnData[i]);
			i++;
		}
		return returnData;
	}
	
	
}
