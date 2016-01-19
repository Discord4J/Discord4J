package sx.blah.discord.api.internal;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_10;
import org.java_websocket.handshake.ServerHandshake;
import sx.blah.discord.Discord4J;
import sx.blah.discord.handle.impl.events.*;
import sx.blah.discord.handle.impl.obj.*;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.json.requests.ConnectRequest;
import sx.blah.discord.json.requests.KeepAliveRequest;
import sx.blah.discord.json.requests.ResumeRequest;
import sx.blah.discord.json.responses.*;
import sx.blah.discord.json.responses.events.*;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.InflaterInputStream;

public class DiscordWS extends WebSocketClient {
	
	private DiscordClientImpl client;
	private static final HashMap<String, String> headers = new HashMap<>();
	public AtomicBoolean isConnected = new AtomicBoolean(true);
	/**
	 * The amount of users a guild must have to be considered "large"
	 */
	public static final int LARGE_THRESHOLD = 50;
	
	static {
		headers.put("Accept-Encoding", "gzip");
	}
	
	public DiscordWS(DiscordClientImpl client, URI serverURI) {
		super(serverURI, new Draft_10(), headers, 0); //Same as super(serverURI) but I added custom headers
//		super(serverURI);
		this.client = client;
		this.connect();
	}
	
	/**
	 * Disconnects the client WS.
	 */
	public void disconnect() {
		isConnected.set(false);
		close();
	}
	
	
	@Override
	public void onOpen(ServerHandshake serverHandshake) {
		if (client.sessionId != null) {
			send(DiscordUtils.GSON.toJson(new ResumeRequest(client.sessionId, client.lastSequence)));
			Discord4J.LOGGER.debug("Reconnected to the Discord websocket.");
		} else if (!client.token.isEmpty()) {
			send(DiscordUtils.GSON.toJson(new ConnectRequest(client.token, "Java", Discord4J.NAME, Discord4J.NAME, "", "", LARGE_THRESHOLD, true)));
			Discord4J.LOGGER.debug("Connected to the Discord websocket.");
		} else
			Discord4J.LOGGER.error("Use the login() method to set your token first!");
	}
	
