package org.thepaddlers.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.thepaddlers.model.User;
import org.thepaddlers.repository.UserRepository;
import org.thepaddlers.security.JwtService;
import org.thepaddlers.api.dto.ErrorResponse;

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public AuthController(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody User input, HttpServletRequest request) {
        if (input.getEmail() == null || input.getName() == null) {
            // Build a simple map to ensure JSON serialization
            Map<String, Object> body = new HashMap<>();
            body.put("status", 400);
            body.put("error", "Bad Request");
            body.put("message", "name and email required");
            body.put("path", request.getRequestURI());
            body.put("code", "AUTH_BAD_REQUEST");
            return ResponseEntity.status(400).contentType(MediaType.APPLICATION_JSON).body(body);
        }
        // naive: create user
        User saved = userRepository.save(input);
        return ResponseEntity.created(URI.create("/api/users/" + saved.getId())).body(saved);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body, HttpServletRequest request) {
        String email = body.get("email");
        if (email == null) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("status", 400);
            resp.put("error", "Bad Request");
            resp.put("message", "email required");
            resp.put("path", request.getRequestURI());
            resp.put("code", "AUTH_BAD_REQUEST");
            return ResponseEntity.status(400).contentType(MediaType.APPLICATION_JSON).body(resp);
        }
        Optional<User> u = userRepository.findByEmail(email);
        if (u.isEmpty()) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("status", 401);
            resp.put("error", "Unauthorized");
            resp.put("message", "invalid credentials");
            resp.put("path", request.getRequestURI());
            resp.put("code", "AUTH_INVALID_CREDENTIALS");
            return ResponseEntity.status(401).contentType(MediaType.APPLICATION_JSON).body(resp);
        }
        // Issue a JWT for local/dev testing
        String token = jwtService.issueToken(Map.of("email", u.get().getEmail()), String.valueOf(u.get().getId()));
        return ResponseEntity.ok(Map.of("token", token, "user", u.get()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(Map.of("token", "refreshed-dev-token"));
    }

    @PostMapping("/verify-phone")
    public ResponseEntity<?> verifyPhone(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(Map.of("verified", true));
    }
}
