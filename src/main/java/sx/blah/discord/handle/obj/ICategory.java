/*
 * This file is part of Discord4J.
 *
 * Discord4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Discord4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */
package sx.blah.discord.handle.obj;

import sx.blah.discord.util.cache.LongMap;

import java.util.EnumSet;
import java.util.List;

/**
 * Do not use this yet because if you do all you will experience is pain and suffering from the deepest depths of the
 * coding gods, but seriously please god do not use this as it's still a work in progress and we need docs and aaahhhh!
 */
public interface ICategory extends IDiscordObject<ICategory> {
	void delete();
	boolean isDeleted();
	List<IChannel> getChannels();
	IGuild getGuild();
	int getPosition();
	void changePosition(int position);
	String getName();
	void changeName(String name);
	boolean isNSFW();
	EnumSet<Permissions> getModifiedPermissions(IUser user);
	EnumSet<Permissions> getModifiedPermissions(IRole role);
	LongMap<PermissionOverride> getUserOverridesLong();
	LongMap<PermissionOverride> getRoleOverridesLong();
	void removePermissionsOverride(IUser user);
	void removePermissionsOverride(IRole role);
	void overrideRolePermissions(IRole role, EnumSet<Permissions> toAdd, EnumSet<Permissions> toRemove);
	void overrideUserPermissions(IUser user, EnumSet<Permissions> toAdd, EnumSet<Permissions> toRemove);
}
