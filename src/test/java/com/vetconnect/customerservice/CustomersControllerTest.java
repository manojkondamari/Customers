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

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import com.vetconnect.customerservice.controller.CustomersController;
import com.vetconnect.customerservice.dto.CustomerRequest;
import com.vetconnect.customerservice.dto.CustomerResponse;
import com.vetconnect.customerservice.exception.DuplicateCustomerException;
import com.vetconnect.customerservice.repository.CustomersRepo;
import com.vetconnect.customerservice.service.CustomersService;


@WebMvcTest(CustomersController.class)
public class CustomersControllerTest {
	
	@Autowired
	private MockMvc mockMvc;
	
	@MockitoBean
	private CustomersService customerService;
	
	@Test
	void registerCustomer_ShouldReturn201() throws Exception {
		
		CustomerResponse response=new CustomerResponse();
		response.setId(1);
		when(customerService.registerCustomers(any())).thenReturn(response);
		
		mockMvc.perform(post("/api/customers")
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
		mockMvc.perform(post("/api/customers",customerId)
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
		
		
		mockMvc.perform(post("/api/customers")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						
						{"firstName":"john",
						"email":"john@gmail.com"}"""))
		.andExpect(status().isConflict());
		
		verify(customerService).registerCustomers(any());
		
	}
	
	@Test
	void getCustomerDetails_ShouldReturn200_WhenCustomerExists() throws Exception {
		
		CustomerResponse response=new CustomerResponse();
		response.setId(1);
		when(customerService.getCustomerDetails(anyInt())).thenReturn(response);
		
		mockMvc.perform(get("/api/customers/{id}",1)
				.contentType(MediaType.APPLICATION_JSON))
		.andExpect(status().isOk());
		
		verify(customerService).getCustomerDetails(1);
	}
	
	@Test
	void updateCustomerEmail_ShouldReturnOk() throws Exception{
		CustomerResponse response=new CustomerResponse();
		response.setId(1);
		response.setEmail("john@gmail.com");
		
		when(customerService.updateCustomerEmail(anyInt(), anyString())).thenReturn(response);
		
		mockMvc.perform(patch("/api/customers/{id}",1)
				.contentType(MediaType.APPLICATION_JSON)
				.param("email","john@gmail.com"))
		.andExpect(status().isOk());
		
		verify(customerService).updateCustomerEmail(1, "john@gmail.com");
		}
	
	@Test
	void updateCustomerDetails_ShouldReturn_Created() throws Exception {
		CustomerResponse resp=new CustomerResponse();
		resp.setId(2);
		resp.setEmail("john@gmail.com");
		when(customerService.updateCustomerDetails(anyInt(),any())).thenReturn(resp);
		
		mockMvc.perform(put("/api/customers/{id}",2)
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
		mockMvc.perform(delete("/api/customers/{id}",2))
						.andExpect(status().isNoContent());
	}
	
	
}
