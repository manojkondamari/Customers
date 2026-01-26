package com.vetconnect.customerservice.security;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;
	private final CustomerUserDetailsService userDetailsService;
	private JwtBlacklistService jwtBlacklistService;
	
	public AuthController(AuthenticationManager authenticationManager, JwtService jwtService, 
			CustomerUserDetailsService userDetailsService, JwtBlacklistService jwtBlacklistService) {
		this.authenticationManager=authenticationManager;
		this.jwtService=jwtService;
		this.userDetailsService=userDetailsService;
		this.jwtBlacklistService=jwtBlacklistService;
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
	@PostMapping("/logout")
	public ResponseEntity<String> logout(@RequestHeader("Authorization") String authHeader){
		if(authHeader!=null && authHeader.startsWith("Bearer ")) {
			String token=authHeader.substring(7);
			jwtBlacklistService.blacklistToken(token);
			
			return ResponseEntity.ok("Logged out successfully");
		}
		return ResponseEntity.badRequest().body("Invalid token");
	}
}
