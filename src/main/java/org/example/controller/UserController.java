package org.example.controller;

import org.example.model.User;
import org.example.repo.UserRepository;
import org.example.service.OTPService;
import org.example.service.UserService;
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

    @Autowired
    private UserRepository userRepo;
    @Autowired
    private OTPService otpService;
    @Autowired
    private UserService userService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UserController.class);

    /* ---------- ADMIN: create skeleton user (username + role) ----------- */
    @PostMapping("/add")
    public ResponseEntity<?> addUser(@RequestBody User user) {
        log.info("Add user request: username={}, role={}", user.getUsername(), user.getRole());
        if (isBlank(user.getUsername()) || isBlank(user.getRole()) || isBlank(user.getPassword())) {
            log.warn("Add user failed: missing required fields");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "username,password & role required"));
        }
        if (userRepo.findByUsername(user.getUsername()).isPresent()) {
            log.warn("Add user failed: user already exists: {}", user.getUsername());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", "User already exists"));
        }
        user.setRegistrationCompleted(false);
        user.setPassword(passwordEncoder.encode(user.getPassword())); // no password until tenant finishes
        User saved = userRepo.save(user);
        log.info("User created: id={}, username={}", saved.getId(), saved.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /* ---------- OPTIONAL direct signup (email+mobile+password) ---------- */
    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@RequestBody Map<String, String> payload) {
        log.info("Signup request: email={}, mobile={}", payload.get("email"), payload.get("mobileNumber"));
        String email = payload.get("email");
        String password = payload.get("password");
        String mobile = payload.get("mobileNumber");
        if (anyBlank(email, password, mobile)) {
            log.warn("Signup failed: missing required fields");
            return bad("email,password,mobileNumber required");
        }
        User u = userService.registerUser(email, password, mobile);
        log.info("User signed up: id={}, email={}", u.getId(), u.getMail());
        return ok(Map.of("id", u.getId(), "email", u.getMail()));
    }

    /* ---------- STEP 1 tenant: start registration (username given by admin) ---------- */
    @PostMapping("/registration/start")
    public ResponseEntity<?> startRegistration(@RequestBody Map<String, String> payload) {
        log.info("Start registration: username={}, email={}, mobile={}", payload.get("username"), payload.get("email"), payload.get("mobileNumber"));
        String username = payload.get("username");
        String email = payload.get("email");
        String mobile = payload.get("mobileNumber");
        if (anyBlank(username, email, mobile)) {
            log.warn("Start registration failed: missing required fields");
            return bad("username,email,mobileNumber required");
        }
        Optional<User> opt = userRepo.findByUsername(username);
        if (opt.isEmpty()) {
            log.warn("Start registration failed: username not found: {}", username);
            return notFound("Username not found");
        }
        User u = opt.get();
        if (u.isRegistrationCompleted()) {
            log.warn("Start registration failed: already completed for username={}", username);
            return conflict("Registration already completed");
        }
        u.setMail(email.trim());
        u.setPhone(mobile.trim());
        userRepo.save(u);
        otpService.generateOTP(mobile.trim());
        log.info("OTP generated and sent for registration: mobile={}", mobile);
        return ok(Map.of("message", "OTP sent to mobile"));
    }

    /* ---------- STEP 2 tenant: finish registration with OTP + password ---------- */
    @PostMapping("/registration/finish")
    public ResponseEntity<?> finishRegistration(@RequestBody Map<String, String> payload) {
        log.info("Finish registration: username={}, otp={}", payload.get("username"), payload.get("otp"));
        String username = payload.get("username");
        String otp = payload.get("otp");
        String password = payload.get("password");
        if (anyBlank(username, otp, password)) {
            log.warn("Finish registration failed: missing required fields");
            return bad("username,otp,password required");
        }
        Optional<User> opt = userRepo.findByUsername(username);
        if (opt.isEmpty()) {
            log.warn("Finish registration failed: username not found: {}", username);
            return notFound("Username not found");
        }
        User u = opt.get();
        if (u.isRegistrationCompleted()) {
            log.warn("Finish registration failed: already completed for username={}", username);
            return conflict("Already completed");
        }
        if (u.getPhone() == null) {
            log.warn("Finish registration failed: start registration not done for username={}", username);
            return bad("Start registration first");
        }
        if (!otpService.verifyOTP(u.getPhone(), otp)) {
            log.warn("Finish registration failed: invalid OTP for username={}", username);
            return unauthorized("Invalid OTP");
        }
        u.setPassword(passwordEncoder.encode(password));
        u.setRegistrationCompleted(true);
        userRepo.save(u);
        log.info("Registration completed for username={}", username);
        return ok(Map.of("message", "Registration completed"));
    }

    /* ---------- LOGIN ---------- */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> payload) {
        log.info("Login attempt: username={}", payload.get("username"));
        String username = payload.get("username");
        String password = payload.get("password");
        if (anyBlank(username, password)) {
            log.warn("Login failed: missing required fields");
            return bad("username,password required");
        }
        Optional<User> opt = userRepo.findByUsername(username);
        if (opt.isEmpty()) {
            log.warn("Login failed: invalid credentials for username={}", username);
            return unauthorized("Invalid credentials");
        }
        User u = opt.get();
        if (!u.isRegistrationCompleted()) {
            log.warn("Login failed: registration incomplete for username={}", username);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "Registration incomplete"));
        }
        if (isBlank(password) || isBlank(u.getPassword()) || !passwordEncoder.matches(password, u.getPassword())) {
            log.warn("Login failed: invalid credentials for username={}", username);
            return unauthorized("Invalid credentials");
        }
        log.info("Login successful: username={}, role={}", u.getUsername(), u.getRole());
        return ok(Map.of("username", u.getUsername(), "role", u.getRole()));
    }

    /* ---------- FORGOT PASSWORD (OTP to registered mobile) ---------- */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> payload) {
        log.info("Forgot password request: username={}", payload.get("username"));
        String username = payload.get("username");
        if (isBlank(username)) {
            log.warn("Forgot password failed: missing username");
            return bad("username required");
        }
        Optional<User> opt = userRepo.findByUsername(username);
        if (opt.isEmpty()) {
            log.warn("Forgot password failed: user not found: {}", username);
            return notFound("User not found");
        }
        User u = opt.get();
        if (isBlank(u.getPhone())) {
            log.warn("Forgot password failed: no mobile registered for username={}", username);
            return bad("No mobile registered");
        }
        otpService.generateOTP(u.getPhone());
        log.info("OTP sent for forgot password: username={}, mobile={}", username, u.getPhone());
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

    private ResponseEntity<?> bad(String msg) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(msg);
    }

    private ResponseEntity<?> notFound(String msg) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(msg);
    }

    private ResponseEntity<?> conflict(String msg) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(msg);
    }

    private ResponseEntity<?> unauthorized(String msg) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(msg);
    }

    private ResponseEntity<?> ok(Object body) {
        return ResponseEntity.ok(body);
    }
}
