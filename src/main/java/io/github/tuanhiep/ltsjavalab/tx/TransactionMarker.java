package io.github.tuanhiep.ltsjavalab.tx;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class TransactionMarker {

    @Id
    @GeneratedValue
    private Long id;

    private String label;

    protected TransactionMarker() {
    }

    public TransactionMarker(String label) {
        this.label = label;
    }

    public Long getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }
}
