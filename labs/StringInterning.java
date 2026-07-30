// String literals, interning and compact-string boundaries.
public class StringInterning {
    public static void main(String[] args) {
        String literal1 = "java-lab";
        String literal2 = "java-lab";
        String built    = new String("java-lab");
        String interned = built.intern();

        // SPEC (JLS 3.10.5): identical string literals refer to the same instance.
        assert literal1 == literal2 : "literals must be interned to one instance";

        // SPEC: new String(...) creates a distinct object.
        assert literal1 != built : "new String must not be the pooled instance";

        // SPEC (API contract): intern() returns the pooled instance.
        assert literal1 == interned : "intern() must return the pool reference";

        // Content equality is unaffected by identity.
        assert literal1.equals(built);

        // NOT asserted: LATIN1 vs UTF16 backing array, pool table size, dedup —
        // Compact Strings is a JDK 9+ implementation detail with no API surface.
        System.out.println("PASS string literal identity and intern contract");
    }
}
