package com.hirelens.noteapp.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import com.hirelens.noteapp.config.JwtProperties;
import com.hirelens.noteapp.enums.Role;
import com.hirelens.noteapp.models.User;
import com.nimbusds.jose.jwk.source.ImmutableSecret;

class JwtServiceTest {

    private static final String SECRET = "test-secret-test-secret-test-secret-0123456789";
    private static final String ISSUER = "noteapp-test";

    private SecretKey key() {
        return new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    private JwtEncoder encoder() {
        return new NimbusJwtEncoder(new ImmutableSecret<>(key()));
    }

    private NimbusJwtDecoder decoder(String issuer) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key()).macAlgorithm(MacAlgorithm.HS256).build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuer));
        return decoder;
    }

    private JwtService service(Duration expiration) {
        return new JwtService(encoder(), new JwtProperties(SECRET, ISSUER, expiration));
    }

    private User user() {
        User u = new User();
        u.setId(42L);
        u.setNickname("sofia");
        u.setEmail("sofia@example.com");
        u.setRole(Role.USER);
        return u;
    }

    @Test
    void issuesTokenWithExpectedClaims() {
        String token = service(Duration.ofHours(1)).generateToken(user());
        Jwt jwt = decoder(ISSUER).decode(token);

        assertThat(jwt.getSubject()).isEqualTo("42");
        assertThat(jwt.getClaimAsString("role")).isEqualTo("USER");
        assertThat(jwt.getClaimAsString("email")).isEqualTo("sofia@example.com");
        assertThat(jwt.getClaimAsString("nickname")).isEqualTo("sofia");
        assertThat(jwt.getId()).isNotBlank();
        assertThat(jwt.getClaimAsString("iss")).isEqualTo(ISSUER);
    }

    @Test
    void tokenLastsExactlyOneHour() {
        Jwt jwt = decoder(ISSUER).decode(service(Duration.ofHours(1)).generateToken(user()));
        assertThat(ChronoUnit.SECONDS.between(jwt.getIssuedAt(), jwt.getExpiresAt())).isEqualTo(3600);
    }

    @Test
    void rejectsTamperedToken() {
        String token = service(Duration.ofHours(1)).generateToken(user());
        String tampered = token.substring(0, token.length() - 3) + "abc";
        assertThatThrownBy(() -> decoder(ISSUER).decode(tampered)).isInstanceOf(JwtException.class);
    }

    @Test
    void rejectsExpiredToken() throws InterruptedException {
        // token que expira casi de inmediato; el decoder de abajo no tiene clock skew
        String token = service(Duration.ofMillis(1)).generateToken(user());
        Thread.sleep(50);

        NimbusJwtDecoder strict = NimbusJwtDecoder.withSecretKey(key()).macAlgorithm(MacAlgorithm.HS256).build();
        strict.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                new JwtTimestampValidator(Duration.ZERO),
                new JwtIssuerValidator(ISSUER)));

        assertThatThrownBy(() -> strict.decode(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void rejectsWrongIssuer() {
        String token = service(Duration.ofHours(1)).generateToken(user());
        assertThatThrownBy(() -> decoder("otro-issuer").decode(token)).isInstanceOf(JwtException.class);
    }
}
