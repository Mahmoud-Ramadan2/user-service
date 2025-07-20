package com.mahmoud.appointmentsystem.user_service.service;

import com.mahmoud.appointmentsystem.user_service.model.PasswordResetToken;
import com.mahmoud.appointmentsystem.user_service.repository.PasswordResetTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PasswordResetTokenService {

    @Autowired
    private PasswordResetTokenRepository tokenRepo;
    @Autowired
    private EmailService emailService;

    public void createToken(PasswordResetToken token) {
        if(tokenRepo.findByUser(token.getUser()).isEmpty()){
        tokenRepo.save(token);
        }
    }
    public Optional<PasswordResetToken> getByToken(String token) {
        return tokenRepo.findByToken(token);
    }

    public void deleteToken(PasswordResetToken token) {
        tokenRepo.delete(token);
    }
}
