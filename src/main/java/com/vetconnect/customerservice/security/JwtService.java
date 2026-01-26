package com.vetconnect.customerservice.security;

import java.security.Key;
import java.util.Date;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
	private static final String SECRET_KEY= "this_is_a_very_secure_secret_key_at_least_256_bits_long";
	
	private static final long EXPIRATION_TIME=1000*60*60;
	
	private Key getSigningKey() {
		return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
	}
	
	public String generateToken(UserDetails userDetails) {
		return Jwts.builder()
					.setSubject(userDetails.getUsername())
					.claim("roles", 
							userDetails.getAuthorities()
							.stream()
							.map(a -> a.getAuthority())
							.toList()
						)
					.setIssuedAt(new Date())
					.setExpiration(new Date(System.currentTimeMillis()+EXPIRATION_TIME)
					)
					.signWith(getSigningKey(),SignatureAlgorithm.HS256)
					.compact();
					
	}
	
	public String extractUsername(String token) {
		return extractAllClaims(token).getSubject();
	}

	private Claims extractAllClaims(String token) {
		// TODO Auto-generated method stub
		
		return Jwts.parserBuilder()
				.setSigningKey(getSigningKey())
				.build()
				.parseClaimsJws(token)
				.getBody();
	}
	
	public boolean isTokenValid(String token, UserDetails userDetails) {
		
		final String username=extractUsername(token);
		return username.equals(userDetails.getUsername())
				&& !isTokenExpired(token);
	}
	
	public boolean isTokenExpired(String token) {
		return extractClaims(token)
				.getExpiration()
				.before(new Date());
	}
	
	public Claims extractClaims(String token) {
		return Jwts.parserBuilder()
				.setSigningKey(getSigningKey())
				.build()
				.parseClaimsJws(token)
				.getBody();
	}
	
	public long getExpiry(String token) {
		Claims claims=extractClaims(token);
		Date expiry=claims.getExpiration();
		return (expiry.getTime() - System.currentTimeMillis())/1000;
	}
}
