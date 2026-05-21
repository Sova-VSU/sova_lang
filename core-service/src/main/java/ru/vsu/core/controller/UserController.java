package ru.vsu.core.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.vsu.core.model.dto.user.UpdateNameRequest;
import ru.vsu.core.model.dto.user.UpdatePasswordRequest;
import ru.vsu.core.model.dto.user.UserDto;
import ru.vsu.core.model.dto.user.UserStatsDto;
import ru.vsu.core.service.UserService;

@RestController
@RequestMapping("/users/me")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public UserDto getCurrentUser(@RequestHeader("X-User-ID") String userId) {
        return userService.getById(userId);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCurrentUser(@RequestHeader("X-User-ID") String userId) {
        userService.delete(userId);
    }

    @PatchMapping("/name")
    public UserDto updateName(
            @RequestHeader("X-User-ID") String userId,
            @RequestBody @Valid UpdateNameRequest request
    ) {
        return userService.updateName(userId, request.getName());
    }

    @PatchMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updatePassword(
            @RequestHeader("X-User-ID") String userId,
            @RequestBody @Valid UpdatePasswordRequest request
    ) {
        userService.updatePassword(userId, request.getCurrentPassword(), request.getNewPassword());
    }

    @GetMapping("/stats")
    public UserStatsDto getStats(@RequestHeader("X-User-ID") String userId) {
        return userService.getStats(userId);
    }
}
