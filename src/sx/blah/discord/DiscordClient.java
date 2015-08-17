package sx.blah.discord;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
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
import sx.blah.discord.obj.Message;
import sx.blah.discord.obj.User;

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
     * Re-usable instance of JSONParser.
     */
    private static final JSONParser JSON_PARSER = new JSONParser();

    public DiscordClient(String email, String password) throws URISyntaxException, IOException, ParseException {
        super(new URI(DiscordEndpoints.WEBSOCKET_HUB), new Draft_10());

        token = login(email, password);
        Discord4J.debug("Logged in with token " + token + ".");
        Discord4J.debug("Connecting to Discord WebSocket...");

        this.connect();
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        if (!token.isEmpty()) {
            send("{\"op\":2,\"d\":{\"token\":\"" + token + "\",\"properties\":{\"$os\":\"Linux\",\"$browser\":\"DesuBot\",\"$device\":\"DesuBot\",\"$referrer\":\"\",\"$referring_domain\":\"\"},\"v\":2}}");
            Discord4J.debug("Connected.");
            new Thread(() -> {
                // Keep alive
                while (true) {
                    long l;
                    if ((l = (System.currentTimeMillis() - timer)) >= 41250) {
                        String s = "{\"op\":1,\"d\":" + System.currentTimeMillis() + "}";
                        Discord4J.debug("Sending keep alive... (" + s + "). Took " + l + " ms.");
                        send(s);
                        timer = System.currentTimeMillis();
                    }
                }
            }).start();
        } else System.err.println("Use the login() method to set your token first!");
    }

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
                        Discord4J.debug("Message from: " + username + " (" + id + ") " + " in channel ID " + channelID + ": " + content);
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

                    this.ourUser = new User(username, id);
                    Discord4J.debug("Connected as " + username + " " + id);
                    this.ready = true;
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
                    Discord4J.debug("Unknown message received (ignoring): " + message);
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

    public void deleteMessage(String messageID, String channelID) throws IOException {
        if (this.ready) {
            HttpClient client = HttpClients.createDefault();
            HttpDelete post = new HttpDelete("https://discordapp.com/api/channels/" + channelID + "/messages/" + messageID);
            post.addHeader("Authorization", token);
            //post.addHeader("content-type", "application/json");
            client.execute(post);
        } else {
            System.err.println("Hold your horses! The bot hasn't signed in yet!");
        }
    }

    public boolean isReady() {
        return ready;
    }

    /**
     * @return Gets the user the bot has signed in as.
     */
    public User getOurUser() {
        return ourUser;
    }
}
