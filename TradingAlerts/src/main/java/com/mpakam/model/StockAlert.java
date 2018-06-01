package com.mpakam.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

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
import javax.persistence.Table;

import com.mpakam.util.LocalDateTimeAttributeConvertor;


/**
 * The persistent class for the stock_alerts database table.
 * 
 */
@Entity
@Table(name="stock_alerts")
@NamedQuery(name="StockAlert.findAll", query="SELECT s FROM StockAlert s")
@NamedNativeQueries({ 
	@NamedNativeQuery(name = "StockAlert.getOpenAlerts",  resultClass=StockAlert.class,
		query = "select * from stock_alerts order by 1 desc limit 100"),
	@NamedNativeQuery(name = "StockAlert.getActiveAlerts",  resultClass=StockAlert.class,
	query = "select * from stock_alerts where monitored!=0 order by 1 desc limit 100"),
	@NamedNativeQuery(name = "StockAlert.getActiveStockAlertByStockNum",  resultClass=StockAlert.class,
	query = "select * from stock_alerts where monitored!=0 and stocknum= :stocknum order by 1 desc limit 100"),
	@NamedNativeQuery(name = "StockAlert.retrieveNewAlerts",  resultClass=StockAlert.class,
	query = "select * from stock_alerts  where alert_status = 0")})
public class StockAlert implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="stock_alert_id", unique=true, nullable=false)
	private int stockAlertId;

	@Column(name="buy_sell_signal")
	private int buySellSignal;

	@Column(name="stock_price", precision=10, scale=4)
	private BigDecimal stockPrice;
	
	@Column(name="monitored")
	private int monitored;
	
	@Column(name = "alert_status")
	private int sentStatus;
	
	@Column(name="sent_timestamp", nullable=false)
	@Convert(converter = LocalDateTimeAttributeConvertor.class)
	private LocalDateTime sentTimeStamp;

	

	//bi-directional many-to-one association to Customer
	@ManyToOne
	@JoinColumn(name="customerid")
	private Customer customer;

	//bi-directional many-to-one association to Stock
	@ManyToOne
	@JoinColumn(name="stocknum", nullable=false)
	private Stock stock;

	//bi-directional many-to-one association to StrategyStockQuote
	@ManyToOne
	@JoinColumn(name="strategy_stock_quote_id")
	private StrategyStockQuote strategyStockQuote;
	
	@ManyToOne
	@JoinColumn(name="stock_quote_id")
	private StockQuote stockQuote;

	public StockAlert() {
	}

	public int getStockAlertId() {
		return this.stockAlertId;
	}

	public void setStockAlertId(int stockAlertId) {
		this.stockAlertId = stockAlertId;
	}

	public int getBuySellSignal() {
		return this.buySellSignal;
	}

	public void setBuySellSignal(int buySellSignal) {
		this.buySellSignal = buySellSignal;
	}

	public BigDecimal getStockPrice() {
		return this.stockPrice;
	}

	public void setStockPrice(BigDecimal stockPrice) {
		this.stockPrice = stockPrice;
	}

	public Customer getCustomer() {
		return this.customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	public Stock getStock() {
		return this.stock;
	}

	public void setStock(Stock stock) {
		this.stock = stock;
	}

	public StrategyStockQuote getStrategyStockQuote() {
		return this.strategyStockQuote;
	}

	public void setStrategyStockQuote(StrategyStockQuote strategyStockQuote) {
		this.strategyStockQuote = strategyStockQuote;
	}
	
	public void setMonitored(int monitored) {
		this.monitored=monitored;
	}
	public boolean isMonitored() {
		return this.monitored!=0;
	}

	public int getSentStatus() {
		return sentStatus;
	}

	public void setSentStatus(int sentStatus) {
		this.sentStatus = sentStatus;
	}

	public LocalDateTime getSentTimeStamp() {
		return sentTimeStamp;
	}

	public void setSentTimeStamp(LocalDateTime sentTimeStamp) {
		this.sentTimeStamp = sentTimeStamp;
	}

	public StockQuote getStockQuote() {
		return stockQuote;
	}

	public void setStockQuote(StockQuote stockQuote) {
		this.stockQuote = stockQuote;
	}
}