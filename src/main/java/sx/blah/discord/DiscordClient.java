package sx.blah.discord;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_10;
import org.java_websocket.handshake.ServerHandshake;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import sx.blah.discord.obj.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

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
public abstract class DiscordClient extends WebSocketClient {
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
     * Our token, so we can send messages
     */
    private String token;

    /**
     * Whether our account is ready to be used yet.
     */
    private boolean ready = false;

    /**
     * How long there should be between keep alive
     * messages.
     */
    private long keepaliveDelay;

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
    private final String email;

    /**
     * Private copy of the password you used to log in.
     */
    private final String password;

    public DiscordClient(String email, String password) throws URISyntaxException, IOException, ParseException {
        super(new URI(DiscordEndpoints.WEBSOCKET_HUB), new Draft_10());
        this.email = email;
        this.password = password;
        token = login(email, password);
        Discord4J.logger.debug("Logged in with token {}.", token);
        Discord4J.logger.debug("Connecting to Discord WebSocket...");

        this.connect();
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        if (!token.isEmpty()) {
            send("{\"op\":2,\"d\":{\"token\":\"" + token + "\",\"properties\":{\"$os\":\"Linux\",\"$browser\":\"DesuBot\",\"$device\":\"DesuBot\",\"$referrer\":\"\",\"$referring_domain\":\"\"},\"v\":2}}");
            Discord4J.logger.debug("Connected.");
        } else System.err.println("Use the login() method to set your token first!");
    }

