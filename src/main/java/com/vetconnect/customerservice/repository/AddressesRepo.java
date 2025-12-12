package com.vetconnect.customerservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vetconnect.customerservice.entity.Address;

public interface AddressesRepo extends JpaRepository<Address, Integer> {
	
	List<Address> findAddressByCustomerId(int customerId);
}
