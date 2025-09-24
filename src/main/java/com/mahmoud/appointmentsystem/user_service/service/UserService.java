package com.mahmoud.appointmentsystem.user_service.service;

import com.mahmoud.appointmentsystem.user_service.DTO.UserDTO;
import com.mahmoud.appointmentsystem.user_service.exception.UserNotFoundException;
import com.mahmoud.appointmentsystem.user_service.model.Role;
import com.mahmoud.appointmentsystem.user_service.model.User;
import com.mahmoud.appointmentsystem.user_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public UserDTO getUserById(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("user not found"));
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setEmail(user.getEmail());
        userDTO.setUsername(user.getUsername());
        for (Role role : user.getRole()) {
            userDTO.addRole(role.name());
        }

        return userDTO;
    }
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(()-> new UserNotFoundException("user not found"));
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public void updatePassword(User user, String newPassword) {
        user.setPassword(newPassword);
        userRepository.save(user);
    }


    public User createUser(User user) {
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

}
