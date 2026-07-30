package io.github.tuanhiep.ltsjavalab.jpa;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    @EntityGraph(attributePaths = "lines")
    @Query("select distinct o from PurchaseOrder o")
    List<PurchaseOrder> findAllWithLines();
}
