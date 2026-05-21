package ru.vsu.core.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.vsu.core.exception.ConflictException;
import ru.vsu.core.exception.UnauthorizedException;
import ru.vsu.core.model.dto.auth.AuthResponse;
import ru.vsu.core.model.dto.auth.LoginRequest;
import ru.vsu.core.model.dto.auth.RefreshTokenRequest;
import ru.vsu.core.model.dto.auth.RegisterRequest;
import ru.vsu.core.model.dto.auth.TokenPair;
import ru.vsu.core.model.dto.user.UserDto;
import ru.vsu.core.model.entity.User;
import ru.vsu.core.repository.UserRepository;
import ru.vsu.core.security.JwtService;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ConflictException("Email already taken");
        }

        String refreshToken = jwtService.generateRefreshToken();
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .refreshToken(refreshToken)
                .build();

        user = userRepository.save(user);

        return buildAuthResponse(user, refreshToken);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        String refreshToken = jwtService.generateRefreshToken();
        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        return buildAuthResponse(user, refreshToken);
    }

    public TokenPair refresh(RefreshTokenRequest request) {
        User user = userRepository.findByRefreshToken(request.getRefreshToken())
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        String newRefreshToken = jwtService.generateRefreshToken();
        user.setRefreshToken(newRefreshToken);
        userRepository.save(user);

        return TokenPair.builder()
                .accessToken(jwtService.generateAccessToken(user.getId()))
                .refreshToken(newRefreshToken)
                .expiresIn(jwtService.getAccessTokenExpirySeconds())
                .build();
    }

    private AuthResponse buildAuthResponse(User user, String refreshToken) {
        return AuthResponse.builder()
                .tokens(TokenPair.builder()
                        .accessToken(jwtService.generateAccessToken(user.getId()))
                        .refreshToken(refreshToken)
                        .expiresIn(jwtService.getAccessTokenExpirySeconds())
                        .build())
                .user(UserDto.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .email(user.getEmail())
                        .build())
                .build();
    }
}
