package com.mpakam.controller;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.mpakam.dao.CustomerTickerTrackerDao;
import com.mpakam.dao.MonitoredStockDao;
import com.mpakam.dao.RenkoChartDaoImpl;
import com.mpakam.dao.StockAlertDao;
import com.mpakam.dao.StockDao;
import com.mpakam.dao.StockHlDataDao;
import com.mpakam.dao.StockHlDataDaoImpl;
import com.mpakam.dao.StockQuoteDaoImp;
import com.mpakam.dao.StrategyDao;
import com.mpakam.dao.StrategyStockQuoteDao;
import com.mpakam.model.CustomerTickerTracker;
import com.mpakam.model.MonitoredStock;
import com.mpakam.model.Stock;
import com.mpakam.model.StockAlert;
import com.mpakam.model.StrategyStockQuote;
import com.mpakam.scheduler.ScheduledTasks;
import com.mpakam.service.IStockHlDataService;
import com.mpakam.service.IStockQuoteService;
import com.mpakam.service.MonitoredStockService;
import com.mpakam.util.IQuoteDataProviderService;
import com.mpakam.util.StooqHistoryLoaderUtilService;
import com.mpakam.util.UIToolsService;

@Controller
public class StockQuoteController {
	
	@Autowired
	IQuoteDataProviderService quoteProvider;
	
	@Autowired
	IStockQuoteService quoteService;
	
	@Autowired
	StockDao stockDao;
	
	@Autowired
	UIToolsService uiSvc;
	
	@Autowired
	IStockHlDataService hlSvc;
	
	@Autowired
	StrategyDao strategyDao;
	
	@Autowired
	CustomerTickerTrackerDao trackerDao;
	
	@Autowired
	StrategyStockQuoteDao strategyQuotesDao;
	
	@Autowired
	MonitoredStockDao monitorStockDao;
	
	@Autowired
	ScheduledTasks tasks;
	
	@Autowired
	StockAlertDao saDao;
	
	@Autowired
	MonitoredStockService mStockSvc;
	
	@Autowired
	StooqHistoryLoaderUtilService stooqSvc;
	
	@Autowired
	StockHlDataDao hlDao;
	
	@RequestMapping("/")
	String home() {
		return "Hello World!";
	}

	/*@RequestMapping(value="/stockQuote/{symbl}")
	public @ResponseBody ResponseEntity<TreeSet<StockQuote>> retreiveStockQuote(@PathVariable("symbl") int symbolNum) throws Exception{
		Stock stock = stockDao.findBy(symbolNum);
		TreeSet<StockQuote> quoteList =quoteService.syncBySymbol(stock);
		return new ResponseEntity<TreeSet<StockQuote>> (quoteList,HttpStatus.FOUND);
	}

	@RequestMapping(value = "/users/login", method = RequestMethod.POST)
	public @ResponseBody ResponseEntity<Customer> login(@RequestParam("username") String username, @RequestParam("password") String password) 
			throws NoSuchAlgorithmException, AuthenticationFailedException {
		Customer customer = customerService.authentication(username, password);
		return new ResponseEntity<Customer> (customer, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/users", method = RequestMethod.POST)
	public @ResponseBody ResponseEntity<Void> addCustomer(@RequestBody Customer customer, HttpServletRequest request) 
			throws URISyntaxException, NoSuchAlgorithmException {
		Long id = customerService.addCustomer(customer);
		HttpHeaders header = new HttpHeaders();
		header.setLocation(new URI(request.getRequestURL() + "/" + id.toString()));
		return new ResponseEntity<Void>(header, HttpStatus.CREATED);
	}*/
	
	@Transactional //TODO: Not the best place
	@RequestMapping("/heikenAshi")
    public String heikenAshiChart(@RequestParam(value="stockNum", required=true) int custTrackerId,
    		@RequestParam(value="interval", required=true) int interval,
    		Model model) throws Exception {
		
//		Strategy stratagy = strategyDao.findBy(strategyId);
		CustomerTickerTracker custTrkr = trackerDao.findById(custTrackerId);
		
		System.out.println("Cust Tracker : "+ custTrkr);
		System.out.println("Cust Tracker desc: "+ custTrkr.getDescription());
		System.out.println("Cust Trker: "+custTrkr.getStock());
		
		LinkedList<StrategyStockQuote> quoteList = 
				strategyQuotesDao.retrieveQuotesByStockNumStrategyId(custTrkr.getStock(),
						custTrkr.getStrategyId());
		
		model.addAttribute("quotes", uiSvc.convertToHACandleData(quoteList));
		model.addAttribute("rsidata", uiSvc.stochRsiFromHA(quoteList));
		
		String stockName = custTrkr.getStock().getTicker() +" - " + custTrkr.getStock().getStockName(); 

		model.addAttribute("stockName", stockName );
        return "heikenAshiChart";
    }
	
