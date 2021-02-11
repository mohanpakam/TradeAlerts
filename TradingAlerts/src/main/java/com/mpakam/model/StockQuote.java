package com.mpakam.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.patriques.input.timeseries.Interval;

import com.mpakam.util.LocalDateTimeAttributeConvertor;


/**
 * The persistent class for the stock_quotes database table.
 * 
 */
@Entity
@Table(name="stock_quotes")
@NamedQuery(name="StockQuote.findAll", query="SELECT s FROM StockQuote s")
@NamedNativeQueries({ 
	@NamedNativeQuery(name = "StockQuote.findAllByStock",  resultClass=StockQuote.class,
		query = "select sq.* from stock_quotes sq where sq.stocknum = :stockNum and sq.`interval`=:interval order by sq.quote_datetime asc")
	})
public class StockQuote implements Serializable, Comparable<StockQuote> {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="stock_quote_id", unique=true, nullable=false)
	private int stockQuoteId;

	@Column(nullable=false, precision=10, scale=4)
	private BigDecimal close;

	@Column(nullable=false, precision=10, scale=4)
	private BigDecimal high;

	@Column(nullable=false, precision=10, scale=4)
	private BigDecimal low;

	@Column(nullable=false, precision=10, scale=4)
	private BigDecimal open;
	
	@Column(name="`interval`")
	private int interval;

	@Column(name="quote_datetime", nullable=false)
	@Convert(converter = LocalDateTimeAttributeConvertor.class)
	private LocalDateTime quoteDatetime;

	@Column(name="recorded_timestamp", nullable=false)
	@Convert(converter = LocalDateTimeAttributeConvertor.class)
	private LocalDateTime recordedTimestamp;
	
	//bi-directional many-to-one association to Stock
	@ManyToOne
	@JoinColumn(name="stocknum", nullable=false)
	private Stock stock;

	//bi-directional many-to-one association to StrategyStockQuote
	@OneToMany(mappedBy="stockQuote")
	private Set<StrategyStockQuote> strategyStockQuotes;
	
	//bi-directional many-to-one association to RenkoChart
	@OneToMany(mappedBy="stockQuote")
	private Set<RenkoChartBox> renkoCharts;
	
	//bi-directional many-to-one association to TechAnalysisHeikenashi
	@OneToMany(mappedBy="stockQuote")
	private Set<TechAnalysisHeikenashi> techAnalysisHeikenashis;

	public StockQuote() {
	}
	
	public StockQuote(StockQuote sq) {
		this.close= sq.close;
		this.high = sq.high;
		this.low = sq.low;
//		this.open = sq.open; do not set the Open to help in the aggregate StockQuotes lke Weekly
		this.stock = sq.stock;
	}

	public int getStockQuoteId() {
		return this.stockQuoteId;
	}

	public void setStockQuoteId(int stockQuoteId) {
		this.stockQuoteId = stockQuoteId;
	}

	public BigDecimal getClose() {
		return this.close;
	}

	public void setClose(BigDecimal close) {
		this.close = close;
	}

	public BigDecimal getHigh() {
		return this.high;
	}

	public void setHigh(BigDecimal high) {
		this.high = high;
	}

	public BigDecimal getLow() {
		return this.low;
	}

	public void setLow(BigDecimal low) {
		this.low = low;
	}

	public BigDecimal getOpen() {
		return this.open;
	}

	public void setOpen(BigDecimal open) {
		this.open = open;
	}

	
	public LocalDateTime getQuoteDatetime() {
		return this.quoteDatetime;
	}

	public void setQuoteDatetime(LocalDateTime quoteDatetime) {
		this.quoteDatetime = quoteDatetime;
	}

	public Stock getStock() {
		return this.stock;
	}

	public void setStock(Stock stock) {
		this.stock = stock;
	}

	public Set<StrategyStockQuote> getStrategyStockQuotes() {
		return this.strategyStockQuotes;
	}

	public void setStrategyStockQuotes(Set<StrategyStockQuote> strategyStockQuotes) {
		this.strategyStockQuotes = strategyStockQuotes;
	}

	public StrategyStockQuote addStrategyStockQuote(StrategyStockQuote strategyStockQuote) {
		getStrategyStockQuotes().add(strategyStockQuote);
		strategyStockQuote.setStockQuote(this);
		return strategyStockQuote;
	}

	public StrategyStockQuote removeStrategyStockQuote(StrategyStockQuote strategyStockQuote) {
		getStrategyStockQuotes().remove(strategyStockQuote);
		strategyStockQuote.setStockQuote(null);

		return strategyStockQuote;
	}

	@Override
	public int compareTo(StockQuote o) {
		return this.getQuoteDatetime().compareTo(o.getQuoteDatetime());
	}
	
	public int getInterval() {
		return this.interval;
	}
	public void setInterval( int interval) {
		this.interval = interval;
	}
	public Interval getIntervalObj() {
		return Interval.getByTime(this.interval);
	}
	
	public LocalDateTime getRecordedTimestamp() {
		return recordedTimestamp;
	}

	public void setRecordedTimestamp(LocalDateTime recordedTimestamp) {
		this.recordedTimestamp = recordedTimestamp;
	}
	
	public Set<RenkoChartBox> getRenkoCharts() {
		return this.renkoCharts;
	}

	public void setRenkoCharts(Set<RenkoChartBox> renkoCharts) {
		this.renkoCharts = renkoCharts;
	}

	public RenkoChartBox addRenkoChart(RenkoChartBox renkoChart) {
		getRenkoCharts().add(renkoChart);
		renkoChart.setStockQuote(this);

		return renkoChart;
	}

	public RenkoChartBox removeRenkoChart(RenkoChartBox renkoChart) {
		getRenkoCharts().remove(renkoChart);
		renkoChart.setStockQuote(null);

		return renkoChart;
	}

	@Override
	public String toString() {
		return "StockQuote [stockQuoteId=" + stockQuoteId + ", close=" + close + ", high=" + high + ", low=" + low
				+ ", open=" + open + ", interval=" + interval + ", quoteDatetime=" + quoteDatetime
				+ ", recordedTimestamp=" + recordedTimestamp + ", stock=" + stock + ", strategyStockQuotes="
				+ strategyStockQuotes + "]";
	}

	public Set<TechAnalysisHeikenashi> getTechAnalysisHeikenashis() {
		return this.techAnalysisHeikenashis;
	}

	public void setTechAnalysisHeikenashis(Set<TechAnalysisHeikenashi> techAnalysisHeikenashis) {
		this.techAnalysisHeikenashis = techAnalysisHeikenashis;
	}

	public TechAnalysisHeikenashi addTechAnalysisHeikenashi(TechAnalysisHeikenashi techAnalysisHeikenashi) {
		getTechAnalysisHeikenashis().add(techAnalysisHeikenashi);
		techAnalysisHeikenashi.setStockQuote(this);

		return techAnalysisHeikenashi;
	}

	public TechAnalysisHeikenashi removeTechAnalysisHeikenashi(TechAnalysisHeikenashi techAnalysisHeikenashi) {
		getTechAnalysisHeikenashis().remove(techAnalysisHeikenashi);
		techAnalysisHeikenashi.setStockQuote(null);

		return techAnalysisHeikenashi;
	}
	
}