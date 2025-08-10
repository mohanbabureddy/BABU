
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
@RequestMapping("/api/users")
@CrossOrigin(origins = {"http://localhost:3000", "https://vgrpay.uk","https://d8aff7a8.rentapp1.pages.dev"})
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserRepository userRepo;

    @GetMapping("/all")
    public ResponseEntity<List<User>> getAllUsers() {
        logger.info("Fetching all users");
        List<User> users = userRepo.findAll();
        logger.debug("Found {} users", users.size());
        return ResponseEntity.ok(users);
    }

    @GetMapping("/names")
    public List<String> getUsers() {
        logger.info("Fetching all tenant usernames");
        List<String> usernames = userRepo.findTenantUsernames();
        logger.debug("Usernames: {}", usernames);
        return usernames;
    }

    @PostMapping("/add")
    public ResponseEntity<?> addUser(@RequestBody User user) {
        logger.info("Attempting to add user: {}", user.getUsername());
        if (userRepo.findByUsername(user.getUsername()).isPresent()) {
            logger.warn("User already exists: {}", user.getUsername());
            return ResponseEntity.status(HttpStatus.CONFLICT).body("User already exists");
        }
        User saved = userRepo.save(user);
        logger.info("User created: {}", saved.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
        logger.info("Updating user with id: {}", id);
        Optional<User> userOpt = userRepo.findById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            logger.debug("Current user data: {}", user);
            user.setUsername(updatedUser.getUsername());
            user.setRole(updatedUser.getRole());
            user.setPassword(updatedUser.getPassword());
            User saved = userRepo.save(user);
            logger.info("User updated: {}", saved.getUsername());
            return ResponseEntity.ok(saved);
        } else {
            logger.warn("User not found with id: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        logger.info("Deleting user with id: {}", id);
        Optional<User> user = userRepo.findById(id);
        if (user.isPresent()) {
            userRepo.deleteById(id);
            logger.info("User deleted successfully: {}", id);
            return ResponseEntity.ok("User deleted successfully");
        } else {
            logger.warn("User not found with id: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }
}