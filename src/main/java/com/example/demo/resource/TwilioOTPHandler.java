package com.example.demo.resource;

import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.example.demo.dto.PasswordResetRequestDto;
import com.example.demo.service.TwilioOtpService;

import reactor.core.publisher.Mono;

@Component
public class TwilioOTPHandler {
	 
	@Autowired  
	private TwilioOtpService service;
	
	public Mono<ServerResponse> sendOTP(ServerRequest serverRequest){
		return serverRequest.bodyToMono(PasswordResetRequestDto.class)
				.flatMap(dto -> service.sendOtpForPasswordReset(dto))
				.flatMap(dto -> ServerResponse.status(HttpStatus.OK)
						.body(BodyInserters.fromValue(dto)));
	}



public Mono<ServerResponse> validateOTP(ServerRequest serverRequest){
	return serverRequest.bodyToMono(PasswordResetRequestDto.class)
			.flatMap(dto -> service.validateOTP(dto.getOneTimePassword(),dto.getUserName()))
					.flatMap(dto -> ServerResponse.status(HttpStatus.OK)
					.bodyValue(dto));
}

}
