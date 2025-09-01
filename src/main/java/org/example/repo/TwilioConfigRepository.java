package org.example.repo;

import org.example.model.TwilioConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TwilioConfigRepository extends JpaRepository<TwilioConfigEntity, Long> {
    // Optionally, add a method to get the latest config
    TwilioConfigEntity findTopByOrderByIdDesc();
}

