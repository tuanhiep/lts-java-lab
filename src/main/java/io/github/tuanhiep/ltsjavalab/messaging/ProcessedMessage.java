package io.github.tuanhiep.ltsjavalab.messaging;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class ProcessedMessage {

    @Id
    private String messageId;

    protected ProcessedMessage() {
    }

    public ProcessedMessage(String messageId) {
        this.messageId = messageId;
    }

    public String getMessageId() {
        return messageId;
    }
}
