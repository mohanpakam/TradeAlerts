package com.mpakam.dao;

import com.mpakam.model.Customer;

public interface CustomerDao {

	Customer findBy(String username);
	Long save(Customer customer);
	
}
