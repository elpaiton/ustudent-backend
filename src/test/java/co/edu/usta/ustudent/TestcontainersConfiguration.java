package co.edu.usta.ustudent;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Levanta el mismo motor que produccion para las pruebas de integracion.
 *
 * <p>La version esta fijada a proposito: probar contra {@code postgres:latest}
 * haria que el build cambiara de comportamiento sin que nadie tocara el codigo.
 * Debe coincidir con la de {@code infra/docker/docker-compose.yml}.
 */
@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    PostgreSQLContainer postgresContainer() {
        return new PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"));
    }

}
