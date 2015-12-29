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

package sx.blah.discord.api.internal;

import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.java_websocket.client.WebSocketClient;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.DiscordEndpoints;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.EventDispatcher;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.json.requests.AccountInfoChangeRequest;
import sx.blah.discord.json.requests.LoginRequest;
import sx.blah.discord.json.requests.PresenceUpdateRequest;
import sx.blah.discord.json.requests.PrivateChannelRequest;
import sx.blah.discord.json.responses.AccountInfoChangeResponse;
import sx.blah.discord.json.responses.GatewayResponse;
import sx.blah.discord.json.responses.LoginResponse;
import sx.blah.discord.json.responses.PrivateChannelResponse;
import sx.blah.discord.util.HTTP403Exception;
import sx.blah.discord.util.Presences;
import sx.blah.discord.util.Requests;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author qt
 * @since 7:00 PM 16 Aug, 2015
 * Project: DiscordAPI
 * <p>
 * Defines the client.
 * This class receives and
 * sends messages, as well
 * as holds our user data.
 */
public final class DiscordClientImpl implements IDiscordClient {
    /**
     * Used for keep alive. Keeps last time (in ms)
     * that we sent the keep alive so we can accurately
     * time our keep alive messages.
     */
    protected long timer = System.currentTimeMillis();

    /**
     * User we are logged in as
     */
    protected User ourUser;

    /**
     * Our token, so we can send XHR to Discord.
     */
	protected String token;

    /**
     * Time (in ms) between keep alive
     * messages.
     */
	protected long heartbeat;

    /**
     * Local copy of all guilds/servers.
     */
	protected final List<Guild> guildList = new ArrayList<>();
    
    /**
     * Private copy of the email you logged in with.
     */
	protected String email;

    /**
     * Private copy of the password you used to log in.
     */
	protected String password;

    /**
     * WebSocket over which to communicate with Discord.
     */
	protected WebSocketClient ws;

    /**
     * Event dispatcher.
     */
	protected EventDispatcher dispatcher;

    /**
     * All of the private message channels that the bot is connected to.
     */
    protected final List<PrivateChannel> privateChannels = new ArrayList<>();
	
	/**
     * Whether the api is logged in.
     */
    protected boolean isReady = false;
    
    public DiscordClientImpl(String email, String password) {
        this.dispatcher = new EventDispatcher(this);
		this.email = email;
		this.password = password;
    }
    
    @Override
    public EventDispatcher getDispatcher() {
        return dispatcher;
    }

    @Override
    public String getToken() {
        return token;
    }

    @Override
    public void login() throws IOException, URISyntaxException {
        if (null != ws) {
            ws.close();
        }

        try {
            LoginResponse response = DiscordUtils.GSON.fromJson(Requests.POST.makeRequest(DiscordEndpoints.LOGIN,
                    new StringEntity(DiscordUtils.GSON.toJson(new LoginRequest(email, password))),
                    new BasicNameValuePair("content-type", "application/json")), LoginResponse.class);
            this.token = response.token;
        } catch (HTTP403Exception e) {
            e.printStackTrace();
        }

        this.ws = new DiscordWS(this, new URI(obtainGateway(this.token)));
    }

    /**
     * Gets the WebSocket gateway
     *
     * @param token Our login token
     * @return the WebSocket URL of which to connect
     */
    private String obtainGateway(String token) {
        String gateway = null;
        try {
            GatewayResponse response = DiscordUtils.GSON.fromJson(Requests.GET.makeRequest("https://discordapp.com/api/gateway",
                    new BasicNameValuePair("authorization", token)), GatewayResponse.class);
            gateway = response.url.replaceAll("wss", "ws");
        } catch (HTTP403Exception e) {
            Discord4J.LOGGER.error("Received 403 error attempting to get gateway; is your login correct?");
        }
        Discord4J.LOGGER.debug("Obtained gateway {}.", gateway);
        return gateway;
    }

    @Override
    public Message sendMessage(String content, String channelID) throws IOException {
        Channel channel = getChannelByID(channelID);
        if (channel == null) {
            Discord4J.LOGGER.error("Channel id " +  channelID + " doesn't exist!");
            return null;
        }
        return channel.sendMessage(content);
    }
	
