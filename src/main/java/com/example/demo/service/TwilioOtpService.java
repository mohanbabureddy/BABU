package com.example.demo.service;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.TwilioConfig;
import com.example.demo.dto.PasswordResetRequestDto;
import com.example.demo.dto.PasswordResponseDto;
import com.example.demo.dto.otpStatus;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import reactor.core.publisher.Mono;

@Service
public class TwilioOtpService {
	
	@Autowired
	private TwilioConfig twilioConfig;
	
	Map<String,String>otpMap=new HashMap<>();
	
	
	public Mono<PasswordResponseDto> sendOtpForPasswordReset(PasswordResetRequestDto passwordResetRequestDto) {
		
		PasswordResponseDto passwordResponseDto=null;
		
		try {
		
		PhoneNumber to = new PhoneNumber(passwordResetRequestDto.getPhoneNumber());
		PhoneNumber from =new PhoneNumber(twilioConfig.getTrailNumber());
		String otp=generateOTP();
		String otpMessage="Hi";
		
		Message message = Message.creator(to,from,otpMessage) .create(); 
		
		otpMap.put(passwordResetRequestDto.getUserName(),otp);
		passwordResponseDto=new PasswordResponseDto(otpMessage,otpStatus.DELIVERED);
		} catch(Exception ex) {
			passwordResponseDto=new PasswordResponseDto(ex.getMessage(),otpStatus.FAILED);
		}
		
		return Mono.just(passwordResponseDto);
			
		}	
	
	public Mono<String> validateOTP(String userInputotp,String userName){
		
		if(userInputotp.equals(otpMap.get(userName))){
			
			return Mono.just("valid");		
		
	}
		else {
			return Mono.error(new IllegalArgumentException("Retry"));
		}
	}
		
	

	private String generateOTP() {
		return new DecimalFormat("000000")
				.format(new Random().nextInt(999999));
	}
}
