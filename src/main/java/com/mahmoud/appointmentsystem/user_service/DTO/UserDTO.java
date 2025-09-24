package com.mahmoud.appointmentsystem.user_service.DTO;

import java.util.HashSet;
import java.util.Set;

public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private Set <String>roles;

    public UserDTO() {
    }
    public UserDTO(String username, String email) {
        this.username = username;
        this.email = email;
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String name) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public void addRole(String role) {
        if (this.roles != null) {
            this.roles.add(role);
        }else {
            this.roles = new HashSet<>();
            this.roles.add(role);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
