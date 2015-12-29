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

package sx.blah.discord.api;

import com.google.gson.*;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import sx.blah.discord.Discord4J;
import sx.blah.discord.handle.EventDispatcher;
import sx.blah.discord.handle.impl.events.*;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.json.requests.*;
import sx.blah.discord.json.responses.*;
import sx.blah.discord.json.responses.events.*;
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
public final class DiscordClient {
    /**
     * Used for keep alive. Keeps last time (in ms)
     * that we sent the keep alive so we can accurately
     * time our keep alive messages.
     */
    private long timer = System.currentTimeMillis();

    /**
     * User we are logged in as
     */
    private User ourUser;

    /**
     * Our token, so we can send XHR to Discord.
     */
    private String token;

    /**
     * Time (in ms) between keep alive
     * messages.
     */
    private long heartbeat;

    /**
     * Local copy of all guilds/servers.
     */
    private final List<Guild> guildList = new ArrayList<>();
	
	/**
     * Re-usable instance of Gson.
     */
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    /**
     * Private copy of the email you logged in with.
     */
    private String email;

    /**
     * Private copy of the password you used to log in.
     */
    private String password;
	
	/**
	 * The features enabled for this client
	 */
	private EnumSet<Features> features;

    /**
     * WebSocket over which to communicate with Discord.
     */
    private WebSocketClient ws;

    /**
     * Event dispatcher.
     */
    private EventDispatcher dispatcher;

    /**
     * All of the private message channels that the bot is connected to.
     */
    private final List<PrivateChannel> privateChannels = new ArrayList<>();

    protected DiscordClient(String email, String password, EnumSet<Features> features) {
        this.dispatcher = new EventDispatcher(this);
		this.email = email;
		this.password = password;
		this.features = features;
    }

    /**
     * @return the event dispatcher. This will send events to all listeners
     */
    public EventDispatcher getDispatcher() {
        return dispatcher;
    }

    /**
     * @return Token, used for all XHR requests.
     */
    public String getToken() {
        return token;
    }

