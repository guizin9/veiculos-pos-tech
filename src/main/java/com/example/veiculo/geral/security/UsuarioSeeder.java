package com.example.veiculo.geral.security;

import com.example.veiculo.model.Role;
import com.example.veiculo.model.Usuario;
import com.example.veiculo.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Set;

/**
 * Cria usuários iniciais apenas se a base estiver vazia.
 * As senhas padrão servem só para ambiente de desenvolvimento/demonstração
 * e devem ser trocadas em produção (via variáveis de ambiente / Secrets Manager).
 */
@Component
@RequiredArgsConstructor
public class UsuarioSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(UsuarioSeeder.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() > 0) return;

        criar("admin", "admin123", Set.of(Role.ADMIN));
        criar("vendedor", "vendedor123", Set.of(Role.VENDEDOR));
        criar("operador", "operador123", Set.of(Role.OPERADOR));
        criar("cliente", "cliente123", Set.of(Role.CLIENTE));

        log.warn("Usuários iniciais criados (admin/vendedor/operador/cliente). " +
                "Troque as senhas padrão antes de ir para produção.");
    }

    private void criar(String username, String senha, Set<Role> roles) {
        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setSenha(passwordEncoder.encode(senha));
        usuario.setRoles(new java.util.HashSet<>(roles));
        usuario.setAtivo(true);
        usuario.setDtOpera(OffsetDateTime.now());
        usuarioRepository.save(usuario);
    }
}
