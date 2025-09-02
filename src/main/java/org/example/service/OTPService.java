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
        try {
            String otp = String.valueOf((int)(Math.random() * 9000) + 1000);
            otpStorage.put(mobileNumber, otp);
            smsService.sendSms(mobileNumber, otp);
            return otp;
        } catch (Exception ex) {
            org.slf4j.LoggerFactory.getLogger(OTPService.class)
                .error("Error in generateOTP: {}", ex.getMessage(), ex);
            throw ex;
        }
    }

    public boolean verifyOTP(String mobileNumber, String otp) {
        try {
            return otp.equals(otpStorage.get(mobileNumber));
        } catch (Exception ex) {
            org.slf4j.LoggerFactory.getLogger(OTPService.class)
                .error("Error in verifyOTP: {}", ex.getMessage(), ex);
            throw ex;
        }
    }
}
