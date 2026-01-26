package com.vetconnect.customerservice.service;

import java.lang.System.Logger;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import com.vetconnect.customerservice.security.AuthController;
import com.vetconnect.customerservice.security.AuthCredentialRepository;

import jakarta.transaction.Transactional;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.config.ConfigDataResourceNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.vetconnect.customerservice.dto.AddressRequest;
import com.vetconnect.customerservice.dto.AddressResponse;
import com.vetconnect.customerservice.dto.CustomerRequest;
import com.vetconnect.customerservice.dto.CustomerResponse;
import com.vetconnect.customerservice.dto.CustomersWithAddressResponse;
import com.vetconnect.customerservice.entity.Address;
import com.vetconnect.customerservice.entity.AuthCredentials;
import com.vetconnect.customerservice.entity.Customers;
import com.vetconnect.customerservice.exception.AddressNotFoundException;
import com.vetconnect.customerservice.exception.CustomerAccessDeniedException;
import com.vetconnect.customerservice.exception.DuplicateCustomerException;
import com.vetconnect.customerservice.exception.DuplicateUserException;
import com.vetconnect.customerservice.exception.ResourceInactiveException;
import com.vetconnect.customerservice.exception.ResourceMismatchException;
import com.vetconnect.customerservice.exception.ResourceNotFoundException;
import com.vetconnect.customerservice.repository.AddressesRepo;
import com.vetconnect.customerservice.repository.CustomersRepo;


@Service
public class CustomersServiceImpl implements CustomersService {

    private final AuthCredentialRepository authCredentialRepository;
	private final PasswordEncoder passwordEncoder;
   
	private final CustomersRepo customersRepo;
	public final AddressesRepo addressRepo;
	
	public CustomersServiceImpl(CustomersRepo customersRepo, AddressesRepo addressRepo, PasswordEncoder passwordEncoder, AuthCredentialRepository authCredentialRepository)
	{	this.addressRepo=addressRepo;
		this.passwordEncoder=passwordEncoder;
		this.authCredentialRepository=authCredentialRepository;
		this.customersRepo=customersRepo;
	}	
	
	private static final org.slf4j.Logger log=LoggerFactory.getLogger(CustomersServiceImpl.class);
	
