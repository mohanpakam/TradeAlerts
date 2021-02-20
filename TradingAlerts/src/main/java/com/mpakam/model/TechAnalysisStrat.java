package com.mpakam.model;

import java.io.Serializable;
import javax.persistence.*;

import com.mpakam.constants.StratIdentifier;

import java.math.BigDecimal;


/**
 * The persistent class for the tech_analysis_strat database table.
 * 
 */
@Entity
@Table(name="tech_analysis_strats")
@NamedQuery(name="TechAnalysisStrat.findAll", query="SELECT t FROM TechAnalysisStrat t")
@NamedNativeQueries({ 
	@NamedNativeQuery(name = "TechAnalysisStrat.retrieveLastByStockNum",  resultClass=TechAnalysisStrat.class,
		query = "select taa.* from tech_analysis_strats taa join stock_quotes sq on (sq.stock_quote_id= taa.stock_quote_id) where stocknum = :stockNum order by sq.quote_datetime desc limit 1 ")
	,
		@NamedNativeQuery(name = "TechAnalysisStrat.retrieveStrat ByStockQuote",  resultClass=TechAnalysisStrat.class,
		query = "select taa.* from tech_analysis_strats taa join stock_quotes sq on (sq.stock_quote_id= taa.stock_quote_id) where sq.stock_quote_id = :stockQuoteId order by sq.quote_datetime desc limit 1 ")
	}	)
public class TechAnalysisStrat implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="sq_strat_id", unique=true, nullable=false)
	private int sqStratId;

	@Column(name="strat_id")
	private String stratId;

	//bi-directional many-to-one association to StockQuote2
	@ManyToOne
	@JoinColumn(name="stock_quote_id", nullable=false)
	private StockQuote stockQuote;

	public TechAnalysisStrat() {
	}

	public int getSqStratId() {
		return this.sqStratId;
	}

	public void setSqStratId(int sqStratId) {
		this.sqStratId = sqStratId;
	}

	public String getStratId() {
		return this.stratId;
	}

	public void setStratId(String stratId) {
		this.stratId = stratId;
	}	

	public StockQuote getStockQuote() {
		return this.stockQuote;
	}

	public void setStockQuote(StockQuote stockQuote) {
		this.stockQuote = stockQuote;
	}

}