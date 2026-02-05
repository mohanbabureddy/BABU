package org.example.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.security.SecureRandom;

@Service
public class OTPService {
    private final SmsService smsService;
    private final Map<String, OtpEntry> otpStorage = new ConcurrentHashMap<>();
    private static final long OTP_TTL_MILLIS = 5 * 60 * 1000;
    private static final SecureRandom RNG = new SecureRandom();

    public OTPService(SmsService smsService) {
        this.smsService = smsService;
    }

    public String generateOTP(String mobileNumber) {
        try {
            String otp = String.valueOf(RNG.nextInt(9000) + 1000);
            otpStorage.put(mobileNumber, new OtpEntry(otp, System.currentTimeMillis() + OTP_TTL_MILLIS));
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
            OtpEntry entry = otpStorage.get(mobileNumber);
            if (entry == null) return false;
            if (System.currentTimeMillis() > entry.expiresAt) {
                otpStorage.remove(mobileNumber);
                return false;
            }
            boolean ok = otp.equals(entry.otp);
            if (ok) otpStorage.remove(mobileNumber);
            return ok;
        } catch (Exception ex) {
            org.slf4j.LoggerFactory.getLogger(OTPService.class)
                .error("Error in verifyOTP: {}", ex.getMessage(), ex);
            throw ex;
        }
    }

    private static class OtpEntry {
        private final String otp;
        private final long expiresAt;

        private OtpEntry(String otp, long expiresAt) {
            this.otp = otp;
            this.expiresAt = expiresAt;
        }
    }
}
