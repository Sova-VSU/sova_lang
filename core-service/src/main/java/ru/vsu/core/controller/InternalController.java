package ru.vsu.core.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.vsu.core.security.JwtService;

import java.util.Map;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalController {

    private final JwtService jwtService;

    @GetMapping("/public-key")
    public Map<String, String> getPublicKey() {
        return Map.of("publicKey", jwtService.getPublicKeyBase64());
    }
}
