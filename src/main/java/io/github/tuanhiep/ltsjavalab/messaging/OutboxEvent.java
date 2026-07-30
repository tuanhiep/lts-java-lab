package io.github.tuanhiep.ltsjavalab.messaging;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class OutboxEvent {

    @Id
    private UUID id;

    private String eventType;

    private String aggregateId;

    protected OutboxEvent() {
    }

    public OutboxEvent(String eventType, String aggregateId) {
        this.id = UUID.randomUUID();
        this.eventType = eventType;
        this.aggregateId = aggregateId;
    }

    public UUID getId() {
        return id;
    }

    public String getEventType() {
        return eventType;
    }

    public String getAggregateId() {
        return aggregateId;
    }
}
