package com.example.veiculo.geral.security.dto;

public record LoginResponse(
        String token,
        String tipo,
        long expiraEmMinutos
) {
}
