package com.vetconnect.customerservice;


import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsInstanceOf;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;

import com.vetconnect.customerservice.dto.AddressRequest;
import com.vetconnect.customerservice.dto.AddressResponse;
import com.vetconnect.customerservice.dto.CustomerRequest;
import com.vetconnect.customerservice.dto.CustomerResponse;
import com.vetconnect.customerservice.entity.Address;
import com.vetconnect.customerservice.entity.AuthCredentials;
import com.vetconnect.customerservice.entity.Customers;
import com.vetconnect.customerservice.exception.CustomerAccessDeniedException;
import com.vetconnect.customerservice.exception.DuplicateCustomerException;
import com.vetconnect.customerservice.exception.ResourceInactiveException;
import com.vetconnect.customerservice.exception.ResourceMismatchException;
import com.vetconnect.customerservice.exception.ResourceNotFoundException;
import com.vetconnect.customerservice.repository.AddressesRepo;
import com.vetconnect.customerservice.repository.CustomersRepo;
import com.vetconnect.customerservice.security.AuthCredentialRepository;
import com.vetconnect.customerservice.service.CustomersServiceImpl;

import net.bytebuddy.description.annotation.AnnotationList.Empty;
import static org.mockito.ArgumentMatchers.anyString;


@ExtendWith(MockitoExtension.class)
public class CustomerServiceImplTest {
	
	@Mock
	private CustomersRepo customersRepo;
	
	@Mock
	private AuthCredentialRepository authCredentialRepository;
	@Mock
	private PasswordEncoder passwordEncoder;
	@InjectMocks
	private CustomersServiceImpl customersService;
	
	@Mock
	private AddressesRepo addressRepo;
	
	@BeforeEach
	void setup() {
	 customersService=new CustomersServiceImpl(customersRepo, addressRepo, passwordEncoder, authCredentialRepository);
	}
	
	@Test
	void registerCustomerShouldSaveAndReturnResponse_WhenRequestIsValid() {
		CustomerRequest req=buildValidCustomerRequest();
		
		Customers savedEntity=buildValidCustomerEntity(req);
		
		when(customersRepo.save(any(Customers.class))).thenReturn(savedEntity);
		
		when(passwordEncoder.encode(anyString())).thenReturn("hashed-password");
		when(authCredentialRepository.save(any(AuthCredentials.class))).thenAnswer(invocation -> invocation.getArgument(0));
		
		CustomerResponse resp=customersService.registerCustomers(req);
		
		ArgumentCaptor<Customers> captor=ArgumentCaptor.forClass(Customers.class);
		verify(customersRepo, times(1)).save(captor.capture());
		verify(authCredentialRepository, times(1)).save(any(AuthCredentials.class));
		verify(passwordEncoder,times(1)).encode(anyString());
		
		Customers toSave=captor.getValue();
		verify(customersRepo,times(1)).save(any(Customers.class));
		assertThat(toSave.getId()).isEqualTo(0);
		assertThat(toSave.getFirstName()).isEqualTo(req.getFirstName());
		assertThat(toSave.getLastName()).isEqualTo(req.getLastName());
		assertThat(toSave.getEmail()).isEqualTo(req.getEmail());
		assertThat(toSave.getPhoneNumber()).isEqualTo(req.getPhoneNumber());
		assertThat(toSave.getDateOfBirth()).isEqualTo(req.getDateOfBirth());
		assertThat(toSave.getCreatedAt()).isNotNull();
		assertThat(toSave.getUpdatedAt()).isNotNull();
		
		
		assertThat(resp).isNotNull();
		assertThat(resp.getId()).isEqualTo(45);
		assertThat(resp.getFirstName()).isEqualTo(req.getFirstName());
		assertThat(resp.getLastName()).isEqualTo(req.getLastName());
		assertThat(resp.getEmail()).isEqualTo(req.getEmail());
		assertThat(resp.getPhoneNumber()).isEqualTo(req.getPhoneNumber());
		assertThat(resp.getDateOfBirth()).isEqualTo(req.getDateOfBirth());
		assertThat(resp.getCreatedAt()).isNotNull();
		
	}
	@Test
	void registerCustomers_ShouldPropagateException_WhenEmailIsDuplicate() {
		
		CustomerRequest req=buildValidCustomerRequest();
		
		req.setEmail("john@gmail.com");
		
		when(customersRepo.existsByEmail(req.getEmail())).thenReturn(true);
		
		assertThatThrownBy(()->customersService.registerCustomers(req))
							.isInstanceOf(DuplicateCustomerException.class);
		
		verify(customersRepo, times(1)).existsByEmail(req.getEmail());
	}
	
