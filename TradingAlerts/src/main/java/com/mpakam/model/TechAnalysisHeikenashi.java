package com.mpakam.model;

import java.io.Serializable;
import javax.persistence.*;
import java.math.BigDecimal;


/**
 * The persistent class for the tech_analysis_heikenashi database table.
 * 
 */
@Entity
@Table(name="tech_analysis_heikenashi")
@NamedQuery(name="TechAnalysisHeikenashi.findAll", query="SELECT t FROM TechAnalysisHeikenashi t")
@NamedNativeQueries({ 
	@NamedNativeQuery(name = "TechAnalysisHeikenashi.retrieveLastXQuotesByStockNum",  resultClass=TechAnalysisHeikenashi.class,
		query = "select tah.* from tech_analysis_heikenashi tah join stock_quotes sq on (tah.stock_quote_id = sq.stock_quote_id)\r\n" + 
				"where sq.stocknum = 1234 order by sq.quote_datetime desc limit 13")
	})

public class TechAnalysisHeikenashi implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="ha_id", unique=true, nullable=false)
	private int haId;

	@Column(precision=10, scale=2)
	private BigDecimal xclose;

	@Column(precision=10, scale=2)
	private BigDecimal xhigh;

	@Column(precision=10, scale=2)
	private BigDecimal xlow;

	@Column(precision=10, scale=2)
	private BigDecimal xopen;

	//bi-directional many-to-one association to StockQuote2
	@ManyToOne
	@JoinColumn(name="stock_quote_id", nullable=false)
	private StockQuote stockQuote;

	public TechAnalysisHeikenashi() {
	}

	public int getHaId() {
		return this.haId;
	}

	public void setHaId(int haId) {
		this.haId = haId;
	}

	public BigDecimal getXclose() {
		return this.xclose;
	}

	public void setXclose(BigDecimal xclose) {
		this.xclose = xclose;
	}

	public BigDecimal getXhigh() {
		return this.xhigh;
	}

	public void setXhigh(BigDecimal xhigh) {
		this.xhigh = xhigh;
	}

	public BigDecimal getXlow() {
		return this.xlow;
	}

	public void setXlow(BigDecimal xlow) {
		this.xlow = xlow;
	}

	public BigDecimal getXopen() {
		return this.xopen;
	}

	public void setXopen(BigDecimal xopen) {
		this.xopen = xopen;
	}

	public StockQuote getStockQuote() {
		return this.stockQuote;
	}

	public void setStockQuote(StockQuote stockQuote) {
		this.stockQuote = stockQuote;
	}

}