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

package discord4j.gateway.intent;

import reactor.util.annotation.Nullable;

import java.util.*;

/**
 * An <b>immutable</b>, specialized {@code Set<Intent>}.
 *
 * <p>
 * This is a <a href="https://docs.oracle.com/javase/8/docs/api/java/lang/doc-files/ValueBased.html">value-based</a>
 * class; use of identity-sensitive operations (including reference equality
 * ({@code ==}), identity hash code, or synchronization) on instances of
 * {@code IntentSet} may have unpredictable results and should be avoided.
 * The {@code equals} method should be used for comparisons.
 *
 * @see <a href="https://discord.com/developers/docs/topics/gateway#gateway-intents">Discord Intents</a>
 */
public final class IntentSet extends AbstractSet<Intent> {

    private static final long ALL_RAW = Arrays.stream(Intent.values())
            .mapToLong(Intent::getValue)
            .reduce(0, (a, b) -> a | b);
    private static final long NONE_RAW = 0;

    /**
     * Common instance for {@code all()}.
     */
    private static final IntentSet ALL = new IntentSet(ALL_RAW);

    /**
     * Common instance for {@code none()}.
     */
    private static final IntentSet NONE = new IntentSet(NONE_RAW);

    /**
     * Common instance for {@code nonPrivileged()}.
     */
    private static final IntentSet NON_PRIVILEGED =
            ALL.andNot(IntentSet.of(Intent.GUILD_PRESENCES, Intent.GUILD_MEMBERS, Intent.MESSAGE_CONTENT));

    /**
     * Returns a {@code IntentSet} containing all intents.
     *
     * @return A {@code IntentSet} containing all intents.
     */
    public static IntentSet all() {
        return ALL;
    }

    /**
     * Returns a {@code IntentSet} containing no intents.
     *
     * @return A {@code IntentSet} containing no intents.
     */
    public static IntentSet none() {
        return NONE;
    }

    /**
     * Returns a {@code IntentSet} containing non-privileged intents.
     *
     * @return A {@code IntentSet} containing non-privileged intents.
     * @see <a href="https://discord.com/developers/docs/topics/gateway#privileged-intents">Privileged Intents</a>
     */
    public static IntentSet nonPrivileged() {
        return NON_PRIVILEGED;
    }

    /**
     * Returns a {@code IntentSet} containing all the intents represented by the <i>raw value</i>.
     *
     * @param rawValue A bit-wise OR evaluation of multiple values returned by {@link Intent#getValue()}.
     * @return A {@code IntentSet} containing all the intents represented by the <i>raw value</i>.
     */
    public static IntentSet of(final long rawValue) {
        return new IntentSet(rawValue);
    }

    /**
     * Returns a {@code IntentSet} containing all the supplied intents.
     *
     * @param intents The intents to add to the {@code IntentSet}.
     * @return A {@code IntentSet} containing all the supplied intents.
     */
    public static IntentSet of(final Intent... intents) {
        final long rawValue = Arrays.stream(intents)
                .mapToLong(Intent::getValue)
                .reduce(0, (left, right) -> left | right);
        return new IntentSet(rawValue);
    }

    /**
     * A bit-wise OR evaluation of multiple values returned by {@link Intent#getValue()}.
     */
    private final long rawValue;

    /**
     * Constructs a {@code IntentSet} with a <i>raw value</i>.
     *
     * @param rawValue A bit-wise OR evaluation of multiple values returned by {@link Intent#getValue()}.
     */
    private IntentSet(final long rawValue) {
        this.rawValue = rawValue;
    }

    /**
     * Performs a logical <b>AND</b> of this intent set with the other intent set.
     * <p>
     * The resultant set is the <b>intersection</b> of this set and the other set. A intent is contained if and only
     * if it was
     * contained in both this set and the other set. This is analogous to {@link Set#retainAll(java.util.Collection)}.
     * <pre>
     * {@code
     * IntentSet set0 = IntentSet.of(GUILDS, GUILD_MEMBERS);
     * IntentSet set1 = IntentSet.of(GUILDS);
     *
     * set0.and(set1) = IntentSet.of(GUILDS)
     * }
     * </pre>
     *
     * @param other The other intent set.
     * @return The intersection of this set with the other set.
     */
    public IntentSet and(IntentSet other) {
        return IntentSet.of(this.rawValue & other.rawValue);
    }

