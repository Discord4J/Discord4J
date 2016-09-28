package sx.blah.discord.api.internal;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.tuple.Pair;
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
import sx.blah.discord.api.internal.json.requests.ConnectRequest;
import sx.blah.discord.api.internal.json.requests.KeepAliveRequest;
import sx.blah.discord.api.internal.json.requests.ResumeRequest;
import sx.blah.discord.api.internal.json.responses.*;
import sx.blah.discord.api.internal.json.responses.events.*;
import sx.blah.discord.util.LogMarkers;
import sx.blah.discord.util.MessageList;
import sx.blah.discord.util.RequestBuilder;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.UnresolvedAddressException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.zip.InflaterInputStream;

import static sx.blah.discord.Discord4J.LOGGER;

//This is what Hlaaftana uses so it must be good :shrug:
@WebSocket(maxBinaryMessageSize = Integer.MAX_VALUE, maxIdleTime = Integer.MAX_VALUE, maxTextMessageSize = Integer.MAX_VALUE)
public class DiscordWS {

	private final WebSocketClient wsClient;
	private final String gateway;
	private volatile DiscordClientImpl client;
	private volatile Session session;
	protected final AtomicBoolean isConnected = new AtomicBoolean(false);
	private volatile ScheduledExecutorService executorService;
	private final AtomicBoolean startingUp = new AtomicBoolean(false);
	protected final AtomicBoolean isReconnecting = new AtomicBoolean(false);
	private final Supplier<TimerTask> cancelReconnectTaskSupplier = new Supplier<TimerTask>() {
		private volatile CancelTask task;

		@Override
		public TimerTask get() {
			if (task == null || task.ranOrCancelled)
				task = new CancelTask();

			return task;
		}

		class CancelTask extends TimerTask {

			volatile boolean ranOrCancelled = false;

			@Override
			public void run() {
				if (!ranOrCancelled) {
					ranOrCancelled = true;
					Discord4J.LOGGER.error(LogMarkers.WEBSOCKET, "Reconnection attempt timed out.");
					disconnect(DiscordDisconnectedEvent.Reason.RECONNECTION_FAILED);
				}
			}

			@Override
			public boolean cancel() {
				ranOrCancelled = true;
				return super.cancel();
			}
		}
	};
	private final Timer cancelReconnectTimer = new Timer("Reconnection Timer", true);
	private final boolean isDaemon;
	private final boolean withReconnects;
	private final boolean async;
	private final Pair<Integer, Integer> shard;
	private final AtomicBoolean sentPing = new AtomicBoolean(false);
	private final AtomicLong pingResponseTime = new AtomicLong(-1L);
	private final long timeoutTime;
	private final int maxMissedPingCount;
	private final AtomicInteger missedPingCount = new AtomicInteger(0);
	private final AtomicInteger reconnectAttempts = new AtomicInteger(0);
	private final int maxReconnectAttempts;
	private static final int INITIAL_RECONNECT_TIME = 15; //The factor by which the reconnect time is exponentially increased by on successive failures
	private static final String GATEWAY_VERSION = "5";
	private static final int READY_TIMEOUT = 10; //Time in seconds where the ready event will timeout from wait for guilds
	private final Thread shutdownHook = new Thread() {//Ensures this websocket is closed properly
		@Override
		public void run() {
			isConnected.set(false);
			try {
				if (session != null && session.isOpen())
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

	/**
	 * The websocket session id.
	 */
	public String sessionId;

	/**
	 * Is the websocket ready to recieve events.
	 */
	public boolean isReady;

	/**
	 * The amount of guilds on this websocket
	 */
	private int guildCount;

	public DiscordWS(IDiscordClient client, String gateway, long timeout, int maxMissedPingCount, boolean isDaemon,
					 int reconnectAttempts, boolean async, Pair<Integer, Integer> shard) throws Exception {
		this.client = (DiscordClientImpl)client;
		this.timeoutTime = timeout;
		this.maxMissedPingCount = maxMissedPingCount;
		this.isDaemon = isDaemon;
		this.withReconnects = reconnectAttempts > 0;
		this.maxReconnectAttempts = reconnectAttempts;
		this.shard = shard;
		this.startingUp.set(true);
		this.async = async;
		//Ensuring gateway is ready
		if (!gateway.endsWith("/"))
			gateway += "/";
		gateway += "?encoding=json&v="+GATEWAY_VERSION;
		this.gateway = gateway;

		SslContextFactory sslFactory = new SslContextFactory();
		wsClient = new WebSocketClient(sslFactory);
		wsClient.setDaemon(true);
		if (timeout != -1) {
			wsClient.setConnectTimeout(timeout);
			wsClient.setAsyncWriteTimeout(timeout);
		}
		wsClient.start();
		connect();
		Runtime.getRuntime().addShutdownHook(shutdownHook);
		if (!isDaemon) {
			new Timer("WebSocketClient Keep-Alive").scheduleAtFixedRate(new TimerTask() { //Required b/c the ws client doesn't close correctly unless it is a daemon
				@Override
				public void run() { //Prevents the WebSocketClient from closing until the websocket is disconnected
					if (!isConnected.get() && !startingUp.get() && !isReconnecting.get()) {
						this.cancel();
					}
				}
			}, 0, 1000);
		}
	}

	private void connect() throws URISyntaxException, IOException {
		ClientUpgradeRequest upgradeRequest = new ClientUpgradeRequest();
		upgradeRequest.setHeader("Accept-Encoding", "gzip, deflate");
		wsClient.connect(this, new URI(gateway), upgradeRequest);
	}

	/**
	 * Disconnects the client WS.
	 */
	public synchronized void disconnect(DiscordDisconnectedEvent.Reason reason) {
		if (startingUp.get() && reason != DiscordDisconnectedEvent.Reason.INIT_ERROR)
			reason = DiscordDisconnectedEvent.Reason.INIT_ERROR;

		if (reason == DiscordDisconnectedEvent.Reason.LOGGED_OUT
				|| reason == DiscordDisconnectedEvent.Reason.INIT_ERROR
				|| reason == DiscordDisconnectedEvent.Reason.INVALID_SESSION) {
			clearCache();
		}

		isConnected.set(false);
		if (withReconnects && (reason == DiscordDisconnectedEvent.Reason.UNKNOWN
				|| reason == DiscordDisconnectedEvent.Reason.MISSED_PINGS
				|| reason == DiscordDisconnectedEvent.Reason.TIMEOUT
				|| reason == DiscordDisconnectedEvent.Reason.INIT_ERROR
				|| reason == DiscordDisconnectedEvent.Reason.INVALID_SESSION
				|| (reason == DiscordDisconnectedEvent.Reason.RECONNECTION_FAILED
				&& reconnectAttempts.get() <= maxReconnectAttempts))) {

			isReconnecting.set(true);

			if (reconnectAttempts.incrementAndGet() > maxReconnectAttempts) {
				Discord4J.LOGGER.error(LogMarkers.WEBSOCKET, "Reconnection was attempted too many times ({} attempts)", reconnectAttempts);
				isReconnecting.set(false);//When reconnect has timed out then allow the bot to fully disconnect
				disconnect(DiscordDisconnectedEvent.Reason.RECONNECTION_FAILED);
				return;
			} else {
				final TimerTask cancelReconnectTask = cancelReconnectTaskSupplier.get();
				Discord4J.LOGGER.info(LogMarkers.WEBSOCKET, "Attempting to reconnect...");

				cancelReconnectTimer.schedule(cancelReconnectTask, TimeUnit.SECONDS.toMillis(((int)
						(INITIAL_RECONNECT_TIME*Math.pow(2, reconnectAttempts.get())))
						+ThreadLocalRandom.current().nextLong(-2, 2))); //Applies jitter to not spam discord servers with tons of simultaneous reconnections at the same time

				if (reason == DiscordDisconnectedEvent.Reason.INIT_ERROR || reason == DiscordDisconnectedEvent.Reason.INVALID_SESSION) {
					try {
						client.connectWebSocket(shard.getLeft(), gateway);
						disconnect(DiscordDisconnectedEvent.Reason.RECONNECTING);
						return;
					} catch (Exception e) {
						Discord4J.LOGGER.error(LogMarkers.WEBSOCKET, "Error caught while attempting to reconnect.", e);
					}
				} else if (session == null || !session.isOpen()) {
					try {
						connect();
					} catch (UnresolvedAddressException | URISyntaxException | IOException e) {
						Discord4J.LOGGER.error(LogMarkers.WEBSOCKET, "Error caught while attempting to reconnect.", e);
						disconnect(DiscordDisconnectedEvent.Reason.RECONNECTION_FAILED);
						return;
					}
				}
			}
		}
		client.dispatcher.dispatch(new DiscordDisconnectedEvent(reason));
		executorService.shutdownNow();
		startingUp.set(false);
		sentPing.set(false);
		missedPingCount.set(0);
		if(!isReconnecting.get()) { //Doesn't let the bot actually disconnect unless reconnecting has failed
			client.ws.remove(shard.getLeft());
			for (DiscordVoiceWS vws : client.voiceConnections.values()) { //Ensures that voice connections are closed.
				VoiceDisconnectedEvent.Reason voiceReason;
				try {
					voiceReason = VoiceDisconnectedEvent.Reason.valueOf(reason.toString());
				} catch (IllegalArgumentException e) {
					voiceReason = VoiceDisconnectedEvent.Reason.UNKNOWN;
				}
				vws.disconnect(voiceReason);
			}
			Runtime.getRuntime().removeShutdownHook(shutdownHook);
			if (reason != DiscordDisconnectedEvent.Reason.INIT_ERROR) {
				session.close();
			}
			try {
				wsClient.stop();
			} catch (Exception e) {
				LOGGER.error(LogMarkers.WEBSOCKET, "Error caught attempting to close the WebSocketClient!", e);
			}
		}
	}

	/**
	 * Clears the api's cache
	 */
	protected void clearCache() {
		sessionId = null;
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
	}

	private void startKeepalive() {
		if (executorService == null || executorService.isShutdown() || executorService.isTerminated()) {
			executorService = Executors.newScheduledThreadPool(1, r -> {
				Thread thread = Executors.defaultThreadFactory().newThread(r);
				thread.setName("Discord4J WebSocket Heartbeat Executor");
				return thread;
			});
		}

		Runnable keepAlive = () -> {
			if (this.isConnected.get() && !this.isReconnecting.get()) {
				if (sentPing.get()) {
					if (missedPingCount.get() > maxMissedPingCount && maxMissedPingCount > 0) {
						Discord4J.LOGGER.warn(LogMarkers.KEEPALIVE, "Missed {} heartbeat responses in a row, disconnecting...", missedPingCount);
						disconnect(DiscordDisconnectedEvent.Reason.MISSED_PINGS);
					} else if ((System.currentTimeMillis()-client.timer) > timeoutTime && timeoutTime > 0) {
						Discord4J.LOGGER.warn(LogMarkers.KEEPALIVE, "Connection timed out at {}ms", System.currentTimeMillis()-client.timer);
						disconnect(DiscordDisconnectedEvent.Reason.TIMEOUT);
					}
					Discord4J.LOGGER.debug(LogMarkers.KEEPALIVE, "Last ping was not responded to!");
					missedPingCount.incrementAndGet();
				}

				long l = System.currentTimeMillis()-client.timer;
				Discord4J.LOGGER.debug(LogMarkers.KEEPALIVE, "Sending keep alive... ({}). Took {} ms.", System.currentTimeMillis(), l);
				send(DiscordUtils.GSON.toJson(new KeepAliveRequest(client.lastSequence)));
				client.timer = System.currentTimeMillis();
				sentPing.set(true);
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

		if (object.has("s") && !object.get("s").isJsonNull())
			client.lastSequence = object.get("s").getAsLong();

		if (op == GatewayOps.DISPATCH.ordinal()) { //Event dispatched
			String type = object.get("t").getAsString();
			JsonElement eventObject = object.get("d");

			switch (type) {
				case "RESUMED":
					resumed();
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

				case "MESSAGE_DELETE_BULK":
					messageDeleteBulk(eventObject);
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

				case "CHANNEL_PINS_UPDATE"://Fired when pins are changed, this is mostly intended for clients and I already implemented this functionality on MESSAGE_UPDATE so I'm ignoring it
					//Payload for future reference is:
					// {
					// 		"last_pin_timestamp": "TIMESTAMP",
					// 		"channel_id": "CHANNEL ID"
					// }
					//Ignored
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

				case "GUILD_EMOJIS_UPDATE":
					//Ignored for now TODO: do something with emojis
					break;

				case "GUILD_INTEGRATIONS_UPDATE":
					//Ignored for now TODO: do something with integrations
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
				client.connectWebSocket(shard.getLeft(), redirectResponse.url);
				disconnect(DiscordDisconnectedEvent.Reason.RECONNECTING);
			} catch (Exception e) {
				Discord4J.LOGGER.error(LogMarkers.WEBSOCKET, "Discord4J Internal Exception", e);
			}
		} else if (op == GatewayOps.INVALID_SESSION.ordinal()) { //Invalid session ABANDON EVERYTHING!!!
			Discord4J.LOGGER.warn(LogMarkers.WEBSOCKET, "Invalid session! Attempting to clear caches and reconnect...");
			disconnect(DiscordDisconnectedEvent.Reason.INVALID_SESSION);
		} else if (op == GatewayOps.HELLO.ordinal()) {
			connected();

			HelloResponse helloResponse = DiscordUtils.GSON.fromJson(object.get("d"), HelloResponse.class);

			client.heartbeat = helloResponse.heartbeat_interval;
			startKeepalive();

			if (this.sessionId != null) {
				handleReconnect();
			} else if (!client.getToken().isEmpty()) {
				send(DiscordUtils.GSON_NO_NULLS.toJson(new ConnectRequest(client.getToken(), "Java",
						Discord4J.NAME, Discord4J.NAME, "", "", LARGE_THRESHOLD, true, shard)));
			} else {
				Discord4J.LOGGER.error(LogMarkers.WEBSOCKET, "Use the login() method to set your token first!");
			}

		} else if (op == GatewayOps.HEARTBEAT_ACK.ordinal()) {
			if (!sentPing.get()) {
				Discord4J.LOGGER.warn(LogMarkers.KEEPALIVE, "Received pong without sending ping! Is the websocket out of sync?");
			} else {
				pingResponseTime.set(System.currentTimeMillis()-client.timer);
				Discord4J.LOGGER.trace(LogMarkers.KEEPALIVE, "Received pong... Response time is {}ms", pingResponseTime.get());
				sentPing.set(false);
				missedPingCount.set(0);
			}
		} else {
			Discord4J.LOGGER.warn(LogMarkers.WEBSOCKET, "Unhandled opcode received: {} (ignoring), REPORT THIS TO THE DISCORD4J DEV!", op);
		}
	}

	private void connected() {
		isConnected.set(true);
		startingUp.set(false);
		reconnectAttempts.set(0);
		if (isReconnecting.get()) {
			isReconnecting.set(false);
			cancelReconnectTaskSupplier.get().cancel();
		}
	}

	private void handleReconnect() {
		send(DiscordUtils.GSON.toJson(new ResumeRequest(sessionId, client.lastSequence, client.getToken())));
	}

	private void resumed() {
		Discord4J.LOGGER.info(LogMarkers.WEBSOCKET, "Reconnected to the Discord websocket.");
		client.dispatcher.dispatch(new DiscordReconnectedEvent());
	}

	private void ready(JsonElement eventObject) {
		final ReadyEventResponse event = DiscordUtils.GSON.fromJson(eventObject, ReadyEventResponse.class);
		Discord4J.LOGGER.info(LogMarkers.WEBSOCKET, "Connected to the Discord websocket v"+event.v);

		isReconnecting.set(false);
		isConnected.set(true); //Redundancy due to how reconnects work

		new RequestBuilder(client).setAsync(true).doAction(() -> { //Ready event handling 1/2
			this.sessionId = event.session_id;

			client.ourUser = DiscordUtils.getUserFromJSON(client, event.user);

			Discord4J.LOGGER.debug(LogMarkers.KEEPALIVE, "Received heartbeat interval of {}.", client.heartbeat);

			Discord4J.LOGGER.info(LogMarkers.WEBSOCKET, "Connected to {} guilds on shard {}", event.guilds.length, shard.getLeft());
			if (event.guilds.length > MessageList.MAX_GUILD_COUNT) //Disable initial caching for performance
				MessageList.shouldDownloadHistoryAutomatically(false);

			this.guildCount = event.guilds.length;
			return true;
		}).andThen(() -> { //Ready event handling 2/2
			for (PrivateChannelResponse privateChannelResponse : event.private_channels) {
				PrivateChannel channel = (PrivateChannel) DiscordUtils.getPrivateChannelFromJSON(client, privateChannelResponse);
				client.privateChannels.add(channel);
			}

			Discord4J.LOGGER.debug(LogMarkers.WEBSOCKET, "Logged in as {} (ID {}).", client.ourUser.getName(), client.ourUser.getID());

			setReady();

			return true;
		}).execute();
	}

	private void setReady(){
		if (this.async || client.getGuilds(this.shard.getLeft()).size() == this.guildCount) {
			this.isReady = true;
			client.isReady();
		}
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
					if(!message.getEmbedded().isEmpty()) {
						client.dispatcher.dispatch(new MessageEmbedEvent(message, new ArrayList<>()));
					}
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
			client.dispatcher.dispatch(new GuildUnavailableEvent(event.id));
			return;
		}

		Guild guild = (Guild) DiscordUtils.getGuildFromJSON(client, event);
		client.guildList.add(guild);
		client.dispatcher.dispatch(new GuildCreateEvent(guild));

		Discord4J.LOGGER.debug(LogMarkers.EVENTS, "New guild has been created/joined! \"{}\" with ID {}.", guild.getName(), guild.getID());

		setReady();
	}

	private void guildMemberAdd(JsonElement eventObject) {
		GuildMemberAddEventResponse event = DiscordUtils.GSON.fromJson(eventObject, GuildMemberAddEventResponse.class);
		String guildID = event.guild_id;
		Guild guild = (Guild) client.getGuildByID(guildID);
		if (guild != null) {
			User user = (User) DiscordUtils.getUserFromGuildMemberResponse(client, guild, new GuildResponse.MemberResponse(event.user, event.roles));
			guild.addUser(user);
			LocalDateTime timestamp = DiscordUtils.convertFromTimestamp(event.joined_at);
			Discord4J.LOGGER.debug(LogMarkers.EVENTS, "User \"{}\" joined guild \"{}\".", user.getName(), guild.getName());
			client.dispatcher.dispatch(new UserJoinEvent(guild, user, timestamp));
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
				guild.getJoinTimes().remove(user);
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
			boolean rolesChanged = oldRoles.size() != event.roles.length+1;//Add one for the @everyone role
			if (!rolesChanged) {
				rolesChanged = oldRoles.stream().filter(role -> {
					if (role.equals(guild.getEveryoneRole()))
						return false;

					for (String roleID : event.roles) {
						if (role.getID().equals(roleID)) {
							return false;
						}
					}

					return true;
				}).collect(Collectors.toList()).size() > 0;
			}

			if (rolesChanged) {
				user.getRolesForGuild(guild).clear();
				for (String role : event.roles)
					user.addRole(guild.getID(), guild.getRoleByID(role));

				user.addRole(guild.getID(), guild.getEveryoneRole());

				client.dispatcher.dispatch(new UserRoleUpdateEvent(oldRoles, user.getRolesForGuild(guild), user, guild));
			}

			if (!user.getNicknameForGuild(guild).equals(Optional.ofNullable(event.nick))) {
				String oldNick = user.getNicknameForGuild(guild).orElse(null);
				user.addNick(guild.getID(), event.nick);

				client.dispatcher.dispatch(new NickNameChangeEvent(guild, user, oldNick, event.nick));
			}
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

			if (oldMessage.isPinned() && !event.pinned) {
				client.dispatcher.dispatch(new MessageUnpinEvent(toUpdate));
			} else if (!oldMessage.isPinned() && event.pinned) {
				client.dispatcher.dispatch(new MessagePinEvent(toUpdate));
			} else if (oldMessage.getEmbedded().size() < toUpdate.getEmbedded().size()) {
				client.dispatcher.dispatch(new MessageEmbedEvent(toUpdate, oldMessage.getEmbedded()));
			} else {
				client.dispatcher.dispatch(new MessageUpdateEvent(oldMessage, toUpdate));
			}
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
				if (message.isPinned()) {
					((Message) message).setPinned(false); //For consistency with the event
					client.dispatcher.dispatch(new MessageUnpinEvent(message));
				}

				client.dispatcher.dispatch(new MessageDeleteEvent(message));
			}
		}
	}

	private void messageDeleteBulk(JsonElement eventObject) { //TODO: maybe add a separate event for this?
		MessageDeleteBulkEventResponse event = DiscordUtils.GSON.fromJson(eventObject, MessageDeleteBulkEventResponse.class);
		for (String id : event.ids) {
			messageDelete(DiscordUtils.GSON.toJsonTree(new MessageDeleteEventResponse(id, event.channel_id)));
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
					user.setPresence(presence, 0);
					client.dispatcher.dispatch(new PresenceUpdateEvent(user, oldPresence, presence, 0));
					Discord4J.LOGGER.debug(LogMarkers.EVENTS, "User \"{}\" changed presence to {}", user.getName(), user.getPresence());
				}
				if (!user.getStatus().equals(status)) {
					Status oldStatus = user.getStatus();
					user.setStatus(status, 0);
					client.dispatcher.dispatch(new StatusChangeEvent(user, oldStatus, status, 0));
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
			client.dispatcher.dispatch(new GuildUnavailableEvent(event.id));
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
				if (!channel.isPrivate())
					channel.getGuild().getChannels().remove(channel);
				else
					client.privateChannels.remove(channel);

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
					VoiceChannel oldChannel = (VoiceChannel) toUpdate.copy();

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
			IUser user = DiscordUtils.getUserFromGuildMemberResponse(client, guildToUpdate, member);
			guildToUpdate.addUser(user);
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
				toUpdate = DiscordUtils.getRoleFromJSON(guild, event.role);
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
			if (client.getUserByID(user.getID()) != null) {
				guild.getUsers().remove(user);
				((Guild) guild).getJoinTimes().remove(user);
			}

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
				user.setIsDeaf(guild.getID(), event.deaf);
				user.setIsMute(guild.getID(), event.mute);
				user.setIsDeafLocally(event.self_deaf);
				user.setIsMutedLocally(event.self_mute);

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
		if(isConnected.get()) { //Prevents disconnect from being called multiple times
			disconnect(DiscordDisconnectedEvent.Reason.UNKNOWN);
		}
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
		}
	}

	/**
	 * Gets the most recent ping response time by discord.
	 *
	 * @return The response time (in ms).
	 */
	public long getResponseTime() {
		return pingResponseTime.get();
	}
}
