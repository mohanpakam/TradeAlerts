package com.mpakam.util;

import java.util.List;
import java.util.TreeSet;

import com.mpakam.model.Stock;
import com.mpakam.model.StockQuote;

public interface IQuoteDataProviderService {

	/*
	 * lookup Quotes by Symbol
	 */

	TreeSet<StockQuote> retrieveCandleData(Stock stockObj) throws Exception;

	TreeSet<StockQuote> retrieveDailyCandleData(Stock stockObj) throws Exception;

	TreeSet<StockQuote> retrieveFailProofCandleData(Stock stockObj) throws Exception;
	
	
}
