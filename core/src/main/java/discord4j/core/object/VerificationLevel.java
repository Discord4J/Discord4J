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
 * {@link discord4j.core.object.entity.Member Members} of the server must meet the following criteria before they can
 * send messages in text channels or initiate a direct message conversation. If a member has an assigned role this does
 * not apply.
 */
public enum VerificationLevel {

	/** Unrestricted. */
	NONE,

	/** Must have verified email on account. */
	LOW,

	/** Must be registered on Discord for longer than 5 minutes. */
	MEDIUM,

	/** (╯°□°）╯︵ ┻━┻ - Must be a member of the server for longer than 10 minutes. */
	HIGH,

	/** ┻━┻ミヽ(ಠ益ಠ)ﾉ彡┻━┻ - Must have a verified phone number. */
	VERY_HIGH
}
