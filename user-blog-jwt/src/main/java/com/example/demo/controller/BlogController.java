package com.example.demo.controller;

import com.example.demo.model.Blog;
import com.example.demo.repository.BlogRepository;
import com.example.demo.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/blogs")
public class BlogController {
    @Autowired BlogRepository blogRepository;

    // Anyone authenticated can list blogs
    @GetMapping
    public List<Blog> listAll() {
        return blogRepository.findAll();
    }

    @GetMapping("/mine")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public List<Blog> listMine() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return blogRepository.findByOwnerUsername(username);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Blog create(@RequestBody Blog blog) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        blog.setOwnerUsername(username);
        return blogRepository.save(blog);
    }

    @GetMapping("/{id}")
    public Blog get(@PathVariable Long id) {
        return blogRepository.findById(id).orElseThrow(() -> new RuntimeException("Not found"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Blog update(@PathVariable Long id, @RequestBody Blog payload) {
        Blog b = blogRepository.findById(id).orElseThrow(() -> new RuntimeException("Not found"));
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !b.getOwnerUsername().equals(username)) {
            throw new RuntimeException("Forbidden: can only update your own blog");
        }
        b.setTitle(payload.getTitle());
        b.setContent(payload.getContent());
        return blogRepository.save(b);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public void delete(@PathVariable Long id) {
        Blog b = blogRepository.findById(id).orElseThrow(() -> new RuntimeException("Not found"));
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !b.getOwnerUsername().equals(username)) {
            throw new RuntimeException("Forbidden: can only delete your own blog");
        }
        blogRepository.deleteById(id);
    }
}
