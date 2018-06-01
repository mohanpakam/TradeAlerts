package com.mpakam.scheduler;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.IntStream;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.mpakam.dao.CustomerTickerTrackerDao;
import com.mpakam.dao.MonitoredStockDao;
import com.mpakam.dao.StockDao;
import com.mpakam.dao.StockQuoteDao;
import com.mpakam.model.MonitoredStock;
import com.mpakam.service.EmailService;
import com.mpakam.service.IStockHlDataService;
import com.mpakam.service.IStockQuoteService;
import com.mpakam.service.IStockTickDataService;

@Component
public class ScheduledTasks {
	
    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    
    @Autowired
    CustomerTickerTrackerDao customerTkrDao;
    
    @Autowired
    IStockQuoteService stockQuoteService;
    
    @Autowired
    StockDao stockDao;
    
    @Autowired
    MonitoredStockDao monitorStockDao;
    
    @Autowired
    IStockTickDataService tickerService;
    
    @Autowired
    IStockHlDataService hlService;
    
    @Autowired
    StockQuoteDao quoteDao;
    
    @Autowired
    EmailService emailSvc;
    
    @Scheduled(cron="0 * 9-16 * * MON-FRI")
    public void analyzeMinAlerts() throws Exception {
    	LocalDateTime tmeNow = LocalDateTime.now();
    	LocalDateTime tmeNowZeroSec = tmeNow.withSecond(0).withNano(0);
        log.info("The time is now {}" + tmeNow);
        tickerService.saveCurrentPriceForAllMonitoredStocks();
        
        /*analyzeRenkoChart(tmeNowZeroSec.minusMinutes(1),1);

        if(tmeNowZeroSec.getMinute() %15 == 0) { // every 15min
        	LocalDateTime quoteTime=tmeNowZeroSec.minusMinutes(15);
        	log.debug("START - Fifteen minutes kicker " + quoteTime);
        	analyzeRenkoChart(quoteTime,15);
        	log.debug("END - Fifteen minutes kicker " + quoteTime);
        }
        if(tmeNowZeroSec.getMinute() % 5== 0) { // every 5min
        	LocalDateTime quoteTime=tmeNowZeroSec.minusMinutes(5);
        	log.debug("START - Five minutes kicker " + quoteTime);
        	analyzeRenkoChart(quoteTime,5);
        	log.debug("END - Five minutes kicker " + quoteTime);
        }
        */
        if(tmeNowZeroSec.getMinute() % 5== 0) { // every 5min
        	LocalDateTime quoteTime=tmeNowZeroSec.minusMinutes(5);
        	log.debug("START - Five minutes kicker " + quoteTime);
        	analyze5MinAlerts(quoteTime);
        	log.debug("END - Five minutes kicker " + quoteTime);
        }
        
        
    }

//    @Scheduled(cron="30 0/5 9-17 * * MON-FRI") //Removed as we dont have to calculate this every 5 min.
    public void analyze5MinAlerts(LocalDateTime quoteTime) throws Exception {
    	log.info("5 minute ticker running", dateFormat.format(new Date()));
    	try {
			hlService.getStockHlData();
			List<MonitoredStock> list =monitorStockDao.retrievegetActivelyMonitoredStocksByTime(5);
	        stockQuoteService.iexAnalyzeStock(list);
	        if(list.size()>0)
 	        	emailSvc.sendEmail(quoteTime);
		} catch (Exception e) {
			e.printStackTrace();
		}
    	log.info("5 minute ticker finished", dateFormat.format(new Date()));
    }
    
//    @Scheduled(cron="0 1,16,31,46 9-17  * * MON-FRI")
    public void analyze15MinAlerts(){
        log.info("START Every 15 min job - The time is now {}", dateFormat.format(new Date()));
        try {
			hlService.getStockHlData();
			List<MonitoredStock> list =monitorStockDao.retrievegetActivelyMonitoredStocksByTime(15);
	        stockQuoteService.iexAnalyzeStock(list);
		} catch (Exception e) {
			e.printStackTrace();
		}
        log.info("END Every 15 min job - The time is now {}", dateFormat.format(new Date()));
    }
    
    @Transactional
//    @Scheduled(cron="0 1/31 9-17 * * MON-FRI")
    public void analyze30MinAlerts() throws InterruptedException, ExecutionException {
    	List<MonitoredStock> list =monitorStockDao.retrievegetActivelyMonitoredStocksByTime(30);
        stockQuoteService.analyzeStock(list);
    }
    
    //@Scheduled(cron="0 33 10-15 * * MON-FRI") //from 10:33 AM to 15:33 PM JOB 
    public void analyze60MinAlerts() throws InterruptedException, ExecutionException {
        log.info("Every hour job - The time is now {}", dateFormat.format(new Date()));
        List<MonitoredStock> list =monitorStockDao.retrievegetActivelyMonitoredStocksByTime(60);
        stockQuoteService.iexAnalyzeStock(list);
    }
    
   // @Scheduled(cron="0 3 16 * * MON-FRI") //@ 4:03 PM ET
    public void analyze60MinAlerts2() throws InterruptedException, ExecutionException {
        log.info("Every hour job - The time is now {}", dateFormat.format(new Date()));
        List<MonitoredStock> list =monitorStockDao.retrievegetActivelyMonitoredStocksByTime(60);
        stockQuoteService.iexAnalyzeStock(list);
    }
   
    //@Scheduled(cron = "0 20 16 * * MON-FRI") // @ 4:20 PM ET, run a daily analyze job
    public void analyzeDailyAlerts() throws Exception {
        log.info("The time is now {}", dateFormat.format(new Date()));
//        stockQuoteService.analyzeDailyStocks();
        quoteDao.cleanUp();
        stockQuoteService.batchDailyAnalyzeStock();
    }
    
//    @Scheduled(cron="30 0,15,30,45 9-17  * * MON-FRI") //Removed as we dont have to calculate this every 5 min.
    public void analyzeRenkoChart(LocalDateTime time, int interval ) throws Exception {
    	log.info( interval+ " minute ticker running", dateFormat.format(new Date()));
    	try {
			hlService.getStockHlData();
			List<MonitoredStock> list =monitorStockDao.retrievegetActivelyMonitoredStocksByTime(interval);
	        stockQuoteService.iexRenkoChartAnalyzeStock(list);
	        if(list.size()>0)
	        	emailSvc.sendEmail();
		} catch (Exception e) {
			e.printStackTrace();
		}
    	log.info(interval+ " minute ticker finished", dateFormat.format(new Date()));
    }
}