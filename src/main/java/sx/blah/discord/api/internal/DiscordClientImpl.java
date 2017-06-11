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
import sx.blah.discord.api.internal.json.requests.voice.VoiceStateUpdateRequest;
import sx.blah.discord.api.internal.json.responses.ApplicationInfoResponse;
import sx.blah.discord.api.internal.json.responses.GatewayResponse;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.ShardReadyEvent;
import sx.blah.discord.handle.impl.obj.Guild;
import sx.blah.discord.handle.impl.obj.User;
import sx.blah.discord.handle.impl.obj.VoiceState;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.modules.ModuleLoader;
import sx.blah.discord.util.*;
import sx.blah.discord.util.cache.ICacheDelegateProvider;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Defines the client. This class receives and sends messages, as well as holds our user data.
 */
public final class DiscordClientImpl implements IDiscordClient {

	static {
		if (!Discord4J.audioDisabled.get()) Services.load();
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
	 * The module loader for this client.
	 */
	private volatile ModuleLoader loader;

	/**
	 * Caches the available regions for discord.
	 */
	private final List<IRegion> REGIONS = new CopyOnWriteArrayList<>();

	/**
	 * The maximum amount of pings discord can miss before a new session is created.
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
	 * Provides cache objects used by this api.
	 */
	private final ICacheDelegateProvider cacheProvider;

	/**
	 * The specific shard (if there is one) that the client is running on.
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
	private final int retryCount;
	private final int maxCacheCount;

	public DiscordClientImpl(String token, int shardCount, boolean isDaemon, int maxMissedPings, int maxReconnectAttempts,
							 int retryCount, int maxCacheCount, ICacheDelegateProvider provider, int[] shard) {
		this.token = "Bot " + token;
		this.retryCount = retryCount;
		this.maxMissedPings = maxMissedPings;
		this.isDaemon = isDaemon;
		this.shardCount = shardCount == -1 ? 1 : shardCount;
		this.maxCacheCount = maxCacheCount;
		this.cacheProvider = provider;
		this.shard = shard;
		this.dispatcher = new EventDispatcher(this);
		this.reconnectManager = new ReconnectManager(this, maxReconnectAttempts);
		this.loader = new ModuleLoader(this);

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

	@Override
	public List<IRegion> getRegions() {
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
		try {
			UserObject owner = getApplicationInfo().owner;

			IUser user = getUserByID(Long.parseUnsignedLong(owner.id));
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
			GatewayResponse response = REQUESTS.GET.makeRequest(DiscordEndpoints.GATEWAY, GatewayResponse.class);
			gateway = response.url + "?encoding=json&v=5";
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
				ShardImpl shardObj = new ShardImpl(this, gateway, new int[]{shard[0], shard[1]});
				getShards().add(shardObj);
				shardObj.login();

				getDispatcher().waitFor(ShardReadyEvent.class);
			} else {
				for (int i = 0; i < shardCount; i++) {
					final int shardNum = i;
					ShardImpl shard = new ShardImpl(this, gateway, new int[]{shardNum, shardCount});
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
	@Deprecated
	public void changeStatus(Status status) {
		// old functionality just in case
		getShards().forEach(s -> s.changeStatus(status));
	}

	@Override
	@Deprecated
	public void changePresence(boolean isIdle) {
		// old functionality just in case
		getShards().forEach(s -> s.changePresence(isIdle));
	}

	@Override
	public void changePlayingText(String playingText) {
		getShards().forEach(s -> s.changePlayingText(playingText));
	}

	@Override
	public void online(String playingText) {
		getShards().forEach(s -> s.online(playingText));
	}

	@Override
	public void online() {
		getShards().forEach(IShard::online);
	}

	@Override
	public void idle(String playingText) {
		getShards().forEach(s -> s.idle(playingText));
	}

	@Override
	public void idle() {
		getShards().forEach(IShard::idle);
	}

	@Override
	public void streaming(String playingText, String streamingUrl) {
		getShards().forEach(s -> s.streaming(playingText, streamingUrl));
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
