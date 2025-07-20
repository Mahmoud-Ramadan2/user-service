package com.mahmoud.appointmentsystem.user_service.repository;

import com.mahmoud.appointmentsystem.user_service.model.PasswordResetToken;
import com.mahmoud.appointmentsystem.user_service.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {


    Optional<PasswordResetToken> findByToken(String token);
    Optional<PasswordResetToken>findByUser(User user);



}
