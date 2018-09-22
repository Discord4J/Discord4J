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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */
package discord4j.core.object.util;

import java.util.*;

/** An <i>immutable</i> specialized {@link Set} implementation for use with the {@link Permission} type. */
public final class PermissionSet extends AbstractSet<Permission> {

    /** Common instance for {@code all()}. */
    private static final PermissionSet ALL = new PermissionSet(0x7FF7FDFF);

    /** Common instance for {@code none()}. */
    private static final PermissionSet NONE = new PermissionSet(0x00000000);

    /**
     * Returns a {@code PermissionSet} containing all permissions.
     *
     * @return A {@code PermissionSet} containing all permissions.
     */
    public static PermissionSet all() {
        return ALL;
    }

    /**
     * Returns a {@code PermissionSet} containing no permissions.
     *
     * @return A {@code PermissionSet} containing no permissions.
     */
    public static PermissionSet none() {
        return NONE;
    }

    /**
     * Returns a {@code PermissionSet} containing all the permissions represented by the <i>raw value</i>.
     *
     * @param rawValue A bit-wise OR evaluation of multiple values returned by {@link Permission#getValue()}.
     * @return A {@code PermissionSet} containing all the permissions represented by the <i>raw value</i>.
     */
    public static PermissionSet of(final int rawValue) {
        return new PermissionSet(rawValue);
    }

    /**
     * Returns a {@code PermissionSet} containing all the supplied permissions.
     *
     * @param permissions The permissions to add to the {@code PermissionSet}.
     * @return A {@code PermissionSet} containing all the supplied permissions.
     */
    public static PermissionSet of(final Permission... permissions) {
        final int rawValue = Arrays.stream(permissions)
                .mapToInt(Permission::getValue)
                .reduce(0, (left, right) -> left | right);
        return new PermissionSet(rawValue);
    }

    /** A bit-wise OR evaluation of multiple values returned by {@link Permission#getValue()}. */
    private final int rawValue;

    /**
     * Constructs a {@code PermissionSet} with a <i>raw value</i>.
     *
     * @param rawValue A bit-wise OR evaluation of multiple values returned by {@link Permission#getValue()}.
     */
    private PermissionSet(final int rawValue) {
        this.rawValue = rawValue;
    }

    /**
     * Performs a logical AND of of this permission set with the other permission set.
     *
     * @param other The other permission set.
     * @return A new permission set of this set AND the other set.
     */
    public PermissionSet and(PermissionSet other) {
        return PermissionSet.of(this.rawValue & other.rawValue);
    }

    /**
     * Performs a logical OR of this permission set with the other permission set.
     *
     * @param other The other permission set.
     * @return A new permission set of this set OR the other set.
     */
    public PermissionSet or(PermissionSet other) {
        return PermissionSet.of(this.rawValue | other.rawValue);
    }

    /**
     * Performs a logical NOT of this permission set.
     *
     * @return A new permission set representing this set's complement.
     */
    public PermissionSet not() {
        return PermissionSet.of(~this.rawValue);
    }

    /**
     * Performs a logical XOR of this permission set with the other permission set.
     *
     * @param other The other permission set.
     * @return A new permission set of this set XOR the other set.
     */
    public PermissionSet xor(PermissionSet other) {
        return PermissionSet.of(this.rawValue ^ other.rawValue);
    }

    /**
     * Subtracts the contents of the given permission set from this permission set.
     *
     * @param other The other permission set.
     * @return A new permission set with the contents of the other set removed.
     */
    public PermissionSet subtract(PermissionSet other) {
        return PermissionSet.of(this.rawValue & (~other.rawValue));
    }

    /**
     * Gets this {@code PermissionSet} as an {@link EnumSet}.
     *
     * @return This {@code PermissionSet} as an {@link EnumSet}.
     */
    public EnumSet<Permission> asEnumSet() {
        final EnumSet<Permission> permissions = EnumSet.allOf(Permission.class);
        permissions.removeIf(permission -> !contains(permission));
        return permissions;
    }

    /**
     * Gets the <i>raw value</i> for this {@code PermissionSet}.
     *
     * @return The <i>raw value</i> for this {@code PermissionSet}.
     * @see PermissionSet
     */
    public int getRawValue() {
        return rawValue;
    }

    @Override
    public boolean contains(final Object o) {
        return (o instanceof Permission) && ((((Permission) o).getValue() & rawValue) > 0);
    }

    @Override
    public Iterator<Permission> iterator() {
        // Wrap so users aren't confused when remove doesn't "work"
        return Collections.unmodifiableSet(asEnumSet()).iterator();
    }

    @Override
    public int size() {
        return Long.bitCount(rawValue);
    }

    @Override
    public String toString() {
        return "PermissionSet{" +
                "rawValue=" + rawValue +
                "} " + super.toString();
    }
}
