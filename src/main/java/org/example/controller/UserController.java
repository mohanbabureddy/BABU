package org.example.controller;

import org.example.model.User;
import org.example.repo.UserRepository;
import org.example.service.OTPService;
import org.example.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = {
        "http://localhost:3000",
        "https://vgrpay.uk",
        "https://d8aff7a8.rentapp1.pages.dev"
})
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserRepository userRepo;
    @Autowired
    private OTPService otpService;
    @Autowired
    private UserService userService; // if you keep extra business logic
    @Autowired
    private PasswordEncoder passwordEncoder;

    /* ---------- ADMIN: create skeleton user (username + role) ----------- */
    @PostMapping("/add")
    public ResponseEntity<?> addUser(@RequestBody User user) {
        if (isBlank(user.getUsername()) || isBlank(user.getRole())) {
            return bad("username & role required");
        }
        if (userRepo.findByUsername(user.getUsername()).isPresent()) {
            return conflict("User already exists");
        }
        user.setRegistrationCompleted(false);
        user.setPassword(null); // no password until tenant finishes
        User saved = userRepo.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /* ---------- OPTIONAL direct signup (email+mobile+password) ---------- */
    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String password = payload.get("password");
        String mobile = payload.get("mobileNumber");
        if (anyBlank(email, password, mobile)) return bad("email,password,mobileNumber required");
        // Delegate (ensure service encodes password & sets flags)
        User u = userService.registerUser(email, password, mobile);
        return ok(Map.of("id", u.getId(), "email", u.getMail()));
    }

    /* ---------- STEP 1 tenant: start registration (username given by admin) ---------- */
    @PostMapping("/registration/start")
    public ResponseEntity<?> startRegistration(@RequestBody Map<String, String> payload) {
        String username = payload.get("username");
        String email = payload.get("email");
        String mobile = payload.get("mobileNumber");
        if (anyBlank(username, email, mobile)) return bad("username,email,mobileNumber required");
        Optional<User> opt = userRepo.findByUsername(username);
        if (opt.isEmpty()) return notFound("Username not found");
        User u = opt.get();
        if (u.isRegistrationCompleted()) return conflict("Registration already completed");
        u.setMail(email.trim());
        u.setPhone(mobile.trim());
        userRepo.save(u);
        String otp = otpService.generateOTP(mobile.trim()); // store internally; do NOT log in prod
        log.info("OTP generated for {} (masked): ****", mobile);
        return ok(Map.of("message", "OTP sent to mobile"));
    }

    /* ---------- STEP 2 tenant: finish registration with OTP + password ---------- */
    @PostMapping("/registration/finish")
    public ResponseEntity<?> finishRegistration(@RequestBody Map<String, String> payload) {
        String username = payload.get("username");
        String otp = payload.get("otp");
        String password = payload.get("password");
        if (anyBlank(username, otp, password)) return bad("username,otp,password required");
        Optional<User> opt = userRepo.findByUsername(username);
        if (opt.isEmpty()) return notFound("Username not found");
        User u = opt.get();
        if (u.isRegistrationCompleted()) return conflict("Already completed");
        if (u.getPhone() == null) return bad("Start registration first");
        if (!otpService.verifyOTP(u.getPhone(), otp)) return unauthorized("Invalid OTP");
        u.setPassword(passwordEncoder.encode(password));
        u.setRegistrationCompleted(true);
        userRepo.save(u);
        return ok(Map.of("message", "Registration completed"));
    }

    /* ---------- LOGIN ---------- */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> payload) {
        String username = payload.get("username");
        String password = payload.get("password");
        if (anyBlank(username, password)) return bad("username,password required");
        Optional<User> opt = userRepo.findByUsername(username);
        if (opt.isEmpty()) return unauthorized("Invalid credentials");
        User u = opt.get();
        if (!u.isRegistrationCompleted()) return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "Registration incomplete"));
        if (u.getPassword() == null || !passwordEncoder.matches(password, u.getPassword()))
            return unauthorized("Invalid credentials");
        return ok(Map.of("username", u.getUsername(), "role", u.getRole()));
    }

    /* ---------- FORGOT PASSWORD (OTP to registered mobile) ---------- */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> payload) {
        String username = payload.get("username");
        if (isBlank(username)) return bad("username required");
        Optional<User> opt = userRepo.findByUsername(username);
        if (opt.isEmpty()) return notFound("User not found");
        User u = opt.get();
        if (isBlank(u.getPhone())) return bad("No mobile registered");
        otpService.generateOTP(u.getPhone());
        return ok(Map.of("message", "OTP sent"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> payload) {
        String username = payload.get("username");
        String otp = payload.get("otp");
        String newPassword = payload.get("newPassword");
        if (anyBlank(username, otp, newPassword)) return bad("username,otp,newPassword required");
        Optional<User> opt = userRepo.findByUsername(username);
        if (opt.isEmpty()) return notFound("User not found");
        User u = opt.get();
        if (!otpService.verifyOTP(u.getPhone(), otp)) return unauthorized("Invalid OTP");
        u.setPassword(passwordEncoder.encode(newPassword));
        userRepo.save(u);
        return ok(Map.of("message", "Password updated"));
    }

     /* ---------- LIST / NAMES ---------- */
    @GetMapping("/all")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepo.findAll());
    }

    @GetMapping("/names")
    public List<String> getTenantUsernames() {
        return userRepo.findTenantUsernames();
    }

    /* ---------- UPDATE (admin) ---------- */
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User updated) {
        Optional<User> opt = userRepo.findById(id);
        if (opt.isEmpty()) return notFound("User not found");
        User u = opt.get();
        if (!isBlank(updated.getUsername())) u.setUsername(updated.getUsername());
        if (!isBlank(updated.getRole())) u.setRole(updated.getRole());
        // password optional
        if (!isBlank(updated.getPassword())) {
            u.setPassword(passwordEncoder.encode(updated.getPassword()));
        }
        // allow admin to update email/mobile if needed
        if (!isBlank(updated.getMail())) u.setMail(updated.getMail());
        if (!isBlank(updated.getPhone())) u.setPhone(updated.getPhone());
        userRepo.save(u);
        return ResponseEntity.ok(u);
    }

    /* ---------- DELETE ---------- */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        if (!userRepo.existsById(id)) return notFound("User not found");
        userRepo.deleteById(id);
        return ok(Map.of("message", "User deleted"));
    }

    /* ---------- Helpers ---------- */
    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private boolean anyBlank(String... arr) {
        for (String s : arr) if (isBlank(s)) return true;
        return false;
    }

    private ResponseEntity<Map<String, String>> ok(Map<String, ?> body) {
        //noinspection unchecked
        return ResponseEntity.ok((Map<String, String>) body);
    }

    private ResponseEntity<Map<String, String>> bad(String msg) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", msg));
    }

    private ResponseEntity<Map<String, String>> notFound(String msg) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", msg));
    }

    private ResponseEntity<Map<String, String>> conflict(String msg) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", msg));
    }

    private ResponseEntity<Map<String, String>> unauthorized(String msg) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", msg));
    }
}