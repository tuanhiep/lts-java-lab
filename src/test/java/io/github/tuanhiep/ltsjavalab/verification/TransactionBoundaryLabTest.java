package io.github.tuanhiep.ltsjavalab.verification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;

import io.github.tuanhiep.ltsjavalab.tx.TransactionBoundaryService;
import io.github.tuanhiep.ltsjavalab.tx.TransactionMarkerRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TransactionBoundaryLabTest {

    @Autowired
    TransactionBoundaryService service;

    @Autowired
    TransactionMarkerRepository repository;

    @BeforeEach
    void clean() {
        repository.deleteAll();
    }

    @Test
    void selfInvocationKeepsTheOuterTransaction() {
        var names = service.outerCallsInnerOnThis();

        assertThat(names.outer()).isNotBlank();
        assertThat(names.inner())
                .as("REQUIRES_NEW advice is bypassed on this.inner()")
                .isEqualTo(names.outer());

        assertThat(service.requiresNewName())
                .as("the same method called through the proxy owns a transaction")
                .endsWith(".requiresNewName");
    }

    @Test
    void checkedExceptionCommitsByDefault() {
        assertThatThrownBy(() -> service.checkedExceptionCommits("commit"))
                .isInstanceOf(IOException.class);
        assertThat(repository.count()).isOne();
    }

    @Test
    void explicitRollbackRuleRollsBackCheckedException() {
        assertThatThrownBy(() -> service.checkedExceptionRollsBack("rollback"))
                .isInstanceOf(IOException.class);
        assertThat(repository.count()).isZero();
    }
}
