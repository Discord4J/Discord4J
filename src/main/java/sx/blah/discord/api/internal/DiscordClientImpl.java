package sx.blah.discord.api.internal;

import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.handle.impl.events.DiscordDisconnectedEvent;
import sx.blah.discord.handle.impl.events.PresenceUpdateEvent;
import sx.blah.discord.handle.impl.events.StatusChangeEvent;
import sx.blah.discord.handle.impl.obj.User;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.api.internal.json.requests.*;
import sx.blah.discord.api.internal.json.responses.*;
import sx.blah.discord.modules.ModuleLoader;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.Image;
import sx.blah.discord.util.LogMarkers;
import sx.blah.discord.util.RateLimitException;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Defines the client.
 * This class receives and
 * sends messages, as well
 * as holds our user data.
 */
public final class DiscordClientImpl implements IDiscordClient {

	static {
		ServiceUtil.loadServices();
	}

	/**
	 * Used for keep alive. Keeps last time (in ms)
	 * that we sent the keep alive so we can accurately
	 * time our keep alive messages.
	 */
	protected volatile long timer = System.currentTimeMillis();

	/**
	 * User we are logged in as
	 */
	protected volatile User ourUser;

	/**
	 * Our token, so we can send XHR to Discord.
	 */
	protected volatile String token;

	/**
	 * Time (in ms) between keep alive
	 * messages.
	 */
	protected volatile long heartbeat;

	/**
	 * Local copy of all guilds/servers.
	 */
	protected final List<IGuild> guildList = new CopyOnWriteArrayList<>();

	/**
	 * Private copy of the email you logged in with.
	 */
	protected volatile String email;

	/**
	 * Private copy of the password you used to log in.
	 */
	protected volatile String password;

	/**
	 * WebSocket over which to communicate with Discord.
	 */
	public volatile DiscordWS ws;

	/**
	 * Holds the active connections to voice sockets.
	 */
	public final Map<IGuild, DiscordVoiceWS> voiceConnections = new ConcurrentHashMap<>();

	/**
	 * Event dispatcher.
	 */
	protected volatile EventDispatcher dispatcher;

	/**
	 * All of the private message channels that the bot is connected to.
	 */
	protected final List<IPrivateChannel> privateChannels = new CopyOnWriteArrayList<>();

	/**
	 * Whether the api is logged in.
	 */
	protected volatile boolean isReady = false;

	/**
	 * The websocket session id.
	 */
	protected volatile String sessionId;

	/**
	 * Caches the last operation done by the websocket, required for handling redirects.
	 */
	protected volatile long lastSequence = 0;

	/**
	 * Caches the available regions for discord.
	 */
	protected final List<IRegion> REGIONS = new CopyOnWriteArrayList<>();

	/**
	 * The module loader for this client.
	 */
	protected volatile ModuleLoader loader;

	/**
	 * The time for the client to timeout.
	 */
	protected final long timeoutTime;

	/**
	 * The maximum amount of pings discord can miss.
	 */
	protected final int maxMissedPingCount;

	/**
	 * Whether the websocket should act as a daemon.
	 */
	protected final boolean isDaemon;

	/**
	 * Whether this client represents a bot.
	 */
	protected volatile boolean isBot;

	/**
	 * The maximum amount of attempts before reconnections are aborted.
	 */
	protected final int reconnectAttempts;

	/**
	 * When this client was logged into. Useful for determining uptime.
	 */
	protected volatile LocalDateTime launchTime;

	/**
	 * The requests holder object.
	 */
	public final Requests REQUESTS = new Requests(this);

	private DiscordClientImpl(long timeoutTime, int maxMissedPingCount, boolean isDaemon, boolean isBot, int reconnectAttempts) {
		this.timeoutTime = timeoutTime;
		this.maxMissedPingCount = maxMissedPingCount;
		this.isDaemon = isDaemon;
		this.isBot = isBot;
		this.reconnectAttempts = reconnectAttempts;
		this.dispatcher = new EventDispatcher(this);
		this.loader = new ModuleLoader(this);
	}

	public DiscordClientImpl(String email, String password, long timeoutTime, int maxMissedPingCount, boolean isDaemon, int reconnectAttempts) {
		this(timeoutTime, maxMissedPingCount, isDaemon, false, reconnectAttempts);
		this.email = email;
		this.password = password;
	}

	public DiscordClientImpl(String token, long timeoutTime, int maxMissedPingCount, boolean isDaemon, int reconnectAttempts) {
		this(timeoutTime, maxMissedPingCount, isDaemon, true, reconnectAttempts);
		this.token = token;
	}

