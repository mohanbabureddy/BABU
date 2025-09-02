package org.example.service;

import org.example.dto.OccupantDto;
import org.example.model.Occupant;
import org.example.model.User;
import org.example.repo.OccupantRepository;
import org.example.repo.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@Service
public class OccupantService {

    @Value("${app.upload.root:uploads}") private String root;
    @Value("${app.upload.public-base:/uploads}") private String publicBase;

    private final OccupantRepository repo;
    private final UserRepository userRepo;

    public OccupantService(OccupantRepository repo, UserRepository userRepo) {
        this.repo = repo;
        this.userRepo = userRepo;
    }

    private OccupantDto toDto(Occupant o) {
        String url = null;
        if (o.getAadharStoragePath() != null) {
            url = publicBase + "/" + o.getAadharStoragePath().replace('\\','/');
        }
        return new OccupantDto(
            o.getId(),
            o.getTenant().getUsername(),
            o.getName(),
            o.getAadharFileName(),
            url,
            o.getUploadedAt(),
            o.isVerified()
        );
    }

    public List<OccupantDto> list(String tenantUsername) {
        return repo.findByTenant_UsernameOrderByUploadedAtDesc(tenantUsername)
                .stream().map(this::toDto).toList();
    }

    public OccupantDto add(String tenantUsername, String name, MultipartFile file) throws Exception {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Name required");
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("File required");
        String ct = file.getContentType();
        if (ct == null || !(ct.equals("application/pdf")||ct.equals("image/jpeg")||ct.equals("image/png")))
            throw new IllegalArgumentException("Invalid file type");
        if (file.getSize() > 2 * 1024 * 1024) throw new IllegalArgumentException("File too large");

        User tenant = userRepo.findByUsername(tenantUsername)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found"));

        // Sanitize name for file system usage
        String sanitized = name.trim().replaceAll("[^a-zA-Z0-9_-]", "_");
        String ext = switch (ct) {
            case "image/jpeg" -> ".jpeg";
            case "image/png" -> ".png";
            case "application/pdf" -> ".pdf";
            default -> "";
        };
        String fileName = sanitized + ext;
        Path target = Path.of(System.getProperty("user.dir"), root, "aadhaar", tenantUsername, fileName);
        try {
            Files.createDirectories(target.getParent());
        } catch (Exception dirEx) {
            throw new IOException("Failed to create directory for file upload: " + target.getParent(), dirEx);
        }
        file.transferTo(target.toFile());

        Occupant o = new Occupant();
        o.setTenant(tenant);
        o.setName(name.trim());
        o.setAadharFileName(file.getOriginalFilename());
        o.setAadharContentType(ct);
        // store relative piece so URL builds consistently
        o.setAadharStoragePath("aadhaar/" + tenantUsername + "/" + fileName);
        repo.save(o);
        return toDto(o);
    }

    public void delete(Long id, String unused, boolean ignore) {
        Occupant o = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Not found"));
        if (o.isVerified() && !ignore) {
            throw new IllegalStateException("Cannot delete a verified occupant");
        }
        if (o.getAadharStoragePath() != null) {
            try { Files.deleteIfExists(Path.of(root, o.getAadharStoragePath())); } catch (Exception ignored2) {}
        }
        repo.delete(o);
    }

    /*public TenantDto getTenantInfo(String username) {
        User tenant = userRepo.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Tenant not found"));
        if (!"TENANT".equals(tenant.getRole())) {
            throw new IllegalArgumentException("User is not a tenant");
        }
        return new TenantDto(
            tenant.getId(),
            tenant.getUsername(),
            tenant.getPhone(),
            tenant.getMail(),
            tenant.isRegistrationCompleted()
        );
    }*/

    public Map<String, Object> verifyOccupant(Long id) {
        Occupant o = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Occupant not found"));
        if (!o.isVerified()) {
            o.setVerified(true);
            o.setVerifiedBy("OWNER"); // set real username if auth later
            o.setVerifiedAt(java.time.Instant.now());
            repo.save(o);
        }
        return Map.of("status", "ok", "id", o.getId(), "verified", true);
    }

    public Occupant findById(Long id) {
        return repo.findById(id).orElse(null);
    }
}
