package com.vetconnect.customerservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.vetconnect.customerservice.entity.Address;
import com.vetconnect.customerservice.entity.Customers;

public interface CustomersRepo extends JpaRepository<Customers, Integer>{
	boolean existsByEmail(String mail);
	
	//@EntityGraph(attributePaths = {"addresses","authCredentials"})
	@Override
	@Query("SELECT DISTINCT c FROM Customers c LEFT JOIN FETCH c.addresses")
	List<Customers> findAll();
	
}