    /**
     * Called when the websocket receives a message.
     * This method is basically the whole library.
     *
     * @param message
     */
    @Override
    public void onMessage(String message) {
        try {
            JSONObject object = ((JSONObject) JSON_PARSER.parse(message));
            String s = (String) object.get("t");

            switch (s) {
                /*
                 * This is called when someone sends a message.
                 */
                case "MESSAGE_CREATE":
                    JSONObject d = (JSONObject) object.get("d");
                    JSONObject author = (JSONObject) d.get("author");

                    String username = (String) author.get("username");
                    String id = (String) author.get("id");
                    String channelID = (String) d.get("channel_id");
                    String content = (String) d.get("content");
                    String messageID = (String) d.get("id");
                    JSONArray array = (JSONArray) d.get("mentions");

                    String[] mentionedIDs = new String[array.size()];
                    boolean mentioned = false;
                    for (int i = 0; i < array.size(); i++) {
                        JSONObject userInfo = (JSONObject) array.get(i);
                        String userID = (String) userInfo.get("id");
                        if (userID.equalsIgnoreCase(this.ourUser.getId())) {
                            mentioned = true;
                        }
                        mentionedIDs[i] = userID;
                    }

	                Message message1 = new Message(messageID, content, id, username, channelID, mentionedIDs);
	                this.getChannelByID(channelID).addMessage(message1);

                    if (!id.equalsIgnoreCase(ourUser.getId())
                            && this.ready) {
                        Discord4J.logger.debug("Message from: {} ({}) in channel ID {}: {}", username, id, channelID, content);
                        this.onMessageReceive(message1);
                        if (mentioned) {
                            this.onMentioned(message1);
                        }
                    }
                    break;

                case "READY":
                    d = (JSONObject) object.get("d");

                    JSONObject user = (JSONObject) d.get("user");
                    id = (String) user.get("id");
                    username = (String) user.get("username");
                    String avatar = (String) user.get("avatar");

                    this.keepaliveDelay = (long) d.get("heartbeat_interval");
                    Discord4J.logger.debug("Received heartbeat interval of {}.", this.keepaliveDelay);

                    JSONArray guilds = (JSONArray) d.get("guilds");

                    // I hope you like loops.
                    for (Object o : guilds) {
                        JSONObject guild = (JSONObject) o;
                        JSONArray members = (JSONArray) guild.get("members");
                        JSONArray channels = (JSONArray) guild.get("channels");
                        String name = (String) guild.get("name");
                        String guildID = (String) guild.get("id");

                        List<User> memberList = new ArrayList<>();
                        for (Object o1 : members) {
                            JSONObject member = (JSONObject) o1;
                            memberList.add(new User((String) member.get("username"), (String) member.get("id"), (String) member.get("avatar")));
                        }

                        List<Channel> channelList = new ArrayList<>();
                        for (Object o1 : channels) {
                            JSONObject channel = (JSONObject) o1;
                            String type = (String) channel.get("type");
                            if ("text".equalsIgnoreCase(type)) {
                                channelID = (String) channel.get("id");
                                String chName = (String) channel.get("name");
                                List<Message> messages = new ArrayList<>();
                                try {
                                    messages = this.getChannelMessages(channelID);
                                } catch (Exception e) {
                                    Discord4J.logger.error("Unable to fetch messages for channel {} ({}). Cause: {}.", channelID, chName, e.getMessage());
                                }
                                channelList.add(new Channel(chName, channelID, messages));
                            }
                        }

                        guildList.add(new Guild(name, guildID, channelList, memberList));
                    }

                    this.ourUser = new User(username, id, avatar);
                    Discord4J.logger.debug("Connected as {} ({}).", username, id);
                    this.ready = true;
                    new Thread(() -> {
                        // Keep alive
                        while (this.ready) {
                            long l;
                            if ((l = (System.currentTimeMillis() - timer)) >= keepaliveDelay) {
                                String s1 = "{\"op\":1,\"d\":" + System.currentTimeMillis() + "}";
                                Discord4J.logger.debug("Sending keep alive... ({}). Took {} ms.", s1, l);
                                send(s1);
                                timer = System.currentTimeMillis();
                            }
                        }
                    }).start();
                    break;

                case "TYPING_START":
                    d = (JSONObject) object.get("d");
                    id = (String) d.get("id");
                    channelID = (String) d.get("channel_id");

                    this.onStartTyping(id, channelID);
                    break;



                case "GUILD_CREATE":
                    d = (JSONObject) object.get("d");
                    String name = (String) d.get("name");
                    id = (String) d.get("id");
                    JSONArray members = (JSONArray) d.get("members");
                    JSONArray channels = (JSONArray) d.get("channels");

                    List<User> memberList = new ArrayList<>();
                    for (Object o : members) {
                        JSONObject object1 = (JSONObject) o;
                        memberList.add(new User((String) object1.get("username"), (String) object1.get("id")));
                    }

                    List<Channel> channelList = new ArrayList<>();
                    for (Object o : channels) {
                        JSONObject channel = (JSONObject) o;
                        if (((String) channel.get("type")).equalsIgnoreCase("text")) {
                            channelList.add(new Channel((String) channel.get("name"), (String) channel.get("id")));
                        }
                    }

                    this.guildList.add(new Guild(name, id, channelList, memberList));
                    break;

                case "GUILD_MEMBER_ADD":
                    //TODO
                    break;

                case "MESSAGE_UPDATE":
                    //todo
                    // this one is sort of complicated,
                    // because it doesn't always have an ID
                    // or username attached.
                    break;

                case "MESSAGE_DELETE":
                    d = (JSONObject) object.get("d");
                    id = (String) d.get("id");
                    channelID = (String) d.get("channel_id");

                    this.onMessageDelete(id, channelID);
                    break;

                case "PRESENCE_UPDATE":
                    d = (JSONObject) object.get("d");
                    // todo lol
                    break;

                default:
                    Discord4J.logger.warn("Unknown message received (ignoring): {}", message);
            }
        } catch (ParseException e) {
            System.err.println(e.toString());
        }
    }

    @Override
    public void onClose(int i, String s, boolean b) {
    }

    @Override
    public void onError(Exception e) {
        e.printStackTrace();
    }

    /**
     * Called when we receive a message
     *
     * @param message Message received
     */
    public abstract void onMessageReceive(Message message);

