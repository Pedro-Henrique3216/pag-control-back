package com.pedrohenrique.pagcontrolback.controllers;

import com.pedrohenrique.pagcontrolback.dtos.command.CreateUserCommand;
import com.pedrohenrique.pagcontrolback.dtos.request.LoginRequestDto;
import com.pedrohenrique.pagcontrolback.dtos.request.ResendConfirmationEmailRequest;
import com.pedrohenrique.pagcontrolback.dtos.request.UserRequestDto;
import com.pedrohenrique.pagcontrolback.dtos.response.LoginResponseDto;
import com.pedrohenrique.pagcontrolback.dtos.response.UserResponseDto;
import com.pedrohenrique.pagcontrolback.mappers.UserMapper;
import com.pedrohenrique.pagcontrolback.model.User;
import com.pedrohenrique.pagcontrolback.usecases.AuthenticateUserUseCase;
import com.pedrohenrique.pagcontrolback.usecases.CreateUserUseCase;
import com.pedrohenrique.pagcontrolback.usecases.ResendEmailConfirmationUseCase;
import com.pedrohenrique.pagcontrolback.usecases.VerifyEmailUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/users")
public class UserController {

    private final CreateUserUseCase createUserUseCase;
    private final AuthenticateUserUseCase authenticateUserUseCase;
    private final VerifyEmailUseCase verifyEmailUseCase;
    private final ResendEmailConfirmationUseCase resendEmailConfirmationUseCase;

    public UserController(
            CreateUserUseCase createUserUseCase,
            AuthenticateUserUseCase authenticateUserUseCase,
            VerifyEmailUseCase verifyEmailUseCase,
            ResendEmailConfirmationUseCase resendEmailConfirmationUseCase
    ) {
        this.createUserUseCase = createUserUseCase;
        this.authenticateUserUseCase = authenticateUserUseCase;
        this.verifyEmailUseCase = verifyEmailUseCase;
        this.resendEmailConfirmationUseCase = resendEmailConfirmationUseCase;
    }

    @PostMapping("/sign-up")
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody UserRequestDto userRequestDto, UriComponentsBuilder uriBuilder) {
        CreateUserCommand command = new CreateUserCommand(
                userRequestDto.name(),
                userRequestDto.fantasyName(),
                userRequestDto.email(),
                userRequestDto.password(),
                userRequestDto.phone(),
                userRequestDto.personType()
        );
        User userSaved = createUserUseCase.execute(command);
        URI uri = uriBuilder.path("/users/{id}").buildAndExpand(userSaved.getId()).toUri();
        return ResponseEntity.created(uri).body(UserMapper.toResponse(userSaved));

    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        String token = authenticateUserUseCase.execute(loginRequestDto.email(), loginRequestDto.password());
        return ResponseEntity.ok(new LoginResponseDto(token));
    }

    @PostMapping("/resend-confirmation")
    public ResponseEntity<Void> resendEmailConfirmation(@Valid @RequestBody ResendConfirmationEmailRequest request) {
        resendEmailConfirmationUseCase.execute(request.email());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/confirm")
    public ResponseEntity<Void> confirm(@RequestParam(required = true) String token) {
        verifyEmailUseCase.execute(token);
        return ResponseEntity.ok().build();
    }
}