	@Override
	public EventDispatcher getDispatcher() {
		return dispatcher;
	}

	@Override
	public ModuleLoader getModuleLoader() {
		return loader;
	}

	@Override
	public String getToken() {
		if (isBot) {
			return "Bot "+token;
		} else {
			return token;
		}
	}

	@Override
	public void login(boolean async) throws DiscordException {
		try {
			if (ws != null) {
				ws.disconnect(DiscordDisconnectedEvent.Reason.RECONNECTING);

				lastSequence = 0;
				sessionId = null; //Prevents the websocket from sending a resume request.
			}

			if (!isBot) {
				LoginResponse response = DiscordUtils.GSON.fromJson(REQUESTS.POST.makeRequest(DiscordEndpoints.LOGIN,
						new StringEntity(DiscordUtils.GSON.toJson(new LoginRequest(email, password))),
						new BasicNameValuePair("content-type", "application/json")), LoginResponse.class);
				this.token = response.token;
			} else {
				if (!validateToken())
					throw new DiscordException("Invalid token!");
			}

			this.ws = new DiscordWS(this, obtainGateway(getToken()), timeoutTime, maxMissedPingCount, isDaemon,
					reconnectAttempts, async);

			launchTime = LocalDateTime.now();
		} catch (Exception e) {
			Discord4J.LOGGER.error(LogMarkers.API, "Exception caught, logging in!", e);
			throw new DiscordException("Login error occurred! Are your login details correct?");
		}
	}

	@Override
	public void login() throws DiscordException {
		login(false);
	}

	private boolean validateToken() {
		try {
			REQUESTS.GET.makeRequest(DiscordEndpoints.USERS + "@me/guilds",
					new BasicNameValuePair("authorization", getToken()));
			return true;
		} catch (RateLimitException | DiscordException e) {
			return false;
		}
	}

	@Override
	public void logout() throws RateLimitException, DiscordException {
		if (isReady()) {
			if (!isBot())
				REQUESTS.POST.makeRequest(DiscordEndpoints.LOGOUT,
						new BasicNameValuePair("authorization", token));

			ws.disconnect(DiscordDisconnectedEvent.Reason.LOGGED_OUT);
		} else
			Discord4J.LOGGER.error(LogMarkers.API, "Bot has not signed in yet!");
	}

	/**
	 * Gets the WebSocket gateway
	 *
	 * @param token Our login token
	 * @return the WebSocket URL of which to connect
	 */
	private String obtainGateway(String token) {
		String gateway = null;
		try {
			GatewayResponse response = DiscordUtils.GSON.fromJson(REQUESTS.GET.makeRequest(DiscordEndpoints.GATEWAY,
					new BasicNameValuePair("authorization", token)), GatewayResponse.class);
			gateway = response.url;//.replaceAll("wss", "ws");
		} catch (RateLimitException | DiscordException e) {
			Discord4J.LOGGER.error(LogMarkers.API, "Discord4J Internal Exception", e);
		}
		Discord4J.LOGGER.debug(LogMarkers.API, "Obtained gateway {}.", gateway);
		return gateway;
	}

	private void changeAccountInfo(Optional<String> username, Optional<String> email, Optional<String> password, Optional<Image> avatar) throws RateLimitException, DiscordException {
		Discord4J.LOGGER.debug(LogMarkers.API, "Changing account info.");

		if (!isReady()) {
			Discord4J.LOGGER.error(LogMarkers.API, "Bot has not signed in yet!");
			return;
		}

		try {
			AccountInfoChangeResponse response = DiscordUtils.GSON.fromJson(REQUESTS.PATCH.makeRequest(DiscordEndpoints.USERS+"@me",
					new StringEntity(DiscordUtils.GSON.toJson(new AccountInfoChangeRequest(email.orElse(this.email),
							this.password, password.orElse(this.password), username.orElse(getOurUser().getName()),
							avatar == null ? Image.forUser(ourUser).getData() :
									(avatar.isPresent() ? avatar.get().getData() : Image.defaultAvatar().getData())))),
					new BasicNameValuePair("Authorization", token),
					new BasicNameValuePair("content-type", "application/json; charset=UTF-8")), AccountInfoChangeResponse.class);

			if (!this.token.equals(response.token)) {
				Discord4J.LOGGER.debug(LogMarkers.API, "Token changed, updating it.");
				this.token = response.token;
			}
		} catch (UnsupportedEncodingException e) {
			Discord4J.LOGGER.error(LogMarkers.API, "Discord4J Internal Exception", e);
		}
	}

