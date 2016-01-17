/*
 * Discord4J - Unofficial wrapper for Discord API
 * Copyright (c) 2015
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package sx.blah.discord.handle.impl.obj;

import sx.blah.discord.api.DiscordEndpoints;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Presences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class User implements IUser {
	
	/**
	 * Display name of the user.
	 */
	protected String name;
	
	/**
	 * The user's avatar location.
	 */
	protected String avatar;
	
	/**
	 * The game the user is playing.
	 */
	protected Optional<String> game;
	
	/**
	 * User ID.
	 */
	protected final String id;
	
	/**
	 * User discriminator.
	 * Distinguishes users with the same name.
	 * This is here in case it becomes necessary.
	 */
	protected final String discriminator;
	
	/**
	 * This user's presence.
	 * One of [online/idle/offline].
	 */
	protected Presences presence;
	
	/**
	 * The user's avatar in URL form.
	 */
	protected String avatarURL;
	
	/**
	 * The roles the user is a part of. (Key = guild id).
	 */
	protected HashMap<String, List<IRole>> roles;
	
	/**
	 * The client that created this object.
	 */
	protected final IDiscordClient client;
	
	public User(IDiscordClient client, String name, String id, String discriminator, String avatar, Presences presence) {
		this.client = client;
		this.id = id;
		this.name = name;
		this.discriminator = discriminator;
		this.avatar = avatar;
		this.avatarURL = String.format(DiscordEndpoints.AVATARS, this.id, this.avatar);
		this.presence = presence;
		this.roles = new HashMap<>();
	}
	
	@Override
	public String getID() {
		return id;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public Optional<String> getGame() {
		return game == null ? Optional.empty() : game;
	}
	
	/**
	 * Sets the user's CACHED game.
	 *
	 * @param game The game.
	 */
	public void setGame(Optional<String> game) {
		this.game = game;
	}
	
	/**
	 * Sets the user's CACHED username.
	 *
	 * @param name The username.
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public String getAvatar() {
		return avatar;
	}
	
	@Override
	public String getAvatarURL() {
		return avatarURL;
	}
	
	/**
	 * Sets the user's CACHED avatar id.
	 *
	 * @param avatar The user's avatar id.
	 */
	public void setAvatar(String avatar) {
		this.avatar = avatar;
		this.avatarURL = String.format(DiscordEndpoints.AVATARS, this.id, this.avatar);
	}
	
	@Override
	public Presences getPresence() {
		return presence;
	}
	
	/**
	 * Sets the CACHED presence of the user.
	 *
	 * @param presence The new presence.
	 */
	public void setPresence(Presences presence) {
		this.presence = presence;
	}
	
	@Override
	public String mention() {
		return "<@"+id+">";
	}
	
	@Override
	public String getDiscriminator() {
		return discriminator;
	}
	
	@Override
	public List<IRole> getRolesForGuild(String guildID) {
		return roles.getOrDefault(guildID, new ArrayList<>());
	}
	
	/**
	 * CACHES a role to the user.
	 *
	 * @param guildID The guild the role is for.
	 * @param role The role.
	 */
	public void addRole(String guildID, IRole role) {
		if (!roles.containsKey(guildID)) {
			roles.put(guildID, new ArrayList<>());
		}
		
		roles.get(guildID).add(role);
	}
	
	@Override
	public String toString() {
		return mention();
	}
	
	@Override
	public boolean equals(Object other) {
		return this.getClass().isAssignableFrom(other.getClass()) && ((IUser) other).getID().equals(getID());
	}
}
