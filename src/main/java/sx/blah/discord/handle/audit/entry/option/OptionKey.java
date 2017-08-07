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

package sx.blah.discord.handle.audit.entry.option;

/**
 * The keys used in an {@link OptionMap}. Use these with
 * {@link sx.blah.discord.handle.audit.entry.AuditLogEntry#getOptionByKey(OptionKey)}.
 *
 * @param <T> The type of the value associated with the key.
 */
public final class OptionKey<T> {
	public static final OptionKey<Integer> DELETE_MEMBER_DAYS = newKey();
	public static final OptionKey<Integer> MEMBERS_REMOVED = newKey();
	public static final OptionKey<Long> CHANNEL_ID = newKey();
	public static final OptionKey<Integer> COUNT = newKey();
	public static final OptionKey<Long> ID = newKey();
	public static final OptionKey<String> TYPE = newKey();
	public static final OptionKey<String> ROLE_NAME = newKey();

	private static <T> OptionKey<T> newKey() {
		return new OptionKey<>();
	}

	private OptionKey() {

	}
}
