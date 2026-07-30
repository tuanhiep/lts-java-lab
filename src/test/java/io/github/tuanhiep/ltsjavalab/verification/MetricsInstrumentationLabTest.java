package io.github.tuanhiep.ltsjavalab.verification;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import org.junit.jupiter.api.Test;

class MetricsInstrumentationLabTest {

    @Test
    void boundedTagsAndHistogramInputsRemainAggregatable() {
        var registry = new SimpleMeterRegistry();
        for (String outcome : new String[] {"2xx", "4xx", "5xx"}) {
            registry.counter("http.requests", "outcome", outcome).increment();
        }

        assertThat(registry.find("http.requests").counters()).hasSize(3);

        Timer timer = Timer.builder("http.latency")
                .serviceLevelObjectives(
                        Duration.ofMillis(100),
                        Duration.ofMillis(300),
                        Duration.ofSeconds(1))
                .register(registry);
        timer.record(Duration.ofMillis(40));
        timer.record(Duration.ofSeconds(4));

        assertThat(timer.count()).isEqualTo(2);
        assertThat(timer.totalTime(java.util.concurrent.TimeUnit.MILLISECONDS))
                .isGreaterThanOrEqualTo(4_040);
        registry.close();
    }

    @Test
    void gaugeNeedsAnApplicationOwnedStrongReference() {
        var registry = new SimpleMeterRegistry();
        AtomicInteger state = new AtomicInteger(7);
        Gauge gauge = Gauge.builder("queue.depth", state, AtomicInteger::get)
                .register(registry);

        assertThat(gauge.value()).isEqualTo(7);
        state.set(9);
        assertThat(gauge.value()).isEqualTo(9);

        // Deliberately no assertion after dropping `state` and requesting GC:
        // Micrometer keeps only a weak reference, but collection timing is not a contract.
        registry.close();
    }
}
