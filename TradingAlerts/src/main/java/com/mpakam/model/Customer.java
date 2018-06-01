package com.mpakam.model;

import java.io.Serializable;
import javax.persistence.*;
import java.util.Set;


/**
 * The persistent class for the customers database table.
 * 
 */
@Entity
@Table(name="customers")
@NamedQuery(name="Customer.findAll", query="SELECT c FROM Customer c")
public class Customer implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(unique=true, nullable=false)
	private int customerid;

	@Column(name="email_id", nullable=false, length=256)
	private String emailId;

	@Column(name="first_name", nullable=false, length=50)
	private String firstName;

	@Column(name="last_name", nullable=false, length=50)
	private String lastName;

	@Column(nullable=false, length=256)
	private String password;

	@Column(nullable=false, length=50)
	private String username;

	//bi-directional many-to-one association to CustomerTickerTracker
	@OneToMany(mappedBy="customer")
	private Set<CustomerTickerTracker> customerTickerTrackers;

	//bi-directional many-to-one association to StockAlert
	@OneToMany(mappedBy="customer")
	private Set<StockAlert> stockAlerts;

	public Customer() {
	}

	public int getCustomerid() {
		return this.customerid;
	}

	public void setCustomerid(int customerid) {
		this.customerid = customerid;
	}

	public String getEmailId() {
		return this.emailId;
	}

	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}

	public String getFirstName() {
		return this.firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return this.lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUsername() {
		return this.username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Set<CustomerTickerTracker> getCustomerTickerTrackers() {
		return this.customerTickerTrackers;
	}

	public void setCustomerTickerTrackers(Set<CustomerTickerTracker> customerTickerTrackers) {
		this.customerTickerTrackers = customerTickerTrackers;
	}

	public CustomerTickerTracker addCustomerTickerTracker(CustomerTickerTracker customerTickerTracker) {
		getCustomerTickerTrackers().add(customerTickerTracker);
		customerTickerTracker.setCustomer(this);

		return customerTickerTracker;
	}

	public CustomerTickerTracker removeCustomerTickerTracker(CustomerTickerTracker customerTickerTracker) {
		getCustomerTickerTrackers().remove(customerTickerTracker);
		customerTickerTracker.setCustomer(null);

		return customerTickerTracker;
	}

	public Set<StockAlert> getStockAlerts() {
		return this.stockAlerts;
	}

	public void setStockAlerts(Set<StockAlert> stockAlerts) {
		this.stockAlerts = stockAlerts;
	}

	public StockAlert addStockAlert(StockAlert stockAlert) {
		getStockAlerts().add(stockAlert);
		stockAlert.setCustomer(this);

		return stockAlert;
	}

	public StockAlert removeStockAlert(StockAlert stockAlert) {
		getStockAlerts().remove(stockAlert);
		stockAlert.setCustomer(null);

		return stockAlert;
	}

}