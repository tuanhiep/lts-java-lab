package io.github.tuanhiep.ltsjavalab.locking;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Version;

@Entity
public class Account {

    @Id
    private Long id;

    @Version
    private long version;

    private long balance;

    protected Account() {
    }

    public Account(Long id, long balance) {
        this.id = id;
        this.balance = balance;
    }

    public Long getId() {
        return id;
    }

    public long getVersion() {
        return version;
    }

    public long getBalance() {
        return balance;
    }

    public void debit(long amount) {
        if (balance < amount) {
            throw new IllegalStateException("insufficient funds");
        }
        balance -= amount;
    }
}
