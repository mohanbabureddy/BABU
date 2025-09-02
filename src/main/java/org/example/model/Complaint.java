package org.example.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "complaints")
public class Complaint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tenantName;
    private String description;
    private String status; // e.g., "OPEN", "RESOLVED"
    private LocalDateTime createdDate;
    private String resolutionComment;
    private LocalDateTime closedDate;
}
