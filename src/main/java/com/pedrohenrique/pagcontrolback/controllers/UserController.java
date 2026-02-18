package com.pedrohenrique.pagcontrolback.controllers;

import com.pedrohenrique.pagcontrolback.dtos.request.UserRequestDto;
import com.pedrohenrique.pagcontrolback.dtos.response.UserResponseDto;
import com.pedrohenrique.pagcontrolback.mappers.UserMapper;
import com.pedrohenrique.pagcontrolback.model.User;
import com.pedrohenrique.pagcontrolback.repositories.UserRepository;
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

    public UserController(CreateUserUseCase createUserUseCase) {
        this.createUserUseCase = createUserUseCase;
    }

    @PostMapping("/signin")
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody UserRequestDto userRequestDto, UriComponentsBuilder uriBuilder) {
        User user = UserMapper.toDomain(userRequestDto);
        User userSaved = createUserUseCase.execute(user);
        URI uri = uriBuilder.path("/users/{id}").buildAndExpand(userSaved.getId()).toUri();
        return ResponseEntity.created(uri).body(UserMapper.toResponse(userSaved));
    }
}
