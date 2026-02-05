package org.example.repo;

import org.example.model.TwilioConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TwilioConfigRepository extends JpaRepository<TwilioConfigEntity, Long> {
    TwilioConfigEntity findTopByOrderByIdDesc();
}
