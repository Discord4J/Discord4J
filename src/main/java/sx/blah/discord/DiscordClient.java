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

package sx.blah.discord;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import sx.blah.discord.handle.IDispatcher;
import sx.blah.discord.handle.impl.EventDispatcher;
import sx.blah.discord.handle.impl.events.*;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.HTTP403Exception;
import sx.blah.discord.util.Presences;
import sx.blah.discord.util.Requests;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
     * Re-usable instance of JSONParser.
     */
    private static final JSONParser JSON_PARSER = new JSONParser();

    /**
     * Private copy of the email you logged in with.
     */
    private String email;

    /**
     * Private copy of the password you used to log in.
     */
    private String password;

    /**
     * Local instance of DiscordClient.
     */
    private static final DiscordClient __INSTANCE = new DiscordClient();

    /**
     * WebSocket over which to communicate with Discord.
     */
    private WebSocketClient ws;

    /**
     * Event dispatcher.
     */
    private IDispatcher dispatcher;

    /**
     * All of the private message channels that the bot is connected to.
     */
    private final List<PrivateChannel> privateChannels = new ArrayList<>();
	
	/**
     * A cached json object of the games list to prevent unnecessary slowdown from file i/o
     */
    private JSONArray games;

    private DiscordClient() {
        this.dispatcher = new EventDispatcher();
        try {
            games = (JSONArray) JSON_PARSER.parse(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("games.json")));
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * Allows you to use a custom event dispatcher.
     *
     * @param dispatcher An instance of IDispatcher.
     */
    public void setDispatcher(IDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    /**
     * @return Singleton instance
     */
    public static DiscordClient get() {
        return __INSTANCE;
    }

    /**
     * @return the event dispatcher. This will send events to all listeners
     */
    public IDispatcher getDispatcher() {
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
    public void login(String email, String password)
            throws IOException, ParseException, URISyntaxException {
        if (null != ws) {
            ws.close();
        }

        this.email = email;
        this.password = password;

        try {
            this.token = (String) ((JSONObject) JSON_PARSER.parse(Requests.POST.makeRequest(DiscordEndpoints.LOGIN,
                    new StringEntity("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"),
                    new BasicNameValuePair("content-type", "application/json")))).get("token");
        } catch (HTTP403Exception e) {
            e.printStackTrace();
        }

        this.ws = new DiscordWS(new URI(obtainGateway(this.token)));
    }

    /**
     * Gets the WebSocket gateway
     *
     * @param token Our login token
     * @return the WebSocket URL of which to connect
     * @throws ParseException
     */
    private String obtainGateway(String token) throws ParseException {
        String s = null;
        try {
            s = ((String) ((JSONObject) JSON_PARSER.parse(
                    Requests.GET.makeRequest("https://discordapp.com/api/gateway",
                            new BasicNameValuePair("authorization", token)))).get("url")).replaceAll("wss", "ws");
        } catch (HTTP403Exception e) {
            Discord4J.logger.error("Received 403 error attempting to get gateway; is your login correct?");
        }
        Discord4J.logger.debug("Obtained gateway {}.", s);
        return s;
    }

    /**
     * Sends a message to the specified channel.
     *
     * @param content   The actual message to send
     * @param channelID The channel to send the message to
     * @return The message that was sent.
     * @throws IOException
     * @throws ParseException
     */
    public Message sendMessage(String content, String channelID) throws IOException, ParseException {
        if (null != ws) {

            content = StringEscapeUtils.escapeJson(content);
            
            try {
                String response = Requests.POST.makeRequest(DiscordEndpoints.CHANNELS + channelID + "/messages",
                        new StringEntity("{\"content\":\"" + content + "\",\"mentions\":[]}","UTF-8"),
                        new BasicNameValuePair("authorization", token),
                        new BasicNameValuePair("content-type", "application/json"));


                JSONObject object1 = (JSONObject) JSON_PARSER.parse(response);
                String time = (String) object1.get("timestamp");
                String messageID = (String) object1.get("id");

                Channel channel = getChannelByID(channelID);
                Message message = new Message(messageID, content, this.ourUser, channel, this.convertFromTimestamp(time));
                channel.addMessage(message); //Had to be moved here so that if a message is edited before the MESSAGE_CREATE event, it doesn't error
                DiscordClient.this.dispatcher.dispatch(new MessageSendEvent(message));
            return message;
            } catch (HTTP403Exception e) {
                Discord4J.logger.error("Received 403 error attempting to send message; is your login correct?");
                return null;
            }

        } else {
            Discord4J.logger.error("Bot has not signed in yet!");
            return null;
        }
    }
	
	/**
     * Edits a specified message. Currently, Discord only allows you to edit your own message
     * 
     * @param content   The new content of the message
     * @param messageID The id of the message to edit
     * @param channelID The channel the message exists in
     * @throws ParseException
     */
    public void editMessage(String content, String messageID, String channelID) throws ParseException {
        if (null != ws) {
    
            content = StringEscapeUtils.escapeJson(content);
            
            Channel channel = getChannelByID(channelID);
            if (channel == null) {
                Discord4J.logger.error("Channel id " +  channelID + " doesn't exist!");
                return;
            }
            
            Message oldMessage = channel.getMessageByID(messageID);
            
            try {
                String response = Requests.PATCH.makeRequest(DiscordEndpoints.CHANNELS + channelID + "/messages/" + messageID, 
                        new StringEntity("{\"content\":\"" + content + "\", \"mentions\":[]}", "UTF-8"), 
                        new BasicNameValuePair("authorization", token),
                        new BasicNameValuePair("content-type", "application/json"));
    
                JSONObject object1 = (JSONObject) JSON_PARSER.parse(response);
    
                Message newMessage = new Message((String) object1.get("id"), content, this.ourUser, getChannelByID(channelID), 
                        this.convertFromTimestamp((String) object1.get("timestamp")));
                //Event dispatched here because otherwise there'll be an NPE as for some reason when the bot edits a message,
                // the event chain goes like this:
                //Original message edited to null, then the null message edited to the new content
                DiscordClient.this.dispatcher.dispatch(new MessageUpdateEvent(oldMessage, newMessage));
            } catch (HTTP403Exception e) {
                Discord4J.logger.error("Received 403 error attempting to send message; is your login correct?");
            }
    
        } else {
            Discord4J.logger.error("Bot has not signed in yet!");
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
                Discord4J.logger.error("Received 403 error attempting to delete message; is your login correct?");
            }
        } else {
            Discord4J.logger.error("Bot has not signed in yet!");
        }
    }

    /**
     * TODO: Fix this because it's fucking stupid.
     * Allows you to change the info on your bot.
     * Any fields you don't want to change should be left as an empty string ("")
     *
     * @param username Username (if you want to change it).
     * @param email    Email (if you want to change it)
     * @param password Password (if you want to change it).
     */
    public void changeAccountInfo(String username, String email, String password)
            throws UnsupportedEncodingException, ParseException {
        String s = "{\"username\":\"" +
                (username.isEmpty() ? this.ourUser.getName() : username) +
                "\",\"email\":\"" + (email.isEmpty() ? this.email : email)
                + "\",\"password\":\""
                + this.password
                + "\",\"avatar\":\"" + this.ourUser.getAvatar() + "\",\"new_password\":"
                + (password.isEmpty() ? "null" : "\"" + password + "\"") + "}";
        Discord4J.logger.debug("Token: {}", token);
        Discord4J.logger.debug(s);
        try {
            String response = Requests.PATCH.makeRequest(DiscordEndpoints.USERS + "@me",
                    new StringEntity(s),
                    new BasicNameValuePair("Authorization", token),
                    new BasicNameValuePair("content-type", "application/json; charset=UTF-8"));
            JSONObject object1 = (JSONObject) JSON_PARSER.parse(response);
            this.token = (String) object1.get("token");
        } catch (HTTP403Exception e) {
            Discord4J.logger.error("Received 403 error attempting to change account details; is your login correct?");
        }
    }
	
	/**
     * Changes the bot's presence
     * 
     * @param isIdle Set to true to make the bot idle or false for it to be online
     * @param gameID The (optional) gameID of the game the bot is playing
     */
    public void updatePresence(boolean isIdle, Optional<Long> gameID) {
        String json = "{\"op\":3,\"d\":{\"idle_since\":" + (isIdle ? System.currentTimeMillis() : "null") + 
                ",\"game_id\":" + (gameID.isPresent() ? gameID.get() : "null") + "}}";
        Discord4J.logger.debug(json); 
        
        ws.send(json);
        
        getOurUser().setPresence(isIdle ? Presences.IDLE : Presences.ONLINE);
        getOurUser().setGameID(gameID.orElse(null));
    }

    /**
     * Gets the last 50 messages from a given channel ID.
     *
     * @param channel The channel to get messages from.
     * @return Last 50 messages from the channel.
     * @throws IOException
     * @throws ParseException
     */
    private void getChannelMessages(Channel channel) throws Exception {
        String response = Requests.GET.makeRequest(DiscordEndpoints.CHANNELS + channel.getID() + "/messages?limit=50",
                new BasicNameValuePair("authorization", token));
        JSONArray messageArray = (JSONArray) JSON_PARSER.parse(response);

        for (Object o : messageArray) {
            JSONObject object1 = (JSONObject) o;
            JSONObject author = (JSONObject) object1.get("author");
            JSONArray mentions = (JSONArray) object1.get("mentions");

            String[] mentionsArray = new String[mentions.size()];
            for (int i = 0; i < mentions.size(); i++) {
                JSONObject mention = (JSONObject) mentions.get(i);
                mentionsArray[i] = (String) mention.get(mention.get("id"));
            }

            channel.addMessage(new Message((String) object1.get("id"),
                    (String) object1.get("content"),
                    this.getUserByID((String) author.get("id")),
                    channel,
                    this.convertFromTimestamp((String) object1.get("timestamp"))));
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
            String response = Requests.POST.makeRequest(DiscordEndpoints.USERS + this.ourUser.getID() + "/channels",
                    new StringEntity("{\"recipient_id\":\"" + user.getID() + "\"}"),
                    new BasicNameValuePair("authorization", this.token),
                    new BasicNameValuePair("content-type", "application/json"));
            JSONObject object = (JSONObject) JSON_PARSER.parse(response);
            PrivateChannel channel = new PrivateChannel(user, (String) object.get("id"));
            privateChannels.add(channel);
            return channel;
        } catch (HTTP403Exception e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return null;
    }
	
	/**
     * Attempts to get the name of the game based on its id
     * 
     * @param gameId The game id (nullable!)
     * @return The game name, the Optional will be empty if the game couldn't be found
     */
    public Optional<String> getGameByID(Long gameId) {
        if (games == null || gameId == null)
            return Optional.empty();
        
        for (Object object : games) {
            if (!(object instanceof JSONObject))
                continue;
            
            JSONObject jsonObject = (JSONObject)object;
            Long id = (Long) jsonObject.get("id");
            if (id != null) {
                if (id.equals(gameId))
                    return Optional.ofNullable((String) jsonObject.get("name"));
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * Attempts to get the id of a game based on its name
     * 
     * @param gameName The game name (nullable!)
     * @return The game id, the Optional will be empty if the game couldn't be found
     */
    public Optional<Long> getGameIDByGame(String gameName) {
        if (gameName == null || gameName.isEmpty() || gameName.equals("null"))
            return Optional.empty();
    
        for (Object object : games) {
            if (!(object instanceof JSONObject))
                continue;
        
            JSONObject jsonObject = (JSONObject)object;
            String name = (String) jsonObject.get("name");
            if (name != null) {
                if (name.equalsIgnoreCase(gameName))
                    return Optional.ofNullable((Long) jsonObject.get("id"));
            }
        }
    
        return Optional.empty();
    }

    /**
     * Returns a user from raw JSON data.
     */
    private User constructUserFromJSON(JSONObject user) {
        String id = (String) user.get("id");
        String username = (String) user.get("username");
        String avatar = (String) user.get("avatar");

        User ourUser = new User(username, id, avatar);
        ourUser.setPresence(Presences.ONLINE);
        
        return ourUser;
    }

    class DiscordWS extends WebSocketClient {
        public DiscordWS(URI serverURI) {
            super(serverURI);
            this.connect();
        }

        @Override
        public void onOpen(ServerHandshake serverHandshake) {
            if (!token.isEmpty()) {
                send("{\"op\":2,\"d\":{\"token\":\"" + token + "\",\"properties\":{\"$os\":\"Linux\",\"$browser\":\"DesuBot\",\"$device\":\"DesuBot\",\"$referrer\":\"\",\"$referring_domain\":\"\"},\"v\":2}}");
                Discord4J.logger.debug("Connected to the Discord websocket.");
            } else System.err.println("Use the login() method to set your token first!");
        }

        /**
         * Called when the websocket receives a message.
         * This method is parses from raw JSON to objects,
         * then dispatches them in the form of events.
         *
         * @param frame raw JSON data from Discord servers
         */
        @Override public final void onMessage(String frame) {
            try {
                JSONObject object = ((JSONObject) JSON_PARSER.parse(frame));
                try {
                    String message = (String) object.get("message");
                    if(message.isEmpty()) {
                        Discord4J.logger.error("Received unknown error from Discord. Complain to the Discord devs, not me!");
                    } else
                        Discord4J.logger.debug("Received error from Discord: {}", message);
                } catch (Exception e) { }
                String s = (String) object.get("t");
                JSONObject d = (JSONObject) object.get("d");

                switch (s) {
                    case "READY":

                        DiscordClient.this.ourUser = DiscordClient.this.constructUserFromJSON((JSONObject) d.get("user"));

                        DiscordClient.this.heartbeat = (long) d.get("heartbeat_interval");
                        Discord4J.logger.debug("Received heartbeat interval of {}.", DiscordClient.this.heartbeat);

                        JSONArray guilds = (JSONArray) d.get("guilds");

                        // I hope you like loops.
                        for (Object o : guilds) {
                            JSONObject guild = (JSONObject) o;
                            JSONArray members = (JSONArray) guild.get("members");
                            JSONArray channels = (JSONArray) guild.get("channels");
                            JSONArray presences = (JSONArray) guild.get("presences");
                            String name = (String) guild.get("name");
                            String guildID = (String) guild.get("id");
                            String icon = (String) guild.get("icon");
                            String owner = (String) guild.get("owner_id");

                            Guild g;
                            guildList.add(g = new Guild(name, guildID, icon, owner));

                            for (Object o1 : members) {
                                JSONObject member = (JSONObject) ((JSONObject) o1).get("user");
                                g.addUser(new User((String) member.get("username"), (String) member.get("id"), (String) member.get("avatar")));
                            }

                            for (Object o1 : presences) {
                                JSONObject presence = (JSONObject) o1;
                                User user = g.getUserByID((String) ((JSONObject) presence.get("user")).get("id"));
                                user.setPresence(Presences.valueOf(((String) presence.get("status")).toUpperCase()));
                                user.setGameID((Long) presence.get("game_id"));
                            }

                            for (Object o1 : channels) {
                                JSONObject channel = (JSONObject) o1;
                                String type = (String) channel.get("type");
                                if ("text".equalsIgnoreCase(type)) {
                                    String channelID = (String) channel.get("id");
                                    String chName = (String) channel.get("name");
                                    Channel c;
                                    g.addChannel(c = new Channel(chName, channelID, g));
                                    try {
                                        DiscordClient.this.getChannelMessages(c);
                                    } catch (HTTP403Exception e) {
                                        Discord4J.logger.error("No permission for channel \"{}\" in guild \"{}\". Are you logged in properly?", chName, name);
                                    } catch (Exception e) {
                                        Discord4J.logger.error("Unable to get messages for channel \"{}\" in guild \"{}\" (Cause: {}).", chName, name, e.getClass().getSimpleName());
                                    }
                                }
                            }
                        }
    
                        JSONArray privateChannelsArray = (JSONArray) d.get("private_channels");
    
                        for (Object o : privateChannelsArray) {
                            JSONObject privateChannel = (JSONObject) o;
                            String id = (String) privateChannel.get("id");
                            JSONObject user = (JSONObject) privateChannel.get("recipient");
                            User recipient = new User((String)user.get("username"), (String)user.get("id"), (String)user.get("avatar"));
                            PrivateChannel channel = new PrivateChannel(recipient, id);
                            try {
                                DiscordClient.this.getChannelMessages(channel);
                            } catch (HTTP403Exception e) {
                                Discord4J.logger.error("No permission for the private channel for \"{}\". Are you logged in properly?", channel.getRecipient().getName());
                            } catch (Exception e) {
                                Discord4J.logger.error("Unable to get messages for the private channel for \"{}\" (Cause: {}).", channel.getRecipient().getName(), e.getClass().getSimpleName());
                            }
                            privateChannels.add(channel);
                        }

                        Discord4J.logger.debug("Logged in as {} (ID {}).", DiscordClient.this.ourUser.getName(), DiscordClient.this.ourUser.getID());
                        new Thread(() -> {
                            // Keep alive
                            while (null != ws) {
                                long l;
                                if ((l = (System.currentTimeMillis() - timer)) >= heartbeat) {
                                    Discord4J.logger.debug("Sending keep alive... ({}). Took {} ms.", System.currentTimeMillis(), l);
                                    send("{\"op\":1,\"d\":" + System.currentTimeMillis() + "}");
                                    timer = System.currentTimeMillis();
                                }
                            }
                        }).start();

                        DiscordClient.this.dispatcher.dispatch(new ReadyEvent());
                        break;

                    case "MESSAGE_CREATE":
                        JSONObject author = (JSONObject) d.get("author");

                        String username = (String) author.get("username");
                        String id = (String) author.get("id");
                        String channelID = (String) d.get("channel_id");
                        String content = (String) d.get("content");
                        String messageID = (String) d.get("id");
                        JSONArray array = (JSONArray) d.get("mentions");
                        String time = (String) d.get("timestamp");

                        String[] mentionedIDs = new String[array.size()];
                        boolean mentioned = false;
                        for (int i = 0; i < array.size(); i++) {
                            JSONObject userInfo = (JSONObject) array.get(i);
                            String userID = (String) userInfo.get("id");
                            if (userID.equalsIgnoreCase(DiscordClient.get().getOurUser().getID())) {
                                mentioned = true;
                            }
                            mentionedIDs[i] = userID;
                        }
                        Channel channel = DiscordClient.get().getChannelByID(channelID);


                        if (null != channel) {
                            Message message1 = new Message(messageID, content, DiscordClient.get().getUserByID(id),
                                    channel, DiscordClient.get().convertFromTimestamp(time));
                            if (!id.equalsIgnoreCase(DiscordClient.get().getOurUser().getID())) {
                                channel.addMessage(message1);
                                Discord4J.logger.debug("Message from: {} ({}) in channel ID {}: {}", username, id, channelID, content);
                                if (content.contains("discord.gg/")) {
                                    String inviteCode = content.split("discord\\.gg/")[1].split(" ")[0];
                                    Discord4J.logger.debug("Received invite code \"{}\"", inviteCode);
                                    DiscordClient.get().dispatcher.dispatch(new InviteReceivedEvent(new Invite(inviteCode), message1));
                                }
                                if (mentioned) {
                                    DiscordClient.this.dispatcher.dispatch(new MentionEvent(message1));
                                }
                                DiscordClient.this.dispatcher.dispatch(new MessageReceivedEvent(message1));
                            }
                        }
                        break;


                    case "TYPING_START":
                        id = (String) d.get("user_id");
                        channelID = (String) d.get("channel_id");

                        User user;
                        channel = getChannelByID(channelID);
                        if (channel.isPrivate()) {
                            user = ((PrivateChannel)channel).getRecipient();
                        } else {
                            user = channel.getParent().getUserByID(id);
                        }
                        if(null != channel
                                && null != user) {
                            dispatcher.dispatch(new TypingEvent(user, channel));
                        }
                        break;

                    case "GUILD_CREATE":
                        String name = (String) d.get("name");
                        id = (String) d.get("id");
                        JSONArray members = (JSONArray) d.get("members");
                        JSONArray channels = (JSONArray) d.get("channels");
                        String icon = (String) d.get("icon");
                        String owner = (String) d.get("owner_id");
                        
                        Guild guild = new Guild(name, id, icon, owner);
                        DiscordClient.this.guildList.add(guild);

                        for (Object o : members) {
                            JSONObject object1 = (JSONObject) o;
                            guild.addUser(new User((String) object1.get("username"),
                                    (String) object1.get("id"), (String) object1.get("avatar")));
                        }

                        for (Object o : channels) {
                            JSONObject channelData = (JSONObject) o;
                            if (((String) channelData.get("type")).equalsIgnoreCase("text")) {
                                guild.addChannel(channel = new Channel((String) channelData.get("name"), (String) channelData.get("id"), guild));
                                try {
                                    getChannelMessages(channel);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        DiscordClient.get().dispatcher.dispatch(new GuildCreateEvent(guild));
                        Discord4J.logger.debug("New guild has been joined/created! \"{}\" with ID {}.", name, id);
                        break;

                    case "GUILD_MEMBER_ADD":
                        user = constructUserFromJSON((JSONObject) d.get("user"));
                        String guildID = (String) d.get("guild_id");
                        guild = getGuildByID(guildID);
                        if(null != guild) {
                            guild.addUser(user);
                            Discord4J.logger.debug("User \"{}\" joined guild \"{}\".", user.getName(), guild.getName());
                            dispatcher.dispatch(new UserJoinEvent(guild, user, convertFromTimestamp((String) d.get("joined_at"))));
                        }
                        break;

                    case "GUILD_MEMBER_REMOVE":
                        user = constructUserFromJSON((JSONObject) d.get("user"));
                        guildID = (String) d.get("guild_id");
                        guild = getGuildByID(guildID);
                        if(null != guild
                                && guild.getUsers().contains(user)) {
                            guild.getUsers().remove(user);
                            Discord4J.logger.debug("User \"{}\" has been removed from or left guild \"{}\".", user.getName(), guild.getName());
                            dispatcher.dispatch(new UserLeaveEvent(guild, user));
                        }
                        break;

                    case "MESSAGE_UPDATE":
                        id = (String) d.get("id");
                        channelID = (String) d.get("channel_id");
                        content = (String) d.get("content");

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
                        id = (String) d.get("id");
                        channelID = (String) d.get("channel_id");
                        channel = DiscordClient.this.getChannelByID(channelID);
                        if (null != channel) {
                            Message message = channel.getMessageByID(id);
                            if (null != message
                                    && !message.getAuthor().getID().equalsIgnoreCase(DiscordClient.this.ourUser.getID())) {
                                channel.getMessages().remove(message);
                                DiscordClient.get().dispatcher.dispatch(new MessageDeleteEvent(message));
                            }
                        }
                        break;

                    case "PRESENCE_UPDATE":
                        Presences presences = Presences.valueOf(((String) d.get("status")).toUpperCase());
                        Long gameId = (Long) d.get("game_id");
                        guild = getGuildByID((String) d.get("guild_id"));
                        if(null != guild
                                && null != presences) {
                            user = guild.getUserByID((String)((JSONObject) d.get("user")).get("id"));
                            if(null != user) {
                                if (!user.getPresence().equals(presences)) {
                                    dispatcher.dispatch(new PresenceUpdateEvent(guild, user, user.getPresence(), presences));
                                    user.setPresence(presences);
                                    Discord4J.logger.debug("User \"{}\" changed presence to {}", user.getName(), user.getPresence());
                                }
                                if (!user.getGameID().equals(Optional.ofNullable(gameId))) {
                                    dispatcher.dispatch(new GameChangeEvent(guild, user, user.getGameID().isPresent() ? user.getGameID().get() : null, gameId));
                                    user.setGameID(gameId);
                                    Discord4J.logger.debug("User \"{}\" changed game to {}.", user.getName(), getGameByID(gameId).isPresent() ? getGameByID(gameId).get() : "null");
                                }
                            }
                        }
                        break;

                    case "GUILD_DELETE":
                        id = (String) d.get("id");
                        guild = getGuildByID(id);
                        getGuilds().remove(guild);
                        Discord4J.logger.debug("You have been kicked from or left \"{}\"! :O", guild.getName());
                        dispatcher.dispatch(new GuildLeaveEvent(guild));
                        break;

                    case "CHANNEL_CREATE":
                        boolean isPrivate = (boolean) d.get("is_private");
                        id = (String) d.get("id");
                        if(isPrivate) { // PM channel.
                            boolean b = false;
                            for(PrivateChannel privateChannel : privateChannels) {
                                if(privateChannel.getID().equalsIgnoreCase(id))
                                    b = true;
                            }

                            if(b) break; // we already have this PM channel; no need to create another.
                            user = constructUserFromJSON((JSONObject) d.get("recipient"));
                            PrivateChannel privateChannel;
                            privateChannels.add(privateChannel = new PrivateChannel(user, id));
                            try {
                                getChannelMessages(privateChannel);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else { // Regular channel.
                            String type = (String) d.get("type");
                            if("text".equalsIgnoreCase(type)) {
                                name = (String) d.get("name");
                                id = (String) d.get("id");
                                guildID = (String) d.get("guild_id");
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
                        if("text".equalsIgnoreCase((String) d.get("type"))) {
                            channel = getChannelByID((String) d.get("id"));
                            channel.getParent().getChannels().remove(channel);

                        }
                        break;

                    default:
                        Discord4J.logger.warn("Unknown message received: {} (ignoring): {}", s, frame);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        @Override public void onClose(int i, String s, boolean b) {

        }

        @Override
        public void onError(Exception e) {
            e.printStackTrace();
        }

    }
}
