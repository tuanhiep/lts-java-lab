package io.github.tuanhiep.ltsjavalab.messaging;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class Charge {

    @Id
    @GeneratedValue
    private Long id;

    private String orderId;

    protected Charge() {
    }

    public Charge(String orderId) {
        this.orderId = orderId;
    }

    public Long getId() {
        return id;
    }

    public String getOrderId() {
        return orderId;
    }
}
