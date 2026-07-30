package io.github.tuanhiep.ltsjavalab.verification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.tuanhiep.ltsjavalab.messaging.ChargeRepository;
import io.github.tuanhiep.ltsjavalab.messaging.IdempotentMessageService;
import io.github.tuanhiep.ltsjavalab.messaging.OutboxEventRepository;
import io.github.tuanhiep.ltsjavalab.messaging.ProcessedMessageRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class IdempotencyOutboxLabTest {

    @Autowired
    IdempotentMessageService service;

    @Autowired
    ProcessedMessageRepository processed;

    @Autowired
    ChargeRepository charges;

    @Autowired
    OutboxEventRepository outbox;

    @BeforeEach
    void clean() {
        outbox.deleteAll();
        charges.deleteAll();
        processed.deleteAll();
    }

    @Test
    void duplicateDeliveryProducesOneEffectAndOneOutboxEvent() {
        assertThat(service.process("message-1", "order-1", false))
                .isEqualTo(IdempotentMessageService.Result.APPLIED);
        assertThat(service.process("message-1", "order-1", false))
                .isEqualTo(IdempotentMessageService.Result.DUPLICATE);

        assertThat(processed.count()).isOne();
        assertThat(charges.count()).isOne();
        assertThat(outbox.count()).isOne();
    }

    @Test
    void failureRollsBackDedupEffectAndOutboxTogether() {
        assertThatThrownBy(() -> service.process("message-2", "order-2", true))
                .isInstanceOf(IllegalStateException.class);

        assertThat(processed.count()).isZero();
        assertThat(charges.count()).isZero();
        assertThat(outbox.count()).isZero();

        assertThat(service.process("message-2", "order-2", false))
                .isEqualTo(IdempotentMessageService.Result.APPLIED);
    }
}
