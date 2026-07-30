package io.github.tuanhiep.ltsjavalab.verification;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

class BeanLifecycleLabTest {

    @Test
    void lifecycleOrderAndPrototypeOwnershipAreObservable() {
        Events.values.clear();
        Prototype.destroyed = 0;

        try (var context = new AnnotationConfigApplicationContext(Config.class)) {
            assertThat(Events.values).containsExactly("construct", "postConstruct");

            Supplier<Prototype> factory = context.getBean("prototypeFactory", Supplier.class);
            assertThat(factory.get()).isNotSameAs(factory.get());

            context.getBean(LifecycleBean.class).work();
            assertThat(Events.values).containsExactly("construct", "postConstruct", "work");
        }

        assertThat(Events.values).containsExactly("construct", "postConstruct", "work", "preDestroy");
        assertThat(Prototype.destroyed)
                .as("the container does not own prototype destruction")
                .isZero();
    }

    static final class Events {
        static final List<String> values = new ArrayList<>();
    }

    static final class LifecycleBean {
        LifecycleBean() {
            Events.values.add("construct");
        }

        @PostConstruct
        void init() {
            Events.values.add("postConstruct");
        }

        void work() {
            Events.values.add("work");
        }

        @PreDestroy
        void destroy() {
            Events.values.add("preDestroy");
        }
    }

    static final class Prototype {
        static int destroyed;

        @PreDestroy
        void destroy() {
            destroyed++;
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class Config {
        @Bean
        LifecycleBean lifecycleBean() {
            return new LifecycleBean();
        }

        @Bean
        @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        Prototype prototype() {
            return new Prototype();
        }

        @Bean
        Supplier<Prototype> prototypeFactory(ObjectProvider<Prototype> provider) {
            return provider::getObject;
        }
    }
}