	@Override
	public void changeUsername(String username) throws DiscordException, RateLimitException {
		changeAccountInfo(Optional.of(username), Optional.empty(), Optional.empty(), null);
	}

	@Override
	public void changeEmail(String email) throws DiscordException, RateLimitException {
		changeAccountInfo(Optional.empty(), Optional.of(email), Optional.empty(), null);
	}

	@Override
	public void changePassword(String password) throws DiscordException, RateLimitException {
		changeAccountInfo(Optional.empty(), Optional.empty(), Optional.of(password), null);
	}

	@Override
	public void changeAvatar(Image avatar) throws DiscordException, RateLimitException {
		changeAccountInfo(Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(avatar));
	}

	private void updatePresence(boolean isIdle, Status status) {
		if (!isReady()) {
			Discord4J.LOGGER.error(LogMarkers.API, "Bot has not signed in yet!");
			return;
		}

		if (!status.equals(getOurUser().getStatus())) {
			Status oldStatus = getOurUser().getStatus();
			((User) getOurUser()).setStatus(status);
			dispatcher.dispatch(new StatusChangeEvent(getOurUser(), oldStatus, status));
		}

		if ((getOurUser().getPresence() != Presences.IDLE && isIdle)
				|| (getOurUser().getPresence() == Presences.IDLE && !isIdle)
				|| (getOurUser().getPresence() != Presences.STREAMING && status.getType() == Status.StatusType.STREAM)) {
			Presences oldPresence = getOurUser().getPresence();
			Presences newPresence = isIdle ? Presences.IDLE :
					(status.getType() == Status.StatusType.STREAM ? Presences.STREAMING : Presences.ONLINE);
			((User) getOurUser()).setPresence(newPresence);
			dispatcher.dispatch(new PresenceUpdateEvent(getOurUser(), oldPresence, newPresence));
		}

		ws.send(DiscordUtils.GSON.toJson(new PresenceUpdateRequest(isIdle ? System.currentTimeMillis() : null, status)));
	}

	@Override
	public void changePresence(boolean isIdle) {
		updatePresence(isIdle, getOurUser().getStatus());
	}

	@Override
	public void changeStatus(Status status) {
		updatePresence(getOurUser().getPresence() == Presences.IDLE, status);
	}

	@Override
	public boolean isReady() {
		return isReady && ws != null && ws.isConnected.get() && !ws.isReconnecting.get();
	}

	@Override
	public IUser getOurUser() {
		if (!isReady()) {
			Discord4J.LOGGER.error(LogMarkers.API, "Bot has not signed in yet!");
			return null;
		}
		return ourUser;
	}

	@Override
	public List<IChannel> getChannels(boolean priv) {
		List<IChannel> channels = guildList.stream()
				.map(IGuild::getChannels)
				.flatMap(List::stream)
				.collect(Collectors.toList());
		if (priv)
			channels.addAll(privateChannels);
		return channels;
	}

	@Override
	public List<IChannel> getChannels() {
		return getChannels(false);
	}

	@Override
	public IChannel getChannelByID(String id) {
		return getChannels(true).stream()
				.filter(c -> c.getID().equalsIgnoreCase(id))
				.findAny().orElse(null);
	}

	@Override
	public List<IVoiceChannel> getVoiceChannels() {
		return guildList.stream()
				.map(IGuild::getVoiceChannels)
				.flatMap(List::stream)
				.collect(Collectors.toList());
	}

	@Override
	public IVoiceChannel getVoiceChannelByID(String id) {
		return getVoiceChannels().stream()
				.filter(c -> c.getID().equalsIgnoreCase(id))
				.findAny().orElse(null);
	}

	@Override
	public List<IGuild> getGuilds() {
		return guildList;
	}

	@Override
	public IGuild getGuildByID(String guildID) {
		return guildList.stream()
				.filter(g -> g.getID().equalsIgnoreCase(guildID))
				.findAny().orElse(null);
	}

	@Override
	public List<IUser> getUsers() {
		return guildList.stream()
				.map(IGuild::getUsers)
				.flatMap(List::stream)
				.distinct()
				.collect(Collectors.toList());
	}

	@Override
	public IUser getUserByID(String userID) {
		IGuild guild = guildList.stream()
				.filter(g -> g.getUserByID(userID) != null)
				.findFirst()
				.orElse(null);

		IUser user = guild != null ? guild.getUserByID(userID) : null;

		return ourUser != null && ourUser.getID().equals(userID) ? ourUser : user; // List of users doesn't include the bot user. Check if the id is that of the bot.
	}

	@Override
	public List<IRole> getRoles() {
		return guildList.stream()
				.map(IGuild::getRoles)
				.flatMap(List::stream)
				.collect(Collectors.toList());
	}

