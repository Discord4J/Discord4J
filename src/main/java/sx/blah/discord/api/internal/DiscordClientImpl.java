/*
 *     This file is part of Discord4J.
 *
 *     Discord4J is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Discord4J is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */

package sx.blah.discord.api.internal;

import sx.blah.discord.Discord4J;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.IShard;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.api.internal.json.objects.InviteObject;
import sx.blah.discord.api.internal.json.objects.UserObject;
import sx.blah.discord.api.internal.json.objects.VoiceRegionObject;
import sx.blah.discord.api.internal.json.requests.AccountInfoChangeRequest;
import sx.blah.discord.api.internal.json.requests.PresenceUpdateRequest;
import sx.blah.discord.api.internal.json.requests.voice.VoiceStateUpdateRequest;
import sx.blah.discord.api.internal.json.responses.ApplicationInfoResponse;
import sx.blah.discord.api.internal.json.responses.GatewayResponse;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.shard.ShardReadyEvent;
import sx.blah.discord.handle.impl.obj.Guild;
import sx.blah.discord.handle.impl.obj.User;
import sx.blah.discord.handle.impl.obj.VoiceState;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.modules.Configuration;
import sx.blah.discord.modules.ModuleLoader;
import sx.blah.discord.util.*;
import sx.blah.discord.util.cache.ICacheDelegateProvider;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * The default implementation of {@link IDiscordClient}.
 */
public final class DiscordClientImpl implements IDiscordClient {

	/**
	 * The shards this client controls.
	 */
	private final List<IShard> shards = new CopyOnWriteArrayList<>();

	/**
	 * The user that represents the bot account.
	 */
	volatile User ourUser;

	/**
	 * The authentication token for this account.
	 */
	protected volatile String token;

	/**
	 * The client's event dispatcher.
	 */
	volatile EventDispatcher dispatcher;

	/**
	 * The client's reconnect manager.
	 */
	volatile ReconnectManager reconnectManager;

	/**
	 * The client's module loader.
	 */
	private volatile ModuleLoader loader;

	/**
	 * The cache of the available voice regions.
	 */
	private final Map<String, IRegion> regions = new ConcurrentHashMap<>();

	/**
	 * The maximum number of heartbeats that Discord can miss before a reconnect begins.
	 */
	final int maxMissedPings;

	/**
	 * Whether the websocket should act as a daemon.
	 */
	private final boolean isDaemon;

	/**
	 * The total number of shards this client manages.
	 */
	private int shardCount;

	/**
	 * Provides cache objects used by this client.
	 */
	private final ICacheDelegateProvider cacheProvider;

	/**
	 * The sharding information for this client.
	 */
	private final int[] shard;

	/**
	 * The requests holder object.
	 */
	public final Requests REQUESTS = new Requests(this);

	/**
	 * Timer to keep the program alive if the client is not daemon
	 */
	volatile Timer keepAlive;

	/**
	 * The number of times the client will retry on a 5xx HTTP response from Discord.
	 */
	private final int retryCount;

	/**
	 * The maximum number of messages that will be cached per channel.
	 */
	private final int maxCacheCount;

	/**
	 * The presence object that should be sent to Discord when identifying.
	 */
	private final PresenceUpdateRequest identifyPresence;

	/**
	 * The ID of the owner of this application.
	 */
	private volatile long applicationOwnerID;

