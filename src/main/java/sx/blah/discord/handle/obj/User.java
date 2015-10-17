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

import sx.blah.discord.DiscordEndpoints;
import sx.blah.discord.util.Presences;

/**
 * @author qt
 * @since 5:40 PM 15 Aug, 2015
 * Project: DiscordAPI
 * <p>
 * This class defines the Discord user.
 */
public class User {
    /**
     * Display name of the user.
     */
    private String name;

    /**
     * The user's avatar location.
     */
    private String avatar;

    /**
     * User ID.
     */
    private final String id;

    /**
     * User discriminator.
     * Distinguishes users with the same name.
     * <p>
     * This is here in case it becomes necessary.
     */
    private int discriminator;

    /**
     * This user's presence.
     * One of [online/idle/offline].
     */
    private Presences presence;

	/**
	 * The user's avatar in URL form.
	 */
	private String avatarURL;

    public User(String name, String id, String avatar) {
	    this.id = id;
	    this.name = name;
	    this.avatar = avatar;
	    this.avatarURL = String.format(DiscordEndpoints.AVATARS, this.id, this.avatar);
    }

    // -- Getters and setters. Pretty boring.

    public String getID() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getAvatarURL() {
		return avatarURL;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
	    this.avatarURL = String.format(DiscordEndpoints.AVATARS, this.id, this.avatar);
    }

    public Presences getPresence() {
        return presence;
    }

    public void setPresence(Presences presence) {
        this.presence = presence;
    }

    // STOLEN: idea from hydrabolt :P
    public String mention() {
        return "<@" + id + ">";
    }

    @Override public String toString() {
        return mention();
    }
}
