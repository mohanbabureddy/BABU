package org.example.repo;

import org.example.model.Occupant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface OccupantRepository extends JpaRepository<Occupant, Long> {
    List<Occupant> findByTenant_UsernameOrderByUploadedAtDesc(String username);

    @Query("select o from Occupant o join fetch o.tenant t order by o.uploadedAt desc")
    List<Occupant> findAllWithTenantOrderByUploadedAtDesc();
}
