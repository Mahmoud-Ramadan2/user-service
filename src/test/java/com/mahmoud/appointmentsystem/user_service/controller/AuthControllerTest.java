package com.mahmoud.appointmentsystem.user_service.controller;

import com.mahmoud.appointmentsystem.user_service.DTO.LoginDTO;
import com.mahmoud.appointmentsystem.user_service.DTO.RegisterDTO;
import com.mahmoud.appointmentsystem.user_service.model.Role;
import com.mahmoud.appointmentsystem.user_service.model.User;
import com.mahmoud.appointmentsystem.user_service.security.JWTUtil;
import com.mahmoud.appointmentsystem.user_service.service.UserService;
import org.hibernate.mapping.Any;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 *
 *
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController Tests")
class AuthControllerTest {

    @Mock
    UserService userService;
    @Mock
    private AuthenticationManager authManager;

    @Mock
    private JWTUtil jwtUtil;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthController authController;

    @Captor
    ArgumentCaptor<User> userCaptor;
    @Captor
    ArgumentCaptor<UsernamePasswordAuthenticationToken> authenticationCaptor;


    @Nested
    @DisplayName("Registration Tests")
    class RegistrationTests {


        private RegisterDTO registerDTO;

        /**
         * @throws Exception
         */
        @Test
        @DisplayName("When valid data is provided, it should return a JWT token")
        void register_WithValidData_ReturnsJwtToken() throws Exception {
            //Arrange
            registerDTO = new RegisterDTO();
            registerDTO.setUsername("testUser");
            registerDTO.setPassword("testPassword");
            registerDTO.setEmail("test@mail.com");
            registerDTO.setRoles(Collections.emptySet());

            User user = new User();
            user.setUsername(registerDTO.getUsername());
            user.setPassword("encodedPassword");
            user.setEmail(registerDTO.getEmail());
            user.setRole(Collections.singleton(Role.ROLE_PATIENT));

            when(userService.getUserByUsername(registerDTO.getUsername())).thenReturn(null);
            when(passwordEncoder.encode(registerDTO.getPassword())).thenReturn("encodedPassword");
            when(userService.createUser(any())).thenReturn(user);
            Authentication mockAuthentication = mock(Authentication.class);
            when(authManager.authenticate(any()))
                    .thenReturn(mockAuthentication);
            when(mockAuthentication.getAuthorities()).thenReturn(Collections.emptySet());
            when(jwtUtil.generateToken(anyString(), any())).thenReturn("jwtToken");

            //Act
            ResponseEntity<Map<String, String>> response = authController.registerHandler(registerDTO);


            //Assert and Verify
            verify(userService, times(1)).createUser(userCaptor.capture());
            User createdUser = userCaptor.getValue();
            assertNotNull(createdUser);
            assertTrue(createdUser.getRole().contains(Role.ROLE_PATIENT));
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, createdUser.getRole().size());
            assertEquals("jwtToken", response.getBody().get("jwt-token"));
            verify(userService, times(1)).createUser(userCaptor.capture());


        }

        @Test
        @DisplayName("When username already exists, it should return Conflict status")
        void register_WithExistingUsername_ReturnsConflict() {
            // Arrange
            registerDTO = new RegisterDTO();
            registerDTO.setUsername("existingUser");
            registerDTO.setPassword("testPassword");
            registerDTO.setEmail("test@mail.com");
            registerDTO.setRoles(Collections.emptySet());
            when(userService.getUserByUsername(registerDTO.getUsername())).thenReturn(new User());

            // Act
            ResponseEntity<Map<String, String>> response = authController.registerHandler(registerDTO);

            // Assert
            assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
            assertEquals("User already exists", response.getBody().get("error"));
            verify(userService, never()).createUser(any());

        }

        @Test
        @DisplayName("When no roles is admin, it should default to PATIENT role")
        void register_WithAdminRole_DefaultsToPatientRole() {
            // Arrange
            RegisterDTO dtoWithAdmin = new RegisterDTO(
                    "adminuser",
                    "password",
                    "admin@test.com",
                    Set.of(Role.ROLE_ADMIN)
            );
            when(userService.getUserByUsername(dtoWithAdmin.getUsername())).thenReturn(null);
            when(passwordEncoder.encode(dtoWithAdmin.getPassword())).thenReturn("encodedPassword");
            when(authManager.authenticate(any()))
                    .thenReturn(mock(Authentication.class));
            when(jwtUtil.generateToken(anyString(), any())).thenReturn("jwtToken");
            // Act
            ResponseEntity<Map<String, String>> response = authController.registerHandler(dtoWithAdmin);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("jwtToken", response.getBody().get("jwt-token"));
            verify(userService, times(1)).createUser(userCaptor.capture());
            User createdUser = userCaptor.getValue();
            assertNotNull(createdUser);
            assertEquals(Set.of(Role.ROLE_PATIENT), createdUser.getRole());
        }


    }

    @Nested
    @DisplayName("Login Tests")
    class LoginTests {

        private LoginDTO loginDTO;

        @Captor
        ArgumentCaptor<UsernamePasswordAuthenticationToken> authenticationCaptor;

        @Test
        @DisplayName("When valid credentials are provided, it should return a JWT token")
        void login_WithValidCredentials_ReturnsJwtToken() {
            // Arrange
            String username = "testUser";
            String password = "testPassword";
            loginDTO = new LoginDTO(username, password);

            User user = new User();
            user.setUsername(username);
            user.setPassword("encodedPassword");
            user.setRole(Set.of(Role.ROLE_PATIENT));

            // Mock authentication and JWT generation

            Authentication mockAuthentication = mock(Authentication.class);
            when(authManager.authenticate(new UsernamePasswordAuthenticationToken(username, password))).thenReturn(mockAuthentication);

            when(jwtUtil.generateToken(eq(username), any())).thenReturn("jwtToken");

            // Act
            ResponseEntity<Map<String, String>> response = authController.loginHandler(loginDTO);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("jwtToken", response.getBody().get("jwt-token"));

            // Capture and verify the auth token used
            verify(authManager).authenticate(authenticationCaptor.capture());
            UsernamePasswordAuthenticationToken captured = authenticationCaptor.getValue();
            assertEquals(username, captured.getPrincipal());
            assertEquals(password, captured.getCredentials());
        }
    }
}