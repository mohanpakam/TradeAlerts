package com.mpakam.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mpakam.dao.MonitoredStockDao;
import com.mpakam.dao.StockDao;
import com.mpakam.dao.StockHlDataDao;
import com.mpakam.model.MonitoredStock;
import com.mpakam.model.Stock;
import com.mpakam.model.StockHlData;

@Service
public class StooqHistoryLoaderUtilService {
	
	Map<String, Stock> stockMap =null;

	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	DateTimeFormatter intraDayFormatter = DateTimeFormatter.ofPattern("yyyyMMdd HHmmss");
	
	@Autowired
	StockDao stockDao;
	
	@Autowired
	StockHlDataDao hlDao;
	
	@Autowired
	MonitoredStockDao monitorStockDao;
	
	
	private Function<String, StockHlData> mapToItem = (line) -> {
		  String[] p = line.split(",");// a CSV has comma separated lines
		  StockHlData item = new StockHlData();
		  LocalDateTime now = LocalDateTime.now();
		  if (p[0] != null && p[0].trim().length() > 0) {
		    String dateTimeStr = p[0].trim() +" "+ p[1].trim();
		    LocalDateTime time = LocalDateTime.parse(dateTimeStr, formatter);
		    item.setHlDatetime(time.minusHours(5).minusMinutes(5)); //this is to start with 9:30 instead of 9:35
		    item.setOpen(new BigDecimal(p[2].trim()));
		    item.setHigh(new BigDecimal(p[3].trim()));
		    item.setLow(new BigDecimal(p[4].trim()));
		    item.setClose(new BigDecimal(p[5].trim()));
		    item.setInterval(5);
		    item.setRecordedTimestamp(now);
		  }
		  //more initialization goes here
		  return item;
	};	
	
	private Function<String, StockHlData> mapToItemMap = (line) -> {
		  String[] p = line.split(",");// a CSV has comma separated lines
		  StockHlData item = new StockHlData();
		  LocalDateTime now = LocalDateTime.now();
		  if (p[0] != null && p[0].trim().length() > 0) {
			  String ticker =p[0].trim().substring(0, p[0].trim().length()-3);
			  if(!stockMap.containsKey(ticker)) {
				  return item;
			  }else
				  item.setStock(stockMap.get(ticker));
		    String dateTimeStr = p[2].trim() +" "+ p[3].trim();
		    LocalDateTime time = LocalDateTime.parse(dateTimeStr, intraDayFormatter);
		    item.setHlDatetime(time.minusHours(6));
		    item.setOpen(new BigDecimal(p[4].trim()));
		    item.setHigh(new BigDecimal(p[5].trim()));
		    item.setLow(new BigDecimal(p[6].trim()));
		    item.setClose(new BigDecimal(p[7].trim()));
		    item.setInterval(5);
		    item.setRecordedTimestamp(now);
		  }
		  //more initialization goes here
		  return item;
	};
	
	public List<StockHlData> parseStockHLDataFromHistory(Stock s){
		List<StockHlData> linkedList = processHistoryFile(s.getStooqFilePath());
		return linkedList;
	}
	
	private List<StockHlData> processHistoryFile(String inputFilePath) {
	    List<StockHlData> inputList = new LinkedList<>();
	    try{
	      File inputF = new File(inputFilePath);
	      InputStream inputFS = new FileInputStream(inputF);
	      BufferedReader br = new BufferedReader(new InputStreamReader(inputFS));
	      // skip the header of the csv
	      inputList = br.lines().skip(1).map(mapToItem).collect(Collectors.toList());
	      br.close();
	    } catch (IOException e) {
	      e.printStackTrace();
	    }
	    return inputList ;
	}
	
	public List<StockHlData> processIntraDayFile(String inputFilePath){
		loadStocks();
		List<StockHlData> inputList = new LinkedList<>();
		try {
			File inputF = new File(inputFilePath);
			InputStream inputFS = new FileInputStream(inputF);
			BufferedReader br = new BufferedReader(new InputStreamReader(inputFS));
			// skip the header of the csv
			inputList = br.lines().map(mapToItemMap).filter(m->m.getHlDatetime()!=null).collect(Collectors.toList());
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		inputList.stream().forEach(p->{
			System.out.println("found record "+p.getStock().getTicker()+ ":" + p.getHlDatetime()  );
		});
		return inputList;
	}
	
	private void loadStocks() {
//		if(stockMap == null)
//			stockMap = stockDao.findAll().stream().collect(
//                Collectors.toMap(Stock::getTicker, s->s));
		
		if(stockMap == null) {
			stockMap= new HashMap<>();
			stockMap.put("AAPL", stockDao.findBySymbol("AAPL"));
		}
	}

}