	@Test
	void registerCustomer_ShouldPropagateException_WhenSaveFails() {
		CustomerRequest req=new CustomerRequest();
		
		req.setFirstName("A");
		req.setLastName("B");
		req.setEmail("dup@gmail.com");
		
		when(customersRepo.save(any(Customers.class))).thenThrow(new RuntimeException("DB Error"));
	
		assertThatThrownBy(()->customersService.registerCustomers(req))
				.isInstanceOf(RuntimeException.class)
				.hasMessageContaining("DB Error");
		
		verify(customersRepo, times(1)).save(any(Customers.class));
}
	@Test
	void getCustomerDetails_ShouldReturnCustomer_WhenIdExists() {
		CustomerRequest req=buildValidCustomerRequest();
		
		Customers savedEntity=buildValidCustomerEntity(req);
		int customerId=45;
		String username="johnfrank@gmail.com";
		AuthCredentials authEntity=buildValidAuthEntity(savedEntity);
		
		when(authCredentialRepository.findByUsername(anyString())).thenReturn(Optional.of(authEntity));
		when(customersRepo.findById(customerId)).thenReturn(Optional.of(savedEntity));
		
		CustomerResponse resp=customersService.getCustomerDetails(45, username);
		
		assertThat(resp.getClass()).isNotNull();
		assertThat(resp.getId()).isEqualTo(45);
		assertThat(resp.getFirstName()).isEqualTo(req.getFirstName());
		assertThat(resp.getEmail()).isEqualTo(req.getEmail());
		
		verify(customersRepo, times(1)).findById(customerId);
		verify(authCredentialRepository, times(1)).findByUsername(username);
	}
	
	private AuthCredentials buildValidAuthEntity(Customers savedEntity) {
		AuthCredentials auth=new AuthCredentials();
		
		String username="johnfrank@gmail.com";
		auth.setRoles("ROLE_USER");
		auth.setCustomer(savedEntity);
		auth.setUsername(username);
		
		return auth;
	}
	
	@Test
	void getCustomerDetails_ShouldThrowException_WhenCustomerNotFound() {
		int customerId=45;
		CustomerRequest req=buildValidCustomerRequest();
		Customers savedEntity=buildValidCustomerEntity(req);
		AuthCredentials authEntity=buildValidAuthEntity(savedEntity);
		when(authCredentialRepository.findByUsername(anyString())).thenReturn(Optional.of(authEntity));

		when(customersRepo.findById(customerId)).thenReturn(Optional.empty());
		
		assertThatThrownBy(()->customersService.getCustomerDetails(customerId,"johnfrank@gmail.com"))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessageContaining("Customer with id "+customerId+" not found");
		
		verify(customersRepo, times(1)).findById(customerId);
	}
	
	@Test
	void getCustomerDetails_ShouldThrowException_WhenCustomerMismatch() {
		
		int customerId=1;
		CustomerRequest req=buildValidCustomerRequest();
		Customers savedEntity=buildValidCustomerEntity(req);
		AuthCredentials authEntity=buildValidAuthEntity(savedEntity);
		
		when(authCredentialRepository.findByUsername(anyString())).thenReturn(Optional.of(authEntity));
		
		assertThatThrownBy(()->customersService.getCustomerDetails(customerId, "johnfrank@gmail.com"))
		    				.isInstanceOf(CustomerAccessDeniedException.class)
		    				.hasMessage("You can only access your own data");
		
		
	}
	
