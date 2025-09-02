package org.example.controller;

import org.example.dto.OccupantDto;
import org.example.model.Occupant;
import org.example.service.OccupantService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tenants/occupants")
@CrossOrigin(origins = "*")
public class OccupantController {

    private final OccupantService occupantService;

    public OccupantController(OccupantService occupantService) {
        this.occupantService = occupantService;
    }

    // GET all occupants for a tenant (public)
    @GetMapping(value="/{tenant}", produces=MediaType.APPLICATION_JSON_VALUE)
    public List<OccupantDto> list(@PathVariable String tenant) {
        return occupantService.list(tenant);
    }

    // Add occupant (public)
    @PostMapping(value = "/{tenant}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public OccupantDto add(@PathVariable String tenant,
                           @RequestPart("name") String name,
                           @RequestPart("file") MultipartFile file) throws Exception {
        return occupantService.add(tenant, name, file);
    }

    // Delete occupant (public)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) throws Exception {
        occupantService.delete(id, null, true); // pass admin=true to skip checks
        return ResponseEntity.ok("Deleted");
    }

    @PatchMapping("/verify/{id}")
    public Map<String,Object> verify(@PathVariable Long id) {
        try {
            return occupantService.verifyOccupant(id);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }
    }

    @GetMapping("/file/{id}")
    public ResponseEntity<Resource> getFile(@PathVariable Long id) {
        Occupant o = occupantService.findById(id);
        if (o == null || o.getAadharStoragePath() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        java.nio.file.Path filePath = java.nio.file.Path.of(System.getProperty("user.dir"), "uploads", o.getAadharStoragePath());
        java.io.File file = filePath.toFile();
        if (!file.exists()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        String contentType = o.getAadharContentType();
        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType != null ? contentType : MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .body(resource);
    }

}