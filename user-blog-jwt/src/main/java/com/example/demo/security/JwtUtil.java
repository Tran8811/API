package com.example.demo.security;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.Date;
import java.util.stream.Collectors;
import com.example.demo.model.Role;

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expirationMs}")
    private int jwtExpirationMs;

    public String generateToken(String username, Set<Role> roles) {
        Map<String,Object> claims = new HashMap<>();
        claims.put("roles", roles.stream().map(Enum::name).collect(Collectors.toList()));
        return Jwts.builder()
                .setSubject(username)
                .addClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis()+jwtExpirationMs))
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    public boolean validateJwtToken(String token) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    public String getUsernameFromJwt(String token) {
        return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody().getSubject();
    }

    public List<String> getRolesFromJwt(String token) {
        Object rolesObj = Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody().get("roles");
        if (rolesObj instanceof List) {
            List<?> mixed = (List<?>) rolesObj;
            return mixed.stream().map(Object::toString).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
