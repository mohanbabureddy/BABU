package org.example.model;

import javax.persistence.*;

@Entity
@Table(name = "tenant_bills")
public class TenantBill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tenantName;
    private String monthYear;
    private Double rent;
    private Double water;
    private Double electricity;

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    private boolean paid;

    // --- Getters ---
    public Long getId() {
        return id;
    }

    public String getTenantName() {
        return tenantName;
    }

    public String getMonthYear() {
        return monthYear;
    }

    public Double getRent() {
        return rent;
    }

    public Double getWater() {
        return water;
    }

    public Double getElectricity() {
        return electricity;
    }

    // --- Setters ---
    public void setId(Long id) {
        this.id = id;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public void setMonthYear(String monthYear) {
        this.monthYear = monthYear;
    }

    public void setRent(Double rent) {
        this.rent = rent;
    }

    public void setWater(Double water) {
        this.water = water;
    }

    public void setElectricity(Double electricity) {
        this.electricity = electricity;
    }
}
