package com.mahmoud.appointmentsystem.user_service.controller;

import com.mahmoud.appointmentsystem.user_service.DTO.LoginDTO;
import com.mahmoud.appointmentsystem.user_service.DTO.RegisterDTO;
import com.mahmoud.appointmentsystem.user_service.model.PasswordResetToken;
import com.mahmoud.appointmentsystem.user_service.model.Role;
import com.mahmoud.appointmentsystem.user_service.model.User;
import com.mahmoud.appointmentsystem.user_service.security.JWTUtil;
import com.mahmoud.appointmentsystem.user_service.service.EmailService;
import com.mahmoud.appointmentsystem.user_service.service.PasswordResetTokenService;
import com.mahmoud.appointmentsystem.user_service.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * AuthController handles user authentication and registration
 * It provides endpoints for registration, login, password reset, and forgot password functionality
 */
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

    @Autowired
    private PasswordResetTokenService tokenService;
    @Autowired
    private EmailService emailService;

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    /**
     * Register handler for user registration
     *
     * @param body contains user details for registration
     * @return JWT token if registration is successful, error message otherwise
     */
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

    /**
     * Login handler for user authentication
     *
     * @param body contains username and password
     * @return JWT token if authentication is successful, error message otherwise
     */
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

    /**
     * it generates a reset link and sends it to the user's email (usually a frontend link)
     *this link is valid for 3 minutes
     * * @param email
     *
     * @return
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam String email) {
        Optional<User> userOpt = userService.getUserByEmail(email);
        if (userOpt.isEmpty()) {
            logger.error("User with email {} not found", email);
            return ResponseEntity.ok("If email exists, reset link was sent.");
        }
        User user = userOpt.get();

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUser(user);
        String token = UUID.randomUUID().toString(); // Generate a random token
        resetToken.setToken(token);
        resetToken.setExpiryDate(LocalDateTime.now().plusMinutes(3)); // Token valid for 3 minutes
        tokenService.createToken(resetToken);
        String link = "http://localhost:8081/auth/reset-password?token=" + token;  // frontend link to reset password
        String subject = "Password Reset Request";
        String body = "Click the link to reset your password: " + link;
        // Send email with the reset link

        emailService.sendEmail(email, subject, body);

        return ResponseEntity.ok("Reset link sent to email.");
    }

    /**
     * Reset password endpoint
     * This endpoint is called when the user clicks the reset link
     * @param token
     * @param newPassword
     * @return
     */
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam String token, @RequestParam String newPassword) {

        Optional<PasswordResetToken> tokenOpt = tokenService.getByToken(token);
        if (tokenOpt.isEmpty()) {
            logger.error("Invalid or expired token: {}", token);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired token.");
        }
        PasswordResetToken resetToken = tokenOpt.get();
        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            logger.error("Token expired: {}", token);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Token expired.");
        }
        // update the user's password
        User user = resetToken.getUser();
        String encodedPassword = passwordEncoder.encode(newPassword);
        userService.updatePassword(user, encodedPassword);
        // delete the token after successful password reset
        tokenService.deleteToken(resetToken);

        return ResponseEntity.ok("Password successfully reset.");
    }

}
