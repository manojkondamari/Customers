package com.vetconnect.customerservice.security;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;
	private final CustomerUserDetailsService userDetailsService;
	public AuthController(AuthenticationManager authenticationManager, JwtService jwtService, CustomerUserDetailsService userDetailsService) {
		this.authenticationManager=authenticationManager;
		this.jwtService=jwtService;
		this.userDetailsService=userDetailsService;
	}
	
	@PostMapping("/login")
	public ResponseEntity<String> login( @RequestBody LoginRequest request ) {
	
			
				Authentication authentication=authenticationManager.authenticate(
													new UsernamePasswordAuthenticationToken(
															request.getUsername(),
															request.getPassword()
															)
													);
			UserDetails userDetails=userDetailsService.loadUserByUsername(request.getUsername());
			String token=jwtService.generateToken(userDetails);
		
		
		return ResponseEntity.ok(token);
	}
}
