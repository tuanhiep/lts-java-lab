package io.github.tuanhiep.ltsjavalab.tx;

import java.io.IOException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class TransactionBoundaryService {

    public record TransactionNames(String outer, String inner) {
    }

    private final TransactionMarkerRepository repository;

    public TransactionBoundaryService(TransactionMarkerRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public TransactionNames outerCallsInnerOnThis() {
        String outer = TransactionSynchronizationManager.getCurrentTransactionName();
        return new TransactionNames(outer, requiresNewName());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String requiresNewName() {
        return TransactionSynchronizationManager.getCurrentTransactionName();
    }

    @Transactional
    public void checkedExceptionCommits(String label) throws IOException {
        repository.save(new TransactionMarker(label));
        throw new IOException("checked exception");
    }

    @Transactional(rollbackFor = Exception.class)
    public void checkedExceptionRollsBack(String label) throws IOException {
        repository.save(new TransactionMarker(label));
        throw new IOException("checked exception");
    }
}
