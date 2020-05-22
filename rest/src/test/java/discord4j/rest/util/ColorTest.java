package discord4j.rest.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class ColorTest {

    @Test
    public void testRedGreenBlueConstructor() {
        Color color = Color.of(1, 2, 3);
        assertEquals(1, color.getRed());
        assertEquals(2, color.getGreen());
        assertEquals(3, color.getBlue());

        assertThrows(IllegalArgumentException.class, () -> Color.of(-1, -100, -200));
        assertThrows(IllegalArgumentException.class, () -> Color.of(300, 400, 500));
    }

    @Test
    public void testValueConstructor() {
        Color color = Color.of(255);
        assertEquals(0, color.getRed());
        assertEquals(0, color.getGreen());
        assertEquals(255, color.getBlue());
    }
}
