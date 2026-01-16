package com.vetconnect.customerservice.security;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter{
	private final JwtService jwtService;
	private final CustomerUserDetailsService userDetailsService;
	
	
	public JwtAuthenticationFilter(JwtService jwtService, CustomerUserDetailsService userDetailsService) {
		this.jwtService=jwtService;
		this.userDetailsService=userDetailsService;
	}
	
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		
		// TODO Auto-generated method stub
		
		String path=request.getServerName();
		if (path.startsWith("/auth")) {
			filterChain.doFilter(request, response);
			return;
		}
		System.out.println(">>> JWT FIlter EXECUted");
		final String authHeader=request.getHeader("Authorization");
		final String jwt;
		final String username;
		
		if(authHeader==null || !authHeader.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}
		
		jwt=authHeader.substring(7);
		
		username=jwtService.extractUsername(jwt);
		if(username !=null && SecurityContextHolder.getContext().getAuthentication() ==null) {
			
			UserDetails userDetails=userDetailsService.loadUserByUsername(username);
			
			if(jwtService.isTokenValid(jwt, userDetails)) {
				UsernamePasswordAuthenticationToken authtoken= new UsernamePasswordAuthenticationToken(
																	userDetails, null, userDetails.getAuthorities());
				
				SecurityContextHolder.getContext()
								.setAuthentication(authtoken);
			}
		}
		

		filterChain.doFilter(request, response);
		
	}}
