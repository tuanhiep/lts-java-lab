// HashMap API contracts and JDK implementation boundaries.
// SPEC assertions only. Internals (treeify thresholds, capacity) are observed, never asserted.
import java.util.HashMap;

public class HashMapContracts {
    record UserId(String value) {}

    public static void main(String[] args) {
        // SPEC: equal keys (by equals/hashCode) retrieve the same mapping.
        var cache = HashMap.<UserId, String>newHashMap(100);   // Java 19+ sizing factory
        cache.put(new UserId("42"), "ready");
        assert "ready".equals(cache.get(new UserId("42")))
            : "equal keys must resolve to the same entry";

        // SPEC: one null key and null values are permitted.
        var m = new HashMap<String, String>();
        m.put(null, "nullKeyOk");
        m.put("k", null);
        assert "nullKeyOk".equals(m.get(null));
        assert m.get("k") == null && m.containsKey("k")
            : "containsKey must distinguish absent from present-but-null";

        // NOT asserted: table capacity, treeification at 8/64, hash spreading.
        // Those are OpenJDK implementation details and may change between builds.
        System.out.println("PASS HashMap contracts (implementation details observed only)");
    }
}
