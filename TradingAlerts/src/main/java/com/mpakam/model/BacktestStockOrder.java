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
 * The persistent class for the backtest_stock_orders database table.
 * 
 */
@Entity
@Table(name="backtest_stock_orders")
@NamedQuery(name="BacktestStockOrder.findAll", query="SELECT b FROM BacktestStockOrder b")
@NamedNativeQueries({ 
	@NamedNativeQuery(name = "BacktestStockOrder.getOpenOrderForStock",  resultClass=BacktestStockOrder.class,
		query = "select * from backtest_stock_orders where stocknum=:stockNum and close_datetime is null and strategy_id=:strategyId order by 1 desc limit 1"),
	@NamedNativeQuery(name = "BacktestStockOrder.getLatestOrders",  resultClass=BacktestStockOrder.class,
	query = "select s.*,bso.* from backtest_stock_orders bso join stocks s on (s.stocknum = bso.stocknum) " + 
			"where alerted=0 " + 
			" order by stoploss_price desc")})
public class BacktestStockOrder implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="backtest_stock_order_id", unique=true, nullable=false)
	private int backtestStockOrderId;

	@Column(name="close_datetime")
	@Convert(converter = LocalDateTimeAttributeConvertor.class)
	private LocalDateTime closeDatetime;

	@Column(name="close_price", precision=10, scale=2)
	private BigDecimal closePrice;

	@Column(name="entry_datetime")
	@Convert(converter = LocalDateTimeAttributeConvertor.class)
	private LocalDateTime entryDatetime;

	@Column(name="open_datetime")
	@Convert(converter = LocalDateTimeAttributeConvertor.class)
	private LocalDateTime openDatetime;

	@Column(name="open_price", precision=10, scale=2)
	private BigDecimal openPrice;

	@Column(name="profit_loss", precision=10, scale=2)
	private BigDecimal profitLoss;
	
	@Column(name="order_type")
	private String orderType;
	
	@Column(name="stoploss_price", precision=10, scale=2)
	private BigDecimal stopLossPrice;
	
	@Column(name="strategy_id")
	private int strategyId;
	
	@Column(name="alerted")
	private int alerted;

	//bi-directional many-to-one association to Stock
	@ManyToOne
	@JoinColumn(name="stocknum", nullable=false)
	private Stock stock;


	public BacktestStockOrder() {
	}

	public int getBacktestStockOrderId() {
		return this.backtestStockOrderId;
	}

	public void setBacktestStockOrderId(int backtestStockOrderId) {
		this.backtestStockOrderId = backtestStockOrderId;
	}

	public LocalDateTime getCloseDatetime() {
		return this.closeDatetime;
	}

	public void setCloseDatetime(LocalDateTime closeDatetime) {
		this.closeDatetime = closeDatetime;
	}

	public BigDecimal getClosePrice() {
		return this.closePrice;
	}

	public void setClosePrice(BigDecimal closePrice) {
		this.closePrice = closePrice;
	}

	public LocalDateTime getEntryDatetime() {
		return this.entryDatetime;
	}

	public void setEntryDatetime(LocalDateTime entryDatetime) {
		this.entryDatetime = entryDatetime;
	}

	public LocalDateTime getOpenDatetime() {
		return this.openDatetime;
	}

	public void setOpenDatetime(LocalDateTime openDatetime) {
		this.openDatetime = openDatetime;
	}

	public BigDecimal getOpenPrice() {
		return this.openPrice;
	}

	public void setOpenPrice(BigDecimal openPrice) {
		this.openPrice = openPrice;
	}

	public BigDecimal getProfitLoss() {
		return this.profitLoss;
	}

	public void setProfitLoss(BigDecimal profitLoss) {
		this.profitLoss = profitLoss;
	}

	public Stock getStock() {
		return this.stock;
	}

	public void setStock(Stock stock) {
		this.stock = stock;
	}

	public String getOrderType() {
		return orderType;
	}

	public void setOrderType(String orderType) {
		this.orderType = orderType;
	}

	public BigDecimal getStopLossPrice() {
		return stopLossPrice;
	}

	public void setStopLossPrice(BigDecimal stopLossPrice) {
		this.stopLossPrice = stopLossPrice;
	}
	
	public int getStrategyId() {
		return strategyId;
	}

	public void setStrategyId(int strategyId) {
		this.strategyId = strategyId;
	}

	public int getAlerted() {
		return alerted;
	}

	public void setAlerted(int alerted) {
		this.alerted = alerted;
	}

	@Override
	public String toString() {
		return "BacktestStockOrder [backtestStockOrderId=" + backtestStockOrderId + ", closeDatetime=" + closeDatetime
				+ ", closePrice=" + closePrice + ", entryDatetime=" + entryDatetime + ", openDatetime=" + openDatetime
				+ ", openPrice=" + openPrice + ", profitLoss=" + profitLoss + ", orderType=" + orderType
				+ ", stopLossPrice=" + stopLossPrice + ", stock=" + stock 
				+ "]";
	}


}