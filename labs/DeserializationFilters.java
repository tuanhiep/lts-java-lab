// Stream-scoped deserialization allow-list.
// Proves the filter MECHANISM works; it does not prove native serialization is safe.
import java.io.*;

public class DeserializationFilters {
    record Message(String text) implements Serializable {
        @Serial private static final long serialVersionUID = 1L;
    }

    public static void main(String[] args) throws Exception {
        var bytes = new ByteArrayOutputStream();
        try (var out = new ObjectOutputStream(bytes)) { out.writeObject(new Message("ready")); }

        ObjectInputFilter allowOnlyMessage = info -> {
            if (info.depth() > 5 || info.references() > 20 || info.streamBytes() > 4096)
                return ObjectInputFilter.Status.REJECTED;
            var type = info.serialClass();
            if (type == null) return ObjectInputFilter.Status.UNDECIDED;
            return type == Message.class
                ? ObjectInputFilter.Status.ALLOWED
                : ObjectInputFilter.Status.REJECTED;
        };

        // Allowed class passes.
        try (var in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            in.setObjectInputFilter(allowOnlyMessage);
            assert new Message("ready").equals(in.readObject());
        }

        // A class outside the allow-list is rejected.
        var other = new ByteArrayOutputStream();
        try (var out = new ObjectOutputStream(other)) { out.writeObject(new java.util.Date()); }
        boolean rejected = false;
        try (var in = new ObjectInputStream(new ByteArrayInputStream(other.toByteArray()))) {
            in.setObjectInputFilter(allowOnlyMessage);
            in.readObject();
        } catch (InvalidClassException | java.io.InvalidObjectException e) {
            rejected = true;
        }
        assert rejected : "non-allow-listed class must be rejected by the filter";

        System.out.println("PASS allow-list enforced both ways; this does not prove that "
            + "native deserialization of untrusted input is safe)");
    }
}
