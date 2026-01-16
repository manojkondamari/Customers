package com.vetconnect.customerservice.security;

import java.util.Arrays;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.vetconnect.customerservice.entity.AuthCredentials;


@Service
public class CustomerUserDetailsService implements UserDetailsService{

	
	private final AuthCredentialRepository authRepo;
	
	public CustomerUserDetailsService(AuthCredentialRepository authRepo) {
		
		this.authRepo=authRepo;
	}
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		// TODO Auto-generated method stub
		
		AuthCredentials auth=authRepo.findByUsername(username)
									.orElseThrow(()-> new UsernameNotFoundException("User not found: "+username));
		
//		return org.springframework.security.core.userdetails.User
//				.withUsername(auth.getCustomer().getEmail())
//				.password(auth.getPasswordHash())
//				.roles(auth.getRoles().replace("ROLE_",""))
//				.disabled(!auth.isActive())
//				.build();
		List<GrantedAuthority> authorities= Arrays.stream(auth.getRoles().split(","))
												.map(role -> (GrantedAuthority) new SimpleGrantedAuthority(role))
												.toList();
		
		return new CustomUserDetails(
				auth.getUsername(),
				auth.getPasswordHash(),
				authorities,
				auth.getCustomer().getId());
	}

}
