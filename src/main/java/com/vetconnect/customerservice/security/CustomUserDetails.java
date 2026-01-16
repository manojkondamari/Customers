package com.vetconnect.customerservice.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

public class CustomUserDetails extends User{
	private final int customerId;
	
	public CustomUserDetails(String username, String password, Collection<? extends GrantedAuthority> auth, int customerId) {
		super(username, password, auth);
		this.customerId=customerId;
	}
	
	public int getCustomerId() {
		return customerId;
	}
}
