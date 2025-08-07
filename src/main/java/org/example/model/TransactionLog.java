package org.example.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "transaction_logs")
public class TransactionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tenantName;

    private String paymentId;
    private String status; // "SUCCESS" or "FAIL"
    private String errorReason;

    private LocalDateTime timestamp = LocalDateTime.now();

    // Getters and setters
}

