package com.vetconnect.customerservice.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
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
	@Autowired
	private final JwtService jwtService;
	@Autowired
	private final CustomerUserDetailsService userDetailsService;
	@Autowired
	private JwtBlacklistService jwtBlacklistService;
	
	
	public JwtAuthenticationFilter(JwtService jwtService, CustomerUserDetailsService userDetailsService, JwtBlacklistService jwtBlacklistService) {
		this.jwtService=jwtService;
		this.userDetailsService=userDetailsService;
		this.jwtBlacklistService=jwtBlacklistService;
	}
	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String path=request.getServletPath();
		
		
		return path.startsWith("/auth")
				|| path.startsWith("/swagger-ui")
				|| path.startsWith("/v3/api-docs")
				|| path.startsWith("/customers/test-nplus1")
				|| path.equals("/error");
	
	}
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		
		// TODO Auto-generated method stub
		System.out.println(">>> JWT FIlter EXECUted");
		String authHeader=request.getHeader("Authorization");
		if(authHeader==null || !authHeader.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}
		String jwt=authHeader.substring(7);
		
		//Check blacklist first
		if(jwtBlacklistService.isTokenBlacklisted(jwt)) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().write("Token has been revoked");
			return;
		}
		 
		String username=jwtService.extractUsername(jwt);
		
//		String path=request.getServerName();
//		if (path.startsWith("/auth")) {
//			filterChain.doFilter(request, response);
//			return;
//		}
		
		if(username !=null && SecurityContextHolder.getContext().getAuthentication() ==null) {
			
			UserDetails userDetails=userDetailsService.loadUserByUsername(username);
			
			if(jwtService.isTokenValid(jwt, userDetails)) {
				UsernamePasswordAuthenticationToken authtoken= new UsernamePasswordAuthenticationToken(
																	userDetails, null, userDetails.getAuthorities());
				
				authtoken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				
				SecurityContextHolder.getContext()
								.setAuthentication(authtoken);
			}
		}
		

		filterChain.doFilter(request, response);
		
	}}
