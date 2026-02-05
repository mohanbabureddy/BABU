package org.example.controller;

import org.example.model.User;
import org.example.repo.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:3000", "https://vgrpay.uk", "https://d8aff7a8.rentapp1.pages.dev"})
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserRepository userRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> creds) {
        String username = creds.get("username");
        String password = creds.get("password");

        logger.info("Login attempt for user: {}", username);
        logger.debug("Received credentials: username={}, password=****", username);

        Optional<User> user = userRepo.findByUsername(username);

        if (user.isPresent()) {
            logger.debug("User found: {}", user.get());
            User u = user.get();
            if (!u.isRegistrationCompleted()) {
                logger.warn("Login failed: registration incomplete for user: {}", username);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Registration incomplete");
            }
            if (password != null && u.getPassword() != null && passwordEncoder.matches(password, u.getPassword())) {
                logger.info("Login successful for user: {}", username);
                return ResponseEntity.ok(Map.of(
                    "role", u.getRole(),
                    "username", username
                ));
            } else {
                logger.warn("Login failed: incorrect password for user: {}", username);
            }
        } else {
            logger.warn("Login failed: user not found: {}", username);
        }

        logger.debug("Login response: UNAUTHORIZED for user: {}", username);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
    }
}
