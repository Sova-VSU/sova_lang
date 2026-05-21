package ru.vsu.core.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.vsu.core.exception.ConflictException;
import ru.vsu.core.exception.UnauthorizedException;
import ru.vsu.core.model.dto.auth.AuthResponse;
import ru.vsu.core.model.dto.auth.LoginRequest;
import ru.vsu.core.model.dto.auth.RefreshTokenRequest;
import ru.vsu.core.model.dto.auth.RegisterRequest;
import ru.vsu.core.model.dto.auth.TokenPair;
import ru.vsu.core.model.entity.User;
import ru.vsu.core.repository.UserRepository;
import ru.vsu.core.security.JwtService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtService jwtService;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_success() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Ivan");
        request.setEmail("ivan@test.com");
        request.setPassword("pass123");

        when(userRepository.findByEmail("ivan@test.com")).thenReturn(Optional.empty());
        when(jwtService.generateRefreshToken()).thenReturn("refresh-token");
        when(passwordEncoder.encode("pass123")).thenReturn("hashed");
        when(userRepository.save(any())).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId("user-1");
            return u;
        });
        when(jwtService.generateAccessToken("user-1")).thenReturn("access-token");
        when(jwtService.getAccessTokenExpirySeconds()).thenReturn(900L);

        AuthResponse response = authService.register(request);

        assertThat(response.getUser().getEmail()).isEqualTo("ivan@test.com");
        assertThat(response.getTokens().getAccessToken()).isEqualTo("access-token");
        assertThat(response.getTokens().getRefreshToken()).isEqualTo("refresh-token");
    }

    @Test
    void register_emailAlreadyTaken_throwsConflict() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Ivan");
        request.setEmail("ivan@test.com");
        request.setPassword("pass123");

        when(userRepository.findByEmail("ivan@test.com")).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void login_success() {
        LoginRequest request = new LoginRequest();
        request.setEmail("ivan@test.com");
        request.setPassword("pass123");
        User user = User.builder().id("user-1").email("ivan@test.com").password("hashed").build();

        when(userRepository.findByEmail("ivan@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("pass123", "hashed")).thenReturn(true);
        when(jwtService.generateRefreshToken()).thenReturn("new-refresh");
        when(userRepository.save(any())).thenReturn(user);
        when(jwtService.generateAccessToken("user-1")).thenReturn("access-token");
        when(jwtService.getAccessTokenExpirySeconds()).thenReturn(900L);

        AuthResponse response = authService.login(request);

        assertThat(response.getTokens().getAccessToken()).isEqualTo("access-token");
    }

    @Test
    void login_emailNotFound_throwsUnauthorized() {
        LoginRequest request = new LoginRequest();
        request.setEmail("unknown@test.com");
        request.setPassword("pass123");

        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void login_wrongPassword_throwsUnauthorized() {
        LoginRequest request = new LoginRequest();
        request.setEmail("ivan@test.com");
        request.setPassword("wrong");
        User user = User.builder().id("user-1").email("ivan@test.com").password("hashed").build();

        when(userRepository.findByEmail("ivan@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void refresh_success() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("old-refresh");
        User user = User.builder().id("user-1").refreshToken("old-refresh").build();

        when(userRepository.findByRefreshToken("old-refresh")).thenReturn(Optional.of(user));
        when(jwtService.generateRefreshToken()).thenReturn("new-refresh");
        when(userRepository.save(any())).thenReturn(user);
        when(jwtService.generateAccessToken("user-1")).thenReturn("new-access");
        when(jwtService.getAccessTokenExpirySeconds()).thenReturn(900L);

        TokenPair tokens = authService.refresh(request);

        assertThat(tokens.getAccessToken()).isEqualTo("new-access");
        assertThat(tokens.getRefreshToken()).isEqualTo("new-refresh");
    }

    @Test
    void refresh_invalidToken_throwsUnauthorized() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("bad-token");

        when(userRepository.findByRefreshToken("bad-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refresh(request))
                .isInstanceOf(UnauthorizedException.class);
    }
}
