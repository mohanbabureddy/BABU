package com.example.demo;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix="twilio")
@Data
	public class TwilioConfig {
	
	private String accountSid;
	private String AuthToken;
	private String trailNumber;
	

}
