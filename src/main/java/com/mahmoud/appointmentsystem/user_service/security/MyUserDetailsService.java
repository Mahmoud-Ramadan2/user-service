package com.mahmoud.appointmentsystem.user_service.security;

import com.mahmoud.appointmentsystem.user_service.model.User;
import com.mahmoud.appointmentsystem.user_service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    private UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userService.getUserByUsername(username);


        return new org.springframework.security.core.userdetails.User(username,
                user.getPassword(),
                user.getRole().stream().map(role ->new SimpleGrantedAuthority(role.name())).toList()
        );
    }
}