    /**
     * Called when our bot sends a message.
     *
     * @param message The message we sent.
     */
    public abstract void onMessageSend(Message message);

    /**
     * Called when our bot gets mentioned in a message.
     *
     * @param message Message sent
     */
    public abstract void onMentioned(Message message);

    /**
     * Called when a user starts typing.
     *
     * @param userID The user's ID.
     */
    public abstract void onStartTyping(String userID, String channelID);

    /**
     * TODO
     * Called when a user changes presence
     *
     * @param user     The user whose presence changed.
     * @param presence The presence they have changed to (online/idle/offline. Not sure if there are more)
     */
    public abstract void onPresenceChange(User user, String presence);

    /**
     * Called when a message is edited.
     * <p>
     * It does not give us any info about the original message,
     * so you should cache all messages.
     *
     * @param message Edited message
     */
    public abstract void onMessageUpdate(Message message);

    /**
     * Called when a message is deleted.
     * Unfortunately not much info is given to us.
     * If you'd like to see content that was deleted,
     * I recommend you to cache all messages.
     *
     * @param messageID The ID of the message that was deleted.
     * @param channelID The channel the message was deleted from.
     */
    public abstract void onMessageDelete(String messageID, String channelID);

    /**
     * Logs you into Discord using given
     * email and password.
     *
     * @return The User object of who you log in as.
     */
    public String login(String email, String password)
            throws IOException, ParseException {
        return (String) ((JSONObject) JSON_PARSER.parse(Requests.POST.makeRequest(DiscordEndpoints.LOGIN,
                new StringEntity("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"),
                new BasicNameValuePair("content-type", "application/json")))).get("token");
    }

    /**
     * Sends a message to the specified channel.
     *
     * @param content   The actual message to send
     * @param channelID The channel to send the message to
     * @param mentions  All user IDs you have @mentioned in the content field.
     * @return The message that was sent.
     * @throws IOException
     * @throws ParseException
     */
    public Message sendMessage(String content, String channelID, String... mentions) throws IOException, ParseException {
        if (this.ready) {

            String mention = "";
            for (String s : mentions) {
                mention += (s + ",");
            }
            if (mentions.length > 0)
                mention = mention.substring(0, mention.length() - 1);

            String response = Requests.POST.makeRequest(DiscordEndpoints.CHANNELS + channelID + "/messages",
                    new StringEntity("{\"content\":\"" + content + "\",\"mentions\":[" + mention + "]}"),
                    new BasicNameValuePair("authorization", token),
                    new BasicNameValuePair("content-type", "application/json"));

            JSONObject object1 = (JSONObject) JSON_PARSER.parse(response);
            String messageID = (String) object1.get("id");

            Message message = new Message(messageID, content, this.ourUser.getId(), this.ourUser.getName(), channelID, mentions);
            this.onMessageSend(message);
            return message;
        } else {
            System.err.println("Hold your horses! The bot hasn't signed in yet!");
            return null;
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
        if (this.ready) {
            Requests.DELETE.makeRequest(DiscordEndpoints.CHANNELS + channelID + "/messages/" + messageID,
                    new BasicNameValuePair("authorization", token));
        } else {
            System.err.println("Hold your horses! The bot hasn't signed in yet!");
        }
    }

    /**
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
        String response = Requests.PATCH.makeRequest(DiscordEndpoints.USERS + "@me",
                new StringEntity(s),
                new BasicNameValuePair("Authorization", token),
                new BasicNameValuePair("content-type", "application/json; charset=UTF-8"));
        JSONObject object1 = (JSONObject) JSON_PARSER.parse(response);
        this.token = (String) object1.get("token");
    }

    /**
     * Gets the last 50 messages from a given channel ID.
     *
     * @param channelID The channel to get messages from.
     * @return Last 50 messages from the channel.
     * @throws IOException
     * @throws ParseException
     */
    private List<Message> getChannelMessages(String channelID) throws Exception {
        List<Message> messages = new ArrayList<>();
        String response = Requests.GET.makeRequest(DiscordEndpoints.CHANNELS + channelID + "/messages?limit=50",
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

            messages.add(new Message((String) object1.get("id"),
                    (String) object1.get("content"),
                    (String) author.get("id"),
                    (String) author.get("username"),
                    (String) object1.get("channel_id"),
                    mentionsArray));
        }
        return messages;
    }

    /**
     * Joins a channel based on an invite code.
     *
     * @param inviteCode The code given. Note that you will need to parse this from
     *                   https://discord.gg/invite-code-here/
     *                   to
     *                   invite-code-here
     * @return An Invite object, containing most relevant information.
     * @throws IOException
     * @throws ParseException
     */
    public Invite acceptInvite(String inviteCode) throws IOException, ParseException {
        if (this.ready) {
            String response = Requests.POST.makeRequest(DiscordEndpoints.INVITE + inviteCode,
                    new BasicNameValuePair("Authorization", token));

            JSONObject object1 = (JSONObject) JSON_PARSER.parse(response);
            JSONObject guild = (JSONObject) object1.get("guild");
            JSONObject inviter = (JSONObject) object1.get("inviter");
            JSONObject channel = (JSONObject) object1.get("channel");

            return new Invite((String) inviter.get("id"),
                    (String) inviter.get("username"),
                    (String) guild.get("id"),
                    (String) guild.get("name"),
                    (String) channel.get("id"),
                    (String) channel.get("name"));
        } else {
            System.err.println("Hold your horses! The bot hasn't signed in yet!");
            return null;
        }
    }

    /**
     * @return Whether or not the bot is ready to be used (is it logged in?)
     */
    public boolean isReady() {
        return ready;
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
                if (channel.getChannelID().equalsIgnoreCase(id))
                    return channel;
            }
        }

