package com.bboss.cache.config;

import com.bboss.pub.filter.JwtFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

@Slf4j
@Configuration
public class CabsCacheConfig {
	@Value("${jwt.secret}")
	private String jwtSecret;

	@Bean
	public SecretKey jwtSecretKey() {
		log.info("jwtSecret: {}", jwtSecret);
		SecretKey key = new SecretKeySpec(jwtSecret.getBytes(), "HmacSHA1");
		// Spring init JwtFilter before this, cause '@Autowired SecretKey key;' in
		// JwtFilter.java always null.
		// So an ugly static setter is used.
		JwtFilter.setKey(key);
		return key;
	}

}
