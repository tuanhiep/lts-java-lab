package io.github.tuanhiep.ltsjavalab.tx;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionMarkerRepository extends JpaRepository<TransactionMarker, Long> {
}
