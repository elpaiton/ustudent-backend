package co.edu.usta.ustudent.iam.infrastructure.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Proporciona el par RSA con el que se firman y verifican los JWT.
 *
 * <p>En produccion las claves se cargan de archivo y su ausencia es un error de
 * arranque: firmar con una clave distinta en cada despliegue invalidaria todas
 * las sesiones sin avisar.
 *
 * <p>Fuera de produccion, si no hay archivos se genera un par efimero en
 * memoria. Asi el proyecto se clona y arranca sin ceremonia previa, a cambio de
 * que reiniciar cierre las sesiones abiertas, que en desarrollo es irrelevante.
 * Nunca se escribe la clave a disco: lo que no existe no se sube por descuido.
 */
@Component
public class RsaKeyProvider {

    private static final Logger log = LoggerFactory.getLogger(RsaKeyProvider.class);
    private static final int KEY_SIZE = 2048;

    private final RSAPublicKey publicKey;
    private final RSAPrivateKey privateKey;

    public RsaKeyProvider(
            @Value("${ustudent.security.jwt.private-key-path:}") String privateKeyPath,
            @Value("${ustudent.security.jwt.public-key-path:}") String publicKeyPath,
            Environment environment) {

        boolean production = environment.matchesProfiles("prod");
        boolean filesAvailable = hasContent(privateKeyPath) && hasContent(publicKeyPath)
                && Files.exists(Path.of(privateKeyPath)) && Files.exists(Path.of(publicKeyPath));

        if (filesAvailable) {
            this.privateKey = readPrivateKey(Path.of(privateKeyPath));
            this.publicKey = readPublicKey(Path.of(publicKeyPath));
            log.info("Claves JWT cargadas desde archivo");
        } else if (production) {
            throw new IllegalStateException(
                    "En produccion las claves JWT deben existir. Configura "
                            + "JWT_PRIVATE_KEY_PATH y JWT_PUBLIC_KEY_PATH.");
        } else {
            KeyPair pair = generateKeyPair();
            this.privateKey = (RSAPrivateKey) pair.getPrivate();
            this.publicKey = (RSAPublicKey) pair.getPublic();
            log.warn("Sin claves JWT en disco: se genero un par efimero para desarrollo. "
                    + "Las sesiones no sobreviviran a un reinicio.");
        }
    }

    public RSAPublicKey publicKey() {
        return publicKey;
    }

    public RSAPrivateKey privateKey() {
        return privateKey;
    }

    private static boolean hasContent(String value) {
        return value != null && !value.isBlank();
    }

    private static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(KEY_SIZE);
            return generator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("La JVM no soporta RSA", e);
        }
    }

    private static RSAPrivateKey readPrivateKey(Path path) {
        try {
            byte[] der = decodePem(Files.readString(path, StandardCharsets.UTF_8));
            return (RSAPrivateKey) KeyFactory.getInstance("RSA")
                    .generatePrivate(new PKCS8EncodedKeySpec(der));
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("No se pudo leer la clave privada en " + path, e);
        }
    }

    private static RSAPublicKey readPublicKey(Path path) {
        try {
            byte[] der = decodePem(Files.readString(path, StandardCharsets.UTF_8));
            return (RSAPublicKey) KeyFactory.getInstance("RSA")
                    .generatePublic(new X509EncodedKeySpec(der));
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("No se pudo leer la clave publica en " + path, e);
        }
    }

    /** Quita cabecera, pie y saltos de linea de un PEM y decodifica el base64. */
    private static byte[] decodePem(String pem) {
        String base64 = pem.replaceAll("-----BEGIN (.*)-----", "")
                .replaceAll("-----END (.*)-----", "")
                .replaceAll("\\s", "");
        return Base64.getDecoder().decode(base64);
    }
}