	@Override
	public IRole getRoleByID(String roleID) {
		return getRoles().stream()
				.filter(r -> r.getID().equalsIgnoreCase(roleID))
				.findAny().orElse(null);
	}

	@Override
	public List<IMessage> getMessages(boolean includePrivate) {
		return getChannels(includePrivate).stream()
				.map(IChannel::getMessages)
				.flatMap(List::stream)
				.collect(Collectors.toList());
	}

	@Override
	public List<IMessage> getMessages() {
		return getMessages(false);
	}

	@Override
	public IMessage getMessageByID(String messageID) {
		for (IGuild guild : guildList) {
			IMessage message = guild.getMessageByID(messageID);
			if (message != null)
				return message;
		}

		for (IPrivateChannel privateChannel : privateChannels) {
			IMessage message = privateChannel.getMessageByID(messageID);
			if (message != null)
				return message;
		}

		return null;
	}

	@Override
	public IPrivateChannel getOrCreatePMChannel(IUser user) throws DiscordException, RateLimitException {
		if (!isReady()) {
			Discord4J.LOGGER.error(LogMarkers.API, "Bot has not signed in yet!");
			return null;
		}

		if (user.equals(getOurUser()))
			throw new DiscordException("Cannot PM yourself!");

		Optional<IPrivateChannel> opt = privateChannels.stream()
				.filter(c -> c.getRecipient().getID().equalsIgnoreCase(user.getID()))
				.findAny();
		if (opt.isPresent())
			return opt.get();

		PrivateChannelResponse response = null;
		try {
			response = DiscordUtils.GSON.fromJson(REQUESTS.POST.makeRequest(DiscordEndpoints.USERS+this.ourUser.getID()+"/channels",
					new StringEntity(DiscordUtils.GSON.toJson(new PrivateChannelRequest(user.getID()))),
					new BasicNameValuePair("authorization", this.token),
					new BasicNameValuePair("content-type", "application/json")), PrivateChannelResponse.class);

			IPrivateChannel channel = DiscordUtils.getPrivateChannelFromJSON(this, response);
			privateChannels.add(channel);
			return channel;
		} catch (UnsupportedEncodingException e) {
			Discord4J.LOGGER.error(LogMarkers.API, "Error creating creating a private channel!", e);
		}

		return null;
	}

