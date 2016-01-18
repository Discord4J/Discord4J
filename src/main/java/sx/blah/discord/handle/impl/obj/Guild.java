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

import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.DiscordEndpoints;
import sx.blah.discord.api.DiscordException;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.internal.DiscordUtils;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.json.generic.RoleResponse;
import sx.blah.discord.json.requests.ChannelCreateRequest;
import sx.blah.discord.json.requests.EditGuildRequest;
import sx.blah.discord.json.requests.MemberEditRequest;
import sx.blah.discord.json.responses.ChannelResponse;
import sx.blah.discord.json.responses.GuildResponse;
import sx.blah.discord.json.responses.UserResponse;
import sx.blah.discord.util.HTTP403Exception;
import sx.blah.discord.util.Requests;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Guild implements IGuild {
	/**
	 * All text channels in the guild.
	 */
	protected final List<IChannel> channels;
	
	/**
	 * All voice channels in the guild.
	 */
	protected final List<IVoiceChannel> voiceChannels;
	
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
	protected String ownerID;
	
	/**
	 * The roles the guild contains.
	 */
	protected final List<IRole> roles;
	
	/**
	 * The channel where those who are afk are moved to.
	 */
	protected String afkChannel;
	/**
	 * The time in seconds for a user to be idle to be determined as "afk".
	 */
	protected int afkTimeout;
	
	/**
	 * The region this guild is located in.
	 */
	protected String regionID;
	
	/**
	 * The client that created this object.
	 */
	protected final IDiscordClient client;
	
	public Guild(IDiscordClient client, String name, String id, String icon, String ownerID, String afkChannel, int afkTimeout, String region) {
		this(client, name, id, icon, ownerID, afkChannel, afkTimeout, region, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
	}
	
	public Guild(IDiscordClient client, String name, String id, String icon, String ownerID, String afkChannel, int afkTimeout, String region, List<IRole> roles, List<IChannel> channels, List<IVoiceChannel> voiceChannels, List<IUser> users) {
		this.client = client;
		this.name = name;
		this.voiceChannels = voiceChannels;
		this.channels = channels;
		this.users = users;
		this.id = id;
		this.icon = icon;
		this.iconURL = String.format(DiscordEndpoints.ICONS, this.id, this.icon);
		this.ownerID = ownerID;
		this.roles = roles;
		this.afkChannel = afkChannel;
		this.afkTimeout = afkTimeout;
		this.regionID = region;
	}
	
	@Override
	public String getOwnerID() {
		return ownerID;
	}
	
	@Override
	public IUser getOwner() {
		return client.getUserByID(ownerID);
	}
	
	/**
	 * Sets the CACHED owner id.
	 * 
	 * @param id The user if of the new owner.
	 */
	public void setOwnerID(String id) {
		ownerID = id;
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
		if (null == users)
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
	public IRole getRoleForID(String id) {
		for (IRole role : roles) {
			if (role.getID().equals(id))
				return role;
		}
		return null;
	}
	
	@Override
	public List<IVoiceChannel> getVoiceChannels() {
		return voiceChannels;
	}
	
	@Override
	public IVoiceChannel getVoiceChannelForID(String id) {
		for (IVoiceChannel channel : voiceChannels)
			if (channel.getID().equals(id))
				return channel;
		
		return null;
	}
	
	@Override
	public IVoiceChannel getAFKChannel() {
		return getVoiceChannelForID(afkChannel);
	}
	
	@Override
	public int getAFKTimeout() {
		return afkTimeout;
	}
	
	public void setAFKChannel(String id) {
		this.afkChannel = id;
	}
	
	public void setAfkTimeout(int timeout) {
		this.afkTimeout = timeout;
	}
	
	public void addVoiceChannel(IVoiceChannel channel) {
		voiceChannels.add(channel);
	}
	
	@Override
	public IRole createRole() throws HTTP403Exception {
		RoleResponse response = DiscordUtils.GSON.fromJson(Requests.POST.makeRequest(DiscordEndpoints.SERVERS + id + "/roles",
				new BasicNameValuePair("authorization", client.getToken())), RoleResponse.class);
		IRole role = DiscordUtils.getRoleFromJSON(this, response);
		return role;
	}
	
	@Override
	public List<IUser> getBannedUsers() throws HTTP403Exception {
		UserResponse[] users = DiscordUtils.GSON.fromJson(Requests.GET.makeRequest(DiscordEndpoints.SERVERS + id + "/bans",
				new BasicNameValuePair("authorization", client.getToken())), UserResponse[].class);
		List<IUser> banned = new ArrayList<>();
		for (UserResponse user : users) {
			banned.add(DiscordUtils.getUserFromJSON(client, user));
		}
		return banned;
	}
	
	@Override
	public void banUser(String userID) throws HTTP403Exception {
		banUser(userID, 0);
	}
	
	@Override
	public void banUser(String userID, int deleteMessagesForDays) throws HTTP403Exception {
		Requests.PUT.makeRequest(DiscordEndpoints.SERVERS + id + "/bans/" + userID + "?delete-message-days=" + deleteMessagesForDays,
				new BasicNameValuePair("authorization", client.getToken()));
	}
	
	@Override
	public void pardonUser(String userID) throws HTTP403Exception {
		Requests.DELETE.makeRequest(DiscordEndpoints.SERVERS + id + "/bans/" + userID,
				new BasicNameValuePair("authorization", client.getToken()));
	}
	
	@Override
	public void kickUser(String userID) throws HTTP403Exception {
		Requests.DELETE.makeRequest(DiscordEndpoints.SERVERS + id + "/members/" + userID,
				new BasicNameValuePair("authorization", client.getToken()));
	}
	
	@Override
	public void editUserRoles(String userID, String[] roleIDs) throws HTTP403Exception {
		try {
			Requests.PATCH.makeRequest(DiscordEndpoints.SERVERS + id + "/members/" + userID,
					new StringEntity(DiscordUtils.GSON.toJson(new MemberEditRequest(roleIDs))),
					new BasicNameValuePair("authorization", client.getToken()),
					new BasicNameValuePair("content-type", "application/json"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void edit(Optional<String> name, Optional<String> regionID, Optional<IDiscordClient.Image> icon, Optional<String> afkChannelID, Optional<Integer> afkTimeout) throws HTTP403Exception {
		try {
			GuildResponse response = DiscordUtils.GSON.fromJson(Requests.PATCH.makeRequest(DiscordEndpoints.SERVERS + id,
					new StringEntity(DiscordUtils.GSON.toJson(new EditGuildRequest(name.orElse(this.name), regionID.orElse(this.regionID),
							icon.isPresent() ? icon.get().getData() : this.icon, afkChannelID.orElse(null), afkTimeout.orElse(this.afkTimeout)))),
					new BasicNameValuePair("authorization", client.getToken()), 
					new BasicNameValuePair("content-type", "application/json")), GuildResponse.class);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void deleteOrLeaveGuild() throws HTTP403Exception {
		Requests.DELETE.makeRequest(DiscordEndpoints.SERVERS + id,
				new BasicNameValuePair("authorization", client.getToken()));
	}
	
	@Override
	public IChannel createChannel(String name) throws DiscordException, HTTP403Exception {
		if (!client.isReady()) {
			Discord4J.LOGGER.error("Bot has not signed in yet!");
			return null;
		}
		
		if (name == null || name.length() < 2 || name.length() > 100)
			throw new DiscordException("Channel name can only be between 2 and 100 characters!");
		try {
			ChannelResponse response = DiscordUtils.GSON.fromJson(Requests.POST.makeRequest(DiscordEndpoints.SERVERS+getID()+"/channels",
					new StringEntity(DiscordUtils.GSON.toJson(new ChannelCreateRequest(name, "text"))),
					new BasicNameValuePair("authorization", client.getToken()),
					new BasicNameValuePair("content-type", "application/json")),
					ChannelResponse.class);
			
			IChannel channel = DiscordUtils.getChannelFromJSON(client, this, response);
			addChannel(channel);
			
			return channel;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public IVoiceChannel createVoiceChannel(String name) throws DiscordException, HTTP403Exception {
		if (!client.isReady()) {
			Discord4J.LOGGER.error("Bot has not signed in yet!");
			return null;
		}
		
		if (name == null || name.length() < 2 || name.length() > 100)
			throw new DiscordException("Channel name can only be between 2 and 100 characters!");
		try {
			ChannelResponse response = DiscordUtils.GSON.fromJson(Requests.POST.makeRequest(DiscordEndpoints.SERVERS+getID()+"/channels",
					new StringEntity(DiscordUtils.GSON.toJson(new ChannelCreateRequest(name, "voice"))),
					new BasicNameValuePair("authorization", client.getToken()),
					new BasicNameValuePair("content-type", "application/json")),
					ChannelResponse.class);
			
			IVoiceChannel channel = DiscordUtils.getVoiceChannelFromJSON(client, this, response);
			addVoiceChannel(channel);
			
			return channel;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public IRegion getRegion() {
		return client.getRegionForID(regionID);
	}
	
	/**
	 * CACHES the region for this guild.
	 * 
	 * @param regionID The region.
	 */
	public void setRegion(String regionID) {
		this.regionID = regionID;
	}
	
	@Override
	public boolean equals(Object other) {
		return this.getClass().isAssignableFrom(other.getClass()) && ((IGuild) other).getID().equals(getID());
	}
}
