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

import reactor.util.annotation.Nullable;

import java.util.*;

/**
 * An <b>immutable</b>, specialized {@code Set<Permission>}.
 *
 * <p>
 * This is a <a href="https://docs.oracle.com/javase/8/docs/api/java/lang/doc-files/ValueBased.html">value-based</a>
 * class; use of identity-sensitive operations (including reference equality
 * ({@code ==}), identity hash code, or synchronization) on instances of
 * {@code PermissionSet} may have unpredictable results and should be avoided.
 * The {@code equals} method should be used for comparisons.
 *
 * @see <a href="https://discord.com/developers/docs/topics/permissions">Discord Permissions</a>
 */
public final class PermissionSet extends AbstractSet<Permission> {

    private static final long ALL_RAW = Arrays.stream(Permission.values())
        .mapToLong(Permission::getValue)
        .reduce(0, (a, b) -> a | b);
    private static final long NONE_RAW = 0;

    /** Common instance for {@code all()}. */
    private static final PermissionSet ALL = new PermissionSet(ALL_RAW);

    /** Common instance for {@code none()}. */
    private static final PermissionSet NONE = new PermissionSet(NONE_RAW);

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
    public static PermissionSet of(final long rawValue) {
        return new PermissionSet(rawValue);
    }

    /**
     * Returns a {@code PermissionSet} containing all the permissions represented by the <i>raw value</i>.
     *
     * @param rawValue A bit-wise OR evaluation of multiple values returned by {@link Permission#getValue()}, as a
     * string.
     * @return A {@code PermissionSet} containing all the permissions represented by the <i>raw value</i>.
     */
    public static PermissionSet of(final String rawValue) {
        return new PermissionSet(Long.parseUnsignedLong(rawValue));
    }

    /**
     * Returns a {@code PermissionSet} containing all the supplied permissions.
     *
     * @param permissions The permissions to add to the {@code PermissionSet}.
     * @return A {@code PermissionSet} containing all the supplied permissions.
     */
    public static PermissionSet of(final Permission... permissions) {
        final long rawValue = Arrays.stream(permissions)
                .mapToLong(Permission::getValue)
                .reduce(0, (left, right) -> left | right);
        return new PermissionSet(rawValue);
    }

    /** A bit-wise OR evaluation of multiple values returned by {@link Permission#getValue()}. */
    private final long rawValue;

    /**
     * Constructs a {@code PermissionSet} with a <i>raw value</i>.
     *
     * @param rawValue A bit-wise OR evaluation of multiple values returned by {@link Permission#getValue()}.
     */
    private PermissionSet(final long rawValue) {
        this.rawValue = rawValue;
    }

    /**
     * Performs a logical <b>AND</b> of this permission set with the other permission set.
     * <p>
     * The resultant set is the <b>intersection</b> of this set and the other set. A permission is contained if and only if it was
     * contained in both this set and the other set. This is analogous to {@link Set#retainAll(java.util.Collection)}.
     * <pre>
     * {@code
     * PermissionSet set0 = PermissionSet.of(KICK_MEMBERS, BAN_MEMBERS);
     * PermissionSet set1 = PermissionSet.of(KICK_MEMBERS);
     *
     * set0.and(set1) = PermissionSet.of(KICK_MEMBERS)
     * }
     * </pre>
     *
     * @param other The other permission set.
     * @return The intersection of this set with the other set.
     */
    public PermissionSet and(PermissionSet other) {
        return PermissionSet.of(this.rawValue & other.rawValue);
    }

    /**
     * Performs a logical <b>OR</b> of this permission set with the other permission set.
     * <p>
     * The resultant set is the <b>union</b> of this set and the other set. A permission is contained if and only if it
     * was contained in either this set or the other set. This is analogous to {@link Set#addAll(java.util.Collection)}.
     * <pre>
     * {@code
     * PermissionSet set0 = PermissionSet.of(KICK_MEMBERS);
     * PermissionSet set1 = PermissionSet.of(BAN_MEMBERS);
     *
     * set0.or(set1) = PermissionSet.of(KICK_MEMBERS, BAN_MEMBERS)
     * }
     * </pre>
     *
     * @param other The other permission set.
     * @return The union of this set with the other set.
     */
    public PermissionSet or(PermissionSet other) {
        return PermissionSet.of(this.rawValue | other.rawValue);
    }

