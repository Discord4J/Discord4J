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

import java.util.ArrayList;
import java.util.List;

/**
 * @author qt
 * @since 4:05 PM 17 Aug, 2015
 * Project: DiscordAPI
 * <p>
 * This class defines a guild/server/clan/whatever it's called.
 */
public class Guild {
    /**
     * All text channels in the guild.
     */
    private final List<Channel> channels;

    /**
     * All users connected to the guild.
     */
    private final List<User> users;

    /**
     * The name of the guild.
     */
    private final String name;

    /**
     * The ID of this guild.
     */
    private final String id;
	
	/**
     * The location of the guild icon
     */
    private String icon;
	
	/**
     * The url pointing to the guild icon
     */
    private String iconURL;
	
	/**
     * The user id for the owner of the guild
     */
    private final String ownerID;

    public Guild(String name, String id, String icon, String ownerID) {
        this.name = name;
        this.id = id;
        this.channels = new ArrayList<>();
        this.users = new ArrayList<>();
        this.icon = icon;
        this.iconURL = String.format(DiscordEndpoints.ICONS, this.id, this.icon);
        this.ownerID = ownerID;
    }

    public Guild(String name, String id, String icon, String ownerID, List<Channel> channels, List<User> users) {
        this.name = name;
        this.channels = channels;
        this.users = users;
        this.id = id;
        this.icon = icon;
        this.iconURL = String.format(DiscordEndpoints.ICONS, this.id, this.icon);
        this.ownerID = ownerID;
    }
    
    public String getOwnerID() {
        return ownerID;
    }
    
    public String getIcon() {
        return icon;
    }
    
    public String getIconURL() {
        return iconURL;
    }
    
    public void setIcon(String icon) {
        this.icon = icon;
        this.iconURL = String.format(DiscordEndpoints.ICONS, this.id, this.icon);
    }

    /**
     * @return All channels on the server.
     */
    public List<Channel> getChannels() {
        return channels;
    }

    /**
     * @param id The ID of the channel you want to find.
     * @return The channel with given ID.
     */
    public Channel getChannelByID(String id) {
        for (Channel c : channels) {
            if (c.getID().equalsIgnoreCase(id))
                return c;
        }

        return null; // Not found, return null.
    }

    /**
     * @return All users connected to the guild.
     */
    public List<User> getUsers() {
        return users;
    }

    /**
     * @param id ID of the user you want to find.
     * @return The user with given ID.
     */
    public User getUserByID(String id) {
	    if(null == users)
		    return null;
        for (User user : users) {
            if (null != user
		            && null != user.getID()
                    && user.getID().equalsIgnoreCase(id))
                return user;
        }

        return null; // Not found, return null.
    }

    /**
     * @return The name of the guild
     */
    public String getName() {
        return name;
    }

    /**
     * @return The ID of this guild.
     */
    public String getID() {
        return id;
    }

    public void addUser(User user) {
		this.users.add(user);
    }

	public void addChannel(Channel channel) {
		this.channels.add(channel);
	}
}
