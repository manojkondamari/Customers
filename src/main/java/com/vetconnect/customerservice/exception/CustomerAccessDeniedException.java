package com.vetconnect.customerservice.exception;

public class CustomerAccessDeniedException extends RuntimeException{
	public CustomerAccessDeniedException(String message) {
		super(message);
	}
}
