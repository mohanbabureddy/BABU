package org.example.controller;

import org.example.model.TenantBill;
import org.example.repo.TenantBillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tenants")
@CrossOrigin(origins = "http://localhost:3000")
public class TenantController {

    @Autowired
    private TenantBillRepository repository;

    @GetMapping("/{name}")
    public List<TenantBill> getTenantBills(@PathVariable String name) {
        return repository.findByTenantNameOrderByMonthYearDesc(name);
    }

    @PutMapping("/markPaid/{id}")
    public ResponseEntity<?> markAsPaid(@PathVariable Long id) {
        TenantBill bill = repository.findById(id).orElseThrow();
        bill.setPaid(true);
        repository.save(bill);
        return ResponseEntity.ok("Payment marked as paid");
    }

    @PostMapping("/addBill")
    public ResponseEntity<?> addBill(@RequestBody TenantBill bill) {
        // Optional: Prevent duplicate for same tenant + month
        Optional<TenantBill> existing = repository.findByTenantNameAndMonthYear(
                bill.getTenantName(), bill.getMonthYear()
        );

        if (existing.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Bill already exists for this tenant and month");
        }

        repository.save(bill);
        return ResponseEntity.ok("Bill added successfully");
    }

    @GetMapping("/all")
    public List<TenantBill> getAllBills() {
        return repository.findAll(Sort.by(Sort.Direction.DESC, "monthYear"));
    }

    @DeleteMapping("/deleteBill/{id}")
    public ResponseEntity<?> deleteBill(@PathVariable Long id) {
        repository.deleteById(id);
        return ResponseEntity.ok("Bill deleted successfully");
    }

    @PutMapping("/updateBill/{id}")
    public ResponseEntity<?> updateBill(@PathVariable Long id, @RequestBody TenantBill updatedBill) {
        TenantBill bill = repository.findById(id).orElseThrow();
        bill.setTenantName(updatedBill.getTenantName());
        bill.setMonthYear(updatedBill.getMonthYear());
        bill.setRent(updatedBill.getRent());
        bill.setWater(updatedBill.getWater());
        bill.setElectricity(updatedBill.getElectricity());
        repository.save(bill);
        return ResponseEntity.ok("Updated");
    }



}
