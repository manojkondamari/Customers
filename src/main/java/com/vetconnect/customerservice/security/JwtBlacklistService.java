package com.vetconnect.customerservice.security;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class JwtBlacklistService {
	
	@Autowired
	private RedisTemplate<String,String> redisTemplate;
	
	@Value("${jwt.expiration}")
	private long jwtExpiration;
	
	private static final String BLACKLIST_PREFIX="jwt:blacklist";
	
	public void blacklistToken(String token) {
		String key=BLACKLIST_PREFIX+token;
		
		redisTemplate.opsForValue().set(key, "blacklisted", jwtExpiration,TimeUnit.MILLISECONDS);
		
		
	}
	
	public boolean isTokenBlacklisted(String token) {
		String key=BLACKLIST_PREFIX + token;
		return Boolean.TRUE.equals(redisTemplate.hasKey(key));
	}
}
