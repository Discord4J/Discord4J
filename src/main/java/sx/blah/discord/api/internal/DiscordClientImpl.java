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
import sx.blah.discord.api.internal.json.responses.ApplicationInfoResponse;
import sx.blah.discord.api.internal.json.responses.GatewayResponse;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.ShardReadyEvent;
import sx.blah.discord.handle.impl.obj.User;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.modules.ModuleLoader;
import sx.blah.discord.util.*;

import java.io.IOException;
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
							 int retryCount, int maxCacheCount) {
		this.token = "Bot " + token;
		this.retryCount = retryCount;
		this.maxMissedPings = maxMissedPings;
		this.isDaemon = isDaemon;
		this.shardCount = shardCount;
		this.maxCacheCount = maxCacheCount;
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
			for (int i = 0; i < shardCount; i++) {
				final int shardNum = i;
				ShardImpl shard = new ShardImpl(this, gateway, new int[] {shardNum, shardCount});
				getShards().add(shardNum, shard);
				shard.login();

				getDispatcher().waitFor(ShardReadyEvent.class);

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
		return getOurUser().getVoiceStates().values().stream().map(IVoiceState::getChannel).filter(Objects::nonNull).collect(Collectors.toList());
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
				.distinct()
				.collect(Collectors.toList());
	}

	@Override
	public IUser getUserByID(String userID) {
		return getUsers().stream()
				.filter(u -> u.getID().equals(userID))
				.findFirst().orElse(null);
	}

	@Override
	public IUser fetchUser(String id) {
		IUser cached = getUserByID(id);
		return cached == null ? DiscordUtils.getUserFromJSON(null, REQUESTS.GET.makeRequest(DiscordEndpoints.USERS + id, UserObject.class)) : cached;
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
	public IPrivateChannel getOrCreatePMChannel(IUser user) {
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
			byte[] data = REQUESTS.GET.makeRequest(DiscordEndpoints.INVITE + code);
			if (data != null && data.length > 0)
				invite = DiscordUtils.MAPPER.readValue(data, InviteObject.class);
			else
				return null;
		} catch (Exception e) {
			Discord4J.LOGGER.error(LogMarkers.API, "Encountered error while retrieving invite: ", e);
			return null;
		}

		return invite == null ? null : DiscordUtils.getInviteFromJSON(this, invite);
	}

	public int getRetryCount() {
		return retryCount;
	}

	public int getMaxCacheCount() {
		return maxCacheCount;
	}
}
