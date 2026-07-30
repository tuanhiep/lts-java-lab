package io.github.tuanhiep.ltsjavalab.verification;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.github.tuanhiep.ltsjavalab.locking.Account;
import io.github.tuanhiep.ltsjavalab.locking.AccountRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.open-in-view=false"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class OptimisticLockingPostgresIT extends PostgresContainerBase {

    @Autowired
    AccountRepository repository;

    @Autowired
    PlatformTransactionManager transactionManager;

    @BeforeEach
    void reset() {
        repository.deleteAll();
        repository.saveAndFlush(new Account(1L, 100L));
    }

    @Test
    void twoWritersLoadedAtTheSameVersionCannotBothCommit() throws Exception {
        var loaded = new CountDownLatch(2);
        var release = new CountDownLatch(1);

        Callable<Boolean> writer = () -> {
            try {
                new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
                    Account account = repository.findById(1L).orElseThrow();
                    loaded.countDown();
                    try {
                        if (!release.await(10, TimeUnit.SECONDS)) {
                            throw new IllegalStateException("writers did not rendezvous");
                        }
                    } catch (InterruptedException exception) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException(exception);
                    }
                    account.debit(10L);
                    repository.flush();
                });
                return true;
            } catch (RuntimeException optimisticFailure) {
                return false;
            }
        };

        List<Boolean> results;
        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(writer);
            var second = executor.submit(writer);
            assertThat(loaded.await(10, TimeUnit.SECONDS)).isTrue();
            release.countDown();
            results = List.of(first.get(10, TimeUnit.SECONDS), second.get(10, TimeUnit.SECONDS));
        }

        assertThat(results).containsExactlyInAnyOrder(true, false);
        Account winner = repository.findById(1L).orElseThrow();
        assertThat(winner.getBalance()).isEqualTo(90L);
        assertThat(winner.getVersion()).isEqualTo(1L);
    }
}
