package io.github.tuanhiep.ltsjavalab.verification;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.tuanhiep.ltsjavalab.autoconfig.PaymentClient;
import io.github.tuanhiep.ltsjavalab.autoconfig.PaymentClientAutoConfiguration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class AutoConfigurationLabTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(PaymentClientAutoConfiguration.class));

    @Test
    void defaultBeanExistsWhenTheUserProvidesNone() {
        runner.run(context -> {
            assertThat(context).hasSingleBean(PaymentClient.class);
            assertThat(context.getBean(PaymentClient.class).charge("42"))
                    .isEqualTo("default:42");
        });
    }

    @Test
    void userBeanMakesConditionalAutoConfigurationBackOff() {
        PaymentClient custom = orderId -> "custom:" + orderId;

        runner.withBean(PaymentClient.class, () -> custom)
                .run(context -> {
                    assertThat(context).hasSingleBean(PaymentClient.class);
                    assertThat(context.getBean(PaymentClient.class)).isSameAs(custom);
                });
    }
}
