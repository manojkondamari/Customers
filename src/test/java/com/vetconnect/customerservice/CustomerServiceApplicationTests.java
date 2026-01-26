package com.vetconnect.customerservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("test")
class CustomerServiceApplicationTests {
	@MockBean
	 private RedisConnectionFactory redisConnectionFactory;
	 @MockBean
	 private RedisTemplate<String, Object> redisTemplate;
	@Test
	void contextLoads() {
	}

}