	@Override
	public IInvite getInviteForCode(String code) {
		if (!isReady()) {
			Discord4J.LOGGER.error(LogMarkers.API, "Bot has not signed in yet!");
			return null;
		}

		try {
			InviteJSONResponse response = DiscordUtils.GSON.fromJson(REQUESTS.GET.makeRequest(DiscordEndpoints.INVITE+code,
					new BasicNameValuePair("authorization", token)), InviteJSONResponse.class);

			return DiscordUtils.getInviteFromJSON(this, response);
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public List<IRegion> getRegions() throws RateLimitException, DiscordException {
		if (REGIONS.isEmpty()) {
			RegionResponse[] regions = DiscordUtils.GSON.fromJson(REQUESTS.GET.makeRequest(
					DiscordEndpoints.VOICE+"regions",
					new BasicNameValuePair("authorization", this.token)),
					RegionResponse[].class);

			Arrays.stream(regions)
					.map(DiscordUtils::getRegionFromJSON)
					.forEach(REGIONS::add);
		}

		return REGIONS;
	}

	@Override
	public IRegion getRegionByID(String regionID) {
		try {
			return getRegions().stream()
					.filter(r -> r.getID().equals(regionID))
					.findAny().orElse(null);
		} catch (RateLimitException | DiscordException e) {
			Discord4J.LOGGER.error(LogMarkers.API, "Discord4J Internal Exception", e);
		}
		return null;
	}

	@Override
	public IGuild createGuild(String name, IRegion region) throws RateLimitException, DiscordException {
		return createGuild(name, region, (Image) null);
	}

	@Override
	public IGuild createGuild(String name, IRegion region, Image icon) throws RateLimitException, DiscordException {
		if (isBot())
			throw new DiscordException("This action can only be performed by as user");

		try {
			GuildResponse guildResponse = DiscordUtils.GSON.fromJson(REQUESTS.POST.makeRequest(DiscordEndpoints.APIBASE+"/guilds",
					new StringEntity(DiscordUtils.GSON_NO_NULLS.toJson(
							new CreateGuildRequest(name, region.getID(), icon))),
					new BasicNameValuePair("authorization", this.token),
					new BasicNameValuePair("content-type", "application/json")), GuildResponse.class);
			IGuild guild = DiscordUtils.getGuildFromJSON(this, guildResponse);
			guildList.add(guild);
			return guild;
		} catch (UnsupportedEncodingException e) {
			Discord4J.LOGGER.error(LogMarkers.API, "Discord4J Internal Exception", e);
		}
		return null;
	}

	@Override
	public long getResponseTime() {
		return ws.getResponseTime();
	}

	@Override
	public List<IVoiceChannel> getConnectedVoiceChannels() {
		return ourUser.getConnectedVoiceChannels();
	}

	@Override
	public boolean isBot() {
		return isBot;
	}

	/**
	 * FOR INTERNAL USE ONLY: Converts this user client to a bot client.
	 *
	 * @param token The bot's new token.
	 */
	public void convert(String token) {
		isBot = true;
		email = null;
		password = null;
		this.token = token;

		if (isReady()) {
			((User) getOurUser()).convertToBot();
		}
	}

	@Override
	public List<IApplication> getApplications() throws RateLimitException, DiscordException {
		if (isBot())
			throw new DiscordException("This action can only be performed by a user");

		List<IApplication> applications = new ArrayList<>();

		ApplicationResponse[] responses = DiscordUtils.GSON.fromJson(REQUESTS.GET.makeRequest(DiscordEndpoints.APPLICATIONS,
				new BasicNameValuePair("authorization", getToken()),
				new BasicNameValuePair("content-type", "application/json")), ApplicationResponse[].class);

		for (ApplicationResponse response : responses)
			applications.add(DiscordUtils.getApplicationFromJSON(this, response));

		return applications;
	}

	@Override
	public IApplication createApplication(String name) throws DiscordException, RateLimitException {
		if (isBot())
			throw new DiscordException("This action can only be performed by a user");

		ApplicationResponse response = null;
		try {
			response = DiscordUtils.GSON.fromJson(REQUESTS.POST.makeRequest(DiscordEndpoints.APPLICATIONS,
					new StringEntity(DiscordUtils.GSON.toJson(new ApplicationCreateRequest(name))),
					new BasicNameValuePair("authorization", getToken()),
					new BasicNameValuePair("content-type", "application/json")), ApplicationResponse.class);
			return DiscordUtils.getApplicationFromJSON(this, response);

		} catch (UnsupportedEncodingException e) {
			Discord4J.LOGGER.error(LogMarkers.API, "Discord4J Internal Exception", e);
		}

		return null;
	}

	@Override
	public LocalDateTime getLaunchTime() {
		return launchTime;
	}

	private ApplicationInfoResponse getApplicationInfo() throws DiscordException, RateLimitException {
		return DiscordUtils.GSON.fromJson(REQUESTS.GET.makeRequest(DiscordEndpoints.APPLICATIONS+"/@me",
				new BasicNameValuePair("authorization", getToken()),
				new BasicNameValuePair("content-type", "application/json")), ApplicationInfoResponse.class);
	}

	@Override
	public String getDescription() throws DiscordException {
		try {
			return getApplicationInfo().description;
		} catch (RateLimitException e) {
			Discord4J.LOGGER.error(LogMarkers.API, "Discord4J Internal Exception", e);
		}
		return null;
	}

	@Override
	public String getApplicationIconURL() throws DiscordException {
		try {
			ApplicationInfoResponse info = getApplicationInfo();
			return String.format(DiscordEndpoints.APPLICATION_ICON, info.id, info.icon);
		} catch (RateLimitException e) {
			Discord4J.LOGGER.error(LogMarkers.API, "Discord4J Internal Exception", e);
		}
		return null;
	}

	@Override
	public String getApplicationClientID() throws DiscordException {
		try {
			return getApplicationInfo().id;
		} catch (RateLimitException e) {
			Discord4J.LOGGER.error(LogMarkers.API, "Discord4J Internal Exception", e);
		}
		return null;
	}

	@Override
	public String getApplicationName() throws DiscordException {
		try {
			return getApplicationInfo().name;
		} catch (RateLimitException e) {
			Discord4J.LOGGER.error(LogMarkers.API, "Discord4J Internal Exception", e);
		}
		return null;
	}

	@Override
	public IUser getApplicationOwner() throws DiscordException {
		try {
			UserResponse owner = getApplicationInfo().owner;

			IUser user = getUserByID(owner.id);
			if (user == null)
				user = DiscordUtils.getUserFromJSON(this, owner);

			return user;
		} catch (RateLimitException e) {
			Discord4J.LOGGER.error(LogMarkers.API, "Discord4J Internal Exception", e);
		}
		return null;
	}
}
