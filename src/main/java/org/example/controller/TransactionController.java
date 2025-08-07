package org.example.controller;

import org.example.model.TransactionLog;
import org.example.repo.TransactionLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/tenants")
@CrossOrigin(origins = {"http://localhost:3000", "https://vgrpay.uk","https://d8aff7a8.rentapp1.pages.dev"})
public class TransactionController {

    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);

    @Autowired
    private TransactionLogRepository logRepo;

    @PostMapping("/logSuccess")
    public ResponseEntity<?> logSuccess(@RequestBody Map<String, String> body) {
        String tenantName = body.get("tenantName");
        String paymentId = body.get("paymentId");

        logger.info("Logging SUCCESS transaction for tenant: {}, paymentId: {}", tenantName, paymentId);

        TransactionLog log = new TransactionLog();
        log.setTenantName(tenantName);
        log.setPaymentId(paymentId);
        log.setStatus("SUCCESS");
        logRepo.save(log);

        logger.info("Success transaction logged for paymentId: {}", paymentId);

        return ResponseEntity.ok("Success logged");
    }

    @PostMapping("/logFailure")
    public ResponseEntity<?> logFailure(@RequestBody Map<String, Object> errorData) {
        String paymentId = (String) errorData.get("metadata.payment_id");

        logger.warn("Logging FAILURE transaction for paymentId: {}", paymentId);

        TransactionLog log = new TransactionLog();
        log.setStatus("FAIL");
        log.setPaymentId(paymentId);
        log.setErrorReason(errorData.toString());

        logRepo.save(log);

        logger.info("Failure transaction logged for paymentId: {}", paymentId);

        return ResponseEntity.ok("Failure logged");
    }
}
