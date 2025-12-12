package com.vetconnect.customerservice.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name="auth_credentials")
public class AuthCredentials {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	@OneToOne
	@JoinColumn(name="customerId", unique=true)
	private Customers customer;
	
	private String passwordHash;
	private String roles;
	private String isActive;
	private LocalDateTime lastLogin; 
}
