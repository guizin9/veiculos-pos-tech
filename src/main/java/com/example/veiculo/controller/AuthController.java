package com.example.veiculo.controller;

import com.example.veiculo.geral.security.TokenService;
import com.example.veiculo.geral.security.dto.LoginRequest;
import com.example.veiculo.geral.security.dto.LoginResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    @PostMapping("login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.senha()));
        String token = tokenService.gerarToken(authentication);
        return ResponseEntity.ok(new LoginResponse(token, "Bearer", tokenService.getExpiracaoMinutos()));
    }
}
