// Pattern matching, record patterns and sealed exhaustiveness.
public class PatternMatching {
    sealed interface Shape permits Circle, Rectangle {}
    record Circle(double radius) implements Shape {}
    record Rectangle(double w, double h) implements Shape {}

    static String describe(Shape s) {
        // Exhaustive switch: no default branch, because Shape is sealed.
        return switch (s) {
            case Circle(double r) when r <= 0 -> "invalid circle";
            case Circle(double r)             -> "circle:" + Math.round(Math.PI * r * r);
            case Rectangle(double w, double h) when w == h -> "square:" + Math.round(w * h);
            case Rectangle(double w, double h)             -> "rect:" + Math.round(w * h);
        };
    }

    public static void main(String[] args) {
        // SPEC (JEP 440/441, final in Java 21): record deconstruction + guards.
        assert describe(new Circle(10)).startsWith("circle:")  : describe(new Circle(10));
        assert describe(new Circle(-1)).equals("invalid circle");
        assert describe(new Rectangle(3, 3)).equals("square:9");
        assert describe(new Rectangle(2, 5)).equals("rect:10");

        // SPEC: flow scoping of instanceof bindings, including the inverted form.
        Object o = "hello";
        if (!(o instanceof String str)) { throw new AssertionError("unreachable"); }
        assert str.length() == 5 : "binding must be in scope after the negated guard";

        System.out.println("PASS record patterns, guards, sealed exhaustiveness and flow scoping");
    }
}
