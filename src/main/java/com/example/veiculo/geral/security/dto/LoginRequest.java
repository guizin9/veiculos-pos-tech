package com.example.veiculo.geral.security.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Campo username é obrigatório")
        String username,
        @NotBlank(message = "Campo senha é obrigatório")
        String senha
) {
}
