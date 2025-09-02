package org.example.repo;

import org.example.model.TenantBill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TenantBillRepository extends JpaRepository<TenantBill, Long> {
    List<TenantBill> findByTenantNameOrderByMonthYearDesc(String tenantName);

    Optional<TenantBill> findByTenantNameAndMonthYear(String tenantName, String monthYear);
}
