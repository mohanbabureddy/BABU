package org.example.repo;

import org.example.model.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    List<Complaint> findByTenantNameOrderByCreatedDateDesc(String tenantName);

    List<Complaint> findAllByOrderByCreatedDateDesc();


}

