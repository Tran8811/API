package com.example.demo.controller;

import com.example.demo.model.*;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtUtil jwtUtil;

    @PostMapping("/register")
    public Map<String,Object> register(@RequestBody RegisterRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new RuntimeException("Username exists");
        }
        User u = new User();
        u.setUsername(req.getUsername());
        u.setPassword(passwordEncoder.encode(req.getPassword()));
        Set<Role> roles = new HashSet<>();
        if (req.getRole() != null && req.getRole().equalsIgnoreCase("ADMIN")) roles.add(Role.ROLE_ADMIN);
        else roles.add(Role.ROLE_USER);
        u.setRoles(roles);
        userRepository.save(u);
        Map<String,Object> m = new HashMap<>();
        m.put("msg","registered");
        return m;
    }

    @PostMapping("/login")
    public Map<String,Object> login(@RequestBody LoginRequest req) {
        User u = userRepository.findByUsername(req.getUsername())
                .orElseThrow(() -> new RuntimeException("Bad credentials"));
        if (!passwordEncoder.matches(req.getPassword(), u.getPassword())) throw new RuntimeException("Bad credentials");
        String token = jwtUtil.generateToken(u.getUsername(), u.getRoles());
        Map<String,Object> res = new HashMap<>();
        res.put("token", token);
        return res;
    }

    // DTO classes
    public static class RegisterRequest {
        private String username; private String password; private String role;
        // getters/setters

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }
    }
    public static class LoginRequest {
        private String username; private String password;
        // getters/setters

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
