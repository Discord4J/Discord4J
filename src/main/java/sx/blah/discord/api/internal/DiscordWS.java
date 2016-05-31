package sx.blah.discord.api.internal;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import org.eclipse.jetty.websocket.api.extensions.Frame;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.events.*;
import sx.blah.discord.handle.impl.obj.*;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.json.requests.ConnectRequest;
import sx.blah.discord.json.requests.KeepAliveRequest;
import sx.blah.discord.json.requests.ResumeRequest;
import sx.blah.discord.json.responses.*;
import sx.blah.discord.json.responses.events.*;
import sx.blah.discord.util.LogMarkers;
import sx.blah.discord.util.RequestBuilder;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.InflaterInputStream;

//This is what Hlaaftana uses so it must be good :shrug:
@WebSocket(maxBinaryMessageSize = Integer.MAX_VALUE, maxIdleTime = Integer.MAX_VALUE, maxTextMessageSize = Integer.MAX_VALUE)
public class DiscordWS {

	private volatile DiscordClientImpl client;
	private volatile Session session;
	public AtomicBoolean isConnected = new AtomicBoolean(true);
	private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2, new ThreadFactory() {
		private volatile int executorCount = 0;

		@Override
		public Thread newThread(Runnable r) {
			Thread thread = Executors.defaultThreadFactory().newThread(r);
			thread.setName("Discord4J WebSocket Client Executor "+(executorCount++));
			return thread;
		}
	});
	private final boolean isDaemon;
	private volatile boolean sentPing = false;
	private volatile long lastPingSent = -1L;
	private volatile long pingResponseTime = -1L;
	private final long timeoutTime;
	private final int maxMissedPingCount;
	private volatile int missedPingCount = 0;
	private static final String GATEWAY_VERSION = "4";
	private static final int READY_TIMEOUT = 5; //Time in seconds where the ready event will timeout from wait for guilds
	private final Thread shutdownHook = new Thread() {//Ensures this websocket is closed properly
		@Override
		public void run() {
			isConnected.set(false);
			try {
				if (session != null)
					session.disconnect(); //Harsh disconnect to close the process ASAP
			} catch (IOException e) {
				Discord4J.LOGGER.error(LogMarkers.WEBSOCKET, "Error disconnecting the websocket on jvm shutdown!", e);
			}
		}
	};

	/**
	 * The amount of users a guild must have to be considered "large"
	 */
	public static final int LARGE_THRESHOLD = 250; //250 is currently the max handled by discord

	public static DiscordWS connect(IDiscordClient client, String gateway, long timeout, int maxMissedPingCount, boolean isDaemon) throws Exception {
		//Ensuring gateway is v4 ready
		if (!gateway.endsWith("/"))
			gateway += "/";
		gateway += "?encoding=json&v="+GATEWAY_VERSION;

		SslContextFactory sslFactory = new SslContextFactory();
		WebSocketClient wsClient = new WebSocketClient(sslFactory);
		wsClient.setDaemon(true);
		if (timeout != -1) {
			wsClient.setConnectTimeout(timeout);
			wsClient.setAsyncWriteTimeout(timeout);
		}
		DiscordWS socket = new DiscordWS((DiscordClientImpl) client, wsClient, timeout, maxMissedPingCount, isDaemon);
		wsClient.start();
		ClientUpgradeRequest upgradeRequest = new ClientUpgradeRequest();
		upgradeRequest.setHeader("Accept-Encoding", "gzip, deflate");
		wsClient.connect(socket, new URI(gateway), upgradeRequest);
		return socket;
	}

	public DiscordWS(DiscordClientImpl client, WebSocketClient wsClient, long timeout, int maxMissedPingCount, boolean isDaemon) {
		this.client = client;
		this.timeoutTime = timeout;
		this.maxMissedPingCount = maxMissedPingCount;
		this.isDaemon = isDaemon;
		Runtime.getRuntime().addShutdownHook(shutdownHook);
		if (!isDaemon) {
			new Timer("WebSocketClient Keep-Alive").scheduleAtFixedRate(new TimerTask() { //Required b/c the ws client doesn't close correctly unless it is a daemon
				@Override
				public void run() { //Prevents the WebSocketClient from closing until the websocket is disconnected
					if (!isConnected.get()) {
						this.cancel();
					}
				}
			}, 0, 1000);
		}
	}

	/**
	 * Disconnects the client WS.
	 */
	public void disconnect(DiscordDisconnectedEvent.Reason reason) {
		if (isConnected.get()) {
			client.dispatcher.dispatch(new DiscordDisconnectedEvent(reason));
			isConnected.set(false);
			executorService.shutdownNow();
			client.ws = null;
			for (DiscordVoiceWS vws : client.voiceConnections.values()) { //Ensures that voice connections are closed.
				VoiceDisconnectedEvent.Reason voiceReason;
				try {
					voiceReason = VoiceDisconnectedEvent.Reason.valueOf(reason.toString());
				} catch (IllegalArgumentException e) {
					voiceReason = VoiceDisconnectedEvent.Reason.UNKNOWN;
				}
				vws.disconnect(voiceReason);
			}
			clearCache();
			Runtime.getRuntime().removeShutdownHook(shutdownHook);
			if (reason != DiscordDisconnectedEvent.Reason.INIT_ERROR) {
				session.close();
			}
		}
	}

	/**
	 * Clears the api's cache
	 */
	private void clearCache() {
		client.sessionId = null;
		client.voiceConnections.clear();
		client.guildList.clear();
		client.heartbeat = 0;
		client.lastSequence = 0;
		client.ourUser = null;
		client.privateChannels.clear();
		client.REGIONS.clear();
	}

	/**
	 * Sends a message through the websocket.
	 *
	 * @param message The json message to send.
	 */
	public void send(String message) {
		if (session == null || !session.isOpen()) {
			Discord4J.LOGGER.error(LogMarkers.WEBSOCKET, "Socket attempting to send a message ({}) without a valid session!", message);
			return;
		}
		if (isConnected.get()) {
			try {
				session.getRemote().sendString(message);
			} catch (IOException e) {
				Discord4J.LOGGER.error(LogMarkers.WEBSOCKET, "Error caught attempting to send a websocket message", e);
			}
		}
	}

	/**
	 * Sends a message through the websocket.
	 *
	 * @param object This object is converted to json and sent to the websocket.
	 */
	public void send(Object object) {
		send(DiscordUtils.GSON.toJson(object));
	}

	@OnWebSocketConnect
	public void onOpen(Session session) {
		this.session = session;
		if (client.sessionId != null) {
			send(DiscordUtils.GSON.toJson(new ResumeRequest(client.sessionId, client.lastSequence, client.getToken())));
			Discord4J.LOGGER.debug(LogMarkers.WEBSOCKET, "Reconnected to the Discord websocket.");
		} else if (!client.getToken().isEmpty()) {
			send(DiscordUtils.GSON.toJson(new ConnectRequest(client.getToken(), "Java",
					Discord4J.NAME, Discord4J.NAME, "", "", LARGE_THRESHOLD, true)));
		} else {
			Discord4J.LOGGER.error(LogMarkers.WEBSOCKET, "Use the login() method to set your token first!");
			return;
		}

		Runnable pingPong = () -> { //TODO: Remove when HEARTBEAT_ACK is implemented
			if (isConnected.get() && session != null && session.isOpen()) {
				if (sentPing) {
					if (missedPingCount > maxMissedPingCount && maxMissedPingCount > 0) {
						Discord4J.LOGGER.warn(LogMarkers.KEEPALIVE, "Missed {} ping responses in a row, disconnecting...", missedPingCount);
						disconnect(DiscordDisconnectedEvent.Reason.MISSED_PINGS);
					} else if ((System.currentTimeMillis()-lastPingSent) > timeoutTime && timeoutTime > 0) {
						Discord4J.LOGGER.warn(LogMarkers.KEEPALIVE, "Connection timed out at {}ms", System.currentTimeMillis()-lastPingSent);
						disconnect(DiscordDisconnectedEvent.Reason.TIMEOUT);
					}
					Discord4J.LOGGER.debug(LogMarkers.KEEPALIVE, "Last ping was not responded to, skipping ping");
					missedPingCount++;
				} else {
					Discord4J.LOGGER.trace(LogMarkers.KEEPALIVE, "Sending ping...");
					sentPing = true;
					lastPingSent = System.currentTimeMillis();
					try {
						session.getRemote().sendPing(ByteBuffer.wrap(DiscordUtils.GSON.toJson(new KeepAliveRequest(client.lastSequence)).getBytes()));
					} catch (Exception e) {
						Discord4J.LOGGER.error(LogMarkers.KEEPALIVE, "Discord4J Internal Exception", e);
					}
				}
			}
		};
		executorService.scheduleAtFixedRate(pingPong, 5, 5, TimeUnit.SECONDS);
	}

	private void startKeepalive() {
		Runnable keepAlive = () -> {
			if (this.isConnected.get()) {
				long l = System.currentTimeMillis()-client.timer;
				Discord4J.LOGGER.debug(LogMarkers.KEEPALIVE, "Sending keep alive... ({}). Took {} ms.", System.currentTimeMillis(), l);
				send(DiscordUtils.GSON.toJson(new KeepAliveRequest(client.lastSequence)));
				client.timer = System.currentTimeMillis();
			}
		};
		executorService.scheduleAtFixedRate(keepAlive,
				client.timer+client.heartbeat-System.currentTimeMillis(),
				client.heartbeat, TimeUnit.MILLISECONDS);
	}

	/**
	 * Called when the websocket receives a message.
	 * This method is parses from raw JSON to objects,
	 * then dispatches them in the form of events.
	 *
	 * @param message raw JSON data from Discord servers
	 */
	@OnWebSocketMessage
	public final void onMessage(Session session, String message) {
		JsonParser parser = new JsonParser();
		JsonObject object = parser.parse(message).getAsJsonObject();
		if (object.has("message")) {
			String msg = object.get("message").getAsString();
			if (msg == null || msg.isEmpty()) {
				Discord4J.LOGGER.error(LogMarkers.WEBSOCKET, "Received unknown error from Discord. Frame: {}", message);
			} else
				Discord4J.LOGGER.error(LogMarkers.WEBSOCKET, "Received error from Discord: {}. Frame: {}", msg, message);
		}
		int op = object.get("op").getAsInt();

		if (object.has("s"))
			client.lastSequence = object.get("s").getAsLong();

		if (op == GatewayOps.DISPATCH.ordinal()) { //Event dispatched
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

				case "VOICE_STATE_UPDATE":
					voiceStateUpdate(eventObject);
					break;

				case "VOICE_SERVER_UPDATE":
					voiceServerUpdate(eventObject);
					break;

				default:
					Discord4J.LOGGER.warn(LogMarkers.WEBSOCKET, "Unknown message received: {}, REPORT THIS TO THE DISCORD4J DEV! (ignoring): {}", type, message);
			}
		} else if (op == GatewayOps.HEARTBEAT.ordinal()) { //We received a heartbeat, time to send one back
			send(DiscordUtils.GSON.toJson(new KeepAliveRequest(client.lastSequence)));
		} else if (op == GatewayOps.RECONNECT.ordinal()) { //Gateway is redirecting us
			RedirectResponse redirectResponse = DiscordUtils.GSON.fromJson(object.getAsJsonObject("d"), RedirectResponse.class);
			Discord4J.LOGGER.info(LogMarkers.WEBSOCKET, "Received a gateway redirect request, closing the socket at reopening at {}", redirectResponse.url);
			try {
				client.ws = DiscordWS.connect(client, redirectResponse.url, timeoutTime, maxMissedPingCount, isDaemon);
				disconnect(DiscordDisconnectedEvent.Reason.RECONNECTING);
			} catch (Exception e) {
				Discord4J.LOGGER.error(LogMarkers.WEBSOCKET, "Discord4J Internal Exception", e);
			}
		} else if (op == GatewayOps.INVALID_SESSION.ordinal()) { //Invalid session ABANDON EVERYTHING!!!
			Discord4J.LOGGER.warn(LogMarkers.WEBSOCKET, "Invalid session! Attempting to clear caches and reconnect...");
			disconnect(DiscordDisconnectedEvent.Reason.RECONNECTING);
		} else {
			Discord4J.LOGGER.warn(LogMarkers.WEBSOCKET, "Unhandled opcode received: {} (ignoring), REPORT THIS TO THE DISCORD4J DEV!", op);
		}
	}

	private void resumed(JsonElement eventObject) {
		ResumedEventResponse event = DiscordUtils.GSON.fromJson(eventObject, ResumedEventResponse.class);
		client.heartbeat = event.heartbeat_interval;
		startKeepalive();
	}

	private void ready(JsonElement eventObject) {
		final ReadyEventResponse event = DiscordUtils.GSON.fromJson(eventObject, ReadyEventResponse.class);
		Discord4J.LOGGER.info(LogMarkers.WEBSOCKET, "Connected to the Discord Websocket v"+event.v);
		final AtomicInteger guildsToWaitFor = new AtomicInteger(0);

		new RequestBuilder(client).setAsync(true).doAction(() -> { //Ready event handling 1/2
			client.sessionId = event.session_id;

			client.ourUser = DiscordUtils.getUserFromJSON(client, event.user);

			client.heartbeat = event.heartbeat_interval;
			Discord4J.LOGGER.debug(LogMarkers.KEEPALIVE, "Received heartbeat interval of {}.", client.heartbeat);

			startKeepalive();

			client.isReady = true;

			// I hope you like loops.
			Discord4J.LOGGER.info(LogMarkers.WEBSOCKET, "Connected to {} guilds.", event.guilds.length);
			for (GuildResponse guildResponse : event.guilds) {
				if (guildResponse.unavailable) { //Guild can't be reached, so we ignore it
					continue;
				}

				IGuild guild = DiscordUtils.getGuildFromJSON(client, guildResponse);
				if (guild != null)
					client.guildList.add(guild);
			}

			guildsToWaitFor.set(event.guilds.length - client.getGuilds().size());
			Discord4J.LOGGER.trace(LogMarkers.WEBSOCKET, "Initially loaded {}/{} guilds.", client.getGuilds().size(), event.guilds.length);

			client.dispatcher.waitFor((GuildCreateEvent createEvent) -> { //Wait for guilds
				guildsToWaitFor.set(guildsToWaitFor.get()-1);
				Discord4J.LOGGER.trace(LogMarkers.WEBSOCKET, "Loaded {}/{} guilds.", event.guilds.length - guildsToWaitFor.get(), event.guilds.length);
				return guildsToWaitFor.get() <= 0;
			}, READY_TIMEOUT, TimeUnit.SECONDS);
			return true;
		}).andThen(() -> { //Ready event handling 2/2
			for (PrivateChannelResponse privateChannelResponse : event.private_channels) {
				PrivateChannel channel = (PrivateChannel) DiscordUtils.getPrivateChannelFromJSON(client, privateChannelResponse);
				client.privateChannels.add(channel);
			}

			Discord4J.LOGGER.debug(LogMarkers.WEBSOCKET, "Logged in as {} (ID {}).", client.ourUser.getName(), client.ourUser.getID());

			client.dispatcher.dispatch(new ReadyEvent());
			return true;
		}).execute();
	}

	private void messageCreate(JsonElement eventObject) {
		MessageResponse event = DiscordUtils.GSON.fromJson(eventObject, MessageResponse.class);
		boolean mentioned = event.mention_everyone;

		Channel channel = (Channel) client.getChannelByID(event.channel_id);

		if (null != channel) {
			if (!mentioned) { //Not worth checking if already mentioned
				for (UserResponse user : event.mentions) { //Check mention array for a mention
					if (client.getOurUser().getID().equals(user.id)) {
						mentioned = true;
						break;
					}
				}
			}

			if (!mentioned) { //Not worth checking if already mentioned
				for (String role : event.mention_roles) { //Check roles for a mention
					if (client.getOurUser().getRolesForGuild(channel.getGuild()).contains(channel.getGuild().getRoleByID(role))) {
						mentioned = true;
						break;
					}
				}
			}

			IMessage message = DiscordUtils.getMessageFromJSON(client, channel, event);
			if (!channel.getMessages().contains(message)) {
				Discord4J.LOGGER.debug(LogMarkers.EVENTS, "Message from: {} ({}) in channel ID {}: {}", message.getAuthor().getName(),
						event.author.id, event.channel_id, event.content);

				List<String> invites = DiscordUtils.getInviteCodesFromMessage(event.content);
				if (invites.size() > 0) {
					String[] inviteCodes = invites.toArray(new String[invites.size()]);
					Discord4J.LOGGER.debug(LogMarkers.EVENTS, "Received invite codes \"{}\"", (Object) inviteCodes);
					List<IInvite> inviteObjects = new ArrayList<>();
					for (int i = 0; i < inviteCodes.length; i++) {
						IInvite invite = client.getInviteForCode(inviteCodes[i]);
						if (invite != null)
							inviteObjects.add(invite);
					}
					client.dispatcher.dispatch(new InviteReceivedEvent(inviteObjects.toArray(new IInvite[inviteObjects.size()]), message));
				}

				if (mentioned) {
					client.dispatcher.dispatch(new MentionEvent(message));
				}

				if (message.getAuthor().equals(client.getOurUser())) {
					client.dispatcher.dispatch(new MessageSendEvent(message));
					((Channel) message.getChannel()).setTypingStatus(false); //Messages being sent should stop the bot from typing
				} else {
					client.dispatcher.dispatch(new MessageReceivedEvent(message));
				}
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
		if (event.unavailable) { //Guild can't be reached, so we ignore it
			Discord4J.LOGGER.warn(LogMarkers.WEBSOCKET, "Guild with id {} is unavailable, ignoring it. Is there an outage?", event.id);
			return;
		}

		Guild guild = (Guild) DiscordUtils.getGuildFromJSON(client, event);
		client.guildList.add(guild);
		client.dispatcher.dispatch(new GuildCreateEvent(guild));
		Discord4J.LOGGER.debug(LogMarkers.EVENTS, "New guild has been created/joined! \"{}\" with ID {}.", guild.getName(), guild.getID());
	}

	private void guildMemberAdd(JsonElement eventObject) {
		GuildMemberAddEventResponse event = DiscordUtils.GSON.fromJson(eventObject, GuildMemberAddEventResponse.class);
		String guildID = event.guild_id;
		Guild guild = (Guild) client.getGuildByID(guildID);
		if (guild != null) {
			User user = (User) DiscordUtils.getUserFromGuildMemberResponse(client, guild, new GuildResponse.MemberResponse(event.user, event.roles));
			guild.addUser(user);
			Discord4J.LOGGER.debug(LogMarkers.EVENTS, "User \"{}\" joined guild \"{}\".", user.getName(), guild.getName());
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
				Discord4J.LOGGER.debug(LogMarkers.EVENTS, "User \"{}\" has been removed from or left guild \"{}\".", user.getName(), guild.getName());
				client.dispatcher.dispatch(new UserLeaveEvent(guild, user));
			}
		}
	}

	private void guildMemberUpdate(JsonElement eventObject) {
		GuildMemberUpdateEventResponse event = DiscordUtils.GSON.fromJson(eventObject, GuildMemberUpdateEventResponse.class);
		Guild guild = (Guild) client.getGuildByID(event.guild_id);
		User user = (User) client.getUserByID(event.user.id);

		if (guild != null && user != null) {
			List<IRole> oldRoles = new ArrayList<>(user.getRolesForGuild(guild));
			user.getRolesForGuild(guild).clear();
			for (String role : event.roles)
				user.addRole(guild.getID(), guild.getRoleByID(role));

			user.addRole(guild.getID(), guild.getEveryoneRole());

			client.dispatcher.dispatch(new UserRoleUpdateEvent(oldRoles, user.getRolesForGuild(guild), user, guild));
		}
	}

	private void messageUpdate(JsonElement eventObject) {
		MessageResponse event = DiscordUtils.GSON.fromJson(eventObject, MessageResponse.class);
		String id = event.id;
		String channelID = event.channel_id;

		Channel channel = (Channel) client.getChannelByID(channelID);
		if (channel == null)
			return;

		Message toUpdate = (Message) channel.getMessageByID(id);
		if (toUpdate != null) {
			IMessage oldMessage = toUpdate.copy();

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
				client.dispatcher.dispatch(new MessageDeleteEvent(message));
			}
		}
	}

	private void presenceUpdate(JsonElement eventObject) {
		PresenceUpdateEventResponse event = DiscordUtils.GSON.fromJson(eventObject, PresenceUpdateEventResponse.class);
		Status status = DiscordUtils.getStatusFromJSON(event.game);
		Presences presence = status.getType() == Status.StatusType.STREAM ?
				Presences.STREAMING : Presences.valueOf(event.status.toUpperCase());
		Guild guild = (Guild) client.getGuildByID(event.guild_id);
		if (guild != null
				&& presence != null) {
			User user = (User) guild.getUserByID(event.user.id);
			if (user != null) {
				if (event.user.username != null) { //Full object was sent so there is a user change, otherwise all user fields but id would be null
					IUser oldUser = user.copy();
					user = DiscordUtils.getUserFromJSON(client, event.user);
					client.dispatcher.dispatch(new UserUpdateEvent(oldUser, user));
				}

				if (!user.getPresence().equals(presence)) {
					Presences oldPresence = user.getPresence();
					user.setPresence(presence);
					client.dispatcher.dispatch(new PresenceUpdateEvent(guild, user, oldPresence, presence));
					Discord4J.LOGGER.debug(LogMarkers.EVENTS, "User \"{}\" changed presence to {}", user.getName(), user.getPresence());
				}
				if (!user.getStatus().equals(status)) {
					Status oldStatus = user.getStatus();
					user.setStatus(status);
					client.dispatcher.dispatch(new GameChangeEvent(guild, user, oldStatus.getStatusMessage(), status.getStatusMessage()));
					client.dispatcher.dispatch(new StatusChangeEvent(guild, user, oldStatus, status));
					Discord4J.LOGGER.debug(LogMarkers.EVENTS, "User \"{}\" changed status to {}.", user.getName(), status);
				}
			}
		}
	}

	private void guildDelete(JsonElement eventObject) {
		GuildResponse event = DiscordUtils.GSON.fromJson(eventObject, GuildResponse.class);
		Guild guild = (Guild) client.getGuildByID(event.id);
		client.getGuilds().remove(guild);
		if (event.unavailable) { //Guild can't be reached
			Discord4J.LOGGER.warn(LogMarkers.WEBSOCKET, "Guild with id {} is unavailable, is there an outage?", event.id);
			client.dispatcher.dispatch(new GuildUnavailableEvent(guild));
		} else {
			Discord4J.LOGGER.debug(LogMarkers.EVENTS, "You have been kicked from or left \"{}\"! :O", guild.getName());
			client.dispatcher.dispatch(new GuildLeaveEvent(guild));
		}
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
				} else if (type.equalsIgnoreCase("voice")) {
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
		} else if (event.type.equalsIgnoreCase("voice")) {
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
			IUser oldUser = newUser.copy();
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
					IChannel oldChannel = toUpdate.copy();

					toUpdate = (Channel) DiscordUtils.getChannelFromJSON(client, toUpdate.getGuild(), event);

					client.getDispatcher().dispatch(new ChannelUpdateEvent(oldChannel, toUpdate));
				}
			} else if (event.type.equalsIgnoreCase("voice")) {
				VoiceChannel toUpdate = (VoiceChannel) client.getVoiceChannelByID(event.id);
				if (toUpdate != null) {
					VoiceChannel oldChannel = new VoiceChannel(client, toUpdate.getName(),
							toUpdate.getID(), toUpdate.getGuild(), "", toUpdate.getPosition(),
							null, toUpdate.getRoleOverrides(), toUpdate.getUserOverrides());

					toUpdate = (VoiceChannel) DiscordUtils.getVoiceChannelFromJSON(client, toUpdate.getGuild(), event);

					client.getDispatcher().dispatch(new VoiceChannelUpdateEvent(oldChannel, toUpdate));
				}
			}
		}
	}

	private void guildMembersChunk(JsonElement eventObject) {
		GuildMemberChunkEventResponse event = DiscordUtils.GSON.fromJson(eventObject, GuildMemberChunkEventResponse.class);
		Guild guildToUpdate = (Guild) client.getGuildByID(event.guild_id);
		if (guildToUpdate == null) {
			Discord4J.LOGGER.warn(LogMarkers.WEBSOCKET, "Can't receive guild members chunk for guild id {}, the guild is null!", event.guild_id);
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
			IGuild oldGuild = toUpdate.copy();

			toUpdate = (Guild) DiscordUtils.getGuildFromJSON(client, guildResponse);

			if (!toUpdate.getOwnerID().equals(oldGuild.getOwnerID())) {
				client.dispatcher.dispatch(new GuildTransferOwnershipEvent(oldGuild.getOwner(), toUpdate.getOwner(), toUpdate));
			} else {
				client.dispatcher.dispatch(new GuildUpdateEvent(oldGuild, toUpdate));
			}
		}
	}

	private void guildRoleCreate(JsonElement eventObject) {
		GuildRoleEventResponse event = DiscordUtils.GSON.fromJson(eventObject, GuildRoleEventResponse.class);
		IGuild guild = client.getGuildByID(event.guild_id);
		if (guild != null) {
			IRole role = DiscordUtils.getRoleFromJSON(guild, event.role);
			client.dispatcher.dispatch(new RoleCreateEvent(role, guild));
		}
	}

	private void guildRoleUpdate(JsonElement eventObject) {
		GuildRoleEventResponse event = DiscordUtils.GSON.fromJson(eventObject, GuildRoleEventResponse.class);
		IGuild guild = client.getGuildByID(event.guild_id);
		if (guild != null) {
			IRole toUpdate = guild.getRoleByID(event.role.id);
			if (toUpdate != null) {
				IRole oldRole = toUpdate.copy();
				client.dispatcher.dispatch(new RoleUpdateEvent(oldRole, toUpdate, guild));
			}
		}
	}

	private void guildRoleDelete(JsonElement eventObject) {
		GuildRoleDeleteEventResponse event = DiscordUtils.GSON.fromJson(eventObject, GuildRoleDeleteEventResponse.class);
		IGuild guild = client.getGuildByID(event.guild_id);
		if (guild != null) {
			IRole role = guild.getRoleByID(event.role_id);
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

	private void voiceStateUpdate(JsonElement eventObject) {
		VoiceStateResponse event = DiscordUtils.GSON.fromJson(eventObject, VoiceStateResponse.class);
		IGuild guild = client.getGuildByID(event.guild_id);

		if (guild != null) {
			IVoiceChannel channel = guild.getVoiceChannelByID(event.channel_id);
			User user = (User) guild.getUserByID(event.user_id);
			if (user != null) {
				IVoiceChannel oldChannel = user.getConnectedVoiceChannels()
						.stream()
						.filter(vChannel -> vChannel.getGuild().getID().equals(event.guild_id))
						.findFirst()
						.orElse(null);
				if (oldChannel == null)
					oldChannel = user.getConnectedVoiceChannels()
							.stream()
							.findFirst()
							.orElse(null);
				if (channel != oldChannel) {
					if (channel == null) {
						client.dispatcher.dispatch(new UserVoiceChannelLeaveEvent(user, oldChannel));
						user.getConnectedVoiceChannels().remove(oldChannel);
					} else if (oldChannel != null && oldChannel.getGuild().equals(channel.getGuild())) {
						client.dispatcher.dispatch(new UserVoiceChannelMoveEvent(user, oldChannel, channel));
						user.getConnectedVoiceChannels().remove(oldChannel);
						if (!user.getConnectedVoiceChannels().contains(channel))
							user.getConnectedVoiceChannels().add(channel);
					} else {
						client.dispatcher.dispatch(new UserVoiceChannelJoinEvent(user, channel));
						if (!user.getConnectedVoiceChannels().contains(channel))
							user.getConnectedVoiceChannels().add(channel);
					}
				}
			}
		}
	}

	private void voiceServerUpdate(JsonElement eventObject) {
		VoiceUpdateResponse event = DiscordUtils.GSON.fromJson(eventObject, VoiceUpdateResponse.class);
		try {
			event.endpoint = event.endpoint.substring(0, event.endpoint.indexOf(":"));
			client.voiceConnections.put(client.getGuildByID(event.guild_id), DiscordVoiceWS.connect(event, client));
		} catch (Exception e) {
			Discord4J.LOGGER.error(LogMarkers.VOICE_WEBSOCKET, "Discord4J Internal Exception", e);
		}
	}

	@OnWebSocketMessage
	public void onMessage(Session session, byte[] buf, int offset, int length) {
		//Converts binary data to readable string data
		try {
			InflaterInputStream inputStream = new InflaterInputStream(new ByteArrayInputStream(buf));
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

			StringBuilder sb = new StringBuilder();
			String read;
			while ((read = reader.readLine()) != null) {
				sb.append(read);
			}

			String data = sb.toString();
			reader.close();
			inputStream.close();

			onMessage(session, data);
		} catch (IOException e) {
			Discord4J.LOGGER.error(LogMarkers.WEBSOCKET, "Discord4J Internal Exception", e);
		}
	}

	@OnWebSocketClose
	public void onClose(Session session, int code, String reason) {
		Discord4J.LOGGER.debug(LogMarkers.WEBSOCKET, "Websocket disconnected. Exit Code: {}. Reason: {}.", code, reason);
		disconnect(DiscordDisconnectedEvent.Reason.UNKNOWN);
	}

	@OnWebSocketError
	public void onError(Session session, Throwable error) {
		Discord4J.LOGGER.error(LogMarkers.WEBSOCKET, "Websocket error, disconnecting...", error);
		if (session == null || !session.isOpen()) {
			disconnect(DiscordDisconnectedEvent.Reason.INIT_ERROR);
		} else {
			disconnect(DiscordDisconnectedEvent.Reason.UNKNOWN);
		}
	}

	@OnWebSocketFrame
	public void onFrame(Session session, Frame frame) {
		if (frame.getType() == Frame.Type.PING) {
			Discord4J.LOGGER.trace(LogMarkers.KEEPALIVE, "Received ping, sending pong...");
			try {
				session.getRemote().sendPong(ByteBuffer.allocate(0));
			} catch (IOException e) {
				Discord4J.LOGGER.error(LogMarkers.KEEPALIVE, "Discord4J Internal Exception", e);
			}
		} else if (frame.getType() == Frame.Type.PONG) {
			if (!sentPing) {
				Discord4J.LOGGER.warn(LogMarkers.KEEPALIVE, "Received pong without sending ping! Is the websocket out of sync?");
			} else {
				Discord4J.LOGGER.trace(LogMarkers.KEEPALIVE, "Received pong... Response time is {}ms", pingResponseTime = System.currentTimeMillis()-lastPingSent);
				sentPing = false;
				missedPingCount = 0;
			}
		}
	}

	/**
	 * Gets the most recent ping response time by discord.
	 *
	 * @return The response time (in ms).
	 */
	public long getResponseTime() {
		return pingResponseTime;
	}
}
