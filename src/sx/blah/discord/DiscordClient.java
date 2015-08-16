package sx.blah.discord;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
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
     * Used for keep alive.
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
        Discord4J.debug("Logged in (" + token + ").");
        Discord4J.debug("Connecting...");

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

            if (s.equalsIgnoreCase("MESSAGE_CREATE")) {
                JSONObject d = (JSONObject) object.get("d");
                JSONObject author = (JSONObject) d.get("author");

                String username = (String) author.get("username");
                String id = (String) author.get("id");
                String channelID = (String) d.get("channel_id");
                String content = (String) d.get("content");
                JSONArray array = (JSONArray) d.get("mentions");

                String[] mentionedIDs = new String[array.size()];
                for (int i = 0; i < array.size(); i++) {
                    JSONObject userInfo = (JSONObject) array.get(i);
                    mentionedIDs[i] = (String) userInfo.get("id");
                }

                if (!id.equalsIgnoreCase(ourUser.getId())
                        && this.ready) {
                    Discord4J.debug("Message from: " + username + " (" + id + ") " + " in channel ID " + channelID + ": " + content);
                    this.onMessageReceive(new Message(content, id, username, channelID, mentionedIDs));
                }
            } else if (s.equalsIgnoreCase("READY")) {
                JSONObject info = (JSONObject) object.get("d");

                JSONObject user = (JSONObject) info.get("user");
                String id = (String) user.get("id");
                String username = (String) user.get("username");

                this.ourUser = new User(username, id);
                Discord4J.debug("Connected as " + username + " " + id);
                this.ready = true;
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

    public abstract void onMessageReceive(Message message);

    public abstract void onMessageSend(Message m);

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

    public void sendMessage(String content, String channelID, String... mentions) throws IOException, ParseException {
        if (this.ready) {
            HttpClient client = HttpClients.createDefault();
            HttpPost post = new HttpPost("https://discordapp.com/api/channels/" + channelID + "/messages");
            post.addHeader("Authorization", token);
            post.addHeader("content-type", "application/json");
            String mention = "";
            for (String s : mentions) {
                mention += (s + ",");
            }
            mention = mention.substring(0, mention.length() - 1);
            post.setEntity(new StringEntity("{\"content\":\"" + content + "\",\"mentions\":[" + mention + "]}"));
            client.execute(post);

            this.onMessageSend(new Message(content, this.ourUser.getId(), this.ourUser.getName(), channelID, mentions));
        } else System.err.println("Hold your horses! The bot hasn't signed in yet!");
    }

    public boolean isReady() {
        return ready;
    }

    public User getOurUser() {
        return ourUser;
    }
}
