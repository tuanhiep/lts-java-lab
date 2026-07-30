// equals, hashCode and compareTo contracts.
import java.math.BigDecimal;
import java.util.*;

public class EqualityHashingOrdering {
    record UserId(String value) {}

    public static void main(String[] args) {
        // SPEC: records generate component-based equals/hashCode -> safe map keys.
        var map = new HashMap<UserId, String>();
        map.put(new UserId("42"), "ready");
        assert "ready".equals(map.get(new UserId("42")));
        assert new UserId("42").hashCode() == new UserId("42").hashCode()
            : "equal objects must have equal hash codes";

        // SPEC: compareTo consistency with equals is RECOMMENDED, not required.
        // BigDecimal is the canonical documented exception.
        var a = new BigDecimal("1.0");
        var b = new BigDecimal("1.00");
        assert a.compareTo(b) == 0  : "numerically equal";
        assert !a.equals(b)         : "but not equals() — scale differs";

        // Consequence: sorted vs hashed collections legitimately disagree.
        assert new TreeSet<>(List.of(a, b)).size() == 1  : "TreeSet dedups by compareTo";
        assert new HashSet<>(List.of(a, b)).size() == 2  : "HashSet dedups by equals";

        // Mutable key: the entry survives but ordinary lookup typically misses it.
        var mutable = new ArrayList<>(List.of("x"));
        var m2 = new HashMap<List<String>, String>();
        m2.put(mutable, "v");
        mutable.add("y");                       // hash changes while stored

        // SPEC-safe: the entry is still there and still enumerable.
        assert m2.size() == 1 : "the entry still occupies the map";
        assert m2.values().iterator().next().equals("v");

        // NOT asserted: that get() now misses. Lookup failure depends on the new
        // hash landing in a different bucket — overwhelmingly likely, but a
        // collision would make it findable, so lookup is observed rather than asserted.
        System.out.println("OBSERVE get(mutatedKey) "
            + (m2.get(mutable) == null ? "missed (typical)" : "still found (collision)"));

        System.out.println("PASS equality, hashing, ordering and mutable-key boundaries");
    }
}
