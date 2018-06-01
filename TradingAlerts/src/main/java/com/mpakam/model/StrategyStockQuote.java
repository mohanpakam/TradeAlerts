package com.mpakam.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;


/**
 * The persistent class for the strategy_stock_quotes database table.
 * 
 */
@Entity
@Table(name="strategy_stock_quotes")
@NamedQuery(name="StrategyStockQuote.findAll", query="SELECT s FROM StrategyStockQuote s")
@NamedNativeQueries({ 
	@NamedNativeQuery(name = "StrategyStockQuote.getTopXByStockNum",  resultClass=StrategyStockQuote.class,
		query = "select ssq.* from strategy_stock_quotes ssq join stock_quotes sq on (ssq.stock_quote_id= sq.stock_quote_id) where ssq.strategy_id = :strategyId and sq.stocknum = :stockNum and sq.`interval`=:interval order by sq.stock_quote_id desc limit 13") ,
	@NamedNativeQuery(name = "StrategyStockQuote.getAllByStockNumStrategyId",  resultClass=StrategyStockQuote.class,
		query = "select ssq.* from strategy_stock_quotes ssq join stock_quotes sq on (ssq.stock_quote_id= sq.stock_quote_id) where ssq.strategy_id = :strategyId and sq.stocknum = :stockNum and sq.`interval`=:interval order by sq.stock_quote_id desc limit 50"),
	@NamedNativeQuery(name = "StrategyStockQuote.getAllByStockNum",  resultClass=StrategyStockQuote.class,
	query = "select ssq.* from strategy_stock_quotes ssq join stock_quotes sq on (ssq.stock_quote_id= sq.stock_quote_id) where sq.stocknum = :stockNum and sq.`interval`=:interval order by sq.stock_quote_id desc"),
	@NamedNativeQuery(name = "StrategyStockQuote.getLastQuoteByStockNum",  resultClass=StrategyStockQuote.class,
	query = "select ssq.* from strategy_stock_quotes ssq join stock_quotes sq on (ssq.stock_quote_id= sq.stock_quote_id) where ssq.strategy_id = :strategyId and sq.stocknum = :stockNum and sq.`interval`=:interval order by sq.stock_quote_id desc limit 1")})
