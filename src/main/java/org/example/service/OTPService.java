package org.example.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class OTPService {
    private final SmsService smsService;
    private final Map<String, String> otpStorage = new HashMap<>();

    public OTPService(SmsService smsService) {
        this.smsService = smsService;
    }

    public String generateOTP(String mobileNumber) {
        String otp = String.valueOf((int)(Math.random() * 9000) + 1000);
        otpStorage.put(mobileNumber, otp);
        smsService.sendSms(mobileNumber, otp);
        // Integrate SMS provider here to send OTP
        return otp;
    }

    public boolean verifyOTP(String mobileNumber, String otp) {
        return otp.equals(otpStorage.get(mobileNumber));
    }
}