    /**
     * Performs a logical <b>OR</b> of this intent set with the other intent set.
     * <p>
     * The resultant set is the <b>union</b> of this set and the other set. A intent is contained if and only if it
     * was contained in either this set or the other set. This is analogous to {@link Set#addAll(java.util.Collection)}.
     * <pre>
     * {@code
     * IntentSet set0 = IntentSet.of(GUILDS);
     * IntentSet set1 = IntentSet.of(GUILD_MEMBERS);
     *
     * set0.or(set1) = IntentSet.of(GUILDS, GUILD_MEMBERS)
     * }
     * </pre>
     *
     * @param other The other intent set.
     * @return The union of this set with the other set.
     */
    public IntentSet or(IntentSet other) {
        return IntentSet.of(this.rawValue | other.rawValue);
    }

    /**
     * Performs a logical <b>XOR</b> of this intent set with the other intent set.
     * <p>
     * The resultant set is the <b>symmetric difference</b> of this set and the other set. A intent is contained if
     * and only if it was contained in <b>only</b> this set or contained in <b>only</b> the other set.
     * <pre>
     * {@code
     * IntentSet set0 = IntentSet.of(GUILDS, GUILD_MEMBERS, GUILD_BANS);
     * IntentSet set1 = IntentSet.of(GUILD_BANS, GUILD_EMOJIS);
     *
     * set0.xor(set1) = IntentSet.of(GUILDS, GUILD_MEMBERS, GUILD_EMOJIS)
     * }
     * </pre>
     *
     * @param other The other intent set.
     * @return The symmetric difference of this set with the other set.
     */
    public IntentSet xor(IntentSet other) {
        return IntentSet.of(this.rawValue ^ other.rawValue);
    }

    /**
     * Performs a logical <b>AND NOT</b> of this intent set with the other intent set.
     * <p>
     * The resultant set is the <b>relative complement</b> of this set and the other set. A intent is contained if
     * and only if it was contained in this set and <b>not</b> contained in the other set. This is analogous to
     * {@link Set#removeAll(java.util.Collection)}.
     * <pre>
     * {@code
     * IntentSet set0 = IntentSet.of(GUILDS, GUILD_MEMBERS, GUILD_BANS);
     * IntentSet set1 = IntentSet.of(GUILD_MEMBERS, GUILD_BANS, GUILD_EMOJIS);
     *
     * set0.andNot(set1) = IntentSet.of(GUILDS)
     * }
     * </pre>
     *
     * @param other The other intent set.
     * @return The relative complement of this set with the other set.
     */
    public IntentSet andNot(IntentSet other) {
        return IntentSet.of(this.rawValue & (~other.rawValue));
    }

    /**
     * Performs a logical <b>NOT</b> of this intent set.
     * <p>
     * The resultant set is the <b>complement</b> of this set. A intent is contained if and only if it was
     * <b>not</b> contained in this set.
     * <pre>
     * {@code
     * IntentSet set = IntentSet.none();
     *
     * set.not() = IntentSet.all()
     * }
     * </pre>
     *
     * @return The complement of this set.
     */
    public IntentSet not() {
        return IntentSet.of(~this.rawValue & ALL_RAW);
    }

    /**
     * Gets this {@code IntentSet} as an {@link EnumSet}.
     *
     * @return This {@code IntentSet} as an {@link EnumSet}.
     */
    public EnumSet<Intent> asEnumSet() {
        final EnumSet<Intent> intents = EnumSet.allOf(Intent.class);
        intents.removeIf(intent -> !contains(intent));
        return intents;
    }

    /**
     * Gets the <i>raw value</i> for this {@code IntentSet}.
     *
     * @return The <i>raw value</i> for this {@code IntentSet}.
     * @see IntentSet
     */
    public long getRawValue() {
        return rawValue;
    }

    @Override
    public boolean contains(final Object o) {
        return (o instanceof Intent) && ((((Intent) o).getValue() & rawValue) > 0);
    }

    @Override
    public Iterator<Intent> iterator() {
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

        IntentSet that = (IntentSet) o;
        return rawValue == that.rawValue;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(rawValue);
    }

    @Override
    public String toString() {
        return "IntentSet{" +
                "rawValue=" + rawValue +
                "} " + super.toString();
    }
}
