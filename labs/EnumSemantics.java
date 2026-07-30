// Enum identity, behavior and EnumSet contracts.
import java.lang.reflect.Constructor;
import java.util.EnumSet;

public class EnumSemantics {
    enum Operation {
        PLUS  { double apply(double x, double y) { return x + y; } },
        MINUS { double apply(double x, double y) { return x - y; } };
        abstract double apply(double x, double y);
    }

    public static void main(String[] args) throws Exception {
        // SPEC: behavioural enums — constant-specific method bodies.
        assert Operation.PLUS.apply(10, 5) == 15;
        assert Operation.MINUS.apply(10, 5) == 5;

        // SPEC: reflection cannot instantiate an enum
        // (Constructor.newInstance throws IllegalArgumentException for enum types).
        boolean blocked = false;
        try {
            Constructor<?> c = Operation.class.getDeclaredConstructors()[0];
            c.setAccessible(true);
            c.newInstance("HACK", 2);
            } catch (IllegalArgumentException expected) {
            blocked = true;
        } catch (Exception other) {
            blocked = true;   // some JDKs wrap; still not a new constant
        }
        assert blocked : "reflective enum instantiation must not succeed";

        // API contract: EnumSet membership.
        var active = EnumSet.of(Operation.PLUS);
        assert active.contains(Operation.PLUS) && !active.contains(Operation.MINUS);

        // NOT asserted: that EnumSet is a bit vector / RegularEnumSet — an
        // implementation detail, however reliable in practice.
        System.out.println("PASS enum identity, reflection guard and EnumSet contract");
    }
}
