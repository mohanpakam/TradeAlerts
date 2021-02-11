/**
 * 
 */
package com.mpakam.service;

import java.util.List;
import java.util.TreeSet;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mpakam.dao.StockQuoteDao;
import com.mpakam.model.Stock;
import com.mpakam.model.StockQuote;
import com.mpakam.util.IQuoteDataProviderService;

/**
 * @author LuckyMonaA
 *
 */
@Service
public class StockService implements IStockService {

	@Autowired
	IQuoteDataProviderService dataProviderSvc;
	
	@Autowired
	StockQuoteDao quoteDao;
	
	/* (non-Javadoc)
	 * @see com.mpakam.service.StockQuoteService#syncBySymbol(java.lang.String)
	 */
	@Transactional
	@Override
	public TreeSet<StockQuote> syncBySymbol(Stock symbol) throws Exception {
		
		TreeSet<StockQuote> quoteList =dataProviderSvc.retrieveCandleData(symbol);
		quoteDao.save(quoteList);
		return quoteList;
	}

	/* (non-Javadoc)
	 * @see com.mpakam.service.StockQuoteService#calculateHeikenAshi(java.util.List)
	 */
	@Override
	public void calculateHeikenAshi(List<StockQuote> quotes) {
		// TODO Auto-generated method stub

	}

	@Override
	public void calculateStochRsi(List<StockQuote> quotesList) {
		// TODO Auto-generated method stub
		
	}
	
	

}
