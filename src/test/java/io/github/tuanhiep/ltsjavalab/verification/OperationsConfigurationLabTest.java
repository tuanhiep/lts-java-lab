package io.github.tuanhiep.ltsjavalab.verification;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OperationsConfigurationLabTest {

    @Autowired
    Environment environment;

    @Autowired
    ApplicationContext context;

    @Test
    void gracefulShutdownAndActuatorExposureAreDeliberate() {
        assertThat(environment.getProperty("server.shutdown")).isEqualTo("graceful");
        assertThat(environment.getProperty("spring.lifecycle.timeout-per-shutdown-phase"))
                .isEqualTo("5s");
        assertThat(environment.getProperty("management.endpoint.health.probes.enabled"))
                .isEqualTo("true");

        String exposure = environment.getProperty("management.endpoints.web.exposure.include", "");
        assertThat(exposure.split(","))
                .containsExactlyInAnyOrder("health", "info", "prometheus");
        assertThat(exposure).doesNotContain("env", "heapdump", "threaddump");

        assertThat(context.getBeanNamesForType(
                org.springframework.boot.web.server.context.WebServerGracefulShutdownLifecycle.class))
                .isNotEmpty();
    }
}
