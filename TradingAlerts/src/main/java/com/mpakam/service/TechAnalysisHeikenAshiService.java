package com.mpakam.service;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mpakam.dao.StockDao;
import com.mpakam.dao.StockQuoteDao;
import com.mpakam.dao.TechAnalysisAtrDao;
import com.mpakam.model.StockQuote;
import com.mpakam.model.TechAnalysisAtr;

@Service
public class TechAnalysisHeikenAshiService {
	
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	StockDao stockDao;
	
	@Autowired
	TechAnalysisAtrDao atrDao;
	
	@Autowired
	StockQuoteDao stockQuoteDao;

	public TechAnalysisAtr calculateATR(StockQuote currentQuote) throws Exception {

		BigDecimal trueRange = null;
		
		TechAnalysisAtr lastAddedAtr=null;
		
		LinkedList<TechAnalysisAtr> currentAtr = atrDao.retrieveAtrByStockQuote(currentQuote);
		if(currentAtr.size()>0) {
			return currentAtr.get(currentAtr.size()-1); 
		}

		TechAnalysisAtr prevAtr = atrDao.retrieveLastByStockNum(currentQuote.getStock());

		if (prevAtr !=null ) {
			// if(quotes.size()>0)
			trueRange = calculateTR(currentQuote, prevAtr.getStockQuote()); // TODO: This needs to be modified as welel.
			TechAnalysisAtr atr = new TechAnalysisAtr();
			atr.setTrueRange(trueRange);
			atr.setStockQuote(currentQuote);
			atr.setAverageTrueRange(
					new BigDecimal((prevAtr.getAverageTrueRange().doubleValue() * 13 + trueRange.doubleValue()) / 14));
			
			atr.setAtrId((int)atrDao.save(atr));
			lastAddedAtr=atr;
		} else {
			// Initial ATR Calculation
			TreeSet<StockQuote> quoteTreeset = new TreeSet();
			// http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:average_true_range_atr
			StockQuote cQuote = null;
			StockQuote pQuote = null;

			BigDecimal[] trueRangeList = new BigDecimal[14];
			int sqCounter = 0;
			double prevAtrD = 0;

			List<StockQuote> quotes = stockQuoteDao.findAllByStock(currentQuote.getStock());

			for (StockQuote sq : quotes) {
				cQuote = sq;
				double averageTrueRange = 0;
				if (pQuote == null) {
					pQuote = sq;
					trueRange = sq.getHigh().subtract(sq.getLow());
				} else {
					trueRange = calculateTR(cQuote, pQuote);
					pQuote = cQuote;
				}
				if (sqCounter == 13) { // 14th recursion.
					// Calculate the ATR for the 1st time.
					BigDecimal allTrValue = BigDecimal.ZERO;
					for (BigDecimal bd : trueRangeList) {
						if (bd != null)
							allTrValue = allTrValue.add(bd.abs());
					}
					averageTrueRange = allTrValue.doubleValue() / 14;
				} else if (sqCounter < 14) {
					trueRangeList[sqCounter] = trueRange;
				} else { // sqCounter=14 or above
					// Calculate the ATR
					// Current ATR = [(Prior ATR x 13) + Current TR] / 14
					// averageTrueRange=((prevAtr.multiply(new
					// BigDecimal(13))).add(trueRange)).divide(new BigDecimal(14));
					averageTrueRange = (prevAtrD * 13 + trueRange.doubleValue()) / 14;
				}
				TechAnalysisAtr atr = new TechAnalysisAtr();
				atr.setTrueRange(trueRange);
				atr.setStockQuote(sq);
				atr.setAverageTrueRange(new BigDecimal(averageTrueRange));
				prevAtrD = averageTrueRange;
				atr.setAtrId((int)atrDao.save(atr));
				lastAddedAtr=atr;
				sqCounter++;
			}
		}
		return lastAddedAtr;
	}

	private BigDecimal calculateTR(StockQuote cQuote, StockQuote pQuote) {
		BigDecimal hl = cQuote.getHigh().subtract(cQuote.getLow()).abs();
		BigDecimal hCp = cQuote.getHigh().subtract(pQuote.getClose()).abs();
		BigDecimal lCp = cQuote.getLow().subtract(pQuote.getClose()).abs();
		BigDecimal largestTR = null;
		if (hl.compareTo(hCp) >= 0) {
			largestTR = hl;
		} else
			largestTR = hCp;

		if (lCp.compareTo(largestTR) > 0)
			largestTR = lCp;
		return largestTR;
	}
}
