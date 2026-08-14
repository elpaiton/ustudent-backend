package co.edu.usta.ustudent.iam.infrastructure.security;

import co.edu.usta.ustudent.iam.application.port.TokenIssuer;
import co.edu.usta.ustudent.iam.domain.model.User;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Component;

/**
 * Implementacion con JWT firmados en RS256 (Nimbus, via Spring Security).
 *
 * <p>El token de acceso lleva roles y permisos ya resueltos: autorizar no
 * cuesta un viaje a la base de datos. El precio es que un cambio de permisos
 * tarda hasta la vida del token en propagarse, de ahi que sea corta y que
 * existan mecanismos de revocacion explicita.
 */
@Component
public class JwtTokenIssuer implements TokenIssuer {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenIssuer.class);

    private static final String ISSUER = "ustudent";
    private static final String CLAIM_ROLES = "roles";
    private static final String CLAIM_PERMISSIONS = "perms";
    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_TYPE = "typ";
    private static final String TYPE_ACCESS = "access";
    private static final String TYPE_REFRESH = "refresh";

    private final NimbusJwtEncoder encoder;
    private final NimbusJwtDecoder decoder;
    private final Duration accessTtl;
    private final Duration refreshTtl;

    public JwtTokenIssuer(
            RsaKeyProvider keys,
            @Value("${ustudent.security.jwt.access-ttl-minutes:30}") long accessTtlMinutes,
            @Value("${ustudent.security.jwt.refresh-ttl-hours:8}") long refreshTtlHours) {

        RSAKey jwk = new RSAKey.Builder(keys.publicKey())
                .privateKey(keys.privateKey())
                .keyID(UUID.randomUUID().toString())
                .build();
        this.encoder = new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(jwk)));
        this.decoder = NimbusJwtDecoder.withPublicKey(keys.publicKey()).build();
        this.accessTtl = Duration.ofMinutes(accessTtlMinutes);
        this.refreshTtl = Duration.ofHours(refreshTtlHours);
    }

    @Override
    public String issueAccessToken(User user) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .issuedAt(now)
                .expiresAt(now.plus(accessTtl))
                .subject(user.getPublicId().toString())
                .id(UUID.randomUUID().toString())
                .claim(CLAIM_TYPE, TYPE_ACCESS)
                .claim(CLAIM_EMAIL, user.getEmail())
                .claim(CLAIM_ROLES, List.copyOf(user.roleCodes()))
                .claim(CLAIM_PERMISSIONS, List.copyOf(user.effectivePermissions()))
                .build();
        return encode(claims);
    }

    @Override
    public String issueRefreshToken(User user, UUID jti, UUID family) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .issuedAt(now)
                .expiresAt(now.plus(refreshTtl))
                .subject(user.getPublicId().toString())
                .id(jti.toString())
                .claim(CLAIM_TYPE, TYPE_REFRESH)
                .claim("family", family.toString())
                .build();
        return encode(claims);
    }

    @Override
    public Optional<AccessTokenClaims> verifyAccessToken(String token) {
        return decode(token, TYPE_ACCESS).map(jwt -> new AccessTokenClaims(
                UUID.fromString(jwt.getSubject()),
                jwt.getClaimAsString(CLAIM_EMAIL),
                asSet(jwt.getClaimAsStringList(CLAIM_ROLES)),
                asSet(jwt.getClaimAsStringList(CLAIM_PERMISSIONS))));
    }

    @Override
    public Optional<UUID> verifyRefreshToken(String token) {
        return decode(token, TYPE_REFRESH).map(jwt -> UUID.fromString(jwt.getId()));
    }

    @Override
    public Duration accessTokenTtl() {
        return accessTtl;
    }

    @Override
    public Duration refreshTokenTtl() {
        return refreshTtl;
    }

    private String encode(JwtClaimsSet claims) {
        JwsHeader header = JwsHeader.with(org.springframework.security.oauth2.jose.jws.SignatureAlgorithm.RS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    /**
     * Un token de refresco presentado como si fuera de acceso debe rechazarse:
     * de ahi que se compruebe el tipo y no solo la firma.
     */
    private Optional<Jwt> decode(String token, String expectedType) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        try {
            Jwt jwt = decoder.decode(token);
            if (!expectedType.equals(jwt.getClaimAsString(CLAIM_TYPE))) {
                log.debug("Token con tipo inesperado: se esperaba {}", expectedType);
                return Optional.empty();
            }
            return Optional.of(jwt);
        } catch (JwtException e) {
            log.debug("Token invalido: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private static Set<String> asSet(List<String> values) {
        return values == null ? Set.of() : new LinkedHashSet<>(values);
    }
}
