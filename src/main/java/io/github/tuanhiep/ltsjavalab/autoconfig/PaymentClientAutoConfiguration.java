package io.github.tuanhiep.ltsjavalab.autoconfig;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class PaymentClientAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    PaymentClient paymentClient() {
        return orderId -> "default:" + orderId;
    }
}
