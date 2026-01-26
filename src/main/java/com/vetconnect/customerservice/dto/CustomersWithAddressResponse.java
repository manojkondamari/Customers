package com.vetconnect.customerservice.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class CustomersWithAddressResponse {
	private int id;
	private String firstName;
	private String lastName;
	private String email;
	private String phoneNumber;
	private LocalDate dateOfBirth;
	private boolean isActive;
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public LocalDate getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(LocalDate dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}


	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}


	List<AddressResponse> addressResponse;


	public List<AddressResponse> getAddressResponse() {
		return addressResponse;
	}

	public void setAddressResponse(List<AddressResponse> addressResponse) {
		this.addressResponse = addressResponse;
	}
}
