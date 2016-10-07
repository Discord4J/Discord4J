package sx.blah.discord.api.internal;

import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.api.internal.json.objects.InviteObject;
import sx.blah.discord.api.internal.json.objects.PrivateChannelObject;
import sx.blah.discord.api.internal.json.objects.UserObject;
import sx.blah.discord.api.internal.json.objects.VoiceRegionObject;
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
 * Defines the client. This class receives and sends messages, as well as holds our user data.
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
	protected volatile long lastHeartbeat = System.currentTimeMillis();

	/**
	 * User we are logged in as
	 */
	protected volatile User ourUser;

	/**
	 * Our token, so we can send XHR to Discord.
	 */
	protected volatile String token;

	/**
	 * Local copy of all guilds/servers.
	 */
	protected final List<IGuild> guildList = new CopyOnWriteArrayList<>();

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
	 * The sharding information of this client. [0] is the current shard, [1] is the total number of shards.
	 */
	protected int[] shard;

	/**
	 * When this client was logged into. Useful for determining uptime.
	 */
	protected volatile LocalDateTime launchTime;

	/**
	 * The requests holder object.
	 */
	public final Requests REQUESTS = new Requests(this);

	public DiscordClientImpl(String token, long timeoutTime, int maxMissedPingCount, boolean isDaemon, int[] shard, EventDispatcher dispatcher, ModuleLoader loader) {
		this.token = "Bot " + token;
		this.timeoutTime = timeoutTime;
		this.maxMissedPingCount = maxMissedPingCount;
		this.isDaemon = isDaemon;
		this.shard = shard;
		this.dispatcher = dispatcher;
		this.loader = loader;
	}

	public DiscordClientImpl(String token, long timeoutTime, int maxMissedPingCount, boolean isDaemon, int[] shard) {
		this(token, timeoutTime, maxMissedPingCount, isDaemon, shard, null, null);
		this.dispatcher = new EventDispatcher(this);
		this.loader = new ModuleLoader(this);
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
		return token;
	}

	@Override
	public void login() throws DiscordException {
		try {
			if (!validateToken()) throw new DiscordException("Invalid token!");

			this.ws = new DiscordWS(this, obtainGateway(), shard, isDaemon);
			launchTime = LocalDateTime.now();

		} catch (Exception e) {
			Discord4J.LOGGER.error(LogMarkers.API, "Exception caught, logging in!", e);
			throw new DiscordException("Login error occurred! Are your login details correct?");
		}
	}

	private boolean validateToken() {
		try {
			REQUESTS.GET.makeRequest(DiscordEndpoints.USERS + "@me/guilds");
			return true;
		} catch (RateLimitException | DiscordException e) {
			return false;
		}
	}

	/**
	 * Gets the WebSocket gateway
	 *
	 * @return the WebSocket URL to connect to
	 */
	private String obtainGateway() {
		String gateway = null;
		try {
			GatewayResponse response = DiscordUtils.GSON.fromJson(REQUESTS.GET.makeRequest(DiscordEndpoints.GATEWAY),
					GatewayResponse.class);
			gateway = response.url + "?encoding=json&v=5";
		} catch (RateLimitException | DiscordException e) {
			Discord4J.LOGGER.error(LogMarkers.API, "Discord4J Internal Exception", e);
		}
		Discord4J.LOGGER.debug(LogMarkers.API, "Obtained gateway {}.", gateway);
		return gateway;
	}

	/**
	 * @return The current shard this client is on.
	 */
	public int getCurrentShard() {
		return this.shard[0];
	}

	/**
	 * @return The total number of shards
	 */
	public int getTotalShards() {
		return this.shard[1];
	}

	@Override
	public void logout() throws RateLimitException, DiscordException {
		if (isLoggedIn()) {
			getConnectedVoiceChannels().forEach(IVoiceChannel::leave);
			ws.disconnect(DiscordDisconnectedEvent.Reason.LOGGED_OUT);
		} else {
			Discord4J.LOGGER.error(LogMarkers.API, "Bot has yet logged in!");
		}
	}

	private void changeAccountInfo(String username, String avatar) throws RateLimitException, DiscordException {
		Discord4J.LOGGER.debug(LogMarkers.API, "Changing account info.");

		if (!isLoggedIn()) {
			Discord4J.LOGGER.error(LogMarkers.API, "Bot has yet logged in!");
			return;
		}

		try {
			String json = REQUESTS.PATCH.makeRequest(DiscordEndpoints.USERS+"@me",
					new StringEntity(DiscordUtils.GSON.toJson(new AccountInfoChangeRequest(username, avatar))));
			AccountInfoChangeResponse response = DiscordUtils.GSON.fromJson(json, AccountInfoChangeResponse.class);

			if (!this.getToken().equals(response.token)) {
				Discord4J.LOGGER.debug(LogMarkers.API, "Token changed, updating it.");
				this.token = response.token;
			}
		} catch (UnsupportedEncodingException e) {
			Discord4J.LOGGER.error(LogMarkers.API, "Discord4J Internal Exception", e);
		}
	}

	@Override
	public void changeUsername(String username) throws DiscordException, RateLimitException {
		changeAccountInfo(username, Image.forUser(ourUser).getData());
	}

	@Override
	public void changeAvatar(Image avatar) throws DiscordException, RateLimitException {
		changeAccountInfo(ourUser.getName(), avatar.getData());
	}

	private void updatePresence(boolean isIdle, Status status) {
		if (!isLoggedIn()) {
			Discord4J.LOGGER.error(LogMarkers.API, "Bot has yet logged in!");
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

		ws.send(GatewayOps.STATUS_UPDATE, new PresenceUpdateRequest(isIdle ? System.currentTimeMillis() : null, status));
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
		return ws != null && ws.isReady;
	}

	@Override
	public boolean isLoggedIn() {
		return ws != null && ws.hasReceivedReady;
	}

	@Override
	public IUser getOurUser() {
		if (!isLoggedIn()) {
			Discord4J.LOGGER.error(LogMarkers.API, "Bot has yet logged in!");
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
			Discord4J.LOGGER.error(LogMarkers.API, "Bot is not yet ready!");
			return null;
		}

		if (user.equals(getOurUser()))
			throw new DiscordException("Cannot PM yourself!");

		Optional<IPrivateChannel> opt = privateChannels.stream()
				.filter(c -> c.getRecipient().getID().equalsIgnoreCase(user.getID()))
				.findAny();
		if (opt.isPresent())
			return opt.get();

		PrivateChannelObject pmChannel;
		try {
			pmChannel = DiscordUtils.GSON.fromJson(REQUESTS.POST.makeRequest(DiscordEndpoints.USERS+this.ourUser.getID()+"/channels",
					new StringEntity(DiscordUtils.GSON.toJson(new PrivateChannelCreateRequest(user.getID())))),
					PrivateChannelObject.class);

			IPrivateChannel channel = DiscordUtils.getPrivateChannelFromJSON(this, pmChannel);
			privateChannels.add(channel);
			return channel;
		} catch (UnsupportedEncodingException e) {
			Discord4J.LOGGER.error(LogMarkers.API, "Error creating creating a private channel!", e);
		}

		return null;
	}

	@Override
	public IInvite getInviteForCode(String code) {
		if (!isLoggedIn()) {
			Discord4J.LOGGER.error(LogMarkers.API, "Bot has yet logged in!");
			return null;
		}

		try {
			InviteObject response = DiscordUtils.GSON.fromJson(
					REQUESTS.GET.makeRequest(DiscordEndpoints.INVITE+code),
					InviteObject.class);

			return DiscordUtils.getInviteFromJSON(this, response);
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public List<IRegion> getRegions() throws RateLimitException, DiscordException {
		if (REGIONS.isEmpty()) {
			VoiceRegionObject[] regions = DiscordUtils.GSON.fromJson(REQUESTS.GET.makeRequest(
					DiscordEndpoints.VOICE+"regions"),
					VoiceRegionObject[].class);

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
	public long getResponseTime() {
		// TODO
		return 0;
	}

	@Override
	public List<IVoiceChannel> getConnectedVoiceChannels() {
		return ourUser.getConnectedVoiceChannels();
	}

	@Override
	public LocalDateTime getLaunchTime() {
		return launchTime;
	}

	private ApplicationInfoResponse getApplicationInfo() throws DiscordException, RateLimitException {
		return DiscordUtils.GSON.fromJson(REQUESTS.GET.makeRequest(DiscordEndpoints.APPLICATIONS+"/@me"),
				ApplicationInfoResponse.class);
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
			UserObject owner = getApplicationInfo().owner;

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
