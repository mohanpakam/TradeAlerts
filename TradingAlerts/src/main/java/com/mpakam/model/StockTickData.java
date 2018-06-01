package com.mpakam.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

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
 * The persistent class for the stock_tick_data database table.
 * 
 */
@Entity
@Table(name="stock_tick_data")
@NamedQuery(name="StockTickData.findAll", query="SELECT s FROM StockTickData s")
@NamedNativeQueries({ 
	@NamedNativeQuery(name = "StockTickData.findCurrentSessionByStockNum",  resultClass=StockTickData.class,
		query = "select * from stock_tick_data where stocknum = :stocknum and tick_datetime >= :todaySessionStart  order by tick_datetime asc")
	})
public class StockTickData implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="stock_tick_data_id", unique=true, nullable=false)
	private int stockTickDataId;

	@Column(precision=10, scale=2)
	private BigDecimal price;

	@Column(name="tick_datetime", nullable=false)
	@Convert(converter = LocalDateTimeAttributeConvertor.class)
	private LocalDateTime tickDatetime;

	
	//bi-directional many-to-one association to Stock
	@ManyToOne
	@JoinColumn(name="stocknum", nullable=false)
	private Stock stock;

	public StockTickData() {
	}

	public int getStockTickDataId() {
		return this.stockTickDataId;
	}

	public void setStockTickDataId(int stockTickDataId) {
		this.stockTickDataId = stockTickDataId;
	}

	public BigDecimal getPrice() {
		return this.price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public LocalDateTime getTickDatetime() {
		return this.tickDatetime;
	}

	public void setTickDatetime(LocalDateTime tickDatetime) {
		this.tickDatetime = tickDatetime;
	}

	public Stock getStock() {
		return this.stock;
	}

	public void setStock(Stock stock) {
		this.stock = stock;
	}

}