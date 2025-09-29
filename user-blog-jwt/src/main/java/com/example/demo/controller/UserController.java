package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public List<User> listUsers() {
        List<User> users = userRepository.findAll();
        users.forEach(u -> u.setPassword(null)); // don't expose pwd
        return users;
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        User u = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Not found"));
        u.setPassword(null);
        return u;
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public User updateUser(@PathVariable Long id, @RequestBody Map<String,Object> body) {
        User u = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Not found"));
        if (body.containsKey("password")) {
            // For demo: allow change (in real app check current user or admin)
            throw new RuntimeException("Password change via separate endpoint in real app");
        }
        if (body.containsKey("username")) u.setUsername((String)body.get("username"));
        userRepository.save(u);
        u.setPassword(null);
        return u;
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String,String> deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
        return Collections.singletonMap("status","deleted");
    }
}
