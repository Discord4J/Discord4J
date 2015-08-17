package sx.blah.discord;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
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

    public DiscordClient(String email, String password) throws URISyntaxException, IOException, ParseException {
        super(new URI(DiscordEndpoints.WEBSOCKET_HUB), new Draft_10());

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

                    if (!id.equalsIgnoreCase(ourUser.getId())
                            && this.ready) {
                        Discord4J.logger.debug("Message from: {} ({}) in channel ID {}: {}", username, id, channelID, content);
                        Message message1 = new Message(messageID, content, id, username, channelID, mentionedIDs);
                        this.onMessageReceive(message1);
                        if (mentioned) {
                            this.onMentioned(message1);
                        }
                    }
                    break;

                case "READY":
                    //TODO get all guilds, channels and their related info.
                    d = (JSONObject) object.get("d");

                    JSONObject user = (JSONObject) d.get("user");
                    id = (String) user.get("id");
                    username = (String) user.get("username");

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
                                try {
                                    channelList.add(new Channel((String) channel.get("name"), channelID, this.getChannelMessages(channelID)));
                                } catch (IOException e) {
                                    System.err.println("Unable to fetch messages for channel " + channelID + ".");
                                    channelList.add(new Channel((String) channel.get("name"), channelID));
                                }
                            }
                        }

                        guildList.add(new Guild(name, guildID, channelList, memberList));
                    }

                    this.ourUser = new User(username, id);
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

                case "PRESENCE_UPDATE":
                    d = (JSONObject) object.get("d");
                    // todo lol
                    // Guilds (servers) come first as this message has no channel ID parameter.
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
     * When our bot gets mentioned in a message.
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
     * @param messageID
     * @param channelID
     */
    public abstract void onMessageDelete(String messageID, String channelID);

    /**
     * Generic method for sending requests to given URLs.
     *
     * @param url        Where to send the request.
     * @param parameters Parameters of the request
     * @return Lines given back to us by the server
     * @throws IOException
     */
    private String sendRequest(String url, List<BasicNameValuePair> parameters) throws IOException {
        HttpClient client = HttpClients.createDefault();
        HttpPost post = new HttpPost(url);
        post.setEntity(new UrlEncodedFormEntity(parameters, "UTF-8"));

        HttpResponse response = client.execute(post);
        HttpEntity entity = response.getEntity();

        if (null != entity) {
            return EntityUtils.toString(entity);
        } else {
            return "{}";
        }
    }

    /**
     * Logs you into Discord using given
     * email and password.
     *
     * @return The User object of who you log in as.
     */
    public String login(String email, String password)
            throws IOException, ParseException {

        List<BasicNameValuePair> param = new ArrayList<>();
        param.add(new BasicNameValuePair("email", email));
        param.add(new BasicNameValuePair("password", password));

        JSONParser parser = new JSONParser();

        return ((JSONObject) parser.parse(sendRequest(DiscordEndpoints.LOGIN, param))).get("token").toString();

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
            HttpClient client = HttpClients.createDefault();
            HttpPost post = new HttpPost("https://discordapp.com/api/channels/" + channelID + "/messages");
            post.addHeader("Authorization", token);
            post.addHeader("content-type", "application/json");
            String mention = "";
            for (String s : mentions) {
                mention += (s + ",");
            }
            if (mentions.length > 0)
                mention = mention.substring(0, mention.length() - 1);

            post.setEntity(new StringEntity("{\"content\":\"" + content + "\",\"mentions\":[" + mention + "]}"));
            String response = EntityUtils.toString(client.execute(post).getEntity());
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
            HttpClient client = HttpClients.createDefault();
            HttpDelete delete = new HttpDelete("https://discordapp.com/api/channels/" + channelID + "/messages/" + messageID);
            delete.addHeader("Authorization", token);
            //post.addHeader("content-type", "application/json");
            client.execute(delete);
        } else {
            System.err.println("Hold your horses! The bot hasn't signed in yet!");
        }
    }

    /**
     * Gets the last 50 messages from a given channel ID.
     *
     * @param channelID The channel to get messages from.
     * @return Last 50 messages from the channel.
     * @throws IOException
     * @throws ParseException
     */
    private List<Message> getChannelMessages(String channelID) throws IOException, ParseException {
        List<Message> messages = new ArrayList<>();
        HttpClient client = HttpClients.createDefault();
        HttpGet get = new HttpGet("https://discordapp.com/api/channels/" + channelID + "/messages?limit=50");
        get.addHeader("Authorization", token);
        //post.addHeader("content-type", "application/json");
        String response = EntityUtils.toString(client.execute(get).getEntity());
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

    public Invite acceptInvite(String inviteCode) throws IOException, ParseException {
        if (this.ready) {
            HttpClient client = HttpClients.createDefault();
            HttpPost post = new HttpPost("https://discordapp.com/api/invite/" + inviteCode);
            post.addHeader("Authorization", token);
            String response = EntityUtils.toString(client.execute(post).getEntity());
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
     * @return Gets the user the bot has signed in as.
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
}
