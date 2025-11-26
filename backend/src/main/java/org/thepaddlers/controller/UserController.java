package org.thepaddlers.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.thepaddlers.model.User;
import org.thepaddlers.repository.UserRepository;
import org.thepaddlers.api.dto.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<User> list() {
        return userRepository.findAll();
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(HttpServletRequest request) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            ErrorResponse er = ErrorResponse.of(HttpStatus.UNAUTHORIZED, "unauthenticated", request.getRequestURI(), "AUTH_UNAUTHENTICATED");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(er);
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof Map) {
            Object idObj = ((Map<?,?>) principal).get("id");
            try {
                Long id = Long.parseLong(String.valueOf(idObj));
                Optional<User> u = userRepository.findById(id);
                if (u.isPresent()) return ResponseEntity.ok(u.get());
                else {
                    ErrorResponse er = ErrorResponse.of(HttpStatus.NOT_FOUND, "user not found", request.getRequestURI(), "USER_NOT_FOUND");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(er);
                }
            } catch (Exception ex) {
                ErrorResponse er = ErrorResponse.of(HttpStatus.UNAUTHORIZED, "invalid principal", request.getRequestURI(), "AUTH_INVALID_PRINCIPAL");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(er);
            }
        }
        // unknown principal type
        ErrorResponse er = ErrorResponse.of(HttpStatus.UNAUTHORIZED, "invalid principal", request.getRequestURI(), "AUTH_INVALID_PRINCIPAL");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(er);
    }
}
