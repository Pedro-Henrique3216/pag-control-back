package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.config.security.TokenService;
import com.pedrohenrique.pagcontrolback.exceptions.InvalidConfirmationTokenException;
import com.pedrohenrique.pagcontrolback.model.User;
import com.pedrohenrique.pagcontrolback.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class VerifyEmailUseCase {

    private final TokenService tokenService;
    private final UserRepository userRepository;

    public VerifyEmailUseCase(TokenService tokenService, UserRepository userRepository) {
        this.tokenService = tokenService;
        this.userRepository = userRepository;
    }

    @Transactional
    public void execute(String token) {
        var userId = tokenService.getUserId(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidConfirmationTokenException("Invalid confirmation token"));
        if(user.isEmailVerified()){
            return;
        }
        user.verifyEmail();
    }
}