public class StrategyStockQuote implements Serializable, Comparable<StrategyStockQuote>{
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="strategy_stock_quote_id", unique=true, nullable=false)
	private int strategyStockQuoteId;

	@Column(name="avg_gain", precision=10, scale=4)
	private BigDecimal avgGain;

	@Column(name="avg_loss", precision=10, scale=4)
	private BigDecimal avgLoss;

	@Column(precision=10, scale=6)
	private BigDecimal highrsi;

	@Column(precision=10, scale=6)
	private BigDecimal lowrsi;

	@Column(precision=10, scale=6)
	private BigDecimal rsi;
	
	@Column(name="stoch_rsi", precision=10, scale=4)
	private BigDecimal stochRsi;

	@Column(name="stoch_rsi_d", precision=10, scale=4)
	private BigDecimal stochRsiD;

	@Column(name="stoch_rsi_k", precision=10, scale=4)
	private BigDecimal stochRsiK;

	@Column(precision=10, scale=6)
	private BigDecimal xclose;

	@Column(precision=10, scale=6)
	private BigDecimal xhigh;

	@Column(precision=10, scale=6)
	private BigDecimal xlow;

	@Column(precision=10, scale=6)
	private BigDecimal xopen;
	

	//bi-directional many-to-one association to StockAlert
	@OneToMany(mappedBy="strategyStockQuote")
	private Set<StockAlert> stockAlerts;

	//bi-directional many-to-one association to StockQuote
	@ManyToOne
	@JoinColumn(name="stock_quote_id")
	private StockQuote stockQuote;

	//bi-directional many-to-one association to Strategy
	@ManyToOne
	@JoinColumn(name="strategy_id")
	private Strategy strategy;
	
	@Transient
	private boolean sendEmail=true;

	public StrategyStockQuote() {
	}

	public int getStrategyStockQuoteId() {
		return this.strategyStockQuoteId;
	}

	public void setStrategyStockQuoteId(int strategyStockQuoteId) {
		this.strategyStockQuoteId = strategyStockQuoteId;
	}

	public BigDecimal getAvgGain() {
		return (this.avgGain == null)?new BigDecimal(0):this.avgGain;
	}

	public void setAvgGain(BigDecimal avgGain) {
		this.avgGain = avgGain;
	}

	public BigDecimal getAvgLoss() {
		return (this.avgLoss== null)?new BigDecimal(0):this.avgLoss;
	}

	public void setAvgLoss(BigDecimal avgLoss) {
		this.avgLoss = avgLoss;
	}

	public BigDecimal getHighrsi() {
		return (this.highrsi== null)?new BigDecimal(0):this.highrsi;
	}

	public void setHighrsi(BigDecimal highrsi) {
		this.highrsi = highrsi;
	}

	public BigDecimal getLowrsi() {
		return (this.lowrsi== null)?new BigDecimal(0):this.lowrsi;
	}

	public void setLowrsi(BigDecimal lowrsi) {
		this.lowrsi = lowrsi;
	}

	public BigDecimal getRsi() {
		return (this.rsi== null)?new BigDecimal(0):this.rsi;
	}

	public void setRsi(BigDecimal rsi) {
		this.rsi = rsi;
	}

	public BigDecimal getStochRsiD() {
		return (this.stochRsiD== null)?new BigDecimal(0):this.stochRsiD;
	}

	public void setStochRsiD(BigDecimal stochRsiD) {
		this.stochRsiD = stochRsiD;
	}

	public BigDecimal getStochRsiK() {
		return (this.stochRsiK== null)?new BigDecimal(0):this.stochRsiK;
	}

	public void setStochRsiK(BigDecimal stochRsiK) {
		this.stochRsiK = stochRsiK;
	}

	public BigDecimal getXclose() {
		return (this.xclose== null)?new BigDecimal(0):this.xclose;
	}

	public void setXclose(BigDecimal xclose) {
		this.xclose = xclose;
	}

	public BigDecimal getXhigh() {
		return (this.xhigh== null)?new BigDecimal(0):this.xhigh;
	}

	public void setXhigh(BigDecimal xhigh) {
		this.xhigh = xhigh;
	}

	public BigDecimal getXlow() {
		return (this.xlow== null)?new BigDecimal(0):this.xlow;
	}

	public void setXlow(BigDecimal xlow) {
		this.xlow = xlow;
	}

	public BigDecimal getXopen() {
		return (this.xopen== null)?new BigDecimal(0):this.xopen;
	}

	public void setXopen(BigDecimal xopen) {
		this.xopen = xopen;
	}

	public Set<StockAlert> getStockAlerts() {
		return this.stockAlerts;
	}

	public void setStockAlerts(Set<StockAlert> stockAlerts) {
		this.stockAlerts = stockAlerts;
	}

	public StockAlert addStockAlert(StockAlert stockAlert) {
		getStockAlerts().add(stockAlert);
		stockAlert.setStrategyStockQuote(this);

		return stockAlert;
	}

	public StockAlert removeStockAlert(StockAlert stockAlert) {
		getStockAlerts().remove(stockAlert);
		stockAlert.setStrategyStockQuote(null);

		return stockAlert;
	}

	public StockQuote getStockQuote() {
		return this.stockQuote;
	}

	public void setStockQuote(StockQuote stockQuote) {
		this.stockQuote = stockQuote;
	}

	public Strategy getStrategy() {
		return this.strategy;
	}

	public void setStrategy(Strategy strategy) {
		this.strategy = strategy;
	}
	
	public BigDecimal getStochRsi() {
		return (this.stochRsi== null)?new BigDecimal(0):this.stochRsi;
	}

	public void setStochRsi(BigDecimal stochRsi) {
		this.stochRsi = stochRsi;
	}

	@Override
	public int compareTo(StrategyStockQuote o) {
		if(this.getStrategyStockQuoteId() >0)
			return this.getStrategyStockQuoteId() - o.getStrategyStockQuoteId();
		else
			return this.getStockQuote().getStockQuoteId()-o.getStockQuote().getStockQuoteId();
	}
	
	public void setSendEmail(boolean send) {
		this.sendEmail=send;
	}
	public boolean getSendEmail() {
		return this.sendEmail;
	}

	@Override
	public String toString() {
		return "StrategyStockQuote [strategyStockQuoteId=" + strategyStockQuoteId + ", avgGain=" + avgGain
				+ ", avgLoss=" + avgLoss + ", highrsi=" + highrsi + ", lowrsi=" + lowrsi + ", rsi=" + rsi
				+ ", stochRsi=" + stochRsi + ", stochRsiD=" + stochRsiD + ", stochRsiK=" + stochRsiK + ", xclose="
				+ xclose + ", xhigh=" + xhigh + ", xlow=" + xlow + ", xopen=" + xopen 
				+ ", strategy=" + strategy + ", sendEmail=" + sendEmail + "]";
	}
}