    /**
     * Performs a logical <b>XOR</b> of this permission set with the other permission set.
     * <p>
     * The resultant set is the <b>symmetric difference</b> of this set and the other set. A permission is contained if
     * and only if it was contained in <b>only</b> this set or contained in <b>only</b> the other set.
     * <pre>
     * {@code
     * PermissionSet set0 = PermissionSet.of(KICK_MEMBERS, BAN_MEMBERS, ATTACH_FILES);
     * PermissionSet set1 = PermissionSet.of(ATTACH_FILES, CONNECT);
     *
     * set0.xor(set1) = PermissionSet.of(KICK_MEMBERS, BAN_MEMBERS, CONNECT)
     * }
     * </pre>
     *
     * @param other The other permission set.
     * @return The symmetric difference of this set with the other set.
     */
    public PermissionSet xor(PermissionSet other) {
        return PermissionSet.of(this.rawValue ^ other.rawValue);
    }

    /**
     * Performs a logical <b>AND NOT</b> of this permission set with the other permission set.
     * <p>
     * The resultant set is the <b>relative complement</b> of this set and the other set. A permission is contained if
     * and only if it was contained in this set and <b>not</b> contained in the other set. This is analogous to
     * {@link Set#removeAll(java.util.Collection)}.
     * <pre>
     * {@code
     * PermissionSet set0 = PermissionSet.of(KICK_MEMBERS, BAN_MEMBERS, ATTACH_FILES);
     * PermissionSet set1 = PermissionSet.of(BAN_MEMBERS, ATTACH_FILES, CONNECT);
     *
     * set0.andNot(set1) = PermissionSet.of(KICK_MEMBERS)
     * }
     * </pre>
     *
     * @param other The other permission set.
     * @return The relative complement of this set with the other set.
     */
    public PermissionSet andNot(PermissionSet other) {
        return PermissionSet.of(this.rawValue & (~other.rawValue));
    }

    /**
     * Performs a logical <b>AND NOT</b> of this permission set with the other permission set.
     * <p>
     * The resultant set is the <b>relative complement</b> of this set and the other set. A permission is contained if
     * and only if it was contained in this set and <b>not</b> contained in the other set.
     * <pre>
     * {@code
     * PermissionSet set0 = PermissionSet.of(KICK_MEMBERS, BAN_MEMBERS, ATTACH_FILES);
     * PermissionSet set1 = PermissionSet.of(BAN_MEMBERS, ATTACH_FILES, CONNECT);
     *
     * set0.subtract(set1) = PermissionSet.of(KICK_MEMBERS)
     * }
     * </pre>
     *
     * @param other The other permission set.
     * @return The relative complement of this set with the other set.
     *
     * @deprecated Use {@link PermissionSet#andNot(PermissionSet)} instead.
     */
    @Deprecated
    public PermissionSet subtract(PermissionSet other) {
        return PermissionSet.of(this.rawValue & (~other.rawValue));
    }

    /**
     * Performs a logical <b>NOT</b> of this permission set.
     * <p>
     * The resultant set is the <b>complement</b> of this set. A permission is contained if and only if it was
     * <b>not</b> contained in this set.
     * <pre>
     * {@code
     * PermissionSet set = PermissionSet.none();
     *
     * set.not() = PermissionSet.all()
     * }
     * </pre>
     *
     * @return The complement of this set.
     */
    public PermissionSet not() {
        return PermissionSet.of(~this.rawValue & ALL_RAW); // mask with ALL_RAW so undefined perms aren't flipped
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
    public long getRawValue() {
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
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PermissionSet that = (PermissionSet) o;
        return rawValue == that.rawValue;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(rawValue);
    }

    @Override
    public String toString() {
        return "PermissionSet{" +
                "rawValue=" + rawValue +
                "} " + super.toString();
    }
}
