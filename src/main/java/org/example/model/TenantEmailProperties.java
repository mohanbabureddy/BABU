package org.example.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "tenants")
public class TenantEmailProperties {
    private Map<String, String> emails;
    private String adminEmail;

    public String getEmailForTenant(String tenantName) {
        return emails.get(tenantName);
    }
}
