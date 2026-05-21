package ru.vsu.core.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.vsu.core.model.dto.auth.AuthResponse;
import ru.vsu.core.model.dto.auth.LoginRequest;
import ru.vsu.core.model.dto.auth.RefreshTokenRequest;
import ru.vsu.core.model.dto.auth.RegisterRequest;
import ru.vsu.core.model.dto.auth.TokenPair;
import ru.vsu.core.service.AuthService;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@RequestBody @Valid RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody @Valid LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public TokenPair refresh(@RequestBody @Valid RefreshTokenRequest request) {
        return authService.refresh(request);
    }
}
