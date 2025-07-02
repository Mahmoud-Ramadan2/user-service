package com.mahmoud.appointmentsystem.user_service.controller;

import com.mahmoud.appointmentsystem.user_service.DTO.LoginDTO;
import com.mahmoud.appointmentsystem.user_service.DTO.RegisterDTO;
import com.mahmoud.appointmentsystem.user_service.model.Role;
import com.mahmoud.appointmentsystem.user_service.model.User;
import com.mahmoud.appointmentsystem.user_service.security.JWTUtil;
import com.mahmoud.appointmentsystem.user_service.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

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

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> registerHandler(@Valid @RequestBody RegisterDTO body) {
        // Check if the user already exists
       try {
            if (userService.getUserByUsername(body.getUsername()) != null) {
                logger.error("User already exists with username: {}", body.getUsername());
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Collections.singletonMap("error", "User already exists"));
            }
        } catch (RuntimeException e) {
            // User doesn't exist (proceed with registration)
            logger.debug("User does not exist, proceeding with registration...");
        }
        try {
            // Create a new user
            User user = new User();
            user.setUsername(body.getUsername());
            user.setPassword(passwordEncoder.encode(body.getPassword()));
            user.setEmail(body.getEmail());
            if (body.getRoles() == null || body.getRoles().isEmpty()) {
                user.setRole(Collections.singleton(Role.ROLE_PATIENT));// Default to ROLE_PATIENT if no roles provided

            } else if (body.getRoles().stream().anyMatch(r ->
                    !EnumSet.of(Role.ROLE_DOCTOR, Role.ROLE_PATIENT).contains(r)
            )) {
                // If any role is not DOCTOR or PATIENT or insert ROLE_ADMIN, set to ROLE_PATIENT
                user.setRole(Collections.singleton(Role.ROLE_PATIENT));
            } else {
                user.setRole(body.getRoles()); // Otherwise, set the provided roles

            }
            // set Roles in another way
//            Set<Role> allwedRoles = EnumSet.of(Role.ROLE_PATIENT, Role.ROLE_DOCTOR);
//            Set<Role> rolestoSet = (body.getRoles() ==null || body.getRoles().isEmpty() ||
//                    body.getRoles().stream().anyMatch(r-> !allwedRoles.contains(r))) ?
//                    Collections.singleton(Role.ROLE_PATIENT) : body.getRoles();
//            user.setRole(rolestoSet);

            userService.createUser(user);
            logger.info("User registered successfully: {}", body.getUsername());

            // Authenticate the user
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(body.getUsername(), body.getPassword());

            Authentication authentication = authManager.authenticate(authenticationToken);

            // Generate JWT token
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            String token = jwtUtil.generateToken(body.getUsername(), authorities);

            return ResponseEntity.ok(Collections.singletonMap("jwt-token", token));
        } catch (AuthenticationException e) {
            logger.error("Authentication failed for user: {}", body.getUsername(), e);
            return
                    ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(Collections.singletonMap("error", "Invalid Registration Credentials"));
        } catch (Exception e) {
            logger.error("Error occurred while registering user: {}", body.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Internal Server Error"));

        }


    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> loginHandler(@Valid @RequestBody LoginDTO body) {

        try {

            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(body.getUsername(), body.getPassword());
            Authentication authentication = authManager.authenticate(authenticationToken);
            Collection<? extends GrantedAuthority> authorities =
                    authentication.getAuthorities();
            System.out.println("authoritiesauthorities  " + authorities);

            String token = jwtUtil.generateToken(body.getUsername(), authorities);

            return ResponseEntity.ok(Collections.singletonMap("jwt-token", token));
        } catch (AuthenticationException e) {
            logger.error("Authentication failed for user: {}", body.getUsername(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("error", "Invalid Login Credentials"));
        }
    }

}
