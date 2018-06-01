package com.mpakam.model;

import java.io.Serializable;
import javax.persistence.*;
import java.math.BigDecimal;


/**
 * The persistent class for the tech_analysis_ichimoku database table.
 * 
 */
@Entity
@Table(name="tech_analysis_ichimoku")
@NamedQuery(name="TechAnalysisIchimoku.findAll", query="SELECT t FROM TechAnalysisIchimoku t")
public class TechAnalysisIchimoku implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="ichimoku_id", unique=true, nullable=false)
	private int ichimokuId;

	@Column(name="ema_9", precision=10, scale=2)
	private BigDecimal ema9;

	@Column(name="kijun_sen", precision=10, scale=2)
	private BigDecimal kijunSen;

	@Column(name="senkou_span_a", precision=10, scale=2)
	private BigDecimal senkouSpanA;

	@Column(name="senkou_span_b", precision=10, scale=2)
	private BigDecimal senkouSpanB;

	@Column(name="tenken_sen", precision=10, scale=2)
	private BigDecimal tenkenSen;

	//bi-directional many-to-one association to StockQuote
	@ManyToOne
	@JoinColumn(name="stock_quote_id", nullable=false)
	private StockQuote stockQuote;

	public TechAnalysisIchimoku() {
	}

	public int getIchimokuId() {
		return this.ichimokuId;
	}

	public void setIchimokuId(int ichimokuId) {
		this.ichimokuId = ichimokuId;
	}

	public BigDecimal getEma9() {
		return this.ema9;
	}

	public void setEma9(BigDecimal ema9) {
		this.ema9 = ema9;
	}

	public BigDecimal getKijunSen() {
		return this.kijunSen;
	}

	public void setKijunSen(BigDecimal kijunSen) {
		this.kijunSen = kijunSen;
	}

	public BigDecimal getSenkouSpanA() {
		return this.senkouSpanA;
	}

	public void setSenkouSpanA(BigDecimal senkouSpanA) {
		this.senkouSpanA = senkouSpanA;
	}

	public BigDecimal getSenkouSpanB() {
		return this.senkouSpanB;
	}

	public void setSenkouSpanB(BigDecimal senkouSpanB) {
		this.senkouSpanB = senkouSpanB;
	}

	public BigDecimal getTenkenSen() {
		return this.tenkenSen;
	}

	public void setTenkenSen(BigDecimal tenkenSen) {
		this.tenkenSen = tenkenSen;
	}

	public StockQuote getStockQuote() {
		return this.stockQuote;
	}

	public void setStockQuote(StockQuote stockQuote) {
		this.stockQuote = stockQuote;
	}

}