        return null;
    }

    /**
     * New Request system. Reflection is cool, right guys?
     * R-right...?
     */
    private enum Requests {
        POST(HttpPost.class),
        GET(HttpGet.class),
        DELETE(HttpDelete.class),
        PATCH(HttpPatch.class);

        static final HttpClient CLIENT = HttpClients.createDefault();

        final Class<? extends HttpUriRequest> requestClass;

        Requests(Class<? extends HttpUriRequest> clazz) {
            this.requestClass = clazz;
        }

        public Class<? extends HttpUriRequest> getRequestClass() {
            return requestClass;
        }

        public String makeRequest(String url, BasicNameValuePair... headers) {
            try {
                HttpUriRequest request = this.requestClass.getConstructor(String.class).newInstance(url);
                for (BasicNameValuePair header : headers) {
                    request.addHeader(header.getName(), header.getValue());
                }
                return EntityUtils.toString(CLIENT.execute(request).getEntity());
            } catch (Exception e) {
                Discord4J.logger.error("Unable to make request to {}. ({})", url, e.getMessage());
                return null;
            }
        }

        public String makeRequest(String url, HttpEntity entity, BasicNameValuePair... headers) {
            try {
                if (HttpEntityEnclosingRequestBase.class.isAssignableFrom(this.requestClass)) {
                    HttpEntityEnclosingRequestBase request = (HttpEntityEnclosingRequestBase)
                            this.requestClass.getConstructor(String.class).newInstance(url);
                    for (BasicNameValuePair header : headers) {
                        request.addHeader(header.getName(), header.getValue());
                    }
                    request.setEntity(entity);
                    return EntityUtils.toString(CLIENT.execute(request).getEntity());
                } else {
                    Discord4J.logger.error("Tried to attach HTTP entity to invalid type! ({})",
                            this.requestClass.getSimpleName());
                }
            } catch (Exception e) {
                Discord4J.logger.error("Unable to make request to {}. ({})", url, e.getMessage());
            }
            return null;
        }
    }
}
