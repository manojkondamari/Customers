package com.vetconnect.customerservice.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.config.ConfigDataResourceNotFoundException;
import org.springframework.stereotype.Service;

import com.vetconnect.customerservice.dto.AddressRequest;
import com.vetconnect.customerservice.dto.AddressResponse;
import com.vetconnect.customerservice.dto.CustomerRequest;
import com.vetconnect.customerservice.dto.CustomerResponse;
import com.vetconnect.customerservice.entity.Address;
import com.vetconnect.customerservice.entity.Customers;
import com.vetconnect.customerservice.exception.DuplicateCustomerException;
import com.vetconnect.customerservice.exception.ResourceInactiveException;
import com.vetconnect.customerservice.exception.ResourceNotFoundException;
import com.vetconnect.customerservice.repository.AddressesRepo;
import com.vetconnect.customerservice.repository.CustomersRepo;


@Service
public class CustomersServiceImpl implements CustomersService {

	private final CustomersRepo customersRepo;

	public final AddressesRepo addressRepo;
	
	public CustomersServiceImpl(CustomersRepo customersRepo, AddressesRepo addressRepo)
	{
		this.customersRepo=customersRepo;
		this.addressRepo = addressRepo;
	}	

	@Override
	public CustomerResponse registerCustomers(CustomerRequest request) {
		
		
		if(customersRepo.existsByEmail(request.getEmail())) {
			throw new DuplicateCustomerException("customer with "+ request.getEmail()+" already exists");
		}
		
		Customers customer=mapToEntity(request);
		customer.setCreatedAt(LocalDateTime.now());
		Customers savedCustomer=customersRepo.save(customer);
		
		CustomerResponse customerResp= mapToResponse(savedCustomer);
		
		return customerResp;
	}
	
	private Customers mapToEntity(CustomerRequest request) {
		Customers customer=new Customers();
		
		//customerRequest dto ->entity
		
		customer.setFirstName(request.getFirstName());
		customer.setLastName(request.getLastName());
		customer.setEmail(request.getEmail());
		customer.setPhoneNumber(request.getPhoneNumber());
		customer.setDateOfBirth(request.getDateOfBirth());
		customer.setUpdatedAt(LocalDateTime.now());
		customer.setActive(true);
		
		return customer;
		
	}
	
	private CustomerResponse mapToResponse(Customers savedCustomer) {
		CustomerResponse customerResp= new CustomerResponse();
		
		customerResp.setId(savedCustomer.getId());
		customerResp.setFirstName(savedCustomer.getFirstName());
		customerResp.setLastName(savedCustomer.getLastName());
		customerResp.setEmail(savedCustomer.getEmail());
		customerResp.setDateOfBirth(savedCustomer.getDateOfBirth());
		customerResp.setCreatedAt(savedCustomer.getCreatedAt());
		customerResp.setPhoneNumber(savedCustomer.getPhoneNumber());
		customerResp.setActive(savedCustomer.isActive());
		customerResp.setUpdatedAt(savedCustomer.getUpdatedAt());
		return customerResp;
	}


	@Override
	public CustomerResponse getCustomerDetails(int id) {

		Customers customers=customersRepo
				.findById(id)
				.orElseThrow(()-> new ResourceNotFoundException("Customer with id "+id+" not found"));
		
		CustomerResponse customerResp=mapToResponse(customers);
		
		return customerResp;
	}


	@Override
	public CustomerResponse updateCustomerDetails(int id, CustomerRequest request) {
		// TODO Auto-generated method stub
		
		//Customers customers=new Customers();	
		Customers customersUpdated=customersRepo.findById(id)
				.orElseThrow(()-> new ResourceNotFoundException("Customer with id "+id+"not found"));
		

		customersUpdated.setFirstName(request.getFirstName());
		customersUpdated.setLastName(request.getLastName());
		customersUpdated.setDateOfBirth(request.getDateOfBirth());
		customersUpdated.setUpdatedAt(LocalDateTime.now());
		customersUpdated.setEmail(request.getEmail());
		customersUpdated.setPhoneNumber(request.getPhoneNumber());
		//customersRepo.save(customersUpdated);
		
		Customers savedCustomer=customersRepo.save(customersUpdated);
		
		//entity -> customerResponse
		
		CustomerResponse customerResp= mapToResponse(savedCustomer);
		
		return customerResp;
	}

	@Override
	public CustomerResponse updateCustomerEmail(int id, String email) {
			Customers customer=customersRepo.findById(id)
					.orElseThrow(()->new ResourceNotFoundException("Customer with id "+id+" not found"));
			
			customer.setEmail(email);
			customer.setUpdatedAt(LocalDateTime.now());
			Customers entityResp=customersRepo.save(customer);
			
		return mapToResponse(entityResp);
	}
	
	@Override
	public void deleteCustomerDetails(int id) {
		// TODO Auto-generated method stub
		
		Customers customers=customersRepo.findById(id)
				.orElseThrow(()-> new ResourceNotFoundException("Customer with id "+id+" not found"));
		
		//Customers customerResp=customers.get();
		
		customers.setActive(false);
		customers.setUpdatedAt(LocalDateTime.now());
		
		customersRepo.save(customers);
	}


	@Override
	public List<AddressResponse> getAddressForCustomer(int id) {
		// TODO Auto-generated method stub
		
		Customers customers=customersRepo.findById(id)
				.orElseThrow(()-> new ResourceNotFoundException("Customer with id "+id+" not found"));
		
		if(Boolean.FALSE.equals(customers.isActive())) {
			throw new ResourceInactiveException("no address found, please add one"+id);
		}
		
		List<Address> addressList=addressRepo.findAddressByCustomerId(id);
		
		return toAddressResponseList(addressList); 
	}
	  
	
	private List<AddressResponse> toAddressResponseList(List<Address> address){
		
		return address.stream()
						.map(this::mapToAddressResponse)
						.collect(Collectors.toList());
	}
	
	
	@Override
	public AddressResponse registerCustomerAddresses(int id, AddressRequest addressRequest) {
		
		
		Customers customer=customersRepo.findById(id)
										.orElseThrow(()-> new 
												ResourceNotFoundException("Customer with id "+id+" not found"));
		
		if(Boolean.FALSE.equals(customer.isActive())) {
			throw new ResourceInactiveException("Customer not active");
		}

		Address address=mapToAddress(addressRequest, customer);
		
		Address savedAddress=addressRepo.save(address);
		
		AddressResponse addressResponse=mapToAddressResponse(savedAddress);
		
		return addressResponse;
	}
	
	
	
	
	private Address mapToAddress(AddressRequest addressRequest, Customers customer) {
		
		Address address=new Address();
		
		address.setAddressType(addressRequest.getAddressType());
		address.setCity(addressRequest.getCity());
		address.setCustomer(customer);
		address.setStreet(addressRequest.getStreet());
		address.setState(addressRequest.getState());
		address.setCountry(addressRequest.getCountry());
		address.setZipCode(addressRequest.getZipCode());
		
		return address;
	}
	
	
	
	private AddressResponse mapToAddressResponse(Address savedAddress) {
		AddressResponse addressResponse=new AddressResponse();
		
		addressResponse.setAddressType(savedAddress.getAddressType());
		addressResponse.setStreet(savedAddress.getStreet());
		addressResponse.setId(savedAddress.getId());
		addressResponse.setCity(savedAddress.getCity());
		addressResponse.setState(savedAddress.getState());
		addressResponse.setCountry(savedAddress.getCountry());
		addressResponse.setZipCode(savedAddress.getZipCode());
		
		return addressResponse;
	}

	
}
