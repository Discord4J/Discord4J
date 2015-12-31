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

package sx.blah.discord.handle.obj;

import sx.blah.discord.api.DiscordEndpoints;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.util.Presences;

import java.util.Optional;

/**
 * This class defines the Discord user.
 */
public class User {
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
     * TODO: implement
     */
    protected int discriminator;

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
     * The client that created this object.
     */
    protected final IDiscordClient client;

    public User(IDiscordClient client, String name, String id, String avatar) {
	    this.client = client;
        this.id = id;
	    this.name = name;
	    this.avatar = avatar;
	    this.avatarURL = String.format(DiscordEndpoints.AVATARS, this.id, this.avatar);
    }

	/**
     * Gets the user's unique id.
     * 
     * @return The user's id.
     */
    public String getID() {
        return id;
    }
	
	/**
     * Gets the user's username.
     * 
     * @return The username.
     */
    public String getName() {
        return name;
    }
	
	/**
     * Gets the game the user is playing, no value if the user isn't playing a game.
     * 
     * @return The game.
     */
    public Optional<String> getGame() {
        return game;
    }
	
	/**
     * Sets the user's CACHED game.
     * 
     * @param game The game.
     */
	@Deprecated
    public void setGame(Optional<String> game) {
        this.game = game;
    }
	
	/**
     * Sets the user's CACHED username.
     * 
     * @param name The username.
     */
	@Deprecated
    public void setName(String name) {
        this.name = name;
    }
	
	/**
     * Gets the user's avatar id.
     * 
     * @return The avatar id.
     */
    public String getAvatar() {
        return avatar;
    }
	
	/**
     * Gets the user's avatar direct link.
     * 
     * @return The avatar url.
     */
    public String getAvatarURL() {
		return avatarURL;
    }
	
	/**
     * Sets the user's CACHED avatar id.
     * @param avatar The user's avatar id.
     */
	@Deprecated
    public void setAvatar(String avatar) {
        this.avatar = avatar;
	    this.avatarURL = String.format(DiscordEndpoints.AVATARS, this.id, this.avatar);
    }
	
	/**
     * Gets the user's presence.
     * 
     * @return The user's presence.
     */
    public Presences getPresence() {
        return presence;
    }
	
	/**
     * Sets the CACHED presence of the user.
     * 
     * @param presence The new presence.
     */
	@Deprecated
    public void setPresence(Presences presence) {
        this.presence = presence;
    }
	
	/**
     * Formats a string to @mention the user.
     * 
     * @return The formatted string.
     */
    public String mention() {
        return "<@" + id + ">";
    }

    @Override 
    public String toString() {
        return mention();
    }
}
