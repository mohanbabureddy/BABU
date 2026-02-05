package org.example.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import org.example.repo.TwilioConfigRepository;
import org.example.model.TwilioConfigEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SmsService {
    private static final Logger logger = LoggerFactory.getLogger(SmsService.class);
    private final TwilioConfigRepository twilioConfigRepository;
    private TwilioConfigEntity twilioConfigEntity;

    @Autowired
    public SmsService(TwilioConfigRepository twilioConfigRepository) {
        this.twilioConfigRepository = twilioConfigRepository;
        this.twilioConfigEntity = twilioConfigRepository.findTopByOrderByIdDesc();
        if (isConfigValid()) {
            Twilio.init(twilioConfigEntity.getAccountSid(), twilioConfigEntity.getAuthToken());
        } else {
            logger.error("Twilio configuration is missing or incomplete in the database!");
        }
    }

    private boolean isConfigValid() {
        return twilioConfigEntity != null
            && twilioConfigEntity.getAccountSid() != null && !twilioConfigEntity.getAccountSid().isEmpty()
            && twilioConfigEntity.getAuthToken() != null && !twilioConfigEntity.getAuthToken().isEmpty()
            && twilioConfigEntity.getPhoneNumber() != null && !twilioConfigEntity.getPhoneNumber().isEmpty();
    }

    public void sendSms(String to, String message) {
        this.twilioConfigEntity = twilioConfigRepository.findTopByOrderByIdDesc();
        if (!isConfigValid()) {
            logger.error("Cannot send SMS: Twilio configuration is missing or incomplete in the database!");
            return;
        }
        try {
            Message.creator(
                new com.twilio.type.PhoneNumber(to),
                new com.twilio.type.PhoneNumber(twilioConfigEntity.getPhoneNumber()),
                message
            ).create();
            logger.info("SMS sent to {}", to);
        } catch (Exception e) {
            logger.error("Failed to send SMS to {}: {}", to, e.getMessage(), e);
        }
    }
}