	@Test
	void updateCustomerDetails_ShouldReturn_UpdatedDetails() {
		
		int customerId=45;
		CustomerRequest req=buildValidCustomerRequest();
		req.setFirstName("babu");
		Customers customers=buildValidCustomerEntity(req);
		
		when(customersRepo.save(any(Customers.class))).thenReturn(customers);
		when(customersRepo.findById(customerId)).thenReturn(Optional.of(customers));
		CustomerResponse resp=customersService.updateCustomerDetails(customerId, req);
		
		
		ArgumentCaptor<Customers> captor=ArgumentCaptor.forClass(Customers.class);
		verify(customersRepo, times(1)).save(captor.capture());
		Customers toSave=captor.getValue();
		assertThat(toSave.getFirstName()).isEqualTo(req.getFirstName());
		assertThat(toSave.getEmail()).isEqualTo(req.getEmail());
		
		assertThat(resp.getFirstName()).isEqualTo(req.getFirstName());
		assertThat(resp.getUpdatedAt()).isNotNull();
		verify(customersRepo,times(1)).findById(customerId);
		
	}		

	
	@Test
	void updateCustomerEmail_ShouldEmailAndPersistChange() {
		CustomerRequest req=buildValidCustomerRequest();
		//req.setEmail("john@gmail.com");
		int customerId=45;
		Customers customer=buildValidCustomerEntity(req);
		
		when(customersRepo.findById(45)).thenReturn(Optional.of(customer));
		when(customersRepo.save(any())).thenReturn(customer);

		CustomerResponse resp=customersService.updateCustomerEmail(customerId, "john@gmail.com");
		
		ArgumentCaptor<Customers> captor=ArgumentCaptor.forClass(Customers.class);
		verify(customersRepo).save(captor.capture());
		Customers toSave=captor.getValue();
		assertThat(toSave.getEmail()).isEqualTo("john@gmail.com");
		assertThat(resp.getEmail()).isEqualTo("john@gmail.com");
		assertThat(resp.getUpdatedAt()).isNotNull();
		
		verify(customersRepo,times(1)).findById(45);
	}
	
	@Test
	void deleteCustomerDetails_ShouldSoftDeleteCustomerWhenCustomerExists() {
		int customerId=1;
		Customers savedEntity=buildValidCustomerEntity(buildValidCustomerRequest());
		
		savedEntity.setId(customerId);
		savedEntity.setActive(true);
		savedEntity.setUpdatedAt(null);
		//when(customersRepo.delete(any())).thenReturn(Optional.of());
		when(customersRepo.findById(customerId)).thenReturn(Optional.of(savedEntity));
		when(customersRepo.save(any(Customers.class))).thenReturn(savedEntity);
		
		customersService.deleteCustomerDetails(customerId);
		ArgumentCaptor<Customers> captor=ArgumentCaptor.forClass(Customers.class);
		
		verify(customersRepo).save(captor.capture());
		
		Customers toSave=captor.getValue();
		
		assertThat(toSave.getUpdatedAt()).isNotNull();
		assertThat(toSave.isActive()).isFalse();
		
		verify(customersRepo, times(1)).findById(customerId);
		
		verify(customersRepo,times(1)).save(savedEntity);
	}
	
	
	private CustomerRequest buildValidCustomerRequest() {
		CustomerRequest req=new CustomerRequest();
		req.setFirstName("John");
		req.setLastName("frank");
		req.setEmail("johnfrank@gmail.com");
		req.setPhoneNumber("899999999");
		req.setDateOfBirth(LocalDate.of(1990,5,14));
		
		req.setPassword("password123");
		return req;
	}
	
	private Customers buildValidCustomerEntity(CustomerRequest req) {
		Customers savedEntity=new Customers();
		
		savedEntity.setId(45);
		savedEntity.setFirstName(req.getFirstName());
		savedEntity.setLastName(req.getLastName());
		savedEntity.setEmail(req.getEmail());
		savedEntity.setActive(true);
		savedEntity.setPhoneNumber(req.getPhoneNumber());
		savedEntity.setDateOfBirth(req.getDateOfBirth());
		savedEntity.setUpdatedAt(LocalDateTime.now());
		savedEntity.setCreatedAt(LocalDateTime.now());
		
		return savedEntity;
	}
	
