package io.github.tuanhiep.ltsjavalab.verification;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.IntStream;

import io.github.tuanhiep.ltsjavalab.jpa.OrderPersistenceService;
import io.github.tuanhiep.ltsjavalab.jpa.PurchaseOrder;
import io.github.tuanhiep.ltsjavalab.jpa.PurchaseOrderRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
class JpaPersistenceLabTest {

    @Autowired
    PurchaseOrderRepository repository;

    @Autowired
    OrderPersistenceService service;

    @Autowired
    EntityManager entityManager;

    @Autowired
    EntityManagerFactory entityManagerFactory;

    @BeforeEach
    void clean() {
        repository.deleteAll();
    }

    @Test
    @Transactional
    void statementCountDistinguishesNPlusOneFromOneFetchPlan() {
        IntStream.range(0, 3).forEach(index -> {
            PurchaseOrder order = new PurchaseOrder("order-" + index);
            order.addLine("sku-" + index);
            repository.save(order);
        });
        entityManager.flush();
        entityManager.clear();

        var statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.clear();
        repository.findAll().forEach(order -> order.getLines().size());
        long lazyStatements = statistics.getPrepareStatementCount();

        entityManager.clear();
        statistics.clear();
        repository.findAllWithLines().forEach(order -> order.getLines().size());
        long joinedStatements = statistics.getPrepareStatementCount();

        assertThat(lazyStatements).isGreaterThan(joinedStatements);
        assertThat(joinedStatements).isOne();
    }

    @Test
    void dirtyCheckingPersistsWithoutRepositorySave() {
        PurchaseOrder order = repository.save(new PurchaseOrder("before"));

        service.renameWithoutSave(order.getId(), "after");

        assertThat(repository.findById(order.getId()).orElseThrow().getNote()).isEqualTo("after");
    }
}
