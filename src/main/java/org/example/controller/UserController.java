package org.example.controller;

import org.example.model.User;
import org.example.repo.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")  // Changed to plural to match REST best practices and frontend
@CrossOrigin(origins = {"http://localhost:3000", "https://vgrpay.uk","https://d8aff7a8.rentapp1.pages.dev"})
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserRepository userRepo;

    // ✅ 1. Get all users
    @GetMapping("/all")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepo.findAll());
    }

    @GetMapping("/names")
    public List<String> getUsers() {
        return userRepo.findTenantUsernames();

    }

    // ✅ 2. Add new user
    @PostMapping("/add")
    public ResponseEntity<?> addUser(@RequestBody User user) {
        if (userRepo.findByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("User already exists");
        }
        User saved = userRepo.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // ✅ 3. Update by ID
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
        Optional<User> userOpt = userRepo.findById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setUsername(updatedUser.getUsername());
            user.setRole(updatedUser.getRole());
            user.setPassword(updatedUser.getPassword()); // Make sure password update is intentional
            User saved = userRepo.save(user);
            return ResponseEntity.ok(saved);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }

    // ✅ 4. Delete by ID
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        Optional<User> user = userRepo.findById(id);
        if (user.isPresent()) {
            userRepo.deleteById(id);
            return ResponseEntity.ok("User deleted successfully");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }
}
