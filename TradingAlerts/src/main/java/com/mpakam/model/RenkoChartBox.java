package com.mpakam.model;

import java.io.Serializable;
import javax.persistence.*;

import com.mpakam.util.LocalDateTimeAttributeConvertor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;


/**
 * The persistent class for the renko_charts database table.
 * 
 */
@Entity
@Table(name="renko_charts")
@NamedQuery(name="RenkoChartBox.findAll", query="SELECT r FROM RenkoChartBox r")
@NamedNativeQueries({ 
	@NamedNativeQuery(name = "RenkoChartBox.findLastByStocknum",  resultClass=RenkoChartBox.class,
		query = "select ro.* from renko_charts ro join stock_quotes sq on (ro.stock_quote_id = sq.stock_quote_id) where ro.stocknum = :stocknum order by quote_datetime desc limit 1"),
	@NamedNativeQuery(name = "RenkoChartBox.findAllByStocknum",  resultClass=RenkoChartBox.class,
	query = "select ro.* from renko_charts ro join stock_quotes sq on (ro.stock_quote_id = sq.stock_quote_id) where ro.stocknum = :stocknum order by quote_datetime asc")})
public class RenkoChartBox implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="renko_box_id", unique=true, nullable=false)
	private int renkoBoxId;

	@Column(name="close_price", precision=10, scale=2)
	private BigDecimal closePrice;

	@Column(name="ema_14_price", precision=10, scale=2)
	private BigDecimal ema14Price;

	@Column(name="entry_datetime")
	@Convert(converter = LocalDateTimeAttributeConvertor.class)
	private LocalDateTime entryDatetime;

	@Column(name="open_price", precision=10, scale=2)
	private BigDecimal openPrice;

	//bi-directional many-to-one association to StockQuote
	@ManyToOne
	@JoinColumn(name="stock_quote_id", nullable=false)
	private StockQuote stockQuote;

	//bi-directional many-to-one association to Stock
	@ManyToOne
	@JoinColumn(name="stocknum", nullable=false)
	private Stock stock;

	public RenkoChartBox() {
	}

	public int getRenkoBoxId() {
		return this.renkoBoxId;
	}

	public void setRenkoBoxId(int renkoBoxId) {
		this.renkoBoxId = renkoBoxId;
	}

	public BigDecimal getClosePrice() {
		return this.closePrice;
	}

	public void setClosePrice(BigDecimal closePrice) {
		this.closePrice = closePrice;
	}

	public BigDecimal getEma14Price() {
		return this.ema14Price;
	}

	public void setEma14Price(BigDecimal ema14Price) {
		this.ema14Price = ema14Price;
	}

	public LocalDateTime getEntryDatetime() {
		return this.entryDatetime;
	}

	public void setEntryDatetime(LocalDateTime localDateTime) {
		this.entryDatetime = localDateTime;
	}

	public BigDecimal getOpenPrice() {
		return this.openPrice;
	}

	public void setOpenPrice(BigDecimal openPrice) {
		this.openPrice = openPrice;
	}

	public StockQuote getStockQuote() {
		return this.stockQuote;
	}

	public void setStockQuote(StockQuote stockQuote) {
		this.stockQuote = stockQuote;
	}

	public Stock getStock() {
		return this.stock;
	}

	public void setStock(Stock stock) {
		this.stock = stock;
	}

	@Override
	public String toString() {
		return "RenkoChartBox [renkoBoxId=" + renkoBoxId + ", closePrice=" + closePrice + ", ema14Price=" + ema14Price
				+ ", entryDatetime=" + entryDatetime + ", openPrice=" + openPrice + ", stockQuote=" + stockQuote
				+ ", stock=" + stock + "]";
	}
	

}