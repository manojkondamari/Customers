package com.vetconnect.customerservice;



import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.PutMapping;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import com.vetconnect.customerservice.controller.CustomersController;
import com.vetconnect.customerservice.dto.AddressRequest;
import com.vetconnect.customerservice.dto.AddressResponse;
import com.vetconnect.customerservice.dto.CustomerRequest;
import com.vetconnect.customerservice.dto.CustomerResponse;
import com.vetconnect.customerservice.exception.DuplicateCustomerException;
import com.vetconnect.customerservice.exception.ResourceMismatchException;
import com.vetconnect.customerservice.repository.CustomersRepo;
import com.vetconnect.customerservice.security.CustomerUserDetailsService;
import com.vetconnect.customerservice.security.JwtBlacklistService;
import com.vetconnect.customerservice.security.JwtService;
import com.vetconnect.customerservice.security.SecurityConfig;
import com.vetconnect.customerservice.service.CustomersService;

import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;


@AutoConfigureMockMvc(addFilters=false)
@WebMvcTest(
	    controllers = CustomersController.class,
	    excludeAutoConfiguration = {
	        SecurityAutoConfiguration.class,
	        SecurityFilterAutoConfiguration.class
	    }
	)
public class CustomersControllerTest {
	
	@Autowired
	private MockMvc mockMvc;
	   
	@MockBean
	private CustomersService customerService;
	
