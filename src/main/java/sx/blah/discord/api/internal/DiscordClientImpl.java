package sx.blah.discord.api.internal;

import sx.blah.discord.Discord4J;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.IShard;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.api.internal.json.objects.InviteObject;
import sx.blah.discord.api.internal.json.objects.UserObject;
import sx.blah.discord.api.internal.json.objects.VoiceRegionObject;
import sx.blah.discord.api.internal.json.requests.AccountInfoChangeRequest;
import sx.blah.discord.api.internal.json.responses.ApplicationInfoResponse;
import sx.blah.discord.api.internal.json.responses.GatewayBotResponse;
import sx.blah.discord.api.internal.json.responses.GatewayResponse;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.ShardReadyEvent;
import sx.blah.discord.handle.impl.obj.User;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.modules.ModuleLoader;
import sx.blah.discord.util.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import sx.blah.discord.api.ClientBuilder;

/**
 * Defines the client. This class receives and sends messages, as well as holds our user data.
 */
public final class DiscordClientImpl implements IDiscordClient {

	static {
		ServiceUtil.loadServices();
	}

	/**
	 * The shards this client controls.
	 */
	private final List<IShard> shards = new CopyOnWriteArrayList<>();

	/**
	 * User we are logged in as
	 */
	volatile User ourUser;

	/**
	 * Our token, so we can send XHR to Discord.
	 */
	protected volatile String token;

	/**
	 * Event dispatcher.
	 */
	volatile EventDispatcher dispatcher;

	/**
	 * Reconnect manager.
	 */
	volatile ReconnectManager reconnectManager;

	/**
	 * Caches the available regions for discord.
	 */
	private final List<IRegion> REGIONS = new CopyOnWriteArrayList<>();

	/**
	 * Holds the active connections to voice sockets.
	 */
	public final Map<IGuild, DiscordVoiceWS> voiceConnections = new ConcurrentHashMap<>();

	/**
	 * The maximum amount of pings discord can miss before a new session is created.
	 */
	final int maxMissedPings;

	/**
	 * Whether the websocket should act as a daemon.
	 */
	private final boolean isDaemon;
        
        
        private final boolean recommendShardCount;

	/**
	 * The total number of shards this client manages.
	 */
	private int shardCount;

	/**
	 * The requests holder object.
	 */
	public final Requests REQUESTS = new Requests(this);

	/**
	 * Timer to keep the program alive if the client is not daemon
	 */
	volatile Timer keepAlive;
	private final int retryCount;

