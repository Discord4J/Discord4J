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
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;

import java.util.ArrayList;
import java.util.List;

public class Guild implements IGuild {
    /**
     * All text channels in the guild.
     */
    protected final List<IChannel> channels;

    /**
     * All users connected to the guild.
     */
    protected final List<IUser> users;

    /**
     * The name of the guild.
     */
    protected String name;

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
     * The roles the guild contains.
     */
    protected final List<IRole> roles;
    
    /**
     * The client that created this object.
     */
    protected final IDiscordClient client;

    public Guild(IDiscordClient client, String name, String id, String icon, String ownerID) {
        this(client, name, id, icon, ownerID, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    public Guild(IDiscordClient client, String name, String id, String icon, String ownerID, List<IRole> roles, List<IChannel> channels, List<IUser> users) {
        this.client = client;
        this.name = name;
        this.channels = channels;
        this.users = users;
        this.id = id;
        this.icon = icon;
        this.iconURL = String.format(DiscordEndpoints.ICONS, this.id, this.icon);
        this.ownerID = ownerID;
        this.roles = roles;
    }
	
	@Override
    public String getOwnerID() {
        return ownerID;
    }
	
	@Override
    public IUser getOwner() {
        return client.getUserByID(ownerID);
    }
	
	@Override
    public String getIcon() {
        return icon;
    }
	
	@Override
    public String getIconURL() {
        return iconURL;
    }
	
	/**
     * Sets the CACHED icon id for the guild.
     * 
     * @param icon The icon id.
     */
    public void setIcon(String icon) {
        this.icon = icon;
        this.iconURL = String.format(DiscordEndpoints.ICONS, this.id, this.icon);
    }

    @Override
    public List<IChannel> getChannels() {
        return channels;
    }

    @Override
    public IChannel getChannelByID(String id) {
        for (IChannel c : channels) {
            if (c.getID().equalsIgnoreCase(id))
                return c;
        }

        return null; // Not found, return null.
    }

    @Override
    public List<IUser> getUsers() {
        return users;
    }

    @Override
    public IUser getUserByID(String id) {
	    if(null == users)
		    return null;
        for (IUser user : users) {
            if (null != user
		            && null != user.getID()
                    && user.getID().equalsIgnoreCase(id))
                return user;
        }

        return null; // Not found, return null.
    }

    @Override
    public String getName() {
        return name;
    }
	
	/**
     * Sets the CACHED name of the guild.
     * 
     * @param name The name.
     */
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getID() {
        return id;
    }
	
	/**
     * CACHES a user to the guild.
     * 
     * @param user The user.
     */
    public void addUser(IUser user) {
		this.users.add(user);
    }
	
	/**
     * CACHES a channel to the guild.
     * 
     * @param channel The channel.
     */
	public void addChannel(IChannel channel) {
		this.channels.add(channel);
	}
	
	@Override
    public List<IRole> getRoles() {
        return roles;
    }
	
	/**
     * CACHES a role to the guild.
     * 
     * @param role The role.
     */
    public void addRole(IRole role) {
        this.roles.add(role);
    }
	
	@Override
    public IRole getRoleForId(String id) {
        for (IRole role : roles) {
            if (role.getId().equals(id))
                return role;
        }
        return null;
    }
    
    @Override
    public boolean equals(Object other) {
        return this.getClass().isAssignableFrom(other.getClass()) && ((IGuild) other).getID().equals(getID());
    }
}
