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
import com.vetconnect.customerservice.exception.ResourceInactiveException;
import com.vetconnect.customerservice.exception.ResourceNotFoundException;
import com.vetconnect.customerservice.repository.AddressesRepo;
import com.vetconnect.customerservice.repository.CustomersRepo;


@Service
public class CustomersServiceImpl implements CustomersService {

    //private final AddressesRepo addressesRepo = null;

	//@Autowired
	//CustomersRepo customersRepo;
	
	private final CustomersRepo customersRepo;
	
	public CustomersServiceImpl(CustomersRepo customersRepo)
	{
		this.customersRepo=customersRepo;
	}	
	@Autowired
	AddressesRepo addressesRepo;
	

	@Override
	public CustomerResponse registerCustomers(CustomerRequest request) {
		
		Customers customer=new Customers();
		
		//customerRequest dto ->entity
		
		customer.setFirstName(request.getFirstName());
		customer.setLastName(request.getLastName());
		customer.setEmail(request.getEmail());
		customer.setPhoneNumber(request.getPhoneNumber());
		customer.setDateOfBirth(request.getDateOfBirth());
		customer.setCreatedAt(LocalDateTime.now());
		customer.setUpdatedAt(LocalDateTime.now());
		customer.setActive(true);
		
		//save customer details
		Customers savedCustomer=customersRepo.save(customer);
		
		//entity -> customerResponse
		
		CustomerResponse customerResp= new CustomerResponse();
		customerResp.setId(savedCustomer.getId());
		customerResp.setFirstName(savedCustomer.getFirstName());
		customerResp.setLastName(savedCustomer.getLastName());
		customerResp.setEmail(savedCustomer.getEmail());
		customerResp.setDateOfBirth(savedCustomer.getDateOfBirth());
		customerResp.setCreatedAt(savedCustomer.getCreatedAt());
		customerResp.setPhoneNumer(savedCustomer.getPhoneNumber());
		customerResp.setActive(savedCustomer.isActive());
		return customerResp;
	}


	@Override
	public CustomerResponse getCustomerDetails(int id) {
		CustomerResponse customerResp= new CustomerResponse();
		
		
		Optional<Customers> customers=customersRepo.findById(id);
		
		//checking if the data is null
		if(customers==null) {
			throw new NullPointerException("user not found");	
		}
		
		//if not null assign repo(optional data) data to entity type
		Customers savedCustomer=customers.get();
		
		customerResp.setId(savedCustomer.getId());
		customerResp.setFirstName(savedCustomer.getFirstName());
		customerResp.setLastName(savedCustomer.getLastName());
		customerResp.setEmail(savedCustomer.getEmail());
		customerResp.setDateOfBirth(savedCustomer.getDateOfBirth());
		customerResp.setCreatedAt(savedCustomer.getCreatedAt());
		customerResp.setPhoneNumer(savedCustomer.getPhoneNumber());
		customerResp.setActive(savedCustomer.isActive());
		return customerResp;
	}


	@Override
	public CustomerResponse updateCustomerDetails(int id, CustomerRequest request) {
		// TODO Auto-generated method stub
		
		//Customers customers=new Customers();	
		Customers customersUpdated=customersRepo.findById(id)
				.orElseThrow(()-> new ResourceNotFoundException("customer with id not found"+id));
		

		customersUpdated.setFirstName(request.getFirstName());
		customersUpdated.setLastName(request.getLastName());
		customersUpdated.setDateOfBirth(request.getDateOfBirth());
		customersUpdated.setUpdatedAt(LocalDateTime.now());
		customersUpdated.setEmail(request.getEmail());
		customersUpdated.setPhoneNumber(request.getPhoneNumber());
		//customersRepo.save(customersUpdated);
		
		Customers savedCustomer=customersRepo.save(customersUpdated);
		
		//entity -> customerResponse
		
		CustomerResponse customerResp= new CustomerResponse();
		customerResp.setId(savedCustomer.getId());
		customerResp.setFirstName(savedCustomer.getFirstName());
		customerResp.setLastName(savedCustomer.getLastName());
		customerResp.setEmail(savedCustomer.getEmail());
		customerResp.setDateOfBirth(savedCustomer.getDateOfBirth());
		customerResp.setCreatedAt(savedCustomer.getCreatedAt());
		customerResp.setPhoneNumer(savedCustomer.getPhoneNumber());
		
		return customerResp;
	}


	@Override
	public void deleteCustomerDetails(int id) {
		// TODO Auto-generated method stub
		
		Customers customers=customersRepo.findById(id)
				.orElseThrow(()-> new ResourceNotFoundException("customer with id not found"+id));
		
		//Customers customerResp=customers.get();
		
		customers.setActive(false);
		customers.setUpdatedAt(LocalDateTime.now());
		
		customersRepo.save(customers);
	}


	@Override
	public List<AddressResponse> getAddressForCustomer(int id) {
		// TODO Auto-generated method stub
		
		Customers customers=customersRepo.findById(id)
				.orElseThrow(()-> new ResourceNotFoundException("customer with id not found"+id));
		
		if(Boolean.FALSE.equals(customers.isActive())) {
			throw new ResourceInactiveException("no address found, please add one"+id);
		}
		
		List<Address> addressList=addressesRepo.findAddressByCustomerId(id);
		
		return toAddressResponseList(addressList);
	}
	
	private AddressResponse toAddressResponse(Address address) {
		
		AddressResponse addressResponse=new AddressResponse();
		
		addressResponse.setId(address.getId());
		addressResponse.setState(address.getState());
		addressResponse.setStreet(address.getStreet());
		addressResponse.setCity(address.getCity());
		addressResponse.setCountry(address.getCountry());
		addressResponse.setZipCode(address.getZipCode());
		addressResponse.setAddressType(address.getAddressType());
		
		return addressResponse;
	}
	
	private List<AddressResponse> toAddressResponseList(List<Address> address){
		
		
		return address.stream()
						.map(this::toAddressResponse)
						.collect(Collectors.toList());
	}

	
	@Override
	public AddressResponse registerCustomerAddresses(int id, AddressRequest addressRequest) {
		
		Address address=new Address();
		Customers customer=customersRepo.findById(id).orElseThrow(()-> new ResourceNotFoundException("can not find customer with id "+id));

		address.setAddressType(addressRequest.getAddressType());
		address.setCity(addressRequest.getCity());
		address.setCustomer(customer);
		address.setStreet(addressRequest.getStreet());
		address.setState(addressRequest.getState());
		address.setCountry(addressRequest.getCountry());
		address.setZipCode(addressRequest.getZipCode());
		
		Address savedAddress=addressesRepo.save(address);
		
		AddressResponse addressResponse=new AddressResponse();
		
		addressResponse.setAddressType(savedAddress.getAddressType());
		addressResponse.setStreet(savedAddress.getStreet());
		//addressResponse.setId(savedAddress.getId());
		addressResponse.setId(savedAddress.getId());
		addressResponse.setCity(savedAddress.getCity());
		addressResponse.setState(savedAddress.getState());
		addressResponse.setCountry(savedAddress.getCountry());
		addressResponse.setZipCode(savedAddress.getZipCode());
		
		
		return addressResponse;
	}

}
