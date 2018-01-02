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
package discord4j.core.object;

/**
 * Determines whether {@link discord4j.core.object.entity.Member Members} who have not explicitly set their notification
 * settings receive a notification for every message sent in the server or not.
 */
public enum NotificationLevel {

	/** Receive a notification for all messages. */
	ALL_MESSAGES(0),

	/** Receive a notification only for mentions. */
	ONLY_MENTIONS(1);

	private final int value;

	NotificationLevel(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}
