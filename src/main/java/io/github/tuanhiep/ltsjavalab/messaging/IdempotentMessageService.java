package io.github.tuanhiep.ltsjavalab.messaging;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IdempotentMessageService {

    public enum Result {
        APPLIED,
        DUPLICATE
    }

    private final ProcessedMessageRepository processed;
    private final ChargeRepository charges;
    private final OutboxEventRepository outbox;

    public IdempotentMessageService(
            ProcessedMessageRepository processed,
            ChargeRepository charges,
            OutboxEventRepository outbox) {
        this.processed = processed;
        this.charges = charges;
        this.outbox = outbox;
    }

    @Transactional
    public Result process(String messageId, String orderId, boolean failAfterDedup) {
        if (processed.existsById(messageId)) {
            return Result.DUPLICATE;
        }

        processed.save(new ProcessedMessage(messageId));
        if (failAfterDedup) {
            throw new IllegalStateException("injected failure");
        }

        charges.save(new Charge(orderId));
        outbox.save(new OutboxEvent("ChargeApplied", orderId));
        return Result.APPLIED;
    }
}
