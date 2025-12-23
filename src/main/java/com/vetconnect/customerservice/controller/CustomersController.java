package com.vetconnect.customerservice.controller;
import com.vetconnect.customerservice.service.CustomersServiceImpl;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
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
import com.vetconnect.customerservice.entity.Address;
import com.vetconnect.customerservice.service.CustomersService;

@RestController
@RequestMapping("/api/customers")
public class CustomersController {
	
	private CustomersService customerService;
	 CustomersController(CustomersService customerService ){ //CustomersServiceImpl customersServiceImpl
		this.customerService=customerService;
		//this.customersServiceImpl = customersServiceImpl;
	}
	 
	 
	 //http://localhost:8080/api/customers
	
	@PostMapping
	public ResponseEntity<CustomerResponse> registerCustomer(@Valid @RequestBody CustomerRequest customerRequest) {
		CustomerResponse response=customerService.registerCustomers(customerRequest);
		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(response);
	}
	
	
	//http://localhost:8080/api/customers/2
	
	@GetMapping("/{id}")
	public CustomerResponse getCustomerDetails(@PathVariable int id){
		
		CustomerResponse response=customerService.getCustomerDetails(id);
		return response;
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<CustomerResponse> updateCustomerDetails(@PathVariable int id, 
																	@Valid @RequestBody CustomerRequest customerRequest){
		CustomerResponse response=customerService.updateCustomerDetails(id, customerRequest);
		return ResponseEntity.ok(response);
	}
	
	
	@PatchMapping("/{id}")
	public ResponseEntity<CustomerResponse> updateEmail(@PathVariable int id, @NotNull @RequestParam String email){
		CustomerResponse resp=customerService.updateCustomerEmail(id, email);
		
		return ResponseEntity.ok(resp);
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteCustomerDetails(@PathVariable int id) {
		customerService.deleteCustomerDetails(id);
		return ResponseEntity.noContent().build();
	}
	
	
	@PostMapping("/{id}/addresses")
	public ResponseEntity<AddressResponse> registerCustomerAddress(@PathVariable int id, @RequestBody AddressRequest addressRequest){
	AddressResponse	response=customerService.registerCustomerAddresses(id, addressRequest);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
	
	
	@GetMapping("/{id}/addresses")
	public List<AddressResponse> getCustomerAddresses(@PathVariable int id){
		List<AddressResponse> response=customerService.getAddressForCustomer(id);
	
	return response;
	}
}

