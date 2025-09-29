package com.example.demo.security;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class CustomUserDetails implements UserDetailsService {
    private final UserRepository userRepo;
    public CustomUserDetails(UserRepository userRepo){ this.userRepo = userRepo; }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User u = userRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("No user: " + username));
        return new org.springframework.security.core.userdetails.User(
                u.getUsername(),
                u.getPassword(),
                u.getRoles().stream().map(r -> new SimpleGrantedAuthority(r.name())).collect(Collectors.toList())
        );
    }
}
