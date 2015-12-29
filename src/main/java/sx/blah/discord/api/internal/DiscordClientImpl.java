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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.java_websocket.client.WebSocketClient;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.DiscordEndpoints;
import sx.blah.discord.api.Features;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.EventDispatcher;
import sx.blah.discord.handle.impl.events.MessageSendEvent;
import sx.blah.discord.handle.impl.events.MessageUpdateEvent;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.json.requests.*;
import sx.blah.discord.json.responses.*;
import sx.blah.discord.util.HTTP403Exception;
import sx.blah.discord.util.Presences;
import sx.blah.discord.util.Requests;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author qt
 * @since 7:00 PM 16 Aug, 2015
 * Project: DiscordAPI
 * <p>
 * Defines the client.
 * This class receives and
 * sends messages, as well
 * as holds our user data.
 * TODO: Make the code cleaner
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
     * Re-usable instance of Gson.
     */
	protected static final Gson GSON = new GsonBuilder().serializeNulls().create();

    /**
     * Private copy of the email you logged in with.
     */
	protected String email;

    /**
     * Private copy of the password you used to log in.
     */
	protected String password;
	
	/**
	 * The features enabled for this client
	 */
	protected EnumSet<Features> features;

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
	
	/**
     * Used to find urls in order to not escape them
     */
    private static final Pattern urlPattern = Pattern.compile(
            "(?:^|[\\W])((ht|f)tp(s?):\\/\\/|www\\.)"
                    + "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*"
                    + "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
    
    public DiscordClientImpl(String email, String password, EnumSet<Features> features) {
        this.dispatcher = new EventDispatcher(this);
		this.email = email;
		this.password = password;
		this.features = features;
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
            LoginResponse response = GSON.fromJson(Requests.POST.makeRequest(DiscordEndpoints.LOGIN,
                    new StringEntity(GSON.toJson(new LoginRequest(email, password))),
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
            GatewayResponse response = GSON.fromJson(Requests.GET.makeRequest("https://discordapp.com/api/gateway",
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
        if (isReady()) {
            
            //All this weird regex stuff is to prevent any urls from being escaped and therefore breaking them
            List<String> urls = new ArrayList<>();
            Matcher matcher = urlPattern.matcher(content);
            while (matcher.find()) {
                int matchStart = matcher.start(1);
                int matchEnd = matcher.end();
                String url = content.substring(matchStart, matchEnd);
                urls.add(url);
                content = matcher.replaceFirst("@@URL"+(urls.size()-1)+"@@");//Hopefully no one will ever want to send a message with @@URL#@@
            }
            
            content = StringEscapeUtils.escapeJson(content);
            
            for (int i = 0; i < urls.size(); i++) {
                content = content.replace("@@URL"+i+"@@", " "+urls.get(i));
            }
            
            try {
                MessageResponse response = GSON.fromJson(Requests.POST.makeRequest(DiscordEndpoints.CHANNELS + channelID + "/messages",
                        new StringEntity(GSON.toJson(new MessageRequest(content, new String[0])), "UTF-8"),
                        new BasicNameValuePair("authorization", token),
                        new BasicNameValuePair("content-type", "application/json")), MessageResponse.class);
                
                String time = response.timestamp;
                String messageID = response.id;

                Channel channel = getChannelByID(channelID);
                Message message = new Message(messageID, content, this.ourUser, channel, this.convertFromTimestamp(time));
                channel.addMessage(message); //Had to be moved here so that if a message is edited before the MESSAGE_CREATE event, it doesn't error
                DiscordClientImpl.this.dispatcher.dispatch(new MessageSendEvent(message));
            return message;
            } catch (HTTP403Exception e) {
                Discord4J.LOGGER.error("Received 403 error attempting to send message; is your login correct?");
                return null;
            }

        } else {
            Discord4J.LOGGER.error("Bot has not signed in yet!");
            return null;
        }
    }
	
    @Override
    public void editMessage(String content, String messageID, String channelID) {
        if (isReady()) {
    
            content = StringEscapeUtils.escapeJson(content);
            
            Channel channel = getChannelByID(channelID);
            if (channel == null) {
                Discord4J.LOGGER.error("Channel id " +  channelID + " doesn't exist!");
                return;
            }
            
            Message oldMessage = channel.getMessageByID(messageID);
            
            try {
                MessageResponse response = GSON.fromJson(Requests.PATCH.makeRequest(DiscordEndpoints.CHANNELS + channelID + "/messages/" + messageID, 
                        new StringEntity(GSON.toJson(new MessageRequest(content, new String[0])), "UTF-8"),
                        new BasicNameValuePair("authorization", token),
                        new BasicNameValuePair("content-type", "application/json")), MessageResponse.class);
    
                Message newMessage = new Message(response.id, content, this.ourUser, getChannelByID(channelID), 
                        oldMessage.getTimestamp());
                //Event dispatched here because otherwise there'll be an NPE as for some reason when the bot edits a message,
                // the event chain goes like this:
                //Original message edited to null, then the null message edited to the new content
                DiscordClientImpl.this.dispatcher.dispatch(new MessageUpdateEvent(oldMessage, newMessage));
                oldMessage.setContent(content);
            } catch (HTTP403Exception e) {
                Discord4J.LOGGER.error("Received 403 error attempting to send message; is your login correct?");
            }
    
        } else {
            Discord4J.LOGGER.error("Bot has not signed in yet!");
        }
    }
    
    @Override
    public void deleteMessage(String messageID, String channelID) throws IOException {
        if (this.isReady()) {
            try {
                Requests.DELETE.makeRequest(DiscordEndpoints.CHANNELS + channelID + "/messages/" + messageID,
                        new BasicNameValuePair("authorization", token));
            } catch (HTTP403Exception e) {
                Discord4J.LOGGER.error("Received 403 error attempting to delete message; is your login correct?");
            }
        } else {
            Discord4J.LOGGER.error("Bot has not signed in yet!");
        }
    }

    
    @Override
    public void changeAccountInfo(String username, String email, String password) throws UnsupportedEncodingException, URISyntaxException {
        Discord4J.LOGGER.debug("Changing account info.");
        try {
            AccountInfoChangeResponse response = GSON.fromJson(Requests.PATCH.makeRequest(DiscordEndpoints.USERS + "@me",
                    new StringEntity(GSON.toJson(new AccountInfoChangeRequest(email == null || email.isEmpty() ? this.email : email, 
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
        ws.send(GSON.toJson(new PresenceUpdateRequest(isIdle ? System.currentTimeMillis() : null, game.isPresent() ? game.get() : null)));
        
        getOurUser().setPresence(isIdle ? Presences.IDLE : Presences.ONLINE);
        getOurUser().setGame(game.orElse(null));
    }

    /**
     * Gets the last 50 messages from a given channel ID.
     *
     * @param channel The channel to get messages from.
     * @return Last 50 messages from the channel.
     * @throws IOException
     */
    protected void getChannelMessages(Channel channel) throws IOException, HTTP403Exception {
        String response = Requests.GET.makeRequest(DiscordEndpoints.CHANNELS + channel.getID() + "/messages?limit=50",
                new BasicNameValuePair("authorization", token));
        MessageResponse[] messages = GSON.fromJson(response, MessageResponse[].class);

        for (MessageResponse message : messages) {
            channel.addMessage(new Message(message.id,
                    message.content, this.getUserByID(message.author.id), channel, this.convertFromTimestamp(message.timestamp)));
        }
    }

    @Override
    public LocalDateTime convertFromTimestamp(String time) {
        return LocalDateTime.parse(time.split("\\+")[0]).atZone(ZoneId.of("UTC+00:00")).withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
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
            PrivateChannelResponse response = GSON.fromJson(Requests.POST.makeRequest(DiscordEndpoints.USERS + this.ourUser.getID() + "/channels",
                    new StringEntity(GSON.toJson(new PrivateChannelRequest(user.getID()))),
                    new BasicNameValuePair("authorization", this.token),
                    new BasicNameValuePair("content-type", "application/json")), PrivateChannelResponse.class);
           
            PrivateChannel channel = new PrivateChannel(user, response.id);
            privateChannels.add(channel);
            return channel;
        } catch (HTTP403Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Returns a user from raw JSON data.
     */
    private User constructUserFromJSON(String user) {
        UserResponse response = GSON.fromJson(user, UserResponse.class);

        return constructUserFromJSON(response);
    }
    
    /**
     * Returns a user from the java form of the raw JSON data.
     */
    protected User constructUserFromJSON(UserResponse response) {
        User ourUser = new User(response.username, response.id, response.avatar);
        ourUser.setPresence(Presences.ONLINE);
    
        return ourUser;
    }
}
