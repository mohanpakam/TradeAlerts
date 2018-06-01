package com.mpakam.model;

import java.io.Serializable;
import javax.persistence.*;
import java.math.BigDecimal;


/**
 * The persistent class for the tech_analysis_atrs database table.
 * 
 */
@Entity
@Table(name="tech_analysis_atrs")
@NamedQuery(name="TechAnalysisAtr.findAll", query="SELECT t FROM TechAnalysisAtr t")
@NamedNativeQueries({ 
	@NamedNativeQuery(name = "TechAnalysisAtr.retrieveLastByStockNum",  resultClass=TechAnalysisAtr.class,
		query = "select taa.* from tech_analysis_atrs taa join stock_quotes sq on (sq.stock_quote_id= taa.stock_quote_id) where stocknum = :stockNum order by sq.quote_datetime desc limit 1 ")
	,
		@NamedNativeQuery(name = "TechAnalysisAtr.retrieveAtrByStockQuote",  resultClass=TechAnalysisAtr.class,
		query = "select taa.* from tech_analysis_atrs taa join stock_quotes sq on (sq.stock_quote_id= taa.stock_quote_id) where sq.stock_quote_id = :stockQuoteId order by sq.quote_datetime desc limit 1 ")
	}	)
public class TechAnalysisAtr implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="atr_id", unique=true, nullable=false)
	private int atrId;

	@Column(name="average_true_range", precision=10, scale=2)
	private BigDecimal averageTrueRange;

	@Column(name="true_range", precision=10, scale=2)
	private BigDecimal trueRange;

	//bi-directional many-to-one association to StockQuote2
	@ManyToOne
	@JoinColumn(name="stock_quote_id", nullable=false)
	private StockQuote stockQuote;

	public TechAnalysisAtr() {
	}

	public int getAtrId() {
		return this.atrId;
	}

	public void setAtrId(int atrId) {
		this.atrId = atrId;
	}

	public BigDecimal getAverageTrueRange() {
		return this.averageTrueRange;
	}

	public void setAverageTrueRange(BigDecimal averageTrueRange) {
		this.averageTrueRange = averageTrueRange;
	}

	public BigDecimal getTrueRange() {
		return this.trueRange;
	}

	public void setTrueRange(BigDecimal trueRange) {
		this.trueRange = trueRange;
	}

	public StockQuote getStockQuote() {
		return this.stockQuote;
	}

	public void setStockQuote(StockQuote stockQuote) {
		this.stockQuote = stockQuote;
	}

}