package org.example.controller;

import org.example.model.TransactionLog;
import org.example.repo.TransactionLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/tenants")
@CrossOrigin(origins = "http://localhost:3000")
public class TransactionController {

    @Autowired
    private TransactionLogRepository logRepo;

    @PostMapping("/logSuccess")
    public ResponseEntity<?> logSuccess(@RequestBody Map<String, String> body) {
        TransactionLog log = new TransactionLog();
        log.setTenantName(body.get("tenantName"));
        log.setPaymentId(body.get("paymentId"));
        log.setStatus("SUCCESS");
        logRepo.save(log);
        return ResponseEntity.ok("Success logged");
    }

    @PostMapping("/logFailure")
    public ResponseEntity<?> logFailure(@RequestBody Map<String, Object> errorData) {
        TransactionLog log = new TransactionLog();
        log.setStatus("FAIL");
        log.setPaymentId((String) errorData.get("metadata.payment_id"));
        log.setErrorReason(errorData.toString());
        logRepo.save(log);
        return ResponseEntity.ok("Failure logged");
    }
}
