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
package discord4j.core.trait;

/** A Discord object that can be mentioned. */
public interface Mentionable {

	/**
	 * Gets the <i>raw</i> mention. This is the format utilized to directly mention another object (assuming the object
	 * exists in context of the mention).
	 *
	 * @return The <i>raw</i> mention.
	 */
	String getMention();

	/**
	 * Gets the formatted mention. This is the format seen directly in Discord (assuming the object exists in context of
	 * the mention). It should <i>not</i> be used to directly mention another object; use {@link #getMention()} instead.
	 *
	 * @return The formatted mention.
	 */
	String getFormattedMention();
}
