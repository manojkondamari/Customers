package com.vetconnect.customerservice.exception;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.vetconnect.customerservice.dto.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;


@RestControllerAdvice
public class GlobalExceptionHandler {
	
	@ExceptionHandler(DuplicateCustomerException.class)
	public ResponseEntity<ApiError> handleDuplicateCustomer(DuplicateCustomerException ex,
			HttpServletRequest request){
		
		//Map<String, String> fieldErrors=new HashMap<>();
		ApiError error=new ApiError(
				LocalDateTime.now(),
				HttpStatus.CONFLICT.value(),
				"Duplicate Customer",
				ex.getMessage(),
				null,
				request.getRequestURI()
				);
		
		return ResponseEntity
				.status(HttpStatus.CONFLICT)
				.body(error);
	}
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiError> handleValidationErrors(MethodArgumentNotValidException ex,
			HttpServletRequest request){
		Map<String,String> errors=new HashMap<>();
		ex.getBindingResult()
			.getFieldErrors()
			.forEach(error -> 
					errors.put(
							error.getField(), 
							error.getDefaultMessage()
				)
			);
		
		ApiError apiError=new ApiError(
				LocalDateTime.now(),
				HttpStatus.BAD_REQUEST.value(),
				"validation failed",
				ex.getMessage(),
				errors,
				request.getRequestURI()
				);
		return ResponseEntity
				.status(HttpStatus.BAD_REQUEST)
				.body(apiError);
	}
}