	@Transactional
	@RequestMapping("/stocksTable")
	public String displayStocksTable( @RequestParam(value="customerId", required=true) int customerId, Model model) throws Exception {
		List<CustomerTickerTracker> tracker = (List<CustomerTickerTracker>) trackerDao.findByCustomerId(customerId);
		model.addAttribute("tracker", uiSvc.stocksList(tracker));
		return "customerTickerData";
	}
	
	@Transactional
	@RequestMapping("/monitoredStocks")
	public String displayStocksTable( Model model) throws Exception {
		List<MonitoredStock> mStockList = monitorStockDao.retrievegetActivelyMonitoredStocks();
		model.addAttribute("monitoredStocks", uiSvc.monitoredStockList(mStockList));
		return "monitoredDataTable";
	}
	
	@Transactional
	@RequestMapping("/analyze")
	public String analyzeStock( @RequestParam(value="stockNum", required=true) int stockNum, Model model) throws Exception {
		
		List<MonitoredStock> list = monitorStockDao.retreiveByStockNum(stockNum);
        quoteService.iexAnalyzeStock(list);
        
		return "forward:/heikenAshi?custTrackerId="+stockNum +"&inveral="+list.get(0).getStock().getInterval();
	}
	
	@Transactional
	@RequestMapping("/runSyncJob")
	public String runSyncJob( @RequestParam(value="interval", required=true) int interval, Model model) throws Exception {
		Thread thread = new Thread(){
		    public void run(){
		    	List<MonitoredStock> list =monitorStockDao.retrievegetActivelyMonitoredStocksByTime(interval);
		        try {
					quoteService.iexAnalyzeStock(list);
				} catch (InterruptedException | ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    }
		  };
		  thread.start();
		  return "forward:/stocksTable?customerId="+1;
	}

	@RequestMapping("/sample")
	public String sampleTable() throws Exception {
		return "sampleTable";
	}
	
	@RequestMapping("/runDailySync")
	public String runDailySync() throws Exception {
		Thread thread = new Thread(){
		    public void run(){
		    	try {
					tasks.analyzeDailyAlerts();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    }
		  };
		  thread.start();
		  return "forward:/monitoredStocks";
	}
	
	@RequestMapping("/newHeikenAshi")
    public String hAChart(@RequestParam(value="stockNum", required=true) int stockNum,
    		@RequestParam(value="interval", required=true) int interval,
    		Model model) throws Exception {

		Stock stock = stockDao.findBy(stockNum);
		stock.setInterval(interval);
		LinkedList<StrategyStockQuote> quoteList = 
				strategyQuotesDao.retrieveQuotesByStockNumStrategyId(stock,1);
		
		model.addAttribute("quotes", uiSvc.convertToHACandleData(quoteList));
		model.addAttribute("rsidata", uiSvc.stochRsiFromHA(quoteList));
		
		String stockName = stock.getTicker() +" - " + stock.getStockName(); 
		model.addAttribute("stockName", stockName );
        return "heikenAshiChart";
    }
	
	@Transactional
	@RequestMapping("/listAlerts")
	public String listAlerts(Model model) {
		List<StockAlert> stockAlertList= saDao.getTop100Alerts();
		model.addAttribute("monitoredStocks", uiSvc.stockAlertList(stockAlertList));
		return "alertsTable";
	}
	
	@Transactional
	@RequestMapping("/addStockAlertForMonitor")
	public String addStockAlertForMonitor(@RequestParam(value="alertId", required=true) int alertId) {
		StockAlert sa = saDao.findById(alertId);
		sa.setMonitored(1);
		saDao.saveOrUpdate(sa);
		return "forward:/activeAlerts";
	}
	
	@Transactional
	@RequestMapping("/removeStocAlertkForMonitor")
	public String removeStocAlertkForMonitor(@RequestParam(value="alertId", required=true) int alertId) {
		StockAlert sa = saDao.findById(alertId);
		sa.setMonitored(0);
		saDao.saveOrUpdate(sa);
		return "forward:/listAlerts";
	}
	
	@Transactional
	@RequestMapping("/activeAlerts")
	public String activeAlerts(Model model) {
		List<StockAlert> stockAlertList= saDao.getActiveAlerts();
		model.addAttribute("monitoredStocks", uiSvc.stockAlertList(stockAlertList));
		return "alertsTable";
	}
	
	@RequestMapping("/run15MinIEX")
	public String run15MinIEX() throws InterruptedException, ExecutionException {
		 List<MonitoredStock> list =monitorStockDao.retrievegetActivelyMonitoredStocksByTime(15);
	//   stockQuoteService.analyzeStock(list);
		 quoteService.iexAnalyzeStock(list);

		return "forward:/listAlerts";
	}
	
	@RequestMapping("/syncAllMonitored")
	public String syncAllMonitored() throws InterruptedException, ExecutionException {
		 List<MonitoredStock> list =monitorStockDao.retrievegetActivelyMonitoredStocks();
	//   stockQuoteService.analyzeStock(list);
		 quoteService.iexAnalyzeStock(list);

		return "forward:/listAlerts";
	}

	@RequestMapping("/runDailyJob")
	public String runDailyIEX() throws Exception {
		quoteService.batchDailyAnalyzeStock();
		return "forward:/monitoredStocks";
	}
	
	@Transactional
	@RequestMapping("/runHistoryDailyJob")
	public String runHistroyDailyIEX() throws Exception {
//		quoteService.initialBatchAnalyzeStock();
		List<MonitoredStock> list =monitorStockDao.retrievegetActivelyMonitoredStocks();
    	list.parallelStream().forEach(mStock->{
    		Stock s = mStock.getStock();
    		hlSvc.deleteAllForStock(s);
    		hlSvc.saveAll(s,stooqSvc.parseStockHLDataFromHistory(s));
    	});
    	quoteService.iexAnalyzeStock(list);
		return "forward:/monitoredStocks";
	}
	

	@Transactional
	@RequestMapping("/runHistoryDailyJobForStock")
	public String runHistoryDailyJobForStock( @RequestParam(value="stockNum", required=true) int stockNum, Model model) throws Exception {
		List<MonitoredStock> mStockList = monitorStockDao.retreiveByStockNum(stockNum);
		mStockList.parallelStream().forEach(mStock->{
    		Stock s = mStock.getStock();
    		hlSvc.deleteAllForStock(s);
    		hlSvc.saveAll(s,stooqSvc.parseStockHLDataFromHistory(s));
    	});
    	quoteService.iexAnalyzeStock(mStockList);
		List<CustomerTickerTracker> tickTracker= trackerDao.findByTicker(stockNum);
		return "forward:/heikenAshi?custTrackerId="+stockNum +"&inveral="+tickTracker.get(0).getStock().getInterval();
	}	
	@RequestMapping("/gatherHistoryForMonitoredStocks")
	public String gatherHistoryForMonitoredStocks() throws Exception {
		mStockSvc.loadBatchMonitoredStockHistory();
		return "forward:/monitoredStocks";
	}
	
	@RequestMapping("/getStockHlData")
	public String getStockHlData() throws Exception {
		hlSvc.getStockHlData();
		return "forward:/monitoredStocks";
	}
	
	@RequestMapping("/renkoChartAnalyze")
	public void iexRenkoChartAnalyzeStock() throws Exception {
    	List<MonitoredStock> mStockList = monitorStockDao.retrievegetActivelyMonitoredStocks();
    	
    	mStockList.parallelStream().forEach(mStock->{
    		Stock s = mStock.getStock();
    		hlSvc.deleteAllForStock(s);
    		hlSvc.saveAll(s,stooqSvc.parseStockHLDataFromHistory(s));
    	});
    	quoteService.iexRenkoChartAnalyzeStock(mStockList);
    }
	
	@RequestMapping("/logCache")
	public void logCache() throws Exception {
    	StockHlDataDaoImpl.cache.keySet().forEach(p->{
    		System.out.println(p.getTicker() + " : " + StockHlDataDaoImpl.cache.get(p)) ;
    	});
    }
	@RequestMapping("/clearCache")
	public void clearCache() throws Exception {
    	StockHlDataDaoImpl.cache.clear();
    	StockQuoteDaoImp.quotesCache.clear();
    	RenkoChartDaoImpl.cache.clear();
    }
	
	//TODO: while adding a new stock to monitored list, run a 15 min sync.
	
}