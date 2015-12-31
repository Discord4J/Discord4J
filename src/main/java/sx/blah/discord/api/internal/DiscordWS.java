package sx.blah.discord.api.internal;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import sx.blah.discord.Discord4J;
import sx.blah.discord.handle.impl.events.*;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.json.requests.ConnectRequest;
import sx.blah.discord.json.requests.KeepAliveRequest;
import sx.blah.discord.json.responses.*;
import sx.blah.discord.json.responses.events.*;
import sx.blah.discord.util.HTTP403Exception;
import sx.blah.discord.util.Presences;

import java.net.URI;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class DiscordWS extends WebSocketClient {

	private DiscordClientImpl client;
	public AtomicBoolean isConnected = new AtomicBoolean(true);
	
	public DiscordWS(DiscordClientImpl client, URI serverURI) {
		super(serverURI);
		this.client = client;
		this.client = client;
		this.connect();
	}
	
	public void disconnect() {
		isConnected.set(false);
		close();
	}

	
	@Override
	public void onOpen(ServerHandshake serverHandshake) {
		if (!client.token.isEmpty()) {
			send(DiscordUtils.GSON.toJson(new ConnectRequest(client.token, "Java", Discord4J.NAME, Discord4J.NAME, "", "")));
			Discord4J.LOGGER.debug("Connected to the Discord websocket.");
		} else 
			Discord4J.LOGGER.error("Use the login() method to set your token first!");
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
				Discord4J.LOGGER.error("Received unknown error from Discord. Frame: {}", frame);
			} else
				Discord4J.LOGGER.error("Received error from Discord: {}. Frame: {}", message, frame);
		}
		String type = object.get("t").getAsString();
		JsonElement eventObject = object.get("d");

		switch (type) {
			case "READY":
				
				ReadyEventResponse event = DiscordUtils.GSON.fromJson(eventObject, ReadyEventResponse.class);
				
				client.ourUser = DiscordUtils.constructUserFromJSON(client, event.user);

				client.heartbeat = event.heartbeat_interval;
				Discord4J.LOGGER.debug("Received heartbeat interval of {}.", client.heartbeat);

				// I hope you like loops.
				for (GuildResponse guildResponse : event.guilds) {
					Guild guild;
					client.guildList.add(guild = new Guild(client, guildResponse.name, guildResponse.name, guildResponse.icon, guildResponse.owner_id));

					for (GuildResponse.MemberResponse member : guildResponse.members) {
						guild.addUser(new User(client, member.user.username, member.user.id, member.user.avatar));
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
							guild.addChannel(channel = new Channel(client, channelResponse.name, channelResponse.id, guild));
							try {
								DiscordUtils.getChannelMessages(client, channel);
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
					User recipient = new User(client, privateChannelResponse.recipient.username, privateChannelResponse.recipient.id, privateChannelResponse.recipient.avatar);
					PrivateChannel channel = new PrivateChannel(client, recipient, id);
					try {
						DiscordUtils.getChannelMessages(client, channel);
					} catch (HTTP403Exception e) {
						Discord4J.LOGGER.error("No permission for the private channel for \"{}\". Are you logged in properly?", channel.getRecipient().getName());
					} catch (Exception e) {
						Discord4J.LOGGER.error("Unable to get messages for the private channel for \"{}\" (Cause: {}).", channel.getRecipient().getName(), e.getClass().getSimpleName());
					}
					client.privateChannels.add(channel);
				}

				Discord4J.LOGGER.debug("Logged in as {} (ID {}).", client.ourUser.getName(), client.ourUser.getID());
				new Thread(() -> {
					// Keep alive
					while (this.isConnected.get()) {
						long l;
						if ((l = (System.currentTimeMillis() - client.timer)) >= client.heartbeat) {
							Discord4J.LOGGER.debug("Sending keep alive... ({}). Took {} ms.", System.currentTimeMillis(), l);
							send(DiscordUtils.GSON.toJson(new KeepAliveRequest()));
							client.timer = System.currentTimeMillis();
						}
					}
				}).start();
				
				client.isReady = true;
				client.dispatcher.dispatch(new ReadyEvent());
				break;

			case "MESSAGE_CREATE":
				MessageResponse event1 = DiscordUtils.GSON.fromJson(eventObject, MessageResponse.class);
				boolean mentioned = event1.mention_everyone || event1.content.contains("<@"+client.ourUser.getID()+">");
				
				Channel channel = client.getChannelByID(event1.channel_id);
				
				if (null != channel) {
					Message message1 = new Message(client, event1.id, event1.content, client.getUserByID(event1.author.id),
							channel, DiscordUtils.convertFromTimestamp(event1.timestamp));
					if (!event1.author.id.equalsIgnoreCase(client.getOurUser().getID())) {
						channel.addMessage(message1);
						Discord4J.LOGGER.debug("Message from: {} ({}) in channel ID {}: {}", message1.getAuthor().getName(), 
								event1.author.id, event1.channel_id, event1.content);
						if (event1.content.contains("discord.gg/")) {
							String inviteCode = event1.content.split("discord\\.gg/")[1].split(" ")[0];
							Discord4J.LOGGER.debug("Received invite code \"{}\"", inviteCode);
							client.dispatcher.dispatch(new InviteReceivedEvent(client.getInviteForCode(inviteCode), message1));
						}
						if (mentioned) {
							client.dispatcher.dispatch(new MentionEvent(message1));
						}
						client.dispatcher.dispatch(new MessageReceivedEvent(message1));
					}
				}
				break;

			case "TYPING_START":
				TypingEventResponse event2 = DiscordUtils.GSON.fromJson(eventObject, TypingEventResponse.class);

				User user;
				channel = client.getChannelByID(event2.channel_id);
				if (channel.isPrivate()) {
					user = ((PrivateChannel)channel).getRecipient();
				} else {
					user = channel.getParent().getUserByID(event2.user_id);
				}
				if(null != channel
						&& null != user) {
					client.dispatcher.dispatch(new TypingEvent(user, channel));
				}
				break;

			case "GUILD_CREATE":
				GuildResponse event3 = DiscordUtils.GSON.fromJson(eventObject, GuildResponse.class);
				Guild guild = new Guild(client, event3.name, event3.id, event3.icon, event3.owner_id);
				client.guildList.add(guild);

				for (GuildResponse.MemberResponse member : event3.members) {
					guild.addUser(new User(client, member.user.username, member.user.id, member.user.avatar));
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
						guild.addChannel(channel1 = new Channel(client, channelResponse.name, channelResponse.id, guild));
						try {
							DiscordUtils.getChannelMessages(client, channel1);
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
				GuildMemberAddEventResponse event4 = DiscordUtils.GSON.fromJson(eventObject, GuildMemberAddEventResponse.class);
				user = DiscordUtils.constructUserFromJSON(client, event4.user);
				String guildID = event4.guild_id;
				guild = client.getGuildByID(guildID);
				if(null != guild) {
					guild.addUser(user);
					Discord4J.LOGGER.debug("User \"{}\" joined guild \"{}\".", user.getName(), guild.getName());
					client.dispatcher.dispatch(new UserJoinEvent(guild, user, DiscordUtils.convertFromTimestamp(event4.joined_at)));
				}
				break;

			case "GUILD_MEMBER_REMOVE":
				GuildMemberRemoveEventResponse event5 = DiscordUtils.GSON.fromJson(eventObject, GuildMemberRemoveEventResponse.class);
				user = DiscordUtils.constructUserFromJSON(client, event5.user);
				guildID = event5.guild_id;
				guild = client.getGuildByID(guildID);
				if(null != guild
						&& guild.getUsers().contains(user)) {
					guild.getUsers().remove(user);
					Discord4J.LOGGER.debug("User \"{}\" has been removed from or left guild \"{}\".", user.getName(), guild.getName());
					client.dispatcher.dispatch(new UserLeaveEvent(guild, user));
				}
				break;

			case "MESSAGE_UPDATE":
				MessageResponse event6 = DiscordUtils.GSON.fromJson(eventObject, MessageResponse.class);
				String id = event6.id;
				String channelID = event6.channel_id;
				String content = event6.content;

				channel = client.getChannelByID(channelID);
				Message m = channel.getMessageByID(id);
				if(null != m
						&& !m.getAuthor().getID().equals(client.getOurUser().getID())
						&& !m.getContent().equals(content)) {
					Message newMessage;
					int index = channel.getMessages().indexOf(m);
					channel.getMessages().remove(m);
					channel.getMessages().add(index, newMessage = new Message(client, id, content, m.getAuthor(), channel, m.getTimestamp()));
					client.dispatcher.dispatch(new MessageUpdateEvent(m, newMessage));
				}
				break;

			case "MESSAGE_DELETE":
				MessageDeleteEventResponse event7 = DiscordUtils.GSON.fromJson(eventObject, MessageDeleteEventResponse.class);
				id = event7.id;
				channelID = event7.channel_id;
				channel = client.getChannelByID(channelID);
				if (null != channel) {
					Message message = channel.getMessageByID(id);
					if (null != message
							&& !message.getAuthor().getID().equalsIgnoreCase(client.ourUser.getID())) {
						channel.getMessages().remove(message);
						client.dispatcher.dispatch(new MessageDeleteEvent(message));
					}
				}
				break;

			case "PRESENCE_UPDATE":
				PresenceUpdateEventResponse event8 = DiscordUtils.GSON.fromJson(eventObject, PresenceUpdateEventResponse.class);
				Presences presences = Presences.valueOf(event8.status.toUpperCase());
				String gameName = event8.game == null ? null : event8.game.name;
				guild = client.getGuildByID(event8.guild_id);
				if(null != guild
						&& null != presences) {
					user = guild.getUserByID(event8.user.id);
					if(null != user) {
						if (!user.getPresence().equals(presences)) {
							client.dispatcher.dispatch(new PresenceUpdateEvent(guild, user, user.getPresence(), presences));
							user.setPresence(presences);
							Discord4J.LOGGER.debug("User \"{}\" changed presence to {}", user.getName(), user.getPresence());
						}
						if (!user.getGame().equals(Optional.ofNullable(gameName))) {
							client.dispatcher.dispatch(new GameChangeEvent(guild, user, user.getGame().isPresent() ? user.getGame().get() : null, gameName));
							user.setGame(gameName);
							Discord4J.LOGGER.debug("User \"{}\" changed game to {}.", user.getName(), gameName);
						}
					}
				}
				break;

			case "GUILD_DELETE":
				GuildResponse event9 = DiscordUtils.GSON.fromJson(eventObject, GuildResponse.class);
				guild = client.getGuildByID(event9.id);
				client.getGuilds().remove(guild);
				Discord4J.LOGGER.debug("You have been kicked from or left \"{}\"! :O", guild.getName());
				client.dispatcher.dispatch(new GuildLeaveEvent(guild));
				break;

			case "CHANNEL_CREATE":
				boolean isPrivate = eventObject.getAsJsonObject().get("is_private").getAsBoolean();
				
				if(isPrivate) { // PM channel.
					PrivateChannelResponse event10 = DiscordUtils.GSON.fromJson(eventObject, PrivateChannelResponse.class);
					id = event10.id;
					boolean b = false;
					for(PrivateChannel privateChannel : client.privateChannels) {
						if(privateChannel.getID().equalsIgnoreCase(id))
							b = true;
					}

					if(b) break; // we already have this PM channel; no need to create another.
					user = DiscordUtils.constructUserFromJSON(client, event10.recipient);
					PrivateChannel privateChannel;
					client.privateChannels.add(privateChannel = new PrivateChannel(client, user, id));
					try {
						DiscordUtils.getChannelMessages(client, privateChannel);
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				} else { // Regular channel.
					ChannelCreateEventResponse event10 = DiscordUtils.GSON.fromJson(eventObject, ChannelCreateEventResponse.class);
					id = event10.id;
					type = event10.type;
					if("text".equalsIgnoreCase(type)) {
						String name = event10.name;
						guildID = event10.guild_id;
						guild = client.getGuildByID(guildID);
						if(null != guild) {
							channel = new Channel(client, name, id, guild);
							try {
								DiscordUtils.getChannelMessages(client, channel);
							} catch (Exception e) {
								e.printStackTrace();
							}
							guild.addChannel(channel);
							client.dispatcher.dispatch(new ChannelCreateEvent(channel));
						}
					}
				}
				break;

			case "CHANNEL_DELETE":
				ChannelCreateEventResponse event11 = DiscordUtils.GSON.fromJson(eventObject, ChannelCreateEventResponse.class);
				if("text".equalsIgnoreCase(event11.type)) {
					channel = client.getChannelByID(event11.id);
					client.dispatcher.dispatch(new ChannelDeleteEvent(channel));
					channel.getParent().getChannels().remove(channel);
				}
				break;
			
			case "USER_UPDATE":
				UserUpdateEventResponse event12 = DiscordUtils.GSON.fromJson(eventObject, UserUpdateEventResponse.class);
				User newUser = client.getUserByID(event12.id);
				if (newUser != null) {
					User oldUser = new User(client, newUser.getName(), newUser.getID(), newUser.getAvatar());
					newUser.setName(event12.username);
					newUser.setAvatar(event12.avatar);
					client.dispatcher.dispatch(new UserUpdateEvent(oldUser, newUser));
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
