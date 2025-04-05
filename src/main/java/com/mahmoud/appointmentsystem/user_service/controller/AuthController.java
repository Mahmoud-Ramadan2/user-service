package com.mahmoud.appointmentsystem.user_service.controller;

import com.mahmoud.appointmentsystem.user_service.DTO.LoginDTO;
import com.mahmoud.appointmentsystem.user_service.security.JWTUtil;
import com.mahmoud.appointmentsystem.user_service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private AuthenticationManager authManager;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JWTUtil jwtUtil;
    @Autowired
    private UserService userService;


    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> loginHandler(@RequestBody LoginDTO body) {

        try {

            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(body.getUsername(), body.getPassword());
            Authentication authentication = authManager.authenticate(authenticationToken);
            Collection<? extends GrantedAuthority> authorities =
                    authentication.getAuthorities();
            System.out.println("authoritiesauthorities  "+authorities);

            String token = jwtUtil.generateToken(body.getUsername(), authorities);

            return ResponseEntity.ok(Collections.singletonMap("jwt-token", token));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("error", "Invalid Login Credentials"));
        }
    }

}
