package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.config.security.TokenService;
import com.pedrohenrique.pagcontrolback.config.security.UserPrincipal;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthenticateUserUseCase {

    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;


    public AuthenticateUserUseCase(
            TokenService tokenService,
            AuthenticationManager authenticationManager
    ) {
        this.tokenService = tokenService;
        this.authenticationManager = authenticationManager;
    }

    public String execute(String email, String password) {
        UsernamePasswordAuthenticationToken usernamePassword  = new UsernamePasswordAuthenticationToken(email, password);
        Authentication authentication = authenticationManager.authenticate(usernamePassword);
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return tokenService.generateToken(userPrincipal.getUsername(), userPrincipal.getId());
    }
}
