// Virtual threads and explicit admission control.
// Requires Java 21+.
import java.util.concurrent.*;
import java.util.stream.IntStream;

public class VirtualThreadAdmissionControl {
    public static void main(String[] args) throws Exception {
        var downstream = new Semaphore(4);          // the INTENTIONAL limit
        var maxInFlight = new java.util.concurrent.atomic.AtomicInteger();
        var inFlight = new java.util.concurrent.atomic.AtomicInteger();

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var futures = IntStream.range(0, 20)
                .mapToObj(i -> executor.submit(() -> {
                    downstream.acquire();
                    try {
                        maxInFlight.accumulateAndGet(inFlight.incrementAndGet(), Math::max);
                        Thread.sleep(20);           // stand-in for blocking I/O
                        return i;
                    } finally { inFlight.decrementAndGet(); downstream.release(); }
                }))
                .toList();

            int sum = 0;
            for (var f : futures) sum += f.get();
            assert sum == 190 : "all 20 tasks must complete";
        }

        // The semaphore bounds scarce downstream capacity,
        // not a thread pool. Threads are cheap; the dependency is not.
        assert maxInFlight.get() <= 4
            : "admission control breached: " + maxInFlight.get() + " concurrent";

        System.out.println("PASS 20 virtual threads with downstream concurrency capped at "
            + maxInFlight.get() + "/4)");
    }
}
