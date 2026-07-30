// ConcurrentHashMap atomic compound operations.
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class ConcurrentMapAtomicity {
    public static void main(String[] args) throws Exception {
        var map = new ConcurrentHashMap<String, Integer>();
        var initCount = new AtomicInteger();

        // SPEC (API contract): the computeIfAbsent mapping function is applied
        // at most once per absent key.
        int threads = 50;
        var start = new CountDownLatch(1);
        var done = new CountDownLatch(threads);
        try (var pool = Executors.newFixedThreadPool(8)) {
            IntStream.range(0, threads).forEach(i -> pool.submit(() -> {
                try {
                    start.await();
                    map.computeIfAbsent("hot", k -> { initCount.incrementAndGet(); return 42; });
                } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                finally { done.countDown(); }
            }));
            start.countDown();
            assert done.await(10, TimeUnit.SECONDS) : "workers did not finish";
        }

        assert initCount.get() == 1
            : "mapping function ran " + initCount.get() + " times; contract says once";
        assert map.get("hot") == 42;

        // NOT asserted: bin locking, CAS on empty bins, CounterCell striping —
        // all Java 8+ implementation details, not API guarantees.
        System.out.println("PASS ConcurrentHashMap computeIfAbsent under 50 tasks");
    }
}
