package com.vetconnect.userservice;


import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;

import com.vetconnect.customerservice.dto.CustomerRequest;
import com.vetconnect.customerservice.dto.CustomerResponse;
import com.vetconnect.customerservice.entity.Customers;
import com.vetconnect.customerservice.repository.CustomersRepo;
import com.vetconnect.customerservice.service.CustomersServiceImpl;


@ExtendWith(MockitoExtension.class)
public class CustomerServiceImplTest {
	
	@Mock
	private CustomersRepo customersRepo;
	
	private CustomersServiceImpl customersService;
	
	@BeforeEach
	void setup() {
	 customersService=new CustomersServiceImpl(customersRepo);
	}
	
	@Test
	void registerCustomerShouldSaveAndReturnResponse_WhenRequestIsValid() {
		CustomerRequest req=new CustomerRequest();
		
		req.setFirstName("John");
		req.setLastName("frank");
		req.setEmail("johnfrank@gmail.com");
		req.setPhoneNumber("899999999");
		req.setDateOfBirth(LocalDate.of(1990,5,14));
		
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
		
		when(customersRepo.save(any(Customers.class))).thenReturn(savedEntity);
		
		CustomerResponse resp=customersService.registerCustomers(req);
		
		ArgumentCaptor<Customers> captor=ArgumentCaptor.forClass(Customers.class);
		verify(customersRepo, times(1)).save(captor.capture());
		
		Customers toSave=captor.getValue();
		
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
		assertThat(resp.getCreatedAt()).isEqualTo(resp.getCreatedAt());
		
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
}