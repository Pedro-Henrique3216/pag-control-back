package com.pedrohenrique.pagcontrolback.controllers;

import com.pedrohenrique.pagcontrolback.dtos.command.CreateUserCommand;
import com.pedrohenrique.pagcontrolback.dtos.request.LoginRequestDto;
import com.pedrohenrique.pagcontrolback.dtos.request.UserRequestDto;
import com.pedrohenrique.pagcontrolback.dtos.response.LoginResponseDto;
import com.pedrohenrique.pagcontrolback.dtos.response.UserResponseDto;
import com.pedrohenrique.pagcontrolback.mappers.UserMapper;
import com.pedrohenrique.pagcontrolback.model.User;
import com.pedrohenrique.pagcontrolback.usecases.AuthenticateUserUseCase;
import com.pedrohenrique.pagcontrolback.usecases.CreateUserUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/users")
public class UserController {

    private final CreateUserUseCase createUserUseCase;
    private final AuthenticateUserUseCase authenticateUserUseCase;

    public UserController(CreateUserUseCase createUserUseCase, AuthenticateUserUseCase authenticateUserUseCase) {
        this.createUserUseCase = createUserUseCase;
        this.authenticateUserUseCase = authenticateUserUseCase;
    }

    @PostMapping("/signin")
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
}
