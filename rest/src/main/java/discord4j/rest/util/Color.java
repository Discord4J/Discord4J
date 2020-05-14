/*
 * This file is part of Discord4J.
 *
 * Discord4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Discord4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J. If not, see <http://www.gnu.org/licenses/>.
 */
package discord4j.rest.util;

public final class Color {

    /** The color white. */
    public static final Color WHITE = of(255, 255, 255);

    /** The color light gray. */
    public static final Color LIGHT_GRAY = of(192, 192, 192);

    /** The color gray. */
    public static final Color GRAY = of(128, 128, 128);

    /** The color dark gray. */
    public static final Color DARK_GRAY = of(64, 64, 64);

    /** The color black. */
    public static final Color BLACK = of(0, 0, 0);

    /** The color red. */
    public static final Color RED = of(255, 0, 0);

    /** The color pink. */
    public static final Color PINK = of(255, 175, 175);

    /** The color orange. */
    public static final Color ORANGE = of(255, 200, 0);

    /** The color yellow. */
    public static final Color YELLOW = of(255, 255, 0);

    /** The color green. */
    public static final Color GREEN = of(0, 255, 0);

    /** The color magenta. */
    public static final Color MAGENTA = of(255, 0, 255);

    /** The color cyan. */
    public static final Color CYAN = of(0, 255, 255);

    /** The color blue. */
    public static final Color BLUE = of(0, 0, 255);

    /** The color light sea green. This is a Discord color preset. */
    public static final Color LIGHT_SEA_GREEN = of(0x1ABC9C);

    /** The color medium sea green. This is a Discord color preset. */
    public static final Color MEDIUM_SEA_GREEN = of(0x2ECC71);

    /** The color summer sky. This is a Discord color preset. */
    public static final Color SUMMER_SKY = of(0x3498DB);

    /** The color deep lilac. This is a Discord color preset. */
    public static final Color DEEP_LILAC = of(0x9B59B6);

    /** The color ruby. This is a Discord color preset. */
    public static final Color RUBY = of(0xE91E63);

    /** The color moon yellow. This is a Discord color preset. */
    public static final Color MOON_YELLOW = of(0xF1C40F);

    /** The color tahiti gold. This is a Discord color preset. */
    public static final Color TAHITI_GOLD = of(0xE67E22);

    /** The color cinnabar. This is a Discord color preset. */
    public static final Color CINNABAR = of(0xE74C3C);

    /** The color submarine. This is a Discord color preset. */
    public static final Color SUBMARINE = of(0x95A5A6);

    /** The color hoki. This is a Discord color preset. */
    public static final Color HOKI = of(0x607D8B);

    /** The color deep sea. This is a Discord color preset. */
    public static final Color DEEP_SEA = of(0x11806A);

    /** The color sea green. This is a Discord color preset. */
    public static final Color SEA_GREEN = of(0x1F8B4C);

    /** The color endeavour. This is a Discord color preset. */
    public static final Color ENDEAVOUR = of(0x206694);

    /** The color vivid violet. This is a Discord color preset. */
    public static final Color VIVID_VIOLET = of(0x71368A);

    /** The color jazzberry jam. This is a Discord color preset. */
    public static final Color JAZZBERRY_JAM = of(0xAD1457);

    /** The color dark goldenrod. This is a Discord color preset. */
    public static final Color DARK_GOLDENROD = of(0xC27C0E);

    /** The color rust. This is a Discord color preset. */
    public static final Color RUST = of(0xA84300);

    /** The color brown. This is a Discord color preset. */
    public static final Color BROWN = of(0x992D22);

    /** The color gray chateau. This is a Discord color preset. */
    public static final Color GRAY_CHATEAU = of(0x979C9F);

    /** The color bismark. This is a Discord color preset. */
    public static final Color BISMARK = of(0x546E7A);

    /**
     * Initializes a new instance of {@link Color} using the specified red, green, and blue values, which must be given
     * as floats in the range of 0.0F-255.0F.
     *
     * @param red The red component of the RGB value.
     * @param green The green component of the RGB value.
     * @param blue The blue component of the RGB value.
     */
    public static Color of(final float red, final float green, final float blue) {
        return of((int) (red * 255.0F + 0.5F), (int) (green * 255.0F + 0.5F), (int) (blue * 255.0F + 0.5F));
    }

    /**
     * Initializes a new instance of {@link Color} using the specified red, green, and blue values, which must be given
     * as integers in the range of 0-255.
     *
     * @param red The red component of the RGB value.
     * @param green The green component of the RGB value.
     * @param blue The blue component of the RGB value.
     */
    public static Color of(final int red, final int green, final int blue) {
        if ((red & 0xFF) != red || (green & 0xFF) != green || (blue & 0xFF) != blue) {
            throw new IllegalArgumentException("Illegal RGB arguments" +
                " red=0x" + Integer.toHexString(red) +
                " green=0x" + Integer.toHexString(green) +
                " blue=0x" + Integer.toHexString(blue));
        }

        return of((red << 16) | (green << 8) | blue);
    }

    /**
     * Initializes a new instance of {@link Color} using the specified RGB value. The blue value is in bits 0-7, green
     * in bits 8-15, and red in bits 16-23.
     *
     * @param rgb The RGB value.
     */
    public static Color of(final int rgb) {
        return new Color(rgb & 0xFFFFFF);
    }

    /** The color value, in RGB. */
    private final int rgb;

    /**
     * Initializes a new instance of {@link Color} using the specified RGB value. The blue value is in bits 0-7, green
     * in bits 8-15, and red in bits 16-23.
     *
     * @param rgb The RGB value.
     */
    private Color(final int rgb) {
        this.rgb = rgb;
    }

    /**
     * Returns the RGB value for this color. The blue value will be in bits 0-7, green in 8-15, and red in 16-23.
     *
     * @return The RGB value for this color.
     */
    public int getRGB() {
        return rgb;
    }

    /**
     * Returns the red value for this color, as an integer in the range 0-255.
     *
     * @return The red value for this color.
     */
    public int getRed() {
        return (rgb >> 16) & 0xFF;
    }

    /**
     * Returns the green value for this color, as an integer in the range 0-255.
     *
     * @return The green value for this color.
     */
    public int getGreen() {
        return (rgb >> 8) & 0xFF;
    }

    /**
     * Returns the blue value for this color, as an integer in the range 0-255.
     *
     * @return The blue value for this color.
     */
    public int getBlue() {
        return rgb & 0xFF;
    }

    @Override
    public String toString() {
        return "Color{" +
            "red=" + getRed() +
            ", green=" + getGreen() +
            ", blue=" + getBlue() +
            '}';
    }

    public boolean equals(final Object obj) {
        return obj instanceof Color && ((Color) obj).getRGB() == getRGB();
    }

    public int hashCode() {
        return getRGB();
    }
}
