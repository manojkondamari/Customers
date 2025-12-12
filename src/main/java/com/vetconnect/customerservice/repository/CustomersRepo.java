package com.vetconnect.customerservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vetconnect.customerservice.entity.Customers;

public interface CustomersRepo extends JpaRepository<Customers, Integer>{
	
}