	@Override
	@Transactional
	public CustomerResponse registerCustomers(CustomerRequest request) {
		
		
		if(customersRepo.existsByEmail(request.getEmail())) {
			log.warn("Duplicate customer registration attempt for email={}", request.getEmail());

			throw new DuplicateCustomerException("customer with "+ request.getEmail()+" already exists");
		}
		
		Customers customer=mapToEntity(request);
		customer.setCreatedAt(LocalDateTime.now());
		Customers savedCustomer=customersRepo.save(customer);
		
		CustomerResponse customerResp= mapToResponse(savedCustomer);
		
		AuthCredentials auth =new AuthCredentials();
		
		 auth.setUsername(savedCustomer.getEmail());
		 auth.setPasswordHash(passwordEncoder.encode(request.getPassword()));
		 auth.setCustomer(savedCustomer);
		 auth.setIsActive(true);
		 auth.setRoles(request.getRole());
		 
		 if(authCredentialRepository.existsByUsername(request.getEmail())) {
			 throw new DuplicateUserException("user alreday exists");
		 }
		 
		 authCredentialRepository.save(auth);
		log.info("Customer registered successfully with id={}",savedCustomer.getId());
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
	public CustomerResponse getCustomerDetails(int id, String username) {
		
		AuthCredentials auth=authCredentialRepository.findByUsername(username)
											.orElseThrow(()-> new ResourceNotFoundException("can not find user with username: "+username));
		
		int loggedInUserId=auth.getCustomer().getId();
		boolean isAdmin=auth.getRoles().contains("ROLE_ADMIN");
		
		if(!isAdmin && loggedInUserId!=id) {
			throw new CustomerAccessDeniedException("You can only access your own data");
		}
		
		Customers customers=customersRepo
				.findById(id)
				.orElseThrow(()->{
					log.warn("Customer with id{} does not exist",id);
					return new ResourceNotFoundException("Customer with id "+id+" not found");});
		
		
		CustomerResponse customerResp=mapToResponse(customers);
		log.info("Customer details fetched successfully for customer id{} ",id);
		return customerResp;
	}


	@Override
	public CustomerResponse updateCustomerDetails(int id, CustomerRequest request) {
		// TODO Auto-generated method stub
		
		//Customers customers=new Customers();	
		Customers customersUpdated=customersRepo.findById(id)
				.orElseThrow(()-> {
					log.error("Customer not found while updating customer details, id={}", id);
					return new ResourceNotFoundException("Customer with id "+id+"not found");});
		

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
		log.info("customer updated successfully for customerId={}",id);
		return customerResp;
	}

	@Override
	public CustomerResponse updateCustomerEmail(int id, String email) {
			Customers customer=customersRepo.findById(id)
					.orElseThrow(()->{
						log.error("Customer not found while updating email, id={}", id);
						return new ResourceNotFoundException("Customer with id "+id+" not found");
					});
			
			customer.setEmail(email);
			customer.setUpdatedAt(LocalDateTime.now());
			Customers entityResp=customersRepo.save(customer);
			
			log.info("Customers email id updated successfully, id={} ", id);
		return mapToResponse(entityResp);
	}
	
	@Override
	public void deleteCustomerDetails(int id) {
		// TODO Auto-generated method stub
		
		Customers customers=customersRepo.findById(id)
				.orElseThrow(()-> {
					log.warn("Customer with id{} not found", id);
					return new ResourceNotFoundException("Customer with id "+id+" not found");});
		
		//Customers customerResp=customers.get();
		
		customers.setActive(false);
		customers.setUpdatedAt(LocalDateTime.now());
		customersRepo.save(customers);
	}


	@Override
	public List<AddressResponse> getAddressForCustomer(int id) {
		// TODO Auto-generated method stub
		
		Customers customers=customersRepo.findById(id)
				.orElseThrow(()-> {
					log.warn("Customer with id{} not found",id);
					return new ResourceNotFoundException("Customer with id "+id+" not found");});
		
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
										.orElseThrow(()-> {
											log.warn("Customer with id{} not exists",id);
											return new ResourceNotFoundException("Customer with id "+id+" not found");});
		
		if(Boolean.FALSE.equals(customer.isActive())) {
			throw new ResourceInactiveException("Customer not active");
		}

		Address address=mapToAddress(addressRequest, customer);
		
		Address savedAddress=addressRepo.save(address);
		
		AddressResponse addressResponse=mapToAddressResponse(savedAddress);
		
		log.info("Customer addresses added successflly for customerid {}",id);;
		return addressResponse;
	}
	
	@Override
	public AddressResponse updateCustomerAddress(int customerId,int addressId,AddressRequest addressRequest) {
		Customers customer=customersRepo.findById(customerId)
				.orElseThrow(()->{ 
					log.warn("customer with id{} not found",customerId);
					return new ResourceNotFoundException("Customer with id "+customerId+" not found");});

		if(Boolean.FALSE.equals(customer.isActive())) {
		throw new ResourceInactiveException("Customer not active");
		}
		
		Address add=addressRepo.findById(addressId)
								.orElseThrow(()->
								{
									log.warn("Customer address with addressId {} not found",addressId );
									return new AddressNotFoundException("Address with id "+addressId+" not found for customer "+customerId);});
		
		if(add.getCustomer().getId() !=customerId) {
			throw new ResourceMismatchException("Customer id mismatch");
		}
		
		add.setAddressType(addressRequest.getAddressType());
		add.setStreet(addressRequest.getStreet());
		add.setCity(addressRequest.getCity());
		add.setState(addressRequest.getState());
		add.setCountry(addressRequest.getCountry());
		add.setZipCode(addressRequest.getZipCode());
		
		Address addResp=addressRepo.save(add);
		
		AddressResponse resp=mapToAddressResponse(addResp);
		
		log.info("Updated customer addresses successfully");
		return resp;
	}
	@Override
	public void deleteCustomerAddress(int customerId, int addressId) {
		Customers customer=customersRepo.findById(customerId)
						.orElseThrow(()-> {
							log.warn("customer with id{} not found",customerId);
							return new ResourceNotFoundException("Customer with id "+customerId+" not found");});
		if(Boolean.FALSE.equals(customer.isActive())) {
			throw new ResourceInactiveException("Customer not active");
			}
			
		
		Address address=addressRepo.findById(addressId)
										.orElseThrow(()->new 
										AddressNotFoundException("Address with id {addressId} not found for customer {customerId}"));
		
		if(address.getCustomer().getId()!=customerId) {
			throw new ResourceMismatchException("customer id mismatch");
		}
		
		addressRepo.delete(address);
										
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
	
	@Override
	public List<CustomersWithAddressResponse> testNPlusOne() {
		List<Customers> customers= customersRepo.findAll();
		
		
		return customers.stream()
				.map(this::mapToCustomerWithAddressResponse)
				.collect(Collectors.toList());
	}
	
	private CustomersWithAddressResponse mapToCustomerWithAddressResponse(Customers customers) {
		
		CustomersWithAddressResponse customersWithAddressResponse= new CustomersWithAddressResponse();
		
		customersWithAddressResponse.setAddressResponse(toAddressResponseList(customers.getAddresses()));
		customersWithAddressResponse.setActive(customers.isActive());
		customersWithAddressResponse.setId(customers.getId());
		customersWithAddressResponse.setDateOfBirth(customers.getDateOfBirth());
		customersWithAddressResponse.setEmail(customers.getEmail());
		customersWithAddressResponse.setFirstName(customers.getFirstName());
		customersWithAddressResponse.setLastName(customers.getLastName());
		customersWithAddressResponse.setPhoneNumber(customers.getPhoneNumber());
		return customersWithAddressResponse;
		
	}
	
}
