package io.github.tuanhiep.ltsjavalab.verification;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.DataInputStream;
import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootVersion;

class RuntimeBaselineLabTest {

    @Test
    void bootAndJunitBaselinesAreExplicit() {
        assertThat(SpringBootVersion.getVersion()).isEqualTo("4.1.0");
        assertThat(Test.class.getPackage().getImplementationVersion()).startsWith("6.");
        assertThat(Runtime.version().feature()).isGreaterThanOrEqualTo(21);
    }

    @Test
    void projectBytecodeTargetsTheJava21CompatibilityLane() throws IOException {
        String resource = "/" + RuntimeBaselineLabTest.class.getName().replace('.', '/') + ".class";
        try (var input = new DataInputStream(
                RuntimeBaselineLabTest.class.getResourceAsStream(resource))) {
            assertThat(input.readInt()).isEqualTo(0xCAFEBABE);
            input.readUnsignedShort(); // minor
            int major = input.readUnsignedShort();
            assertThat(major).isEqualTo(65); // Java 21 class-file version
        }
    }
}
