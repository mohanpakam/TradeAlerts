package com.mpakam.model;

import java.io.Serializable;
import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Set;


/**
 * The persistent class for the stocks database table.
 * 
 */
@Entity
@Table(name="stocks")
@NamedQuery(name="Stock.findAll", query="SELECT s FROM Stock s")
public class Stock implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(unique=true, nullable=false)
	private int stocknum;

	@Column(name="highest_high", precision=10, scale=2)
	private BigDecimal highestHigh;

	@Column(nullable=false, name="`interval`")
	private int interval;

	@Column(name="lowest_low", precision=10, scale=2)
	private BigDecimal lowestLow;

	@Column(name="renko_box_size", precision=10, scale=2)
	private BigDecimal renkoBoxSize;

	@Column(name="stock_name", nullable=false, length=256)
	private String stockName;

	@Column(name="stooq_file_path", length=960)
	private String stooqFilePath;

	@Column(nullable=false, length=20)
	private String ticker;

	private int trend;

	//bi-directional many-to-one association to RenkoChart
	@OneToMany(mappedBy="stock")
	private Set<RenkoChartBox> renkoCharts;

	//bi-directional many-to-one association to StockQuote
	@OneToMany(mappedBy="stock")
	private Set<StockQuote> stockQuotes;

	public Stock() {
	}

	public int getStocknum() {
		return this.stocknum;
	}

	public void setStocknum(int stocknum) {
		this.stocknum = stocknum;
	}

	public BigDecimal getHighestHigh() {
		return this.highestHigh;
	}

	public void setHighestHigh(BigDecimal highestHigh) {
		this.highestHigh = highestHigh;
	}

	public int getInterval() {
		return this.interval;
	}

	public void setInterval(int interval) {
		this.interval = interval;
	}

	public BigDecimal getLowestLow() {
		return this.lowestLow;
	}

	public void setLowestLow(BigDecimal lowestLow) {
		this.lowestLow = lowestLow;
	}

	public BigDecimal getRenkoBoxSize() {
		return this.renkoBoxSize;
	}

	public void setRenkoBoxSize(BigDecimal renkoBoxSize) {
		this.renkoBoxSize = renkoBoxSize;
	}

	public String getStockName() {
		return this.stockName;
	}

	public void setStockName(String stockName) {
		this.stockName = stockName;
	}

	public String getStooqFilePath() {
		return this.stooqFilePath;
	}

	public void setStooqFilePath(String stooqFilePath) {
		this.stooqFilePath = stooqFilePath;
	}

	public String getTicker() {
		return this.ticker;
	}

	public void setTicker(String ticker) {
		this.ticker = ticker;
	}

	public int getTrend() {
		return this.trend;
	}

	public void setTrend(int trend) {
		this.trend = trend;
	}

	public Set<RenkoChartBox> getRenkoCharts() {
		return this.renkoCharts;
	}

	public void setRenkoCharts(Set<RenkoChartBox> renkoCharts) {
		this.renkoCharts = renkoCharts;
	}

	public RenkoChartBox addRenkoChart(RenkoChartBox renkoChart) {
		getRenkoCharts().add(renkoChart);
		renkoChart.setStock(this);

		return renkoChart;
	}

	public RenkoChartBox removeRenkoChart(RenkoChartBox renkoChart) {
		getRenkoCharts().remove(renkoChart);
		renkoChart.setStock(null);

		return renkoChart;
	}

	public Set<StockQuote> getStockQuotes() {
		return this.stockQuotes;
	}

	public void setStockQuotes(Set<StockQuote> stockQuotes) {
		this.stockQuotes = stockQuotes;
	}

	public StockQuote addStockQuote(StockQuote stockQuote) {
		getStockQuotes().add(stockQuote);
		stockQuote.setStock(this);
		return stockQuote;
	}

	public StockQuote removeStockQuote(StockQuote stockQuote) {
		getStockQuotes().remove(stockQuote);
		stockQuote.setStock(null);

		return stockQuote;
	}
	
	@Override
	public int hashCode() {
		return this.ticker.hashCode() + this.interval;
	}
	
	@Override
	public boolean equals(Object o) {
		Stock s=(Stock)o;
		return s.getTicker().equals(this.getTicker()) && s.getInterval() == this.getInterval();
	}

}