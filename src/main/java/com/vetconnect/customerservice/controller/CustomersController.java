package com.vetconnect.customerservice.controller;
import com.vetconnect.customerservice.service.CustomersServiceImpl;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.lang.System.Logger;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vetconnect.customerservice.dto.AddressRequest;
import com.vetconnect.customerservice.dto.AddressResponse;
import com.vetconnect.customerservice.dto.CustomerRequest;
import com.vetconnect.customerservice.dto.CustomerResponse;
import com.vetconnect.customerservice.dto.CustomersWithAddressResponse;
import com.vetconnect.customerservice.dto.UpdateEmailRequest;
import com.vetconnect.customerservice.entity.Address;
import com.vetconnect.customerservice.entity.Customers;
import com.vetconnect.customerservice.repository.CustomersRepo;
import com.vetconnect.customerservice.service.CustomersService;
import org.slf4j.*;
@RestController
@RequestMapping("/customers")
public class CustomersController {
	
	private static final org.slf4j.Logger log=org.slf4j.LoggerFactory.getLogger(CustomersController.class);
	
	private CustomersService customerService;
	//private CustomersRepo customersRepo;
	
	 CustomersController(CustomersService customerService){ //CustomersServiceImpl customersServiceImpl
		this.customerService=customerService;
		//this.customersRepo=customersRepo;
		//this.customersServiceImpl = customersServiceImpl;
	}
	 @PreAuthorize("permitAll()")
	 @GetMapping("/test-nplus1")
	 public List<CustomersWithAddressResponse> testNPlusOne(){
	List<CustomersWithAddressResponse> list=customerService.testNPlusOne();
		 return list;
	 }
	 
	@PostMapping
	public ResponseEntity<CustomerResponse> registerCustomer(@Valid @RequestBody CustomerRequest customerRequest) {
		CustomerResponse response=customerService.registerCustomers(customerRequest);
		
		log.info("POST/customers called");
		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(response);
	}
	
	
	//http://localhost:8080/api/customers/2
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
	@GetMapping("/{id}")
	public CustomerResponse getCustomerDetails(@PathVariable int id){
		
		String username =
			    SecurityContextHolder.getContext()
			        .getAuthentication()
			        .getName();

		log.info("GET/customers/{} called",id);
		CustomerResponse response=customerService.getCustomerDetails(id,username);
		return response;
	}
	
	@PreAuthorize("hasRole('ADMIN') or #id==authentication.principal.customerId")
	@PutMapping("/{id}")
	public ResponseEntity<CustomerResponse> updateCustomerDetails(@PathVariable int id, 
																	@Valid @RequestBody CustomerRequest customerRequest){
		CustomerResponse response=customerService.updateCustomerDetails(id, customerRequest);
		return ResponseEntity.ok(response);
	}
	
	
	@PatchMapping("/{id}")
	public ResponseEntity<CustomerResponse> updateEmail(@PathVariable int id, @NotNull @RequestBody UpdateEmailRequest email){
		
		log.info("PATCH/customers/{} called",id);
		
		CustomerResponse resp=customerService.updateCustomerEmail(id, email.getEmail());
		
		return ResponseEntity.ok(resp);
	}
	
	@PreAuthorize("hasRole('ADMIN') or #id==authentication.principal.customerId")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteCustomerDetails(@PathVariable int id) {
		customerService.deleteCustomerDetails(id);
		return ResponseEntity.noContent().build();
	}
	
	
	@PostMapping("/{id}/addresses")
	public ResponseEntity<AddressResponse> registerCustomerAddress(@PathVariable int id, @RequestBody AddressRequest addressRequest){
	AddressResponse	response=customerService.registerCustomerAddresses(id, addressRequest);
	
	log.info("POST/customers/{} called",id);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
	
	
	@GetMapping("/{id}/addresses")
	public List<AddressResponse> getCustomerAddresses(@PathVariable int id){
		List<AddressResponse> response=customerService.getAddressForCustomer(id);
	
		log.info("GET/customers/{} called",id);
	return response;
	}
	
	@PreAuthorize("hasRole('ADMIN') or #id==authentication.principal.customerId")
	@PutMapping("/{customerId}/addresses/{addressId}")
	public ResponseEntity<AddressResponse> updateCustomerAddress(
											@PathVariable int customerId,
											@PathVariable int addressId,
											@RequestBody AddressRequest addressRequest){
		AddressResponse response=customerService.updateCustomerAddress(customerId,addressId,addressRequest);
		log.info("PUT/customers/{}/addresses/{}",customerId,addressId);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
	
	@PreAuthorize("hasRole('ADMIN') or #id==authentication.principal.customerId")
	@DeleteMapping("/{customerId}/addresses/{addressId}")
	public ResponseEntity<Void> deleteCustomerAddress(@PathVariable int customerId, @PathVariable int addressId){
		
		customerService.deleteCustomerAddress(customerId, addressId);
		
		return ResponseEntity.noContent().build();
	}
	

}