	public DiscordClientImpl(String token, int shardCount, boolean isDaemon, int maxMissedPings, int maxReconnectAttempts,
							 int retryCount, int maxCacheCount, ICacheDelegateProvider provider, int[] shard,
							 RejectedExecutionHandler backpressureHandler, int minimumPoolSize, int maximumPoolSize,
							 int overflowCapacity, long eventThreadTimeout, TimeUnit eventThreadTimeoutUnit,
							 PresenceUpdateRequest identifyPresence) {
		this.token = "Bot " + token;
		this.retryCount = retryCount;
		this.maxMissedPings = maxMissedPings;
		this.isDaemon = isDaemon;
		this.shardCount = shardCount == -1 ? 1 : shardCount;
		this.maxCacheCount = maxCacheCount;
		this.cacheProvider = provider;
		this.shard = shard;
		this.dispatcher = new EventDispatcher(this, backpressureHandler, minimumPoolSize, maximumPoolSize,
				overflowCapacity, eventThreadTimeout, eventThreadTimeoutUnit);
		this.reconnectManager = new ReconnectManager(this, maxReconnectAttempts);
		this.loader = new ModuleLoader(this);

		// Fixes null from getModuleLoader from enable().
		if (Configuration.AUTOMATICALLY_ENABLE_MODULES) {
			loader.loadModules();
		}

		this.identifyPresence = identifyPresence;

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			if (this.keepAlive != null)
				this.keepAlive.cancel();
		}));
	}

	@Override
	public List<IShard> getShards() {
		return this.shards;
	}

	@Override
	public int getShardCount() {
		return this.shardCount;
	}

	@Override
	public EventDispatcher getDispatcher() {
		return this.dispatcher;
	}

	@Override
	public ModuleLoader getModuleLoader() {
		return this.loader;
	}

	@Override
	public String getToken() {
		return this.token;
	}

	private void changeAccountInfo(String username, String avatar) {
		checkLoggedIn("change account info");

		Discord4J.LOGGER.debug(LogMarkers.API, "Changing account info.");
		REQUESTS.PATCH.makeRequest(DiscordEndpoints.USERS+"@me", new AccountInfoChangeRequest(username, avatar));
	}

	@Override
	public void changeUsername(String username) {
		changeAccountInfo(username, Image.forUser(ourUser).getData());
	}

	@Override
	public void changeAvatar(Image avatar) {
		changeAccountInfo(ourUser.getName(), avatar.getData());
	}

	@Override
	public IUser getOurUser() {
		return ourUser;
	}

	private void loadStandardRegions() {
		synchronized (regions) {
			if (regions.isEmpty()) { // Guarantee so standard regions are first
				VoiceRegionObject[] regionObjects = RequestBuffer.request(() ->
						(VoiceRegionObject[]) REQUESTS.GET.makeRequest(
								DiscordEndpoints.VOICE + "regions", VoiceRegionObject[].class)).get();

				Arrays.stream(regionObjects)
						.map(DiscordUtils::getRegionFromJSON)
						.forEach(r -> regions.putIfAbsent(r.getID(), r));
			}
		}
	}

	public IRegion getGuildRegion(Guild guild) {
		loadStandardRegions();
		synchronized (regions) {
			IRegion region = regions.get(guild.getRegionID());

			if (region == null) { // New region types means Discord has updated
				VoiceRegionObject[] regionObjects = RequestBuffer.request(() ->
						(VoiceRegionObject[]) REQUESTS.GET.makeRequest(
								DiscordEndpoints.GUILDS + guild.getStringID() + "/regions", VoiceRegionObject[].class)).get();

				Arrays.stream(regionObjects)
						.map(DiscordUtils::getRegionFromJSON)
						.forEach(r -> regions.putIfAbsent(r.getID(), r));

				region = regions.get(guild.getRegionID());
			}

			return region;
		}
	}

	private void loadAllRegions() {
		loadStandardRegions();

		shards.stream()
				.map(IShard::getGuilds)
				.flatMap(List::stream)
				.map(g -> (Guild) g)
				.forEach(this::getGuildRegion);
	}

	@Override
	public List<IRegion> getRegions() {
		loadAllRegions();
		return new ArrayList<>(regions.values());
	}

	@Override
	public IRegion getRegionByID(String regionID) {
		loadStandardRegions();
		IRegion region = regions.get(regionID);

		if (region == null) {
			loadAllRegions();
			region = regions.get(regionID);
		}

		return region;
	}

	private ApplicationInfoResponse getApplicationInfo() {
		return REQUESTS.GET.makeRequest(DiscordEndpoints.APPLICATIONS+"/@me", ApplicationInfoResponse.class);
	}

	@Override
	public String getApplicationDescription() {
		try {
			return getApplicationInfo().description;
		} catch (RateLimitException e) {
			Discord4J.LOGGER.error(LogMarkers.API, "Discord4J Internal Exception", e);
		}
		return null;
	}

	@Override
	public String getApplicationIconURL() {
		try {
			ApplicationInfoResponse info = getApplicationInfo();
			return String.format(DiscordEndpoints.APPLICATION_ICON, info.id, info.icon);
		} catch (RateLimitException e) {
			Discord4J.LOGGER.error(LogMarkers.API, "Discord4J Internal Exception", e);
		}
		return null;
	}

	@Override
	public String getApplicationClientID() {
		try {
			return getApplicationInfo().id;
		} catch (RateLimitException e) {
			Discord4J.LOGGER.error(LogMarkers.API, "Discord4J Internal Exception", e);
		}
		return null;
	}

	@Override
	public String getApplicationName() {
		try {
			return getApplicationInfo().name;
		} catch (RateLimitException e) {
			Discord4J.LOGGER.error(LogMarkers.API, "Discord4J Internal Exception", e);
		}
		return null;
	}

	@Override
	public IUser getApplicationOwner() {
		if (applicationOwnerID == 0L) {
			try {
				applicationOwnerID = Long.parseUnsignedLong(getApplicationInfo().owner.id);
			} catch (RateLimitException e) {
				Discord4J.LOGGER.error(LogMarkers.API, "Discord4J Internal Exception", e);
				return null;
			}
		}

		return fetchUser(applicationOwnerID);
	}

	@Override
	public List<ICategory> getCategories() {
		return shards.stream()
				.map(IShard::getCategories)
				.flatMap(List::stream)
				.collect(Collectors.toList());
	}

	@Override
	public ICategory getCategoryByID(long categoryID) {
		for(IShard shard : shards) {
			ICategory category = shard.getCategoryByID(categoryID);
			if (category != null) {
				return category;
			}
		}

		return null;
	}

	@Override
	public List<ICategory> getCategoriesByName(String name) {
		return shards.stream()
				.map(IShard::getCategories)
				.flatMap(List::stream)
				.filter(category -> category.getName().equals(name))
				.collect(Collectors.toList());
	}

	private String obtainGateway() {
		String gateway = null;
		try {
			GatewayResponse response = REQUESTS.GET.makeRequest(DiscordEndpoints.GATEWAY, GatewayResponse.class);
			gateway = response.url + "?encoding=json&v=" + DiscordUtils.API_VERSION;
		} catch (RateLimitException | DiscordException e) {
			Discord4J.LOGGER.error(LogMarkers.API, "Discord4J Internal Exception", e);
		}
		Discord4J.LOGGER.debug(LogMarkers.API, "Obtained gateway {}.", gateway);
		return gateway;
	}

	private void validateToken() {
		REQUESTS.GET.makeRequest(DiscordEndpoints.USERS + "@me");
	}

	// Sharding delegation

	@Override
	public void login() {
		if (!getShards().isEmpty()) {
			throw new DiscordException("Attempt to login client more than once.");
		}

		validateToken();

		String gateway = obtainGateway();
		new RequestBuilder(this).setAsync(true).doAction(() -> {
			if (shard != null) {
				ShardImpl shardObj = new ShardImpl(this, gateway, new int[]{shard[0], shard[1]}, identifyPresence);
				getShards().add(shardObj);
				shardObj.login();

				getDispatcher().waitFor(ShardReadyEvent.class);
			} else {
				for (int i = 0; i < shardCount; i++) {
					final int shardNum = i;
					ShardImpl shard = new ShardImpl(this, gateway, new int[]{shardNum, shardCount}, identifyPresence);
					getShards().add(shardNum, shard);
					shard.login();

					getDispatcher().waitFor(ShardReadyEvent.class);

					if (i != shardCount - 1) { // all but last
						Discord4J.LOGGER.trace(LogMarkers.API, "Sleeping for login ratelimit.");
						Thread.sleep(5000);
					}
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
	public void logout() {
		for (IShard shard : getShards()) {
			shard.logout();
		}
		getShards().clear();
		if (keepAlive != null) keepAlive.cancel();
	}

	@Override
	public boolean isLoggedIn() {
		return getShards().size() == getShardCount() && getShards().stream().map(IShard::isLoggedIn).allMatch(bool -> bool);
	}

	@Override
	public boolean isReady() {
		return getShards().size() == getShardCount() && getShards().stream().map(IShard::isReady).allMatch(bool -> bool);
	}

	@Override
	public void changePresence(StatusType status, ActivityType activity, String text) {
		getShards().forEach(shard -> shard.changePresence(status, activity, text));
	}

	@Override
	public void changePresence(StatusType status) {
		getShards().forEach(shard -> shard.changePresence(status));
	}

	@Override
	public void changeStreamingPresence(StatusType status, String text, String streamUrl) {
		getShards().forEach(shard -> shard.changeStreamingPresence(status, text, streamUrl));
	}

	@Override
	public void mute(IGuild guild, boolean isSelfMuted) {
		VoiceState voiceState = (VoiceState) ourUser.getVoiceStateForGuild(guild);

		String channelID = null;
		long connectingID = ((Guild) guild).connectingVoiceChannelID;
		if (connectingID != 0) {
			channelID = Long.toUnsignedString(connectingID);
		} else if (voiceState.getChannel() != null) {
			channelID = voiceState.getChannel().getStringID();
		}

		voiceState.setSelfMuted(isSelfMuted);

		((ShardImpl) guild.getShard()).ws.send(GatewayOps.VOICE_STATE_UPDATE, new VoiceStateUpdateRequest(
				guild.getStringID(), channelID, isSelfMuted, voiceState.isSelfDeafened()));
	}

	@Override
	public void deafen(IGuild guild, boolean isSelfDeafened) {
		VoiceState voiceState = (VoiceState) ourUser.getVoiceStateForGuild(guild);

		String channelID = null;
		long connectingID = ((Guild) guild).connectingVoiceChannelID;
		if (connectingID != 0) {
			channelID = Long.toUnsignedString(connectingID);
		} else if (voiceState.getChannel() != null) {
			channelID = voiceState.getChannel().getStringID();
		}

		voiceState.setSelfDeafened(isSelfDeafened);

		((ShardImpl) guild.getShard()).ws.send(GatewayOps.VOICE_STATE_UPDATE, new VoiceStateUpdateRequest(
				guild.getStringID(), channelID, voiceState.isSelfMuted(), isSelfDeafened));
	}

	@Override
	public List<IGuild> getGuilds() {
		return getShards().stream()
				.map(IShard::getGuilds)
				.flatMap(List::stream)
				.collect(Collectors.toList());
	}

	@Override
	public IGuild getGuildByID(long guildID) {
		for (IShard shard : shards) {
			IGuild guild = shard.getGuildByID(guildID);
			if (guild != null)
				return guild;
		}

		return null;
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
	public IChannel getChannelByID(long channelID) {
		for (IShard shard : shards) {
			IChannel channel = shard.getChannelByID(channelID);
			if (channel != null)
				return channel;
		}

		return null;
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
		return ((User) getOurUser()).voiceStates.values().stream().map(IVoiceState::getChannel).filter(Objects::nonNull).collect(Collectors.toList());
	}

	@Override
	public IVoiceChannel getVoiceChannelByID(long id) {
		for (IShard shard : shards) {
			IVoiceChannel voiceChannel = shard.getVoiceChannelByID(id);
			if (voiceChannel != null)
				return voiceChannel;
		}

		return null;
	}

	@Override
	public List<IUser> getUsers() {
		return getShards().stream()
				.map(IShard::getUsers)
				.flatMap(List::stream)
				.distinct()
				.collect(Collectors.toList());
	}

	@Override
	public IUser getUserByID(long userID) {
		for (IShard shard : shards) {
			IUser user = shard.getUserByID(userID);
			if (user != null)
				return user;
		}

		return null;
	}

	@Override
	public IUser fetchUser(long id) {
		IUser cached = getUserByID(id);
		return cached == null ? DiscordUtils.getUserFromJSON(shards.get(0), REQUESTS.GET.makeRequest(DiscordEndpoints.USERS + Long.toUnsignedString(id), UserObject.class)) : cached;
	}

	@Override
	public List<IUser> getUsersByName(String name) {
		return getUsersByName(name, false);
	}

	@Override
	public List<IUser> getUsersByName(String name, boolean ignoreCase) {
		return getUsers().stream()
				.filter(u -> ignoreCase ? u.getName().equalsIgnoreCase(name) : u.getName().equals(name))
				.collect(Collectors.toList());
	}

	@Override
	public List<IRole> getRoles() {
		return getShards().stream()
				.map(IShard::getRoles)
				.flatMap(List::stream)
				.collect(Collectors.toList());
	}

	@Override
	public IRole getRoleByID(long roleID) {
		for (IShard shard : shards) {
			IRole role = shard.getRoleByID(roleID);
			if (role != null)
				return role;
		}

		return null;
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
	public IMessage getMessageByID(long messageID) {
		for (IShard shard : shards) {
			IMessage message = shard.getMessageByID(messageID);
			if (message != null)
				return message;
		}

		return null;
	}

	@Override
	public IPrivateChannel getOrCreatePMChannel(IUser user) {
		return user.getShard().getOrCreatePMChannel(user);
	}

	@Override
	public IInvite getInviteForCode(String code) {
		checkLoggedIn("get invite");
		return DiscordUtils.getInviteFromJSON(this, REQUESTS.GET.makeRequest(DiscordEndpoints.INVITE + code, InviteObject.class));
	}

	public int getRetryCount() {
		return retryCount;
	}

	public int getMaxCacheCount() {
		return maxCacheCount;
	}

	public ICacheDelegateProvider getCacheProvider() {
		return cacheProvider;
	}
}
