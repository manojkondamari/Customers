package com.vetconnect.customerservice.service;

import java.util.List;

import com.vetconnect.customerservice.dto.AddressRequest;
import com.vetconnect.customerservice.dto.AddressResponse;
import com.vetconnect.customerservice.dto.CustomerRequest;
import com.vetconnect.customerservice.dto.CustomerResponse;
import com.vetconnect.customerservice.entity.Address;
import com.vetconnect.customerservice.repository.CustomersRepo;

public interface CustomersService {
	//CustomersRepo customersRepo;
	
	CustomerResponse registerCustomers(CustomerRequest request);
	CustomerResponse getCustomerDetails(int id);
	
	CustomerResponse updateCustomerDetails(int id, CustomerRequest request);
	CustomerResponse updateCustomerEmail(int id, String email);
	void deleteCustomerDetails(int id);
	
	List<AddressResponse> getAddressForCustomer(int id);
	
	AddressResponse registerCustomerAddresses(int id, AddressRequest addressRequest);
	
	
}
