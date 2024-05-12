package discord4j.rest.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    @Test
    public void testHexConstructor() {
        String hexCode = "FF9933";
        String hexCodeVariant = "#FF9933";
        int red = 255;
        int green = 153;
        int blue = 51;

        Color color = Color.of(hexCode);
        assertEquals(red, color.getRed());
        assertEquals(green, color.getGreen());
        assertEquals(blue, color.getBlue());

        Color colorVariant = Color.of(hexCodeVariant);
        assertEquals(red, colorVariant.getRed());
        assertEquals(green, colorVariant.getGreen());
        assertEquals(blue, colorVariant.getBlue());

        assertThrows(IllegalArgumentException.class, () -> Color.of("random"));
    }
}
