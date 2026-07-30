package io.github.tuanhiep.ltsjavalab.jpa;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderPersistenceService {

    private final PurchaseOrderRepository repository;

    public OrderPersistenceService(PurchaseOrderRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void renameWithoutSave(long id, String note) {
        PurchaseOrder order = repository.findById(id).orElseThrow();
        order.setNote(note);
    }
}
