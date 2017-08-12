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

package sx.blah.discord.modules;

/**
 * Used to configure module behavior in Discord4J.
 */
public class Configuration {

	/**
	 * Whether modules should be automatically enabled when loaded. This is recommended because it automatically
	 * resolves dependencies.
	 */
	public static boolean AUTOMATICALLY_ENABLE_MODULES = true;

	/**
	 * Whether external modules located in the modules directory should be automatically loaded.
	 */
	public static boolean LOAD_EXTERNAL_MODULES = true;
}