    /**
     * Logs you into Discord using given
     * email and password. Also starts the bot.
     * <p>
     * Very important to use this method.
     */
    public void login()
            throws IOException, URISyntaxException {
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

    /**
     * Sends a message to the specified channel.
     *
     * @param content   The actual message to send
     * @param channelID The channel to send the message to
     * @return The message that was sent.
     * @throws IOException
     */
    public Message sendMessage(String content, String channelID) throws IOException {
        if (null != ws) {

            content = StringEscapeUtils.escapeJson(content);
            
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
                DiscordClient.this.dispatcher.dispatch(new MessageSendEvent(message));
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
	
	/**
     * Edits a specified message. Currently, Discord only allows you to edit your own message
     * 
     * @param content   The new content of the message
     * @param messageID The id of the message to edit
     * @param channelID The channel the message exists in
     */
    public void editMessage(String content, String messageID, String channelID) {
        if (null != ws) {
    
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
                DiscordClient.this.dispatcher.dispatch(new MessageUpdateEvent(oldMessage, newMessage));
                oldMessage.setContent(content);
            } catch (HTTP403Exception e) {
                Discord4J.LOGGER.error("Received 403 error attempting to send message; is your login correct?");
            }
    
        } else {
            Discord4J.LOGGER.error("Bot has not signed in yet!");
        }
    }
    
    /**
     * Deletes a message with given ID from provided channel ID.
     *
     * @param messageID Message (ID) to delete.
     * @param channelID Channel to delete it from.
     * @throws IOException
     */
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

    /**
     * FIXME: Fix this because it's fucking stupid.
     * Allows you to change the info on your bot.
     * Any fields you don't want to change should be left as an empty string ("")
     *
     * @param username Username (if you want to change it).
     * @param email    Email (if you want to change it)
     * @param password Password (if you want to change it).
     */
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
	
	/**
     * Changes the bot's presence
     * 
     * @param isIdle Set to true to make the bot idle or false for it to be online
     * @param game The (optional) game the game the bot is playing
     */
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
    private void getChannelMessages(Channel channel) throws IOException, HTTP403Exception {
        String response = Requests.GET.makeRequest(DiscordEndpoints.CHANNELS + channel.getID() + "/messages?limit=50",
                new BasicNameValuePair("authorization", token));
        MessageResponse[] messages = GSON.fromJson(response, MessageResponse[].class);

        for (MessageResponse message : messages) {
            channel.addMessage(new Message(message.id,
                    message.content, this.getUserByID(message.author.id), channel, this.convertFromTimestamp(message.timestamp)));
        }
    }

    public LocalDateTime convertFromTimestamp(String time) {
        return LocalDateTime.parse(time.split("\\+")[0]).atZone(ZoneId.of("UTC+00:00")).withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * @return Whether or not the bot is ready to be used (is it logged in?)
     */
    public boolean isReady() {
        return null != ws;
    }

    /**
     * @return The user the bot has signed in as.
     */
    public User getOurUser() {
        return ourUser;
    }

    /**
     * Finds channel by given ID.
     *
     * @param id ID of the channel to find.
     * @return The channel.
     */
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

    /**
     * Gets a guild object from a given guild ID.
     * @param guildID
     * @return
     */
    public Guild getGuildByID(String guildID) {
        for (Guild guild : guildList) {
            if (guild.getID().equalsIgnoreCase(guildID))
                return guild;
        }

        return null;
    }

    /**
     * Gets all the guilds this client is connected to.
     *
     * @return The guilds.
     */
    public List<Guild> getGuilds() {
        return guildList;
    }

    @Deprecated public User getUserByID(String userID) {
        User u = null;
        for (Guild guild : guildList) {
            if (null == u) {
                u = guild.getUserByID(userID);
            }
        }

        return u;
    }

    /**
     * Gets a PM channel for a user; if one doesn't exist, it will be created.
     * @param user
     * @return
     */
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
    private User constructUserFromJSON(UserResponse response) {
        User ourUser = new User(response.username, response.id, response.avatar);
        ourUser.setPresence(Presences.ONLINE);
    
        return ourUser;
    }

    class DiscordWS extends WebSocketClient {
		
		private DiscordClient client;
		public boolean isConnected = true;
		
        public DiscordWS(DiscordClient client, URI serverURI) {
            super(serverURI);
			this.client = client;
            this.connect();
        }
		
		public void disconnect() {
			isConnected = false;
			close();
		}

        @Override
        public void onOpen(ServerHandshake serverHandshake) {
            if (!token.isEmpty()) {
				send(GSON.toJson(new ConnectRequest(token, "Java", "Discord4J", "Discord4J", "", "")));
                Discord4J.LOGGER.debug("Connected to the Discord websocket.");
            } else 
				System.err.println("Use the login() method to set your token first!");
        }

        /**
         * Called when the websocket receives a message.
         * This method is parses from raw JSON to objects,
         * then dispatches them in the form of events.
         *
         * @param frame raw JSON data from Discord servers
         */
        @Override 
		public final void onMessage(String frame) {
            JsonParser parser = new JsonParser();
            JsonObject object = parser.parse(frame).getAsJsonObject();
            if (object.has("message")) {
				String message = object.get("message").getAsString();
				if (message == null || message.isEmpty()) {
					Discord4J.LOGGER.error("Received unknown error from Discord. Complain to the Discord devs, not me!");
				} else
					Discord4J.LOGGER.debug("Received error from Discord: {}", message);
			}
            String type = object.get("t").getAsString();
            JsonElement eventObject = object.get("d");
    
            switch (type) {
				case "READY":
					
					ReadyEventResponse event = GSON.fromJson(eventObject, ReadyEventResponse.class);
					
					DiscordClient.this.ourUser = DiscordClient.this.constructUserFromJSON(event.user);

					DiscordClient.this.heartbeat = event.heartbeat_interval;
					Discord4J.LOGGER.debug("Received heartbeat interval of {}.", DiscordClient.this.heartbeat);

					// I hope you like loops.
					for (GuildResponse guildResponse : event.guilds) {
						Guild guild;
						guildList.add(guild = new Guild(guildResponse.name, guildResponse.name, guildResponse.icon, guildResponse.owner_id));

						for (GuildResponse.MemberResponse member : guildResponse.members) {
							guild.addUser(new User(member.user.username, member.user.id, member.user.avatar));
						}

						for (PresenceResponse presence : guildResponse.presences) {
							User user = guild.getUserByID(presence.user.id);
							user.setPresence(Presences.valueOf((presence.status).toUpperCase()));
							user.setGame(presence.game == null ? null : presence.game.name);
						}

						for (ChannelResponse channelResponse : guildResponse.channels) {
							String channelType = channelResponse.type;
							if ("text".equalsIgnoreCase(channelType)) {
								Channel channel;
								guild.addChannel(channel = new Channel(channelResponse.name, channelResponse.id, guild));
								try {
									DiscordClient.this.getChannelMessages(channel);
								} catch (HTTP403Exception e) {
									Discord4J.LOGGER.error("No permission for channel \"{}\" in guild \"{}\". Are you logged in properly?", channelResponse.name, guildResponse.name);
								} catch (Exception e) {
									Discord4J.LOGGER.error("Unable to get messages for channel \"{}\" in guild \"{}\" (Cause: {}).", channelResponse.name, guildResponse.name, e.getClass().getSimpleName());
								}
							}
						}
					}

					for (PrivateChannelResponse privateChannelResponse : event.private_channels) {
						String id = privateChannelResponse.id;
						User recipient = new User(privateChannelResponse.recipient.username, privateChannelResponse.recipient.id, privateChannelResponse.recipient.avatar);
						PrivateChannel channel = new PrivateChannel(recipient, id);
						try {
							DiscordClient.this.getChannelMessages(channel);
						} catch (HTTP403Exception e) {
							Discord4J.LOGGER.error("No permission for the private channel for \"{}\". Are you logged in properly?", channel.getRecipient().getName());
						} catch (Exception e) {
							Discord4J.LOGGER.error("Unable to get messages for the private channel for \"{}\" (Cause: {}).", channel.getRecipient().getName(), e.getClass().getSimpleName());
						}
						privateChannels.add(channel);
					}

					Discord4J.LOGGER.debug("Logged in as {} (ID {}).", DiscordClient.this.ourUser.getName(), DiscordClient.this.ourUser.getID());
					new Thread(() -> {
						// Keep alive
						while (this.isConnected) {
							long l;
							if ((l = (System.currentTimeMillis() - timer)) >= heartbeat) {
								Discord4J.LOGGER.debug("Sending keep alive... ({}). Took {} ms.", System.currentTimeMillis(), l);
								send("{\"op\":1,\"d\":" + System.currentTimeMillis() + "}");
								timer = System.currentTimeMillis();
							}
						}
					}).start();

					DiscordClient.this.dispatcher.dispatch(new ReadyEvent());
					break;

				case "MESSAGE_CREATE":
					MessageResponse event1 = GSON.fromJson(eventObject, MessageResponse.class);
                    boolean mentioned = event1.mention_everyone || event1.content.contains("<@"+ourUser.getID()+">");
                    
                    Channel channel = client.getChannelByID(event1.channel_id);
                    
                    if (null != channel) {
                        Message message1 = new Message(event1.id, event1.content, client.getUserByID(event1.author.id),
                                channel, client.convertFromTimestamp(event1.timestamp));
                        if (!event1.author.id.equalsIgnoreCase(client.getOurUser().getID())) {
                            channel.addMessage(message1);
                            Discord4J.LOGGER.debug("Message from: {} ({}) in channel ID {}: {}", message1.getAuthor().getName(), 
                                    event1.author.id, event1.channel_id, event1.content);
                            if (event1.content.contains("discord.gg/")) {
                                String inviteCode = event1.content.split("discord\\.gg/")[1].split(" ")[0];
                                Discord4J.LOGGER.debug("Received invite code \"{}\"", inviteCode);
								client.dispatcher.dispatch(new InviteReceivedEvent(new Invite(client, inviteCode), message1));
                            }
                            if (mentioned) {
                                DiscordClient.this.dispatcher.dispatch(new MentionEvent(message1));
                            }
                            DiscordClient.this.dispatcher.dispatch(new MessageReceivedEvent(message1));
                        }
                    }
                    break;

                case "TYPING_START":
                    TypingEventResponse event2 = GSON.fromJson(eventObject, TypingEventResponse.class);
    
                    User user;
                    channel = getChannelByID(event2.channel_id);
                    if (channel.isPrivate()) {
                        user = ((PrivateChannel)channel).getRecipient();
                    } else {
                        user = channel.getParent().getUserByID(event2.user_id);
                    }
                    if(null != channel
                            && null != user) {
                        dispatcher.dispatch(new TypingEvent(user, channel));
                    }
                    break;

                case "GUILD_CREATE":
                    GuildResponse event3 = GSON.fromJson(eventObject, GuildResponse.class);
                    Guild guild = new Guild(event3.name, event3.id, event3.icon, event3.owner_id);
                    DiscordClient.this.guildList.add(guild);
    
                    for (GuildResponse.MemberResponse member : event3.members) {
                        guild.addUser(new User(member.user.username, member.user.id, member.user.avatar));
                    }
    
                    for (PresenceResponse presence : event3.presences) {
                        User user1 = guild.getUserByID(presence.user.id);
                        user1.setPresence(Presences.valueOf((presence.status).toUpperCase()));
                        user1.setGame(presence.game == null ? null : presence.game.name);
                    }
    
                    for (ChannelResponse channelResponse : event3.channels) {
                        String channelType = channelResponse.type;
                        if ("text".equalsIgnoreCase(channelType)) {
                            Channel channel1;
                            guild.addChannel(channel1 = new Channel(channelResponse.name, channelResponse.id, guild));
                            try {
                                DiscordClient.this.getChannelMessages(channel1);
                            } catch (HTTP403Exception e) {
                                Discord4J.LOGGER.error("No permission for channel \"{}\" in guild \"{}\". Are you logged in properly?", channelResponse.name, event3.name);
                            } catch (Exception e) {
                                Discord4J.LOGGER.error("Unable to get messages for channel \"{}\" in guild \"{}\" (Cause: {}).", channelResponse.name, event3.name, e.getClass().getSimpleName());
                            }
                        }
                    }

                    client.dispatcher.dispatch(new GuildCreateEvent(guild));
                    Discord4J.LOGGER.debug("New guild has been created/joined! \"{}\" with ID {}.", guild.getName(), guild.getID());
                    break;

                case "GUILD_MEMBER_ADD":
                    GuildMemberAddEventResponse event4 = GSON.fromJson(eventObject, GuildMemberAddEventResponse.class);
                    user = constructUserFromJSON(event4.user);
                    String guildID = event4.guild_id;
                    guild = getGuildByID(guildID);
                    if(null != guild) {
                        guild.addUser(user);
                        Discord4J.LOGGER.debug("User \"{}\" joined guild \"{}\".", user.getName(), guild.getName());
                        dispatcher.dispatch(new UserJoinEvent(guild, user, convertFromTimestamp(event4.joined_at)));
                    }
                    break;

                case "GUILD_MEMBER_REMOVE":
                    GuildMemberRemoveEventResponse event5 = GSON.fromJson(eventObject, GuildMemberRemoveEventResponse.class);
                    user = constructUserFromJSON(event5.user);
                    guildID = event5.guild_id;
                    guild = getGuildByID(guildID);
                    if(null != guild
                            && guild.getUsers().contains(user)) {
                        guild.getUsers().remove(user);
                        Discord4J.LOGGER.debug("User \"{}\" has been removed from or left guild \"{}\".", user.getName(), guild.getName());
                        dispatcher.dispatch(new UserLeaveEvent(guild, user));
                    }
                    break;

                case "MESSAGE_UPDATE":
                    MessageResponse event6 = GSON.fromJson(eventObject, MessageResponse.class);
                    String id = event6.id;
                    String channelID = event6.channel_id;
                    String content = event6.content;

                    channel = DiscordClient.this.getChannelByID(channelID);
                    Message m = channel.getMessageByID(id);
                    if(null != m
                            && !m.getAuthor().getID().equals(getOurUser().getID())
                            && !m.getContent().equals(content)) {
                        Message newMessage;
                        int index = channel.getMessages().indexOf(m);
                        channel.getMessages().remove(m);
                        channel.getMessages().add(index, newMessage = new Message(id, content, m.getAuthor(), channel, m.getTimestamp()));
                        dispatcher.dispatch(new MessageUpdateEvent(m, newMessage));
                    }
                    break;

                case "MESSAGE_DELETE":
                    MessageDeleteEventResponse event7 = GSON.fromJson(eventObject, MessageDeleteEventResponse.class);
                    id = event7.id;
                    channelID = event7.channel_id;
                    channel = DiscordClient.this.getChannelByID(channelID);
                    if (null != channel) {
                        Message message = channel.getMessageByID(id);
                        if (null != message
                                && !message.getAuthor().getID().equalsIgnoreCase(DiscordClient.this.ourUser.getID())) {
                            channel.getMessages().remove(message);
                            client.dispatcher.dispatch(new MessageDeleteEvent(message));
                        }
                    }
                    break;

                case "PRESENCE_UPDATE":
                    PresenceUpdateEventResponse event8 = GSON.fromJson(eventObject, PresenceUpdateEventResponse.class);
                    Presences presences = Presences.valueOf(event8.status.toUpperCase());
                    String gameName = event8.game == null ? null : event8.game.name;
                    guild = getGuildByID(event8.guild_id);
                    if(null != guild
                            && null != presences) {
                        user = guild.getUserByID(event8.user.id);
                        if(null != user) {
                            if (!user.getPresence().equals(presences)) {
                                dispatcher.dispatch(new PresenceUpdateEvent(guild, user, user.getPresence(), presences));
                                user.setPresence(presences);
                                Discord4J.LOGGER.debug("User \"{}\" changed presence to {}", user.getName(), user.getPresence());
                            }
                            if (!user.getGame().equals(Optional.ofNullable(gameName))) {
                                dispatcher.dispatch(new GameChangeEvent(guild, user, user.getGame().isPresent() ? user.getGame().get() : null, gameName));
                                user.setGame(gameName);
                                Discord4J.LOGGER.debug("User \"{}\" changed game to {}.", user.getName(), gameName);
                            }
                        }
                    }
                    break;

                case "GUILD_DELETE":
                    GuildResponse event9 = GSON.fromJson(eventObject, GuildResponse.class);
                    guild = getGuildByID(event9.id);
                    getGuilds().remove(guild);
                    Discord4J.LOGGER.debug("You have been kicked from or left \"{}\"! :O", guild.getName());
                    dispatcher.dispatch(new GuildLeaveEvent(guild));
                    break;

                case "CHANNEL_CREATE":
                    boolean isPrivate = eventObject.getAsJsonObject().get("is_private").getAsBoolean();
                    
                    if(isPrivate) { // PM channel.
                        PrivateChannelResponse event10 = GSON.fromJson(eventObject, PrivateChannelResponse.class);
                        id = event10.id;
                        boolean b = false;
                        for(PrivateChannel privateChannel : privateChannels) {
                            if(privateChannel.getID().equalsIgnoreCase(id))
                                b = true;
                        }

                        if(b) break; // we already have this PM channel; no need to create another.
                        user = constructUserFromJSON(event10.recipient);
                        PrivateChannel privateChannel;
                        privateChannels.add(privateChannel = new PrivateChannel(user, id));
                        try {
                            getChannelMessages(privateChannel);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        
                    } else { // Regular channel.
                        ChannelCreateEventResponse event10 = GSON.fromJson(eventObject, ChannelCreateEventResponse.class);
                        id = event10.id;
                        type = event10.type;
                        if("text".equalsIgnoreCase(type)) {
                            String name = event10.name;
                            guildID = event10.guild_id;
                            guild = getGuildByID(guildID);
                            if(null != guild) {
                                channel = new Channel(name, id, guild);
                                try {
                                    getChannelMessages(channel);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                dispatcher.dispatch(new ChannelCreateEvent(channel));
                            }
                        }
                    }
                    break;

                case "CHANNEL_DELETE":
                    ChannelCreateEventResponse event11 = GSON.fromJson(eventObject, ChannelCreateEventResponse.class);
                    if("text".equalsIgnoreCase(event11.type)) {
                        channel = getChannelByID(event11.id);
                        dispatcher.dispatch(new ChannelDeleteEvent(channel));
                        channel.getParent().getChannels().remove(channel);
                    }
                    break;
				
				case "USER_UPDATE":
					UserUpdateEventResponse event12 = GSON.fromJson(eventObject, UserUpdateEventResponse.class);
					User newUser = getUserByID(event12.id);
					if (newUser != null) {
						User oldUser = new User(newUser.getName(), newUser.getID(), newUser.getAvatar());
						newUser.setName(event12.username);
						newUser.setAvatar(event12.avatar);
						dispatcher.dispatch(new UserUpdateEvent(oldUser, newUser));
					}
					break;

                default:
                    Discord4J.LOGGER.warn("Unknown message received: {} REPORT THIS TO THE DISCORD4J DEV! (ignoring): {}", eventObject.toString(), frame);
			}
        }

        @Override 
		public void onClose(int i, String s, boolean b) {

        }

        @Override
        public void onError(Exception e) {
            e.printStackTrace();
        }
    }
}
