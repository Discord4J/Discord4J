/*
 *     This file is part of Discord4J.
 *
 *     Discord4J is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Discord4J is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */

package sx.blah.discord.handle.impl.obj;

import sx.blah.discord.handle.obj.IEmoji;
import sx.blah.discord.handle.obj.IIDLinkedObject;

import java.util.Objects;

/**
 * An emoji on a reaction. This can either be a unicode or custom guild emoji. In the case of the former,
 * {@link #id} is <code>0</code>. This is a value-based class.
 */
public class ReactionEmoji implements IIDLinkedObject {

	/**
	 * Constructs a {@link ReactionEmoji} from the name and ID of the given guild emoji.
	 *
	 * @param emoji The emoji to get a name and ID from.
	 * @return A reaction emoji with the name and ID of the given guild emoji.
	 */
	public static ReactionEmoji of(IEmoji emoji) {
		return of(emoji.getName(), emoji.getLongID(), emoji.isAnimated());
	}

	/**
	 * Constructs a {@link ReactionEmoji} from the given name, ID and animated flag.
	 *
	 * @param name       The name of the emoji.
	 * @param id         The ID of the emoji.
	 * @param isAnimated Whether the emoji is animated.
	 * @return A reaction emoji with the given name and ID.
	 */
	public static ReactionEmoji of(String name, long id, boolean isAnimated) {
		return new ReactionEmoji(name, id, isAnimated);
	}

	/**
	 * Constructs a {@link ReactionEmoji} from the given name and ID.
	 *
	 * @param name The name of the emoji.
	 * @param id   The ID of the emoji.
	 * @return A reaction emoji with the given name and ID.
	 */
	public static ReactionEmoji of(String name, long id) {
		return new ReactionEmoji(name, id, false);
	}

	/**
	 * Constructs a {@link ReactionEmoji} from the given unicode emoji.
	 *
	 * @param unicode The unicode emoji.
	 * @return A reaction emoji with the given unicode emoji.
	 */
	public static ReactionEmoji of(String unicode) {
		return new ReactionEmoji(unicode, 0L, false);
	}

	/**
	 * The name of the reaction emoji.
	 */
	private final String name;
	/**
	 * The unique snowflake ID of the reaction emoji. The the emoji is a unicode emoji, this value is <code>0</code>.
	 */
	private final long id;
	/**
	 * Whether the emoji is animated.
	 */
	private final boolean isAnimated;

	private ReactionEmoji(String name, long id, boolean isAnimated) {
		this.name = name;
		this.id = id;
		this.isAnimated = isAnimated;
	}

	/**
	 * Gets the name of the emoji. If the emoji is a unicode emoji, it returns the unicode character.
	 *
	 * @return The name of the emoji or the unicode character.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets whether the emoji is a unicode emoji.
	 *
	 * @return Whether the emoji is a unicode emoji.
	 */
	public boolean isUnicode() {
		return id == 0;
	}

	/**
	 * Gets whether the emoji is animated.
	 *
	 * @return Whether the emoji is animated.
	 */
	public boolean isAnimated() {
		return isAnimated;
	}


	@Override
	public long getLongID() {
		return id;
	}

	@Override
	public String toString() {
		return isUnicode() ? getName() : "<" + (isAnimated() ? "a" : "") + ":" + getName() + ":" + Long.toUnsignedString(getLongID()) + ">";
	}

	@Override
	public boolean equals(Object other) {
		if (other == null) return false;
		if (!this.getClass().isAssignableFrom(other.getClass())) return false;

		ReactionEmoji emoji = (ReactionEmoji) other;
		return emoji.name.equals(this.name) && emoji.id == this.id;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, id);
	}
}
