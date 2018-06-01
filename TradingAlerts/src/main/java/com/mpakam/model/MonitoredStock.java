package com.mpakam.model;

import java.io.Serializable;
import javax.persistence.*;
import java.util.Date;


/**
 * The persistent class for the monitored_stocks database table.
 * 
 */
@Entity
@Table(name="monitored_stocks")
@NamedQuery(name="MonitoredStock.findAll", query="SELECT m FROM MonitoredStock m")
@NamedNativeQueries({ 
	@NamedNativeQuery(name = "MonitoredStock.getActivelyMonitoredStocks",  resultClass=MonitoredStock.class,
		query = "select * from monitored_stocks where (trennd!=0 and added_by =0) or added_by !=0"),
	@NamedNativeQuery(name = "MonitoredStock.retreiveByStockNum",  resultClass=MonitoredStock.class,
	query = "select * from monitored_stocks where stocknum=:stockNum"),
	@NamedNativeQuery(name = "MonitoredStock.retrievegetActivelyMonitoredStocksByTime",  resultClass=MonitoredStock.class,
	query = "select * from monitored_stocks where `interval`=:time")
	})
public class MonitoredStock implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="monitored_stock_id", unique=true, nullable=false)
	private int monitoredStockId;

	@Column(name="added_by", nullable=false)
	private int addedBy;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="added_date")
	private Date addedDate;

	@Column(nullable=false, name="`interval`")
	private int interval;

	private int trennd;

	//bi-directional many-to-one association to Stock
	@ManyToOne
	@JoinColumn(name="stocknum", nullable=false)
	private Stock stock;

	public MonitoredStock() {
	}

	public int getMonitoredStockId() {
		return this.monitoredStockId;
	}

	public void setMonitoredStockId(int monitoredStockId) {
		this.monitoredStockId = monitoredStockId;
	}

	public int getAddedBy() {
		return this.addedBy;
	}

	public void setAddedBy(int addedBy) {
		this.addedBy = addedBy;
	}

	public Date getAddedDate() {
		return this.addedDate;
	}

	public void setAddedDate(Date addedDate) {
		this.addedDate = addedDate;
	}

	public int getInterval() {
		return this.interval;
	}

	public void setInterval(int interval) {
		this.interval = interval;
	}

	public int getTrennd() {
		return this.trennd;
	}

	public void setTrennd(int trennd) {
		this.trennd = trennd;
	}

	public Stock getStock() {
		return this.stock;
	}

	public void setStock(Stock stock) {
		this.stock = stock;
	}

}