package discord4j.rest.util;

import java.util.Objects;

public class Color {

    /** Internal mask for red. */
    private static final int RED_MASK = 255 << 16;
    /** Internal mask for green. */
    private static final int GREEN_MASK = 255 << 8;
    /** Internal mask for blue. */
    private static final int BLUE_MASK = 255;
    /** Internal mask for alpha. */
    private static final int ALPHA_MASK = 255 << 24;

    /** The color value, in sRGB. */
    private final int value;
    /** The alpha value. This is in the range 0.0f - 1.0f. */
    private final float falpha;

    /**
     * Initializes a new instance of {@link Color} using the specified
     * red, green, and blue values, which must be given as integers in the
     * range of 0-255. Alpha will default to 255 (opaque).
     *
     * @param red The red component of the RGB value.
     * @param green The green component of the RGB value.
     * @param blue The blue component of the RGB value.
     */
    public Color(int red, int green, int blue) {
        this(red, green, blue, 255);
    }

    /**
     * Initializes a new instance of {@link Color} using the specified
     * red, green, blue, and alpha values, which must be given as integers in
     * the range of 0-255.
     *
     * @param red The red component of the RGB value.
     * @param green The green component of the RGB value.
     * @param blue The blue component of the RGB value.
     * @param alpha The alpha value of the color.
     */
    public Color(int red, int green, int blue, int alpha) {
        if ((red & 255) != red || (green & 255) != green || (blue & 255) != blue
                || (alpha & 255) != alpha) {
            throw new IllegalArgumentException("Bad RGB values"
                    + " red=0x" + Integer.toHexString(red)
                    + " green=0x" + Integer.toHexString(green)
                    + " blue=0x" + Integer.toHexString(blue)
                    + " alpha=0x" + Integer.toHexString(alpha));
        }

        value = (alpha << 24) | (red << 16) | (green << 8) | blue;
        falpha = 1;
    }

    /**
     * Initializes a new instance of {@link Color} using the specified
     * RGB value. The blue value is in bits 0-7, green in bits 8-15, and
     * red in bits 16-23. The other bits are ignored. The alpha value is set
     * to 255 (opaque).
     *
     * @param value The RGB value.
     */
    public Color(int value) {
        this(value, false);
    }

    /**
     * Initializes a new instance of {@link Color} using the specified
     * RGB value. The blue value is in bits 0-7, green in bits 8-15, and
     * red in bits 16-23. The alpha value is in bits 24-31, unless hasalpha
     * is false, in which case alpha is set to 255.
     *
     * @param value The RGB value.
     * @param hasalpha Whether value includes the alpha.
     */
    public Color(int value, boolean hasalpha) {
        // NoteSystemColor calls this constructor, setting falpha to 0; but
        // code in getRGBComponents correctly reports falpha as 1.0 to the user
        // for all instances of SystemColor since frgbvalue is left null here.
        if (hasalpha) {
            falpha = ((value & ALPHA_MASK) >> 24) / 255f;
        } else {
            value |= ALPHA_MASK;
            falpha = 1;
        }
        this.value = value;
    }

    /**
     * Returns the red value for this color, as an integer in the range 0-255.
     *
     * @return The red value for this color.
     */
    public int getRed() {
        return (getRGB() & RED_MASK) >> 16;
    }

    /**
     * Returns the green value for this color, as an integer in the range 0-255.
     *
     * @return The green value for this color.
     */
    public int getGreen() {
        return (getRGB() & GREEN_MASK) >> 8;
    }

    /**
     * Returns the blue value for this color, as an integer in the range 0-255.
     *
     * @return The blue value for this color.
     */
    public int getBlue() {
        return getRGB() & BLUE_MASK;
    }

    /**
     * Returns the alpha value for this color, as an integer in the range 0-255.
     *
     * @return The alpha value for this color.
     */
    public int getAlpha() {
        return (getRGB() & ALPHA_MASK) >>> 24;
    }

    /**
     * Returns the RGB value for this color, in the sRGB color space. The blue
     * value will be in bits 0-7, green in 8-15, red in 16-23, and alpha value in
     * 24-31.
     *
     * @return The RGB value for this color.
     */
    public int getRGB() {
        return value;
    }

    @Override
    public String toString() {
        return "Color{" +
                "r=" + getRed() +
                "g=" + getGreen() +
                "b=" + getBlue() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Color color = (Color) o;
        return value == color.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    /**
     * Converts float values to integer value.
     *
     * @param red The red value.
     * @param green The green value.
     * @param blue The blue value.
     * @param alpha The alpha value.
     * @return The integer value made of 8-bit sections.
     */
    private static int convert(float red, float green, float blue, float alpha) {
        if (red < 0 || red > 1 || green < 0 || green > 1 || blue < 0 || blue > 1
                || alpha < 0 || alpha > 1) {
            throw new IllegalArgumentException("Bad RGB values");
        }
        int redval = Math.round(255 * red);
        int greenval = Math.round(255 * green);
        int blueval = Math.round(255 * blue);
        int alphaval = Math.round(255 * alpha);
        return (alphaval << 24) | (redval << 16) | (greenval << 8) | blueval;
    }
}