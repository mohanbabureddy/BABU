package org.example.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.Instant;

@Setter
@Getter
@Entity
@Table(name="occupants", indexes=@Index(name="idx_occupants_tenant", columnList="tenant_username"))
public class Occupant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_username", referencedColumnName = "username", nullable = false)
    private User tenant;

    @Column(nullable = false, length = 120)
    private String name;
    private String aadharFileName;
    private String aadharContentType;
    private String aadharStoragePath;
    private Instant uploadedAt;

    @Column(nullable = false)
    private boolean verified = false;

    private String verifiedBy;          // optional
    private Instant verifiedAt;

    @PrePersist
    void prePersist() {
        if (uploadedAt == null) uploadedAt = Instant.now();

    }
}