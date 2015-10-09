// Discord4J - Unofficial wrapper for Discord API
// Copyright (c) 2015
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

package sx.blah.discord;

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
import sx.blah.discord.util.Requests;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
	 * Our token, so we can send messages
	 */
	private String token;

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
	private final IDispatcher dispatcher;

	public DiscordClient() {
		dispatcher = new EventDispatcher();
	}

	/**
	 * @return Singleton instance
	 */
	public static DiscordClient get() {
		return __INSTANCE;
	}

	/**
	 * Gets the event dispatcher. This will send events to all listeners
	 *
	 * @return
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
     * email and password.
     */
    public void login(String email, String password)
		    throws IOException, ParseException, URISyntaxException {
	    if (null != ws) {
		    ws.close();
	    }

	    this.email = email;
	    this.password = password;

	    this.token = (String) ((JSONObject) JSON_PARSER.parse(Requests.POST.makeRequest(DiscordEndpoints.LOGIN,
                new StringEntity("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"),
                new BasicNameValuePair("content-type", "application/json")))).get("token");

	    this.ws = new DiscordWS(new URI(obtainGateway(this.token)));
    }

	/**
	 * Gets the WebSocket gateway
	 * @param token Our login token
	 * @return the WebSocket URL of which to connect
	 * @throws ParseException
	 */
	private String obtainGateway(String token) throws ParseException {
		String s = ((String) ((JSONObject)JSON_PARSER.parse(
				Requests.GET.makeRequest("https://discordapp.com/api/gateway",
						new BasicNameValuePair("authorization", token)))).get("url")).replaceAll("wss", "ws");
		Discord4J.logger.debug("Obtained gateway {}.", s);
		return s;
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
        if (null != ws) {

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
	        String time = (String) object1.get("timestamp");
            String messageID = (String) object1.get("id");

            Message message = new Message(messageID, content, this.ourUser, channelID, mentions, this.convertFromTimestamp(time));
	        DiscordClient.this.dispatcher.dispatch(new MessageSendEvent(message));
	        return message;
        } else {
	        Discord4J.logger.error("Bot has not signed in yet!");
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
        if (this.isReady()) {
            Requests.DELETE.makeRequest(DiscordEndpoints.CHANNELS + channelID + "/messages/" + messageID,
                    new BasicNameValuePair("authorization", token));
        } else {
	        Discord4J.logger.error("Bot has not signed in yet!");
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
                    this.getUserByID((String) author.get("id")),
                    (String) object1.get("channel_id"),
                    mentionsArray, this.convertFromTimestamp((String) object1.get("timestamp"))));
        }
	    return messages;
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
                if (channel.getChannelID().equalsIgnoreCase(id))
                    return channel;
            }
        }

        return null;
    }

	public Guild getGuildByID(String guildID) {
		for(Guild guild : guildList) {
			if(guild.getID().equalsIgnoreCase(guildID))
				return guild;
		}

		return null;
	}

	public User getUserByID(String userID) {
		User u = null;
		for(Guild guild : guildList) {
			if(null == u) {
				u = guild.getUserByID(userID);
			}
		}

		return u;
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
				Discord4J.logger.debug("Connected.");
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
				String s = (String) object.get("t");
				JSONObject d = (JSONObject) object.get("d");

				switch (s) {
					case "READY":
						JSONObject user = (JSONObject) d.get("user");
						String id = (String) user.get("id");
						String username = (String) user.get("username");
						String avatar = (String) user.get("avatar");

						DiscordClient.this.ourUser = new User(username, id, avatar);

						DiscordClient.this.keepaliveDelay = (long) d.get("heartbeat_interval");
						Discord4J.logger.debug("Received heartbeat interval of {}.", DiscordClient.this.keepaliveDelay);

						JSONArray guilds = (JSONArray) d.get("guilds");

						// I hope you like loops.
						for (Object o : guilds) {
							JSONObject guild = (JSONObject) o;
							JSONArray members = (JSONArray) guild.get("members");
							JSONArray channels = (JSONArray) guild.get("channels");
							String name = (String) guild.get("name");
							String guildID = (String) guild.get("id");

							Guild g;
							guildList.add(g = new Guild(name, guildID));

							for (Object o1 : members) {
								JSONObject member = (JSONObject) ((JSONObject) o1).get("user");
								g.addUser(new User((String) member.get("username"), (String) member.get("id"), (String) member.get("avatar")));
							}

							for (Object o1 : channels) {
								JSONObject channel = (JSONObject) o1;
								String type = (String) channel.get("type");
								if ("text".equalsIgnoreCase(type)) {
									String channelID = (String) channel.get("id");
									String chName = (String) channel.get("name");
									List<Message> messages = new ArrayList<>();
									try {
										messages = DiscordClient.this.getChannelMessages(channelID);
									} catch (Exception e) {
										Discord4J.logger.error("Unable to fetch messages for channel {} ({}). Cause: {}.", channelID, chName, e.getClass().getSimpleName());
									}
									g.addChannel(new Channel(chName, channelID, messages));
								}
							}
						}

						Discord4J.logger.debug("Connected as {} ({}).", username, id);
						new Thread(() -> {
							// Keep alive
							while (null != ws) {
								long l;
								if ((l = (System.currentTimeMillis() - timer)) >= keepaliveDelay) {
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

						username = (String) author.get("username");
						id = (String) author.get("id");
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

						Message message1 = new Message(messageID, content, DiscordClient.get().getUserByID(id),
								channelID, mentionedIDs, DiscordClient.get().convertFromTimestamp(time));
						Channel c = DiscordClient.get().getChannelByID(channelID);
						if (null != c)
							c.addMessage(message1);
						if (!id.equalsIgnoreCase(DiscordClient.get().getOurUser().getID())) {
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
						break;


					case "TYPING_START":
						// id = (String) d.get("id");
						// channelID = (String) d.get("channel_id");

						//todo call event
						break;

					case "GUILD_CREATE":
						String name = (String) d.get("name");
						id = (String) d.get("id");
						JSONArray members = (JSONArray) d.get("members");
						JSONArray channels = (JSONArray) d.get("channels");

						List<User> memberList = new ArrayList<>();
						for (Object o : members) {
							JSONObject object1 = (JSONObject) o;
							memberList.add(new User((String) object1.get("username"),
									(String) object1.get("id"), (String) object1.get("avatar")));
						}

						List<Channel> channelList = new ArrayList<>();
						for (Object o : channels) {
							JSONObject channel = (JSONObject) o;
							if (((String) channel.get("type")).equalsIgnoreCase("text")) {
								channelList.add(new Channel((String) channel.get("name"), (String) channel.get("id")));
							}
						}
						Guild guild = new Guild(name, id, channelList, memberList);
						DiscordClient.this.guildList.add(guild);
						DiscordClient.get().dispatcher.dispatch(new GuildCreateEvent(guild));
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
						id = (String) d.get("id");
						channelID = (String) d.get("channel_id");
						c = DiscordClient.this.getChannelByID(channelID);
						if (null != c) {
							message1 = c.getMessageByID(id);
							if (null != message1) {
								DiscordClient.get().dispatcher.dispatch(new MessageDeleteEvent(message1));
							}
						}
						break;

					case "PRESENCE_UPDATE":
						// todo lol
						break;

					default:
						Discord4J.logger.warn("Unknown message received (ignoring): {}", frame);
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
