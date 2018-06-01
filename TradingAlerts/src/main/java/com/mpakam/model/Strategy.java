package com.mpakam.model;

import java.io.Serializable;
import javax.persistence.*;
import java.util.Set;


/**
 * The persistent class for the strategy database table.
 * 
 */
@Entity
@Table(name="strategy")
@NamedQuery(name="Strategy.findAll", query="SELECT s FROM Strategy s")
public class Strategy implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="strategy_id", unique=true, nullable=false)
	private int strategyId;

	@Column(name="strategy_interval")
	private int strategyInterval;

	@Column(name="strategy_name", nullable=false, length=256)
	private String strategyName;

	//bi-directional many-to-one association to StrategyStockQuote
	@OneToMany(mappedBy="strategy")
	private Set<StrategyStockQuote> strategyStockQuotes;

	public Strategy() {
	}

	public int getStrategyId() {
		return this.strategyId;
	}

	public void setStrategyId(int strategyId) {
		this.strategyId = strategyId;
	}

	public int getStrategyInterval() {
		return this.strategyInterval;
	}

	public void setStrategyInterval(int strategyInterval) {
		this.strategyInterval = strategyInterval;
	}

	public String getStrategyName() {
		return this.strategyName;
	}

	public void setStrategyName(String strategyName) {
		this.strategyName = strategyName;
	}

	public Set<StrategyStockQuote> getStrategyStockQuotes() {
		return this.strategyStockQuotes;
	}

	public void setStrategyStockQuotes(Set<StrategyStockQuote> strategyStockQuotes) {
		this.strategyStockQuotes = strategyStockQuotes;
	}

	public StrategyStockQuote addStrategyStockQuote(StrategyStockQuote strategyStockQuote) {
		getStrategyStockQuotes().add(strategyStockQuote);
		strategyStockQuote.setStrategy(this);

		return strategyStockQuote;
	}

	public StrategyStockQuote removeStrategyStockQuote(StrategyStockQuote strategyStockQuote) {
		getStrategyStockQuotes().remove(strategyStockQuote);
		strategyStockQuote.setStrategy(null);

		return strategyStockQuote;
	}

}