	@MockBean
	private JwtBlacklistService jwtBlacklistService;
	@MockBean
	private RedisTemplate<String, String> redisTemplate;
	@MockBean
	StringRedisTemplate stringRedisTemplate;
	@MockBean
	private CustomerUserDetailsService customerUserDetailsService;
	@MockBean
	private JwtService jwtService;
	@Test
	void registerCustomer_ShouldReturn201() throws Exception {
		
		CustomerResponse response=new CustomerResponse();
		response.setId(1);
		when(customerService.registerCustomers(any())).thenReturn(response);
		
		mockMvc.perform(post("/customers")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						"firstName":"john",
						"email":"john@gmail.com"
							}
						"""))
				.andExpect(status().isCreated());
		
		verify(customerService).registerCustomers(any());
	}
	
	
	@Test
	void registerCustomer_ShouldReturn400BadRequest_WhenNotValid() throws Exception{
		
		int customerId=2;
		mockMvc.perform(post("/customers",customerId)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"firstName":"John",
						"lastName":"Greesham"}
						"""))
		.andExpect(status().isBadRequest());
		
		verify(customerService, never()).registerCustomers(any());
	}
	
	
	@Test
	void registerCustomer_ShouldReturn409ConflictWithEmail() throws Exception {
		int customerId=2;
		//CustomerRequest req
		when(customerService.registerCustomers(any()))
			.thenThrow(new DuplicateCustomerException("customer with \"+ john@gmail.com+\" already exists"));
		
		
		mockMvc.perform(post("/customers")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						
						{"firstName":"john",
						"email":"john@gmail.com"}"""))
		.andExpect(status().isConflict());
		
		verify(customerService).registerCustomers(any());
		
	}
	
	@Test
	@WithMockUser(username="babu@neel.com", roles="USER")
	void getCustomerDetails_ShouldReturn200_WhenCustomerExists() throws Exception {
		
		CustomerResponse response=new CustomerResponse();
		response.setId(5);
		response.setEmail("babu@neel.com");
		when(customerService.getCustomerDetails(anyInt(), anyString())).thenReturn(response);
		
		mockMvc.perform(get("/customers/{id}",5)
				.contentType(MediaType.APPLICATION_JSON))
		.andExpect(status().isOk());
		
		verify(customerService).getCustomerDetails(5,"babu@neel.com");
	}
	
	
	@Test
	void updateCustomerEmail_ShouldReturnOk() throws Exception{
		CustomerResponse response=new CustomerResponse();
		response.setId(1);
		response.setEmail("john@gmail.com");
		
		when(customerService.updateCustomerEmail(anyInt(), anyString())).thenReturn(response);
		
		mockMvc.perform(patch("/customers/{id}",1)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						
						{
						"email":"john@gmail.com"}"""))
		.andExpect(status().isOk());
		
		verify(customerService).updateCustomerEmail(1, "john@gmail.com");
		}
	
	
	@Test
	void updateCustomerDetails_ShouldReturn_Created() throws Exception {
		CustomerResponse resp=new CustomerResponse();
		resp.setId(2);
		resp.setEmail("john@gmail.com");
		when(customerService.updateCustomerDetails(anyInt(),any())).thenReturn(resp);
		
		mockMvc.perform(put("/customers/{id}",2)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"email":"john@gmail.com",
						"firstName":"John"
					}"""))
		.andExpect(status().isOk());
		
		verify(customerService).updateCustomerDetails(anyInt(), any());
	}
	
	
	@Test
	void deleteCustomerDetails_ShouldReturnNothing() throws Exception {
		mockMvc.perform(delete("/customers/{id}",2))
						.andExpect(status().isNoContent());
	}
	
	
	@Test
	void registerCustomerAddress_ShouldReturn201Created_WhenCustomerIsActive() throws Exception{
		int customerId=1;
		
		AddressResponse resp=new AddressResponse();
		resp.setAddressType("home");
		resp.setId(13);
		resp.setCountry("India");
		resp.setState("Karnataka");
		
		when(customerService.registerCustomerAddresses(anyInt(), any())).thenReturn(resp);
		
		mockMvc.perform(post("/customers/{id}/addresses",1)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"addressType":"home",
								"country":"India",
								"state":"Karnataka"
								}
								"""))
		.andExpect(status().isCreated());
		
		verify(customerService).registerCustomerAddresses(anyInt(), any());
	}
	
	
	@Test
	void getCustomerAddress_ShouldReturnOk_WhenCustomerIsActive() throws Exception{
		AddressResponse resp=new AddressResponse();
		resp.setAddressType("home");
		resp.setId(13);
		resp.setCountry("India");
		resp.setState("Karnataka");
		List<AddressResponse> resList=List.of(resp);
		when(customerService.getAddressForCustomer(anyInt())).thenReturn(resList);
		
		mockMvc.perform(get("/customers/{id}/addresses",1)
						.contentType(MediaType.APPLICATION_JSON))
		.andExpect(status().isOk());
		
		verify(customerService).getAddressForCustomer(anyInt());
	}
	
	
	@Test
	void updateCustomerAddress_ShouldReturnOk_WhenCustomerActiveAndCustomerDetailsMatch() throws Exception{
		int customerId=3;
		int addressId=102;
		
		AddressResponse resp=new AddressResponse();
		resp.setCity("chennai");
		resp.setAddressType("home");
		resp.setCountry("India");
		
		when(customerService.updateCustomerAddress(anyInt(), anyInt(), any())).thenReturn(resp);
		
		mockMvc.perform(put("/customers/{customerId}/addresses/{addressId}",3,5)
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{"city":"chennai",
							"addressType":"home",
							"country":"India"}
							"""))
		.andExpect(status().isOk());
		verify(customerService).updateCustomerAddress(anyInt(), anyInt(), any());
	}
	
	
	@Test
	void updateCustomerAddress_ShouldThrowException_WhenCustomerIdNotMatches() throws Exception{
		int customerId=3;
		int addressId=103;
		
		when(customerService.updateCustomerAddress(anyInt(), anyInt(), any()))
										.thenThrow( new ResourceMismatchException("Customer id mismatch"));
		
		mockMvc.perform(put("/customers/{customerId}/addresses/{addressId}",3,5)
					.contentType(MediaType.APPLICATION_JSON))
		.andExpect(status().isBadRequest());
		
		//verify(customerService).updateCustomerAddress(anyInt(), anyInt(), any());
	}
	
	
	@Test
	void deleteCustomerAddress_ShouldReturnNoContent() throws Exception{
		mockMvc.perform(delete("/customers/{customerId}/addresses/{addressId}",1,2))
			.andExpect(status().isNoContent());
	}
}