	@Test
	void registerCustomerAddress_ShouldReturnAddress() {
		AddressRequest addReq=buildValidAddressRequest();
		int customerId=45;
		
		CustomerRequest req=buildValidCustomerRequest();
		Address address=buildValidAddressEntity(addReq);
		Customers customers=buildValidCustomerEntity(req);
		address.setCustomer(customers);
		when(customersRepo.findById(customerId)).thenReturn(Optional.of(customers));
		when(addressRepo.save(any())).thenReturn(address);
		
		AddressResponse resp=customersService.registerCustomerAddresses(customerId, addReq);
		
		ArgumentCaptor<Address> captor=ArgumentCaptor.forClass(Address.class);
		
		verify(addressRepo).save(captor.capture());
		
		verify(customersRepo).findById(customerId);
		
		Address toSave=captor.getValue();
		//assertThat(toSave.getId()).isEqualTo(10);
		assertThat(toSave.getAddressType()).isEqualTo(addReq.getAddressType());
		assertThat(toSave.getStreet()).isEqualTo(addReq.getStreet());
		assertThat(toSave.getCity()).isEqualTo(addReq.getCity());
		assertThat(toSave.getState()).isEqualTo(addReq.getState());
		assertThat(toSave.getCountry()).isEqualTo(addReq.getCountry());
		assertThat(toSave.getZipCode()).isEqualTo(addReq.getZipCode());
		assertThat(toSave.getCustomer()).isEqualTo(customers);
		
		//assertThat(resp.getId()).isEqualTo(10);
		assertThat(resp.getAddressType()).isEqualTo(addReq.getAddressType());
		assertThat(resp.getStreet()).isEqualTo(addReq.getStreet());
		assertThat(resp.getCity()).isEqualTo(addReq.getCity());
		assertThat(resp.getState()).isEqualTo(addReq.getState());
		assertThat(resp.getCountry()).isEqualTo(addReq.getCountry());
		assertThat(resp.getZipCode()).isEqualTo(addReq.getZipCode());
	}
	
	@Test
	void registerCustomerAddress_ShouldThrowException_WhenCustomerIsNotFound() {
		int customerId=3;
		AddressRequest req=buildValidAddressRequest();
		when(customersRepo.findById(customerId)).thenReturn(Optional.empty());
		
		assertThatThrownBy(()->customersService.registerCustomerAddresses(customerId, req))
							.isInstanceOf(ResourceNotFoundException.class)
							.hasMessageContaining("Customer with id "+customerId+" not found");
		
		verify(addressRepo, never()).save(any());
	}
	
	@Test
	void registerCustomerAddress_ShouldThrowException_WhenCustomerIsInActive() {
		int customerId=3;
		AddressRequest req=buildValidAddressRequest();
		CustomerRequest cusReq=buildValidCustomerRequest();
		Customers customer=buildValidCustomerEntity(cusReq);
		
		customer.setActive(false);
		when(customersRepo.findById(customerId)).thenReturn(Optional.of(customer));
		
		assertThatThrownBy(()->customersService.registerCustomerAddresses(customerId, req))
							.isInstanceOf(ResourceInactiveException.class);
	
		verify(addressRepo, never()).save(any());
		
	}
	
	@Test
	void getAddressForCustomer_ShouldReturnListOfCustomerAddresses() {
		int customerId=10;
		AddressRequest addReq=buildValidAddressRequest();
		Customers customer=buildValidCustomerEntity(buildValidCustomerRequest());
		Address address=buildValidAddressEntity(addReq);
		List<Address> addList=List.of(address);
		
		
		when(customersRepo.findById(customerId)).thenReturn(Optional.of(customer));
		when(addressRepo.findAddressByCustomerId(customerId)).thenReturn(addList);
		
		List<AddressResponse> resp=customersService.getAddressForCustomer(customerId);
		
		AddressResponse addResp=resp.get(0);
		
		assertThat(addResp.getAddressType()).isEqualTo(addReq.getAddressType());
		assertThat(addResp.getStreet()).isEqualTo(addReq.getStreet());
		assertThat(addResp.getCity()).isEqualTo(addReq.getCity());
		assertThat(addResp.getState()).isEqualTo(addReq.getState());
		assertThat(addResp.getZipCode()).isEqualTo(addReq.getZipCode());
		assertThat(addResp.getCountry()).isEqualTo(addReq.getCountry());
		

		verify(customersRepo, times(1)).findById(customerId);
		verify(addressRepo, times(1)).findAddressByCustomerId(customerId);
	}

