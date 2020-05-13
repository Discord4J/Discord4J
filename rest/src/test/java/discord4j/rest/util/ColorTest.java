package discord4j.rest.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class ColorTest {

    @Test
    public void testRedGreenBlueConstructor() {
        Color color = new Color(1, 2, 3);
        assertEquals(1, color.getRed());
        assertEquals(2, color.getGreen());
        assertEquals(3, color.getBlue());
        assertEquals(255, color.getAlpha());

        assertThrows(IllegalArgumentException.class, () -> new Color(-1, -100, -200));
        assertThrows(IllegalArgumentException.class, () -> new Color(300, 400, 500));
    }

    @Test
    public void testRedGreenBlueAlphaConstructor() {
        Color color = new Color(1, 2, 3, 4);
        assertEquals(1, color.getRed());
        assertEquals(2, color.getGreen());
        assertEquals(3, color.getBlue());
        assertEquals(4, color.getAlpha());

        assertThrows(IllegalArgumentException.class, () -> new Color(1, 2, 3, -300));
        assertThrows(IllegalArgumentException.class, () -> new Color(1, 2, 3, 300));
    }

    @Test
    public void testValueConstructor() {
        Color color = new Color(255);
        assertEquals(0, color.getRed());
        assertEquals(0, color.getGreen());
        assertEquals(255, color.getBlue());
        assertEquals(255, color.getAlpha());
    }

    @Test
    public void testValueAlphaConstructor() {
        Color color = new Color(255, true);
        assertEquals(0, color.getRed());
        assertEquals(0, color.getGreen());
        assertEquals(255, color.getBlue());
        assertEquals(0, color.getAlpha());
    }

}
