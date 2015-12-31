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

import java.util.ArrayList;
import java.util.List;

/**
 * This class defines a guild/server/clan/whatever it's called.
 */
public class Guild {
    /**
     * All text channels in the guild.
     */
    protected final List<Channel> channels;

    /**
     * All users connected to the guild.
     */
    protected final List<User> users;

    /**
     * The name of the guild.
     */
    protected final String name;

    /**
     * The ID of this guild.
     */
    protected final String id;
	
	/**
     * The location of the guild icon
     */
    protected String icon;
	
	/**
     * The url pointing to the guild icon
     */
    protected String iconURL;
	
	/**
     * The user id for the owner of the guild
     */
    protected final String ownerID;
    
    /**
     * The client that created this object.
     */
    protected final IDiscordClient client;

    public Guild(IDiscordClient client, String name, String id, String icon, String ownerID) {
        this.client = client;
        this.name = name;
        this.id = id;
        this.channels = new ArrayList<>();
        this.users = new ArrayList<>();
        this.icon = icon;
        this.iconURL = String.format(DiscordEndpoints.ICONS, this.id, this.icon);
        this.ownerID = ownerID;
    }

    public Guild(IDiscordClient client, String name, String id, String icon, String ownerID, List<Channel> channels, List<User> users) {
        this.client = client;
        this.name = name;
        this.channels = channels;
        this.users = users;
        this.id = id;
        this.icon = icon;
        this.iconURL = String.format(DiscordEndpoints.ICONS, this.id, this.icon);
        this.ownerID = ownerID;
    }
	
	/**
     * Gets the user id for the owner of this guild.
     * 
     * @return The owner id.
     */
    public String getOwnerID() {
        return ownerID;
    }
	
	/**
     * Gets the user object for the owner of this guild.
     * 
     * @return The owner.
     */
    public User getOwner() {
        return client.getUserByID(ownerID);
    }
	
	/**
     * Gets the icon id for this guild.
     * 
     * @return The icon id.
     */
    public String getIcon() {
        return icon;
    }
	
	/**
     * Gets the direct link to the guild's icon.
     * 
     * @return The icon url.
     */
    public String getIconURL() {
        return iconURL;
    }
	
	/**
     * Sets the CACHED icon id for the guild.
     * 
     * @param icon The icon id.
     */
    @Deprecated
    public void setIcon(String icon) {
        this.icon = icon;
        this.iconURL = String.format(DiscordEndpoints.ICONS, this.id, this.icon);
    }

    /**
     * Gets all the channels on the server.
     * 
     * @return All channels on the server.
     */
    public List<Channel> getChannels() {
        return channels;
    }

    /**
     * Gets a channel on the guild by a specific channel id.
     * 
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
     * Gets all the users connected to the guild.
     * 
     * @return All users connected to the guild.
     */
    public List<User> getUsers() {
        return users;
    }

    /**
     * Gets a user by its id in the guild.
     * 
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
     * Gets the name of the guild.
     * 
     * @return The name of the guild
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the id of the guild.
     * 
     * @return The ID of this guild.
     */
    public String getID() {
        return id;
    }
	
	/**
     * CACHES a user to the guild.
     * 
     * @param user The user.
     */
    @Deprecated
    public void addUser(User user) {
		this.users.add(user);
    }
	
	/**
     * CACHES a channel to the guild.
     * 
     * @param channel The channel.
     */
    @Deprecated
	public void addChannel(Channel channel) {
		this.channels.add(channel);
	}
    
    
    @Override
    public boolean equals(Object other) {
        return this.getClass().isAssignableFrom(other.getClass()) && ((Guild) other).getID().equals(getID());
    }
}