	public DiscordClientImpl(String token, int shardCount, boolean isDaemon, int maxMissedPings, int maxReconnectAttempts,
							 int retryCount) {
		this.token = "Bot " + token;
		this.retryCount = retryCount;
		this.maxMissedPings = maxMissedPings;
		this.isDaemon = isDaemon;
		this.shardCount = shardCount;
                this.recommendShardCount = false;
		this.dispatcher = new EventDispatcher(this);
		this.reconnectManager = new ReconnectManager(this, maxReconnectAttempts);
		
		final DiscordClientImpl instance = this;
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			if (instance.keepAlive != null)
				instance.keepAlive.cancel();
		}));
	}

        public DiscordClientImpl(ClientBuilder builder) {
		this.token = "Bot " + builder.getToken();
		this.retryCount = builder.get5xxRetryCount();
		this.maxMissedPings = builder.getPingTimeout();
		this.isDaemon = builder.isDaemon();
		this.shardCount = builder.getShardCount();
                this.recommendShardCount = builder.isRecommendingShardCount();
		this.dispatcher = new EventDispatcher(this);
		this.reconnectManager = new ReconnectManager(this, builder.getMaxReconnectAttempts());
		
		final DiscordClientImpl instance = this;
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			if (instance.keepAlive != null)
				instance.keepAlive.cancel();
		}));
        }

	@Override
	public List<IShard> getShards() {
		return this.shards;
	}

	@Override
	public int getShardCount() {
                if (recommendShardCount) {
            		checkLoggedIn("get shard count");
                }
		return this.shardCount;
	}

	@Override
	public EventDispatcher getDispatcher() {
		return this.dispatcher;
	}

	@Override
	public ModuleLoader getModuleLoader() {
		return ModuleLoader.getForClient(this);
	}

	@Override
	public String getToken() {
		return this.token;
	}

	private void changeAccountInfo(String username, String avatar) throws DiscordException, RateLimitException {
		checkLoggedIn("change account info");

		Discord4J.LOGGER.debug(LogMarkers.API, "Changing account info.");
		REQUESTS.PATCH.makeRequest(DiscordEndpoints.USERS+"@me", new AccountInfoChangeRequest(username, avatar));
	}

	@Override
	public void changeUsername(String username) throws DiscordException, RateLimitException {
		changeAccountInfo(username, Image.forUser(ourUser).getData());
	}

	@Override
	public void changeAvatar(Image avatar) throws DiscordException, RateLimitException {
		changeAccountInfo(ourUser.getName(), avatar.getData());
	}

	@Override
	public IUser getOurUser() {
		return ourUser;
	}

	@Override
	public List<IRegion> getRegions() throws DiscordException, RateLimitException {
		if (REGIONS.isEmpty()) {
			VoiceRegionObject[] regions = REQUESTS.GET.makeRequest(
					DiscordEndpoints.VOICE+"regions", VoiceRegionObject[].class);

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

	private ApplicationInfoResponse getApplicationInfo() throws DiscordException, RateLimitException {
		return REQUESTS.GET.makeRequest(DiscordEndpoints.APPLICATIONS+"/@me", ApplicationInfoResponse.class);
	}

	@Override
	public String getApplicationDescription() throws DiscordException {
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
				user = DiscordUtils.getUserFromJSON(getShards().get(0), owner);

			return user;
		} catch (RateLimitException e) {
			Discord4J.LOGGER.error(LogMarkers.API, "Discord4J Internal Exception", e);
		}
		return null;
	}

	private String obtainGateway() {
		String gateway = null;
		try {
                        if (recommendShardCount) {
                                GatewayBotResponse response = REQUESTS.GET.makeRequest(DiscordEndpoints.GATEWAY_BOT, GatewayBotResponse.class);
                                gateway = response.url + "?encoding=json&v=5";
                                this.shardCount = response.shards;
                                Discord4J.LOGGER.debug(LogMarkers.API, "Obtained gateway {} and recommended shard count {}.", gateway, shardCount);
                        } else {
                                GatewayResponse response = REQUESTS.GET.makeRequest(DiscordEndpoints.GATEWAY, GatewayResponse.class);
                                gateway = response.url + "?encoding=json&v=5";
                                Discord4J.LOGGER.debug(LogMarkers.API, "Obtained gateway {}.", gateway);
                        }
		} catch (RateLimitException | DiscordException e) {
			Discord4J.LOGGER.error(LogMarkers.API, "Discord4J Internal Exception", e);
		}
		return gateway;
	}

	private void validateToken() throws DiscordException, RateLimitException {
		REQUESTS.GET.makeRequest(DiscordEndpoints.USERS + "@me");
	}

	// Sharding delegation

	@Override
	public void login() throws DiscordException, RateLimitException {
		if (!getShards().isEmpty()) {
			throw new DiscordException("Attempt to login client more than once.");
		}

		validateToken();

		String gateway = obtainGateway();
		new RequestBuilder(this).setAsync(true).doAction(() -> {
			for (int i = 0; i < shardCount; i++) {
				final int shardNum = i;
				ShardImpl shard = new ShardImpl(this, gateway, new int[] {shardNum, shardCount});
				getShards().add(shardNum, shard);
				shard.login();

				getDispatcher().waitFor((ShardReadyEvent e) -> true, 1, TimeUnit.MINUTES, () ->
					Discord4J.LOGGER.warn(LogMarkers.API, "Shard {} failed to login.", shardNum)
				);

				if (i != shardCount - 1) { // all but last
					Discord4J.LOGGER.trace(LogMarkers.API, "Sleeping for login ratelimit.");
					Thread.sleep(5000);
				}
			}
			getDispatcher().dispatch(new ReadyEvent());
			return true;
		}).build();

		if (!isDaemon) {
			if (keepAlive == null) keepAlive = new Timer("DiscordClientImpl Keep Alive");
			keepAlive.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					Discord4J.LOGGER.trace(LogMarkers.API, "DiscordClientImpl Keep Alive");
				}
			}, 0, 10000);
		}
	}

	@Override
	public void logout() throws DiscordException {
		for (IShard shard : getShards()) {
			shard.logout();
		}
		getShards().clear();
		if (keepAlive != null) keepAlive.cancel();
	}

	@Override
	public boolean isLoggedIn() {
		return !getShards().isEmpty()
                        && getShards().size() == getShardCount()
                        && getShards().stream().map(IShard::isLoggedIn).allMatch(bool -> bool);
	}

	@Override
	public boolean isReady() {
		return !getShards().isEmpty()
                        && getShards().size() == getShardCount()
                        && getShards().stream().map(IShard::isReady).allMatch(bool -> bool);
	}

	@Override
	public void changeStatus(Status status) {
		getShards().forEach(shard -> shard.changeStatus(status));
	}

	@Override
	public void changePresence(boolean isIdle) {
		getShards().forEach(shard -> shard.changePresence(isIdle));
	}

	@Override
	public List<IGuild> getGuilds() {
		return getShards().stream()
				.map(IShard::getGuilds)
				.flatMap(List::stream)
				.collect(Collectors.toList());
	}

	@Override
	public IGuild getGuildByID(String guildID) {
		return getGuilds().stream()
				.filter(g -> g.getID().equals(guildID))
				.findFirst().orElse(null);
	}

	@Override
	public List<IChannel> getChannels(boolean includePrivate) {
		return getShards().stream()
				.map(c -> c.getChannels(includePrivate))
				.flatMap(List::stream)
				.collect(Collectors.toList());
	}

	@Override
	public List<IChannel> getChannels() {
		return getShards().stream()
				.map(IShard::getChannels)
				.flatMap(List::stream)
				.collect(Collectors.toList());
	}

	@Override
	public IChannel getChannelByID(String channelID) {
		return getChannels(true).stream()
				.filter(c -> c.getID().equals(channelID))
				.findFirst().orElse(null);
	}

	@Override
	public List<IVoiceChannel> getVoiceChannels() {
		return getShards().stream()
				.map(IShard::getVoiceChannels)
				.flatMap(List::stream)
				.collect(Collectors.toList());
	}

	@Override
	public List<IVoiceChannel> getConnectedVoiceChannels() {
		return getOurUser().getConnectedVoiceChannels();
	}

	@Override
	public IVoiceChannel getVoiceChannelByID(String id) {
		return getVoiceChannels().stream()
				.filter(vc -> vc.getID().equals(id))
				.findFirst().orElse(null);
	}

	@Override
	public List<IUser> getUsers() {
		return getShards().stream()
				.map(IShard::getUsers)
				.flatMap(List::stream)
				.collect(Collectors.toList());
	}

	@Override
	public IUser getUserByID(String userID) {
		return getUsers().stream()
				.filter(u -> u.getID().equals(userID))
				.findFirst().orElse(null);
	}

	@Override
	public List<IRole> getRoles() {
		return getShards().stream()
				.map(IShard::getRoles)
				.flatMap(List::stream)
				.collect(Collectors.toList());
	}

	@Override
	public IRole getRoleByID(String roleID) {
		return getRoles().stream()
				.filter(r -> r.getID().equals(roleID))
				.findFirst().orElse(null);
	}

	@Override
	public List<IMessage> getMessages(boolean includePrivate) {
		return getShards().stream()
				.map(c -> c.getMessages(includePrivate))
				.flatMap(List::stream)
				.collect(Collectors.toList());
	}

	@Override
	public List<IMessage> getMessages() {
		return getShards().stream()
				.map(IShard::getMessages)
				.flatMap(List::stream)
				.collect(Collectors.toList());
	}

	@Override
	public IMessage getMessageByID(String messageID) {
		return getMessages(true).stream()
				.filter(m -> m.getID().equals(messageID))
				.findFirst().orElse(null);
	}

	@Override
	public IPrivateChannel getOrCreatePMChannel(IUser user) throws DiscordException, RateLimitException {
		IShard shard = getShards().stream().filter(s -> s.getUserByID(user.getID()) != null).findFirst().get();
		return shard.getOrCreatePMChannel(user);
	}

	@Override
	public IInvite getInviteForCode(String code) {
		if (!isReady()) {
			Discord4J.LOGGER.error(LogMarkers.API, "Attempt to get invite before bot is ready!");
			return null;
		}

		InviteObject invite;
		try {
			invite = DiscordUtils.GSON.fromJson(REQUESTS.GET.makeRequest(DiscordEndpoints.INVITE + code), InviteObject.class);
		} catch (DiscordException | RateLimitException e) {
			Discord4J.LOGGER.error(LogMarkers.API, "Encountered error while retrieving invite: ", e);
			return null;
		}

		return invite == null ? null : DiscordUtils.getInviteFromJSON(this, invite);
	}

	public int getRetryCount() {
		return retryCount;
	}
}