	private void startKeepalive() {
		new Thread(()->{
			// Keep alive
			while (this.isConnected.get()) {
				long l;
				if ((l = (System.currentTimeMillis()-client.timer)) >= client.heartbeat) {
					Discord4J.LOGGER.debug("Sending keep alive... ({}). Took {} ms.", System.currentTimeMillis(), l);
					send(DiscordUtils.GSON.toJson(new KeepAliveRequest()));
					client.timer = System.currentTimeMillis();
				}
			}
		}).start();
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
		int op = object.get("op").getAsInt();
		
		if (op != 7) //Not a redirect op, so cache the last sequence value
			client.lastSequence = object.get("s").getAsLong();
		
		if (op == 0) { //Event dispatched
			String type = object.get("t").getAsString();
			JsonElement eventObject = object.get("d");
			
			switch (type) {
				case "RESUMED":
					resumed(eventObject);
					break;
				
				case "READY":
					ready(eventObject);
					break;
				
				case "MESSAGE_CREATE":
					messageCreate(eventObject);
					break;
				
				case "TYPING_START":
					typingStart(eventObject);
					break;
				
				case "GUILD_CREATE":
					guildCreate(eventObject);
					break;
				
				case "GUILD_MEMBER_ADD":
					guildMemberAdd(eventObject);
					break;
				
				case "GUILD_MEMBER_REMOVE":
					guildMemberRemove(eventObject);
					break;
				
				case "GUILD_MEMBER_UPDATE":
					guildMemberUpdate(eventObject);
					break;
				
				case "MESSAGE_UPDATE":
					messageUpdate(eventObject);
					break;
				
				case "MESSAGE_DELETE":
					messageDelete(eventObject);
					break;
				
				case "PRESENCE_UPDATE":
					presenceUpdate(eventObject);
					break;
				
				case "GUILD_DELETE":
					guildDelete(eventObject);
					break;
				
				case "CHANNEL_CREATE":
					channelCreate(eventObject);
					break;
				
				case "CHANNEL_DELETE":
					channelDelete(eventObject);
					break;
				
				case "USER_UPDATE":
					userUpdate(eventObject);
					break;
				
				case "CHANNEL_UPDATE":
					channelUpdate(eventObject);
					break;
				
				case "MESSAGE_ACK":
					messageAck(eventObject);
					break;
				
				case "GUILD_MEMBERS_CHUNK":
					guildMembersChunk(eventObject);
					break;
				
				case "GUILD_UPDATE":
					guildUpdate(eventObject);
					break;
				
				case "GUILD_ROLE_CREATE":
					guildRoleCreate(eventObject);
					break;
				
				case "GUILD_ROLE_UPDATE":
					guildRoleUpdate(eventObject);
					break;
				
				case "GUILD_ROLE_DELETE":
					guildRoleDelete(eventObject);
					break;
				
				case "GUILD_BAN_ADD":
					guildBanAdd(eventObject);
					break;
				
				case "GUILD_BAN_REMOVE":
					guildBanRemove(eventObject);
					break;
				
				default:
					Discord4J.LOGGER.warn("Unknown message received: {}, REPORT THIS TO THE DISCORD4J DEV! (ignoring): {}", eventObject.toString(), frame);
			}
		} else if (op == 7) { //Gateway is redirecting us
			RedirectResponse redirectResponse = DiscordUtils.GSON.fromJson(object.getAsJsonObject("d"), RedirectResponse.class);
			Discord4J.LOGGER.info("Received a gateway redirect request, closing the socket at reopening at {}", redirectResponse.url);
			try {
				client.ws = new DiscordWS(client, new URI(redirectResponse.url.replaceAll("wss", "ws")));
				disconnect();
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		} else {
			Discord4J.LOGGER.warn("Unhandled opcode received: {} (ignoring), REPORT THIS TO THE DISCORD4J DEV!", op);
		}
	}
	
	private void resumed(JsonElement eventObject) {
		ResumedEventResponse event = DiscordUtils.GSON.fromJson(eventObject, ResumedEventResponse.class);
		client.heartbeat = event.heartbeat_interval;
		startKeepalive();
	}
	
	private void ready(JsonElement eventObject) {
		ReadyEventResponse event = DiscordUtils.GSON.fromJson(eventObject, ReadyEventResponse.class);
		
		client.sessionId = event.session_id;
		
		client.ourUser = DiscordUtils.getUserFromJSON(client, event.user);
		
		client.heartbeat = event.heartbeat_interval;
		Discord4J.LOGGER.debug("Received heartbeat interval of {}.", client.heartbeat);
		
		// I hope you like loops.
		for (GuildResponse guildResponse : event.guilds) {
			if (guildResponse.unavailable) { //Guild can't be reached, so we ignore it
				Discord4J.LOGGER.warn("Guild with id {} is unavailable, ignoring it. Is there an outage?", guildResponse.id);
				continue;
			}
			
			client.guildList.add(DiscordUtils.getGuildFromJSON(client, guildResponse));
		}
		
		for (PrivateChannelResponse privateChannelResponse : event.private_channels) {
			PrivateChannel channel = (PrivateChannel) DiscordUtils.getPrivateChannelFromJSON(client, privateChannelResponse);
			client.privateChannels.add(channel);
		}
		
		for (ReadyEventResponse.ReadStateResponse readState : event.read_state) {
			Channel channel = (Channel) client.getChannelByID(readState.id);
			if (channel != null)
				channel.setLastReadMessageID(readState.last_message_id);
		}
		
		Discord4J.LOGGER.debug("Logged in as {} (ID {}).", client.ourUser.getName(), client.ourUser.getID());
		
		startKeepalive();
		
		client.isReady = true;
		client.dispatcher.dispatch(new ReadyEvent());
	}
	
	private void messageCreate(JsonElement eventObject) {
		MessageResponse event = DiscordUtils.GSON.fromJson(eventObject, MessageResponse.class);
		boolean mentioned = event.mention_everyone || event.content.contains("<@"+client.ourUser.getID()+">");
		
		Channel channel = (Channel) client.getChannelByID(event.channel_id);
		
		if (null != channel) {
			IMessage message = DiscordUtils.getMessageFromJSON(client, channel, event);
			if (!event.author.id.equalsIgnoreCase(client.getOurUser().getID())) {
				channel.addMessage(message);
				Discord4J.LOGGER.debug("Message from: {} ({}) in channel ID {}: {}", message.getAuthor().getName(),
						event.author.id, event.channel_id, event.content);
				
				if (event.content.contains("discord.gg/")) {
					String inviteCode = event.content.split("discord\\.gg/")[1].split(" ")[0];
					Discord4J.LOGGER.debug("Received invite code \"{}\"", inviteCode);
					client.dispatcher.dispatch(new InviteReceivedEvent(client.getInviteForCode(inviteCode), message));
				}
				
				if (mentioned) {
					client.dispatcher.dispatch(new MentionEvent(message));
				}
				
				client.dispatcher.dispatch(new MessageReceivedEvent(message));
			}
		}
	}
	
	private void typingStart(JsonElement eventObject) {
		TypingEventResponse event = DiscordUtils.GSON.fromJson(eventObject, TypingEventResponse.class);
		
		User user;
		Channel channel = (Channel) client.getChannelByID(event.channel_id);
		if (channel != null) {
			if (channel.isPrivate()) {
				user = (User) ((IPrivateChannel) channel).getRecipient();
			} else {
				user = (User) channel.getGuild().getUserByID(event.user_id);
			}
			
			if (user != null) {
				client.dispatcher.dispatch(new TypingEvent(user, channel));
			}
		}
	}
	
	private void guildCreate(JsonElement eventObject) {
		GuildResponse event = DiscordUtils.GSON.fromJson(eventObject, GuildResponse.class);
		Guild guild = (Guild) DiscordUtils.getGuildFromJSON(client, event);
		client.guildList.add(guild);
		client.dispatcher.dispatch(new GuildCreateEvent(guild));
		Discord4J.LOGGER.debug("New guild has been created/joined! \"{}\" with ID {}.", guild.getName(), guild.getID());
	}
	
	private void guildMemberAdd(JsonElement eventObject) {
		GuildMemberAddEventResponse event = DiscordUtils.GSON.fromJson(eventObject, GuildMemberAddEventResponse.class);
		String guildID = event.guild_id;
		Guild guild = (Guild) client.getGuildByID(guildID);
		if (guild != null) {
			User user = (User) DiscordUtils.getUserFromGuildMemberResponse(client, guild, new GuildResponse.MemberResponse(event.user, event.roles));
			guild.addUser(user);
			Discord4J.LOGGER.debug("User \"{}\" joined guild \"{}\".", user.getName(), guild.getName());
			client.dispatcher.dispatch(new UserJoinEvent(guild, user, DiscordUtils.convertFromTimestamp(event.joined_at)));
		}
	}
	
	private void guildMemberRemove(JsonElement eventObject) {
		GuildMemberRemoveEventResponse event = DiscordUtils.GSON.fromJson(eventObject, GuildMemberRemoveEventResponse.class);
		String guildID = event.guild_id;
		Guild guild = (Guild) client.getGuildByID(guildID);
		if (guild != null) {
			User user = (User) guild.getUserByID(event.user.id);
			if (user != null) {
				guild.getUsers().remove(user);
				Discord4J.LOGGER.debug("User \"{}\" has been removed from or left guild \"{}\".", user.getName(), guild.getName());
				client.dispatcher.dispatch(new UserLeaveEvent(guild, user));
			}
		}
	}
	
	private void guildMemberUpdate(JsonElement eventObject) {
		GuildMemberUpdateEventResponse event = DiscordUtils.GSON.fromJson(eventObject, GuildMemberUpdateEventResponse.class);
		Guild guild = (Guild) client.getGuildByID(event.guild_id);
		User user = (User) client.getUserByID(event.user.id);
		
		if (guild != null && user != null) {
			List<IRole> oldRoles = new ArrayList<>(user.getRolesForGuild(guild.getID()));
			user.getRolesForGuild(guild.getID()).clear();
			for (String role : event.roles)
				user.addRole(guild.getID(), guild.getRoleForID(role));
			
			user.addRole(guild.getID(), guild.getRoleForID(guild.getID())); //@everyone role
			
			client.dispatcher.dispatch(new UserRoleUpdateEvent(oldRoles, user.getRolesForGuild(guild.getID()), user));
		}
	}
	
	private void messageUpdate(JsonElement eventObject) {
		MessageResponse event = DiscordUtils.GSON.fromJson(eventObject, MessageResponse.class);
		String id = event.id;
		String channelID = event.channel_id;
		String content = event.content;
		
		Channel channel = (Channel) client.getChannelByID(channelID);
		if (channel == null)
			return;
		
		Message toUpdate = (Message) channel.getMessageByID(id);
		if (toUpdate != null
				&& !toUpdate.getAuthor().getID().equals(client.getOurUser().getID())
				&& !toUpdate.getContent().equals(content)) {
			IMessage oldMessage = new Message(client, toUpdate.getID(), toUpdate.getContent(), toUpdate.getAuthor(),
					toUpdate.getChannel(), toUpdate.getTimestamp(), toUpdate.mentionsEveryone(), toUpdate.getRawMentions(), toUpdate.getAttachments());
			
			toUpdate = (Message) DiscordUtils.getMessageFromJSON(client, channel, event);
			
			client.dispatcher.dispatch(new MessageUpdateEvent(oldMessage, toUpdate));
		}
	}
	
	private void messageDelete(JsonElement eventObject) {
		MessageDeleteEventResponse event = DiscordUtils.GSON.fromJson(eventObject, MessageDeleteEventResponse.class);
		String id = event.id;
		String channelID = event.channel_id;
		Channel channel = (Channel) client.getChannelByID(channelID);
		
		if (channel != null) {
			IMessage message = channel.getMessageByID(id);
			if (message != null) {
				channel.getMessages().remove(message);
				client.dispatcher.dispatch(new MessageDeleteEvent(message));
			}
		}
	}
	
	private void presenceUpdate(JsonElement eventObject) {
		PresenceUpdateEventResponse event = DiscordUtils.GSON.fromJson(eventObject, PresenceUpdateEventResponse.class);
		Presences presences = Presences.valueOf(event.status.toUpperCase());
		String gameName = event.game == null ? null : event.game.name;
		Guild guild = (Guild) client.getGuildByID(event.guild_id);
		if (guild != null
				&& presences != null) {
			User user = (User) guild.getUserByID(event.user.id);
			if (user != null) {
				if (!user.getPresence().equals(presences)) {
					client.dispatcher.dispatch(new PresenceUpdateEvent(guild, user, user.getPresence(), presences));
					user.setPresence(presences);
					Discord4J.LOGGER.debug("User \"{}\" changed presence to {}", user.getName(), user.getPresence());
				}
				if (!user.getGame().equals(Optional.ofNullable(gameName))) {
					client.dispatcher.dispatch(new GameChangeEvent(guild, user, user.getGame(), Optional.ofNullable(gameName)));
					user.setGame(Optional.ofNullable(gameName));
					Discord4J.LOGGER.debug("User \"{}\" changed game to {}.", user.getName(), gameName);
				}
			}
		}
	}
	
	private void guildDelete(JsonElement eventObject) {
		GuildResponse event = DiscordUtils.GSON.fromJson(eventObject, GuildResponse.class);
		Guild guild = (Guild) client.getGuildByID(event.id);
		client.getGuilds().remove(guild);
		Discord4J.LOGGER.debug("You have been kicked from or left \"{}\"! :O", guild.getName());
		client.dispatcher.dispatch(new GuildLeaveEvent(guild));
	}
	
	private void channelCreate(JsonElement eventObject) {
		boolean isPrivate = eventObject.getAsJsonObject().get("is_private").getAsBoolean();
		
		if (isPrivate) { // PM channel.
			PrivateChannelResponse event = DiscordUtils.GSON.fromJson(eventObject, PrivateChannelResponse.class);
			String id = event.id;
			boolean contained = false;
			for (IPrivateChannel privateChannel : client.privateChannels) {
				if (privateChannel.getID().equalsIgnoreCase(id))
					contained = true;
			}
			
			if (contained)
				return; // we already have this PM channel; no need to create another.
			
			client.privateChannels.add(DiscordUtils.getPrivateChannelFromJSON(client, event));
			
		} else { // Regular channel.
			ChannelResponse event = DiscordUtils.GSON.fromJson(eventObject, ChannelResponse.class);
			String type = event.type;
			Guild guild = (Guild) client.getGuildByID(event.guild_id);
			if (guild != null) {
				if (type.equalsIgnoreCase("text")) { //Text channel
					Channel channel = (Channel) DiscordUtils.getChannelFromJSON(client, guild, event);
					guild.addChannel(channel);
					client.dispatcher.dispatch(new ChannelCreateEvent(channel));
				} else if (type.equalsIgnoreCase("voice")) { //FIXME
					VoiceChannel channel = (VoiceChannel) DiscordUtils.getVoiceChannelFromJSON(client, guild, event);
					guild.addVoiceChannel(channel);
					client.dispatcher.dispatch(new VoiceChannelCreateEvent(channel));
				}
			}
		}
	}
	
	private void channelDelete(JsonElement eventObject) {
		ChannelResponse event = DiscordUtils.GSON.fromJson(eventObject, ChannelResponse.class);
		if (event.type.equalsIgnoreCase("text")) {
			Channel channel = (Channel) client.getChannelByID(event.id);
			if (channel != null) {
				channel.getGuild().getChannels().remove(channel);
				client.dispatcher.dispatch(new ChannelDeleteEvent(channel));
			}
		} else if (event.type.equalsIgnoreCase("voice")) { //FIXME
			VoiceChannel channel = (VoiceChannel) client.getVoiceChannelByID(event.id);
			if (channel != null) {
				channel.getGuild().getVoiceChannels().remove(channel);
				client.dispatcher.dispatch(new VoiceChannelDeleteEvent(channel));
			}
		}
	}
	
	private void userUpdate(JsonElement eventObject) {
		UserUpdateEventResponse event = DiscordUtils.GSON.fromJson(eventObject, UserUpdateEventResponse.class);
		User newUser = (User) client.getUserByID(event.id);
		if (newUser != null) {
			IUser oldUser = new User(client, newUser.getName(), newUser.getID(), newUser.getDiscriminator(), newUser.getAvatar(), newUser.getPresence());
			newUser = DiscordUtils.getUserFromJSON(client, event);
			client.dispatcher.dispatch(new UserUpdateEvent(oldUser, newUser));
		}
	}
	
	private void channelUpdate(JsonElement eventObject) {
		ChannelUpdateEventResponse event = DiscordUtils.GSON.fromJson(eventObject, ChannelUpdateEventResponse.class);
		if (!event.is_private) {
			if (event.type.equalsIgnoreCase("text")) {
				Channel toUpdate = (Channel) client.getChannelByID(event.id);
				if (toUpdate != null) {
					Channel oldChannel = new Channel(client, toUpdate.getName(),
							toUpdate.getID(), toUpdate.getGuild(), toUpdate.getTopic(), toUpdate.getPosition(),
							toUpdate.getMessages(), toUpdate.getRoleOverrides(), toUpdate.getUserOverrides());
					oldChannel.setLastReadMessageID(toUpdate.getLastReadMessageID());
					
					toUpdate = (Channel) DiscordUtils.getChannelFromJSON(client, toUpdate.getGuild(), event);
					
					client.getDispatcher().dispatch(new ChannelUpdateEvent(oldChannel, toUpdate));
				}
			} else if (event.type.equalsIgnoreCase("voice")) { //FIXME
				VoiceChannel toUpdate = (VoiceChannel) client.getVoiceChannelByID(event.id);
				if (toUpdate != null) {
					VoiceChannel oldChannel = new VoiceChannel(client, toUpdate.getName(),
							toUpdate.getID(), toUpdate.getGuild(), toUpdate.getTopic(), toUpdate.getPosition(),
							toUpdate.getMessages(), toUpdate.getRoleOverrides(), toUpdate.getUserOverrides());
					
					toUpdate = (VoiceChannel) DiscordUtils.getVoiceChannelFromJSON(client, toUpdate.getGuild(), event);
					
					client.getDispatcher().dispatch(new VoiceChannelUpdateEvent(oldChannel, toUpdate));
				}
			}
		}
	}
	
	private void messageAck(JsonElement eventObject) {
		MessageAcknowledgedEventResponse event = DiscordUtils.GSON.fromJson(eventObject, MessageAcknowledgedEventResponse.class);
		IChannel channelAck = client.getChannelByID(event.channel_id);
		if (channelAck != null) {
			IMessage messageAck = channelAck.getMessageByID(event.message_id);
			if (messageAck != null)
				client.getDispatcher().dispatch(new MessageAcknowledgedEvent(messageAck));
		}
	}
	
	private void guildMembersChunk(JsonElement eventObject) {
		GuildMemberChunkEventResponse event = DiscordUtils.GSON.fromJson(eventObject, GuildMemberChunkEventResponse.class);
		Guild guildToUpdate = (Guild) client.getGuildByID(event.guild_id);
		if (guildToUpdate == null) {
			Discord4J.LOGGER.warn("Can't receive guild members chunk for guild id {}, the guild is null!", event.guild_id);
			return;
		}
		
		for (GuildResponse.MemberResponse member : event.members) {
			guildToUpdate.addUser(DiscordUtils.getUserFromGuildMemberResponse(client, guildToUpdate, member));
		}
	}
	
	private void guildUpdate(JsonElement eventObject) {
		GuildResponse guildResponse = DiscordUtils.GSON.fromJson(eventObject, GuildResponse.class);
		Guild toUpdate = (Guild) client.getGuildByID(guildResponse.id);
		
		if (toUpdate != null) {
			Guild oldGuild = new Guild(client, toUpdate.getName(), toUpdate.getID(), toUpdate.getIcon(),
					toUpdate.getOwnerID(), toUpdate.getAFKChannel() == null ? null : toUpdate.getAFKChannel().getID(),
					toUpdate.getAFKTimeout(), toUpdate.getRegion().getID(), toUpdate.getRoles(), toUpdate.getChannels(), toUpdate.getVoiceChannels(),
					toUpdate.getUsers());
			
			toUpdate = (Guild) DiscordUtils.getGuildFromJSON(client, guildResponse);
			
			client.dispatcher.dispatch(new GuildUpdateEvent(oldGuild, toUpdate));
		}
	}
	
	private void guildRoleCreate(JsonElement eventObject) {
		GuildRoleEventResponse event = DiscordUtils.GSON.fromJson(eventObject, GuildRoleEventResponse.class);
		IGuild guild = client.getGuildByID(event.guild_id);
		if (guild != null) {
			IRole role = DiscordUtils.getRoleFromJSON(guild, event.role);
			((Guild) guild).addRole(role);
			client.dispatcher.dispatch(new RoleCreateEvent(role, guild));
		}
	}
	
	private void guildRoleUpdate(JsonElement eventObject) {
		GuildRoleEventResponse event = DiscordUtils.GSON.fromJson(eventObject, GuildRoleEventResponse.class);
		IGuild guild = client.getGuildByID(event.guild_id);
		if (guild != null) {
			IRole toUpdate = guild.getRoleForID(event.role.id);
			if (toUpdate != null) {
				IRole oldRole = new Role(toUpdate.getPosition(),
						Permissions.generatePermissionsNumber(toUpdate.getPermissions()), toUpdate.getName(),
						toUpdate.isManaged(), toUpdate.getID(), toUpdate.isHoisted(), toUpdate.getColor().getRGB(), guild);
				toUpdate = DiscordUtils.getRoleFromJSON(guild, event.role);
				client.dispatcher.dispatch(new RoleUpdateEvent(oldRole, toUpdate, guild));
			}
		}
	}
	
	private void guildRoleDelete(JsonElement eventObject) {
		GuildRoleDeleteEventResponse event = DiscordUtils.GSON.fromJson(eventObject, GuildRoleDeleteEventResponse.class);
		IGuild guild = client.getGuildByID(event.guild_id);
		if (guild != null) {
			IRole role = guild.getRoleForID(event.role_id);
			if (role != null) {
				guild.getRoles().remove(role);
				client.dispatcher.dispatch(new RoleDeleteEvent(role, guild));
			}
		}
	}
	
	private void guildBanAdd(JsonElement eventObject) {
		GuildBanEventResponse event = DiscordUtils.GSON.fromJson(eventObject, GuildBanEventResponse.class);
		IGuild guild = client.getGuildByID(event.guild_id);
		if (guild != null) {
			IUser user = DiscordUtils.getUserFromJSON(client, event.user);
			if (client.getUserByID(user.getID()) != null)
				guild.getUsers().remove(user);
			
			client.dispatcher.dispatch(new UserBanEvent(user, guild));
		}
	}
	
	private void guildBanRemove(JsonElement eventObject) {
		GuildBanEventResponse event = DiscordUtils.GSON.fromJson(eventObject, GuildBanEventResponse.class);
		IGuild guild = client.getGuildByID(event.guild_id);
		if (guild != null) {
			IUser user = DiscordUtils.getUserFromJSON(client, event.user);
			
			client.dispatcher.dispatch(new UserPardonEvent(user, guild));
		}
	}
	
	@Override
	public void onMessage(ByteBuffer bytes) {
		//Converts binary data to readable string data
		try {
			InflaterInputStream inputStream = new InflaterInputStream(new ByteArrayInputStream(bytes.array()));
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			
			StringBuilder sb = new StringBuilder();
			String read;
			while ((read = reader.readLine()) != null) {
				sb.append(read);
			}
			
			String data = sb.toString();
			reader.close();
			inputStream.close();
			
			onMessage(data);
		} catch (IOException e) {
			e.printStackTrace();
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