    @Override
    public Message editMessage(String content, String messageID, String channelID) {
        Channel channel = getChannelByID(channelID);
        if (channel == null) {
            Discord4J.LOGGER.error("Channel id " +  channelID + " doesn't exist!");
            return null;
        }
        
        Message message = channel.getMessageByID(messageID);
        if (message == null) {
            Discord4J.LOGGER.error("Message id " +  messageID + " doesn't exist!");
            return null;
        }
        
        return message.edit(content);
    }
    
    @Override
    public void deleteMessage(String messageID, String channelID) throws IOException {
        Channel channel = getChannelByID(channelID);
        if (channel == null) {
            Discord4J.LOGGER.error("Channel id " +  channelID + " doesn't exist!");
            return;
        }
        
        Message message = channel.getMessageByID(messageID);
        if (message == null) {
            Discord4J.LOGGER.error("Message id " +  messageID + " doesn't exist!");
            return;
        }
        
        message.delete();
    }

    
    @Override
    public void changeAccountInfo(String username, String email, String password) throws UnsupportedEncodingException, URISyntaxException {
        Discord4J.LOGGER.debug("Changing account info.");
        try {
            AccountInfoChangeResponse response = DiscordUtils.GSON.fromJson(Requests.PATCH.makeRequest(DiscordEndpoints.USERS + "@me",
                    new StringEntity(DiscordUtils.GSON.toJson(new AccountInfoChangeRequest(email == null || email.isEmpty() ? this.email : email, 
							this.password, password, username == null || username.isEmpty() ? ourUser.getName() : username,
							ourUser.getAvatar()))),
                    new BasicNameValuePair("Authorization", token),
                    new BasicNameValuePair("content-type", "application/json; charset=UTF-8")), AccountInfoChangeResponse.class);
            
			if (!this.token.equals(response.token)) {
				Discord4J.LOGGER.debug("Token changed, reopening the websocket.");
				this.token = response.token;
				((DiscordWS) this.ws).disconnect();
				this.ws = new DiscordWS(this, new URI(obtainGateway(this.token)));
			}
        } catch (HTTP403Exception e) {
            Discord4J.LOGGER.error("Received 403 error attempting to change account details; is your login correct?");
        }
    }
	
    @Override
    public void updatePresence(boolean isIdle, Optional<String> game) {
        ws.send(DiscordUtils.GSON.toJson(new PresenceUpdateRequest(isIdle ? System.currentTimeMillis() : null, game.isPresent() ? game.get() : null)));
        
        getOurUser().setPresence(isIdle ? Presences.IDLE : Presences.ONLINE);
        getOurUser().setGame(game.orElse(null));
    }
    
    @Override
    public boolean isReady() {
        return isReady && ws != null;
    }

    @Override
    public User getOurUser() {
        return ourUser;
    }

    @Override
    public Channel getChannelByID(String id) {
        for (Guild guild : guildList) {
            for (Channel channel : guild.getChannels()) {
                if (channel.getID().equalsIgnoreCase(id))
                    return channel;
            }
        }

        for(PrivateChannel channel : privateChannels) {
            if(channel.getID().equalsIgnoreCase(id))
                return channel;
        }

        return null;
    }

    @Override
    public Guild getGuildByID(String guildID) {
        for (Guild guild : guildList) {
            if (guild.getID().equalsIgnoreCase(guildID))
                return guild;
        }

        return null;
    }

    @Override
    public List<Guild> getGuilds() {
        return guildList;
    }
    
    @Override
	public User getUserByID(String userID) {
        User u = null;
        for (Guild guild : guildList) {
            if (null == u) {
                u = guild.getUserByID(userID);
            }
        }

        return u;
    }

    @Override
    public PrivateChannel getOrCreatePMChannel(User user) throws Exception {
        for(PrivateChannel channel : privateChannels) {
            if(channel.getRecipient().getID().equalsIgnoreCase(user.getID())) {
                return channel;
            }
        }

        try {
            PrivateChannelResponse response = DiscordUtils.GSON.fromJson(Requests.POST.makeRequest(DiscordEndpoints.USERS + this.ourUser.getID() + "/channels",
                    new StringEntity(DiscordUtils.GSON.toJson(new PrivateChannelRequest(user.getID()))),
                    new BasicNameValuePair("authorization", this.token),
                    new BasicNameValuePair("content-type", "application/json")), PrivateChannelResponse.class);
           
            PrivateChannel channel = new PrivateChannel(this, user, response.id);
            privateChannels.add(channel);
            return channel;
        } catch (HTTP403Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
