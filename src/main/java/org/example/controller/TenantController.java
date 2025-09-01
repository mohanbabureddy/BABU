package org.example.controller;

import org.example.model.TenantBill;
import org.example.model.TenantEmailProperties;
import org.example.model.User;
import org.example.repo.TenantBillRepository;
import org.example.repo.UserRepository;
import org.example.service.EmailWithInvoiceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tenants")
@CrossOrigin(origins = {
        "http://localhost:3000",
        "https://vgrpay.uk",
        "https://d8aff7a8.rentapp1.pages.dev"
})
public class TenantController {

    private static final Logger logger = LoggerFactory.getLogger(TenantController.class);

    @Autowired
    private TenantEmailProperties tenantEmailProperties;

    @Autowired
    private TenantBillRepository repository;

    @Autowired
    private EmailWithInvoiceService emailWithInvoiceService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Fetch all bills for the given tenant, ordered by monthYear descending.
     */
    @GetMapping("/{name}")
    public List<TenantBill> getTenantBills(@PathVariable String name) {
        logger.info("Fetching bills for tenant: {}", name);
        List<TenantBill> bills = repository.findByTenantNameOrderByMonthYearDesc(name);
        logger.debug("Fetched {} bills for {}", bills.size(), name);
        return bills;
    }

    /**
     * Mark a bill as paid, persist the change, and send a PDF invoice
     * to both the tenant and the admin.
     */
    @PutMapping("/markPaid/{id}")
    public ResponseEntity<?> markAsPaid(@PathVariable Long id) {
        logger.info("Marking bill as paid for ID: {}", id);
        TenantBill bill = repository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Bill with ID {} not found", id);
                    return new RuntimeException("Bill not found");
                });

        bill.setPaid(true);
        bill.setPaidDate(LocalDateTime.now());

        repository.save(bill);
        logger.info("Bill ID {} marked as paid", id);

        String tenantName  = bill.getTenantName();
        String tenantEmail = tenantEmailProperties.getEmailForTenant(tenantName);
        String adminEmail  = tenantEmailProperties.getAdminEmail();
        String tenantPhone = null;
        Optional<User> user = userRepository.findByUsername(tenantName);
        if (user.isPresent()) {
            tenantPhone = user.get().getPhone();
        }


        if (tenantEmail == null || adminEmail == null) {
            logger.warn("Missing email configuration for tenant: {}", tenantName);
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Email configuration missing for tenant or admin.");
        }

        try {
            emailWithInvoiceService.sendBillPaidEmail(bill, tenantEmail, adminEmail, tenantPhone);
            logger.info("Invoice email and SMS sent to {} and {}", tenantEmail, adminEmail);
        } catch (Exception ex) {
            logger.error("Failed to send invoice email/SMS for bill ID {}", id, ex);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Payment marked as paid, but failed to send invoice email/SMS.");
        }

        return ResponseEntity.ok("Payment marked as paid; invoice and SMS sent.");
    }

    /**
     * Add a new bill for a tenant and Notify them via email and SMS.
     */
    @PostMapping("/addBill")
    public ResponseEntity<?> addBill(@RequestBody TenantBill bill) throws Exception {
        logger.info("Adding new bill: {}", bill);
        Optional<TenantBill> existing = repository
                .findByTenantNameAndMonthYear(bill.getTenantName(), bill.getMonthYear());

        if (existing.isPresent()) {
            logger.warn("Bill already exists for {} in {}",
                    bill.getTenantName(), bill.getMonthYear());
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Collections.singletonMap("error", "Bill already exists for this tenant and month."));
        }

        bill.setCreatedDate(LocalDate.now());
        repository.save(bill);
        logger.info("Bill added successfully for {} for month {}", bill.getTenantName(), bill.getMonthYear());

        String tenantName = bill.getTenantName();
        String tenantEmail = tenantEmailProperties.getEmailForTenant(tenantName);
        String tenantPhone;
        Optional<User> user = userRepository.findByUsername(tenantName);
        tenantPhone = user.map(User::getPhone).orElse(null);
        ; // Get phone number

        // Send email and SMS asynchronously (non-blocking)
        new Thread(() -> {
            try {
                emailWithInvoiceService.notifyBillGenerated(bill, tenantEmail, bill.getMonthYear(), tenantPhone);
                logger.info("Notified successfully for the user {}", bill.getTenantName());
            } catch (Exception e) {
                logger.error("Failed to send email/SMS for user {}: {}", bill.getTenantName(), e.getMessage());
            }
        }).start();

        return ResponseEntity.ok("Bill added successfully and notification triggered.");
    }

    /**
     * Fetch all bills across tenants, ordered by monthYear descending.
     */
    @GetMapping("/all")
    public List<TenantBill> getAllBills() {
        logger.info("Fetching all bills");
        List<TenantBill> allBills = repository.findAll(
                Sort.by(Sort.Direction.DESC, "monthYear")
        );
        logger.debug("Total bills fetched: {}", allBills.size());
        return allBills;
    }

    /**
     * Delete a bill by ID.
     */
    @DeleteMapping("/deleteBill/{id}")
    public ResponseEntity<?> deleteBill(@PathVariable Long id) {
        logger.info("Deleting bill ID {}", id);
        repository.deleteById(id);
        logger.info("Bill ID {} deleted successfully", id);
        return ResponseEntity.ok("Bill deleted successfully.");
    }

    /**
     * Update an existing bill's details.
     */
    @PutMapping("/updateBill/{id}")
    public ResponseEntity<?> updateBill(
            @PathVariable Long id,
            @RequestBody TenantBill updatedBill
    ) {
        logger.info("Updating bill for user {}", updatedBill.getTenantName());
        TenantBill bill = repository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Bill with ID {} not found for update", id);
                    return new RuntimeException("Bill not found");
                });

        bill.setTenantName(updatedBill.getTenantName());
        bill.setMonthYear(updatedBill.getMonthYear());
        bill.setRent(updatedBill.getRent());
        bill.setWater(updatedBill.getWater());
        bill.setElectricity(updatedBill.getElectricity());

        repository.save(bill);
        logger.info("Bill for user {} updated successfully", bill.getTenantName());
        return ResponseEntity.ok("Bill updated successfully.");
    }
}