	@Test
	void updateCustomerAddress_ShouldReturnUpdatedAddress_WhenIdMatches() {
		int customerId=45;
		Customers customer=buildValidCustomerEntity(buildValidCustomerRequest());
		int addressId=102;
		AddressRequest addReq=buildValidAddressRequest();
		Address address=buildValidAddressEntity(addReq);
		customer.setActive(true);
		address.setCustomer(customer);
		
		addReq.setAddressType("work");
		addReq.setState("amaravati");
		addReq.setzipCode("560012");
		when(customersRepo.findById(customerId)).thenReturn(Optional.of(customer));
		when(addressRepo.findById(addressId)).thenReturn(Optional.of(address));
		when(addressRepo.save(any())).thenReturn(address);
		AddressResponse response=customersService.updateCustomerAddress(customerId, addressId, addReq);
		
		ArgumentCaptor<Address> captor=ArgumentCaptor.forClass(Address.class);
		
		verify(addressRepo).save(captor.capture());
		
		Address toSave=captor.getValue();
		
		assertThat(toSave.getAddressType()).isEqualTo(addReq.getAddressType());
		assertThat(toSave.getState()).isEqualTo(addReq.getState());
		assertThat(toSave.getZipCode()).isEqualTo(addReq.getZipCode());
		
		assertThat(response.getAddressType()).isEqualTo(addReq.getAddressType());
		assertThat(response.getState()).isEqualTo(addReq.getState());
		assertThat(response.getZipCode()).isEqualTo(addReq.getZipCode());
		
		
		verify(customersRepo).findById(customerId);
		verify(addressRepo).findById(addressId);
		
	}
	@Test
	void updateCustomerAddress_ShouldThrowException_WhenIdMismatch() {
		int customerId=45;
		int addressId=102;
		AddressRequest addReq=buildValidAddressRequest();
		
		Customers customer=buildValidCustomerEntity(buildValidCustomerRequest());
		customer.setActive(true);
		Customers actualOwner=buildValidCustomerEntity(buildValidCustomerRequest());
		actualOwner.setId(4);
		
		Address address=buildValidAddressEntity(addReq);
		address.setCustomer(actualOwner);
		
		when(customersRepo.findById(customerId)).thenReturn(Optional.of(customer));
		when(addressRepo.findById(addressId)).thenReturn(Optional.of(address));
		
		assertThatThrownBy(()->customersService.updateCustomerAddress(customerId, addressId, addReq))
							.isInstanceOf(ResourceMismatchException.class);
		
		verify(addressRepo,never()).save(any());
	}
	
	@Test
	void deleteCustomerAddress_ShouldDeleteAddress() {
		
		int customerId=45;
		int addressId=102;
		
		Customers customer=buildValidCustomerEntity(buildValidCustomerRequest());
		
		Address address=buildValidAddressEntity(buildValidAddressRequest());
		address.setCustomer(customer);
		when(customersRepo.findById(customerId)).thenReturn(Optional.of(customer));
		when(addressRepo.findById(addressId)).thenReturn(Optional.of(address));
		
		customersService.deleteCustomerAddress(customerId, addressId);
		
		verify(customersRepo).findById(customerId);
		verify(addressRepo).findById(addressId);
		verify(addressRepo).delete(address);
		
	}
	
	private AddressRequest buildValidAddressRequest() {
		AddressRequest req=new AddressRequest();
		req.setAddressType("home");
		req.setStreet("Thanisandra");
		req.setCity("Bengaluru");
		req.setState("Karnataka");
		req.setCountry("India");
		req.setzipCode("560077");
		
		return req;
	}
	
	private Address buildValidAddressEntity(AddressRequest req) {
		Customers customer=new Customers();
		Address address=new Address();
		
		address.setCustomer(customer);
		address.setId(102);
		address.setAddressType(req.getAddressType());
		address.setStreet(req.getStreet());
		address.setCity(req.getCity());
		address.setState(req.getState());
		address.setCountry(req.getCountry());
		address.setZipCode(req.getZipCode());
		
		return address;
	}
}