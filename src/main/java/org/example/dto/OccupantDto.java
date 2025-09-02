package org.example.dto;

import java.time.Instant;

public class OccupantDto {
    public Long id;
    public String tenantUsername;
    public String name;
    public String aadharFileName;
    public String url;
    public Instant uploadedAt;
    public boolean verified;

    public OccupantDto(Long id, String tenantUsername, String name, String aadharFileName, String url, Instant uploadedAt, boolean verified) {
        this.id = id;
        this.tenantUsername = tenantUsername;
        this.name = name;
        this.aadharFileName = aadharFileName;
        this.url = url;
        this.uploadedAt = uploadedAt;
        this.verified = verified;
    }
}
