// Reference reachability and collection-timing boundaries.
// This lab demonstrates the assertion boundary:
// the ONLY assertion is the reachability guarantee. Clearing TIMING is
// collector policy and is therefore observed and printed, never asserted.
import java.lang.ref.*;

public class ReferenceReachability {
    public static void main(String[] args) throws Exception {
        Object referent = new Object();
        var queue = new ReferenceQueue<Object>();
        var weak = new WeakReference<>(referent, queue);

        // SPEC: while strongly reachable, get() must not return null.
        assert weak.get() != null : "strongly reachable referent must be retrievable";

        referent = null;              // drop the only strong reference
        System.gc();                  // best-effort REQUEST, not a command
        Reference<?> dequeued = queue.remove(1_000);

        // Deliberately NOT asserted — `assert weak.get() == null` would encode
        // the myth that clearing happens on the next GC. Both outcomes are legal.
        System.out.println(dequeued != null
            ? "OBSERVE weak reference cleared and enqueued within 1s"
            : "OBSERVE weak reference not cleared within 1s; this is specification-compliant");

        System.out.println("PASS reference reachability (collection timing observed only)");
    }
}
