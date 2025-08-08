package org.example.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "tenant_bills")
public class TenantBill {

    // --- Setters ---
    // --- Getters ---
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tenantName;
    private String monthYear;
    private Double rent;
    private Double water;
    private Double electricity;
    private boolean paid;
    private LocalDateTime paidDate;
    private LocalDate CreatedDate;


}
