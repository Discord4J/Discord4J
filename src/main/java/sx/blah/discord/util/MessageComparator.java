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

package sx.blah.discord.util;

import sx.blah.discord.handle.obj.IMessage;

import java.util.Comparator;

/**
 * Compares messages based on their creation timestamps.
 */
public class MessageComparator implements Comparator<IMessage> {

	/**
	 * Singleton instance of the reversed message comparator.
	 *
	 * <p>This comparator defines messages in reverse chronological order. <b>Older</b> messages are <b>last</b>.
	 */
	public static final MessageComparator REVERSED = new MessageComparator(true);

	/**
	 * Singleton instance of the default message comparator.
	 *
	 * <p>This comparator defines messages in chronological order. <b>Older</b> messages are <b>first</b>.
	 */
	public static final MessageComparator DEFAULT = new MessageComparator(false);

	private boolean reverse;

	public MessageComparator(boolean reverse) {
		this.reverse = reverse;
	}

	@Override
	public int compare(IMessage o1, IMessage o2) {
		return o1.equals(o2) ? 0 : (reverse ? -1 : 1) * o1.getTimestamp().compareTo(o2.getTimestamp());
	}
}
