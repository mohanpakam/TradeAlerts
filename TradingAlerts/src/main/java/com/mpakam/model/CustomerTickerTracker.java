package com.mpakam.model;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the customer_ticker_tracker database table.
 * 
 */
@Entity
@Table(name="customer_ticker_tracker")
@NamedQuery(name="CustomerTickerTracker.findAll", query="SELECT c FROM CustomerTickerTracker c")
public class CustomerTickerTracker implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="quote_tick_id", unique=true, nullable=false)
	private int quoteTickId;

	@Column(length=256)
	private String description;

	@Column(name="strategy_id", nullable=false)
	private int strategyId;

	//bi-directional many-to-one association to Customer
	@ManyToOne
	@JoinColumn(name="customerid", nullable=false)
	private Customer customer;

	//bi-directional many-to-one association to Stock
	@ManyToOne
	@JoinColumn(name="stocknum", nullable=false)
	private Stock stock;

	public CustomerTickerTracker() {
	}

	public int getQuoteTickId() {
		return this.quoteTickId;
	}

	public void setQuoteTickId(int quoteTickId) {
		this.quoteTickId = quoteTickId;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getStrategyId() {
		return this.strategyId;
	}

	public void setStrategyId(int strategyId) {
		this.strategyId = strategyId;
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

}