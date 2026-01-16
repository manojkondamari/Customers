package com.vetconnect.customerservice.security;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vetconnect.customerservice.entity.AuthCredentials;

@Repository
public interface AuthCredentialRepository extends JpaRepository<AuthCredentials,Long>{
	
	Optional<AuthCredentials> findByUsername(String username);
	
	boolean existsByUsername(String username);
}
