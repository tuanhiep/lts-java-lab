// Lambda capture semantics and identity boundaries.
// JLS 15.27.4 leaves lambda identity and
// allocation UNSPECIFIED, so instance reuse is observed, never asserted.
import java.util.function.Supplier;

public class LambdaCaptureSemantics {
    static Supplier<Integer> nonCapturing() { return () -> 42; }
    static Supplier<Integer> capturing(int x) { return () -> x + 5; }

    public static void main(String[] args) {
        // SPEC: semantics — the value is captured at evaluation time.
        int x = 10;
        Supplier<Integer> cap = capturing(x);
        assert cap.get() == 15 : "captured value semantics";
        assert nonCapturing().get() == 42;

        // Effectively-final is a COMPILE-TIME rule; it cannot be probed at runtime,
        // so there is nothing to assert here — see the card.

        // OBSERVED, NOT ASSERTED: whether the runtime reuses one instance for a
        // non-capturing lambda. JLS 15.27.4: evaluation "may or may not" allocate
        // a new instance, and identity is unspecified.
        boolean sameNonCapturing = nonCapturing() == nonCapturing();
        boolean sameCapturing    = capturing(1) == capturing(1);
        System.out.println("OBSERVE non-capturing lambda reuses instance = " + sameNonCapturing
            + " (typical: true) | capturing reuses instance = " + sameCapturing
            + " (typical: false) — NEITHER is guaranteed by the JLS");

        System.out.println("PASS lambda capture semantics (identity observed only)");
    }
}
