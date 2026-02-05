package org.example.controller;

import org.example.model.Occupant;
import org.example.repo.OccupantRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/occupants")
@CrossOrigin(origins = "*")
public class AdminOccupantController {

    private final OccupantRepository repo;

    public AdminOccupantController(OccupantRepository repo) {
        this.repo = repo;
    }

    private String fileUrl(HttpServletRequest req, Long id) {
        String base = ServletUriComponentsBuilder.fromRequestUri(req)
            .replacePath(null).build().toString();
        return base + "/api/tenants/occupants/file/" + id;
    }

    private Map<String, Object> occupantToMap(Occupant o, HttpServletRequest req) {
        return Map.of(
            "id", o.getId(),
            "tenantUsername", o.getTenant().getUsername(),
            "name", o.getName(),
            "aadharFileName", o.getAadharFileName(),
            "uploadedAt", o.getUploadedAt(),
            "verified", o.isVerified(),
            "aadharUrl", fileUrl(req, o.getId())
        );
    }

    @GetMapping
    public List<Map<String,Object>> all(HttpServletRequest req) {
        return repo.findAllWithTenantOrderByUploadedAtDesc().stream()
            .map(o -> occupantToMap(o, req))
            .toList();
    }

    @GetMapping("/paged")
    public Map<String,Object> allPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            HttpServletRequest req) {
        Page<Occupant> p = repo.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "uploadedAt")));
        return Map.of(
            "content", p.getContent().stream().map(o -> occupantToMap(o, req)).toList(),
            "page", p.getNumber(),
            "size", p.getSize(),
            "totalElements", p.getTotalElements(),
            "totalPages", p.getTotalPages()
        );
    }

    @PostMapping("/verify/{id}")
    public Map<String, Object> verifyOccupant(@PathVariable Long id) {
        Occupant occupant = repo.findById(id).orElse(null);
        if (occupant == null) {
            throw new IllegalArgumentException("Occupant not found");
        }
        if (occupant.isVerified()) {
            return Map.of("status", "already verified");
        }
        occupant.setVerified(true);
        repo.save(occupant);
        return Map.of("status", "verified", "id", id);
    }
}
