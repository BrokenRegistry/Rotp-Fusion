package rotp.multiplayer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.jupiter.api.Test;

import rotp.util.Rand;

/** Characterizes the random stream used by a persisted galaxy. */
class SimulationBaselineTest {
    @Test
    void equalPositiveSeedsProduceEqualRandomStreams() {
        Rand first = new Rand(136L);
        Rand second = new Rand(136L);

        for (int i = 0; i < 32; i++) {
            assertEquals(first.nextInt(), second.nextInt());
            assertEquals(first.nextDouble(), second.nextDouble());
        }
    }

    @Test
    void serializedRandomStreamContinuesAtSamePosition() throws Exception {
        Rand original = new Rand(136L);
        original.nextInt();
        original.nextGaussian();

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream output = new ObjectOutputStream(bytes)) {
            output.writeObject(original);
        }

        Rand restored;
        try (ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            restored = (Rand) input.readObject();
        }

        assertEquals(original.nextGaussian(), restored.nextGaussian());
        for (int i = 0; i < 32; i++) {
            assertEquals(original.nextLong(), restored.nextLong());
        }
    }
}
