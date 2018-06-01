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
 * The persistent class for the stock_hl_data database table.
 * 
 */
@Entity
@Table(name="stock_hl_data")
@NamedQuery(name="StockHlData.findAll", query="SELECT s FROM StockHlData s")
@NamedNativeQueries({ 
	@NamedNativeQuery(name = "StockHlData.findLastByStock",  resultClass=StockHlData.class,
		query = "select * from stock_hl_data where stocknum = :stocknum and `interval` =:interval order by hl_datetime desc limit 1"),
	@NamedNativeQuery(name = "StockHlData.findAllEntriesAfter",  resultClass=StockHlData.class,
	query = "select * from stock_hl_data where stocknum = :stocknum and hl_datetime>:startTime order by hl_datetime asc"),
	@NamedNativeQuery(name = "StockHlData.deleteAllForStock",
	query = "delete from stock_hl_data where stocknum = :stocknum ")
	})
public class StockHlData implements Serializable, Comparable<StockHlData> {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="stock_hl_data_id", unique=true, nullable=false)
	private int stockHlDataId;

	@Column(precision=10, scale=2)
	private BigDecimal high;

	@Column(name="hl_datetime", nullable=false)
	@Convert(converter = LocalDateTimeAttributeConvertor.class)
	private LocalDateTime hlDatetime;
	
	@Column(name="recorded_timestamp", nullable=false)
	@Convert(converter = LocalDateTimeAttributeConvertor.class)
	private LocalDateTime recordedTimestamp;

	@Column(nullable=false, name="`interval`")
	private int interval;

	@Column(precision=10, scale=2)
	private BigDecimal low;
	
	@Column(precision=10, scale=2)
	private BigDecimal open;
	
	@Column(precision=10, scale=2)
	private BigDecimal close;

	//bi-directional many-to-one association to Stock
	@ManyToOne
	@JoinColumn(name="stocknum", nullable=false)
	private Stock stock;

	public StockHlData() {
	}

	public int getStockHlDataId() {
		return this.stockHlDataId;
	}

	public void setStockHlDataId(int stockHlDataId) {
		this.stockHlDataId = stockHlDataId;
	}

	public BigDecimal getHigh() {
		return this.high;
	}

	public void setHigh(BigDecimal high) {
		this.high = high;
	}

	public LocalDateTime getHlDatetime() {
		return this.hlDatetime;
	}

	public void setHlDatetime(LocalDateTime dateTime) {
		this.hlDatetime = dateTime;
	}

	public int getInterval() {
		return this.interval;
	}

	public void setInterval(int interval) {
		this.interval = interval;
	}

	public BigDecimal getLow() {
		return this.low;
	}

	public void setLow(BigDecimal low) {
		this.low = low;
	}

	public Stock getStock() {
		return this.stock;
	}

	public void setStock(Stock stock) {
		this.stock = stock;
	}
	
	public LocalDateTime getRecordedTimestamp() {
		return recordedTimestamp;
	}

	public void setRecordedTimestamp(LocalDateTime recordedTimestamp) {
		this.recordedTimestamp = recordedTimestamp;
	}

	public BigDecimal getOpen() {
		return open;
	}

	public void setOpen(BigDecimal open) {
		this.open = open;
	}

	public BigDecimal getClose() {
		return close;
	}

	public void setClose(BigDecimal close) {
		this.close = close;
	}

	@Override
	public int compareTo(StockHlData o) {
		if(this.getHlDatetime()!=null)
			return this.getHlDatetime().compareTo(o.getHlDatetime());
		else
			return this.getStockHlDataId() - o.getStockHlDataId();
	}

	@Override
	public String toString() {
		return "StockHlData [stockHlDataId=" + stockHlDataId + ", high=" + high + ", hlDatetime=" + hlDatetime
				+ ", recordedTimestamp=" + recordedTimestamp + ", interval=" + interval + ", low=" + low + ", open="
				+ open + ", close=" + close + ", stock=" + stock + "]";
	}

}