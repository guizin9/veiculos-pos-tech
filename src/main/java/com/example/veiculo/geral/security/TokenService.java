package com.example.veiculo.geral.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

@Service
public class TokenService {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiracao-min:120}")
    private long expiracaoMinutos;

    public String gerarToken(Authentication authentication) {
        Instant agora = Instant.now();

        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(a -> a.replaceFirst("^ROLE_", ""))
                .toList();

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer("veiculos-api")
                .issueTime(Date.from(agora))
                .expirationTime(Date.from(agora.plus(expiracaoMinutos, ChronoUnit.MINUTES)))
                .subject(authentication.getName())
                .claim("roles", roles)
                .build();

        SignedJWT signedJwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
        try {
            signedJwt.sign(new MACSigner(jwtSecret.getBytes(StandardCharsets.UTF_8)));
        } catch (JOSEException e) {
            throw new IllegalStateException("Falha ao assinar JWT", e);
        }
        return signedJwt.serialize();
    }

    public long getExpiracaoMinutos() {
        return expiracaoMinutos;
    }
}
