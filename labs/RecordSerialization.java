import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class RecordSerialization {
    record User(String username, int age) implements Serializable {
        static int constructions;

        User {
            constructions++;
            if (age < 0) {
                throw new IllegalArgumentException("age must be non-negative");
            }
        }
    }

    public static void main(String[] args) throws Exception {
        var user = new User("Alice", 30);
        var age = User.class.getDeclaredField("age");
        age.setAccessible(true);

        try {
            age.set(user, -5);
            throw new AssertionError("record component field unexpectedly changed");
        } catch (IllegalAccessException expected) {
            assert user.age() == 30;
        }

        var bytes = new ByteArrayOutputStream();
        try (var out = new ObjectOutputStream(bytes)) {
            out.writeObject(user);
        }

        User.constructions = 0;
        Object restored;
        try (var in = new ObjectInputStream(
                new ByteArrayInputStream(bytes.toByteArray()))) {
            restored = in.readObject();
        }

        assert restored.equals(user);
        assert User.constructions == 1
                : "record canonical constructor did not run during deserialization";
        System.out.println("OBSERVE record field mutation rejected; "
                + "canonical constructor invoked during deserialization");
        System.out.println("PASS record serialization invariants");
    }
}
