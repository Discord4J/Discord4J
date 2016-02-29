package sx.blah.discord.api.internal;

import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.DiscordEndpoints;
import sx.blah.discord.api.DiscordException;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.EventDispatcher;
import sx.blah.discord.handle.impl.events.DiscordDisconnectedEvent;
import sx.blah.discord.handle.impl.events.VoiceDisconnectedEvent;
import sx.blah.discord.handle.impl.obj.User;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.json.requests.*;
import sx.blah.discord.json.responses.*;
import sx.blah.discord.modules.ModuleLoader;
import sx.blah.discord.util.*;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.*;
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
	protected User ourUser;

	/**
	 * Our token, so we can send XHR to Discord.
	 */
	protected String token;

	/**
	 * Time (in ms) between keep alive
	 * messages.
	 */
	protected volatile long heartbeat;

	/**
	 * Local copy of all guilds/servers.
	 */
	protected final List<IGuild> guildList = new ArrayList<>();

	/**
	 * Private copy of the email you logged in with.
	 */
	protected String email;

	/**
	 * Private copy of the password you used to log in.
	 */
	protected String password;

	/**
	 * WebSocket over which to communicate with Discord.
	 */
	public DiscordWS ws;

	/**
	 * Voice WebSocket over which to communicate with Discord.
	 */
	public DiscordVoiceWS voiceWS;

	/**
	 * Event dispatcher.
	 */
	protected EventDispatcher dispatcher;

	/**
	 * All of the private message channels that the bot is connected to.
	 */
	protected final List<IPrivateChannel> privateChannels = new ArrayList<>();

	/**
	 * The voice channel the bot is currently in.
	 */
	public IVoiceChannel connectedVoiceChannel = null;

	/**
	 * Whether the api is logged in.
	 */
	protected boolean isReady = false;

	/**
	 * The websocket session id.
	 */
	protected String sessionId;

	/**
	 * Caches the last operation done by the websocket, required for handling redirects.
	 */
	protected long lastSequence = 0;

	/**
	 * Caches the available regions for discord.
	 */
	protected final List<IRegion> REGIONS = new ArrayList<>();

	/**
	 * The module loader for this client.
	 */
	protected ModuleLoader loader;

	/**
	 * The audio channel for this client.
	 */
	protected AudioChannel audioChannel;

	/**
	 * The time for the client to timeout.
	 */
	protected final long timeoutTime;

	/**
	 * The maximum amount of pings discord can miss.
	 */
	protected final int maxMissedPingCount;

	public DiscordClientImpl(String email, String password, long timeoutTime, int maxMissedPingCount) {
		this.timeoutTime = timeoutTime;
		this.maxMissedPingCount = maxMissedPingCount;
		this.dispatcher = new EventDispatcher(this);
		this.loader = new ModuleLoader(this);
		this.audioChannel = new AudioChannel(this);
		this.email = email;
		this.password = password;
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
	public AudioChannel getAudioChannel() {
		return audioChannel;
	}

	@Override
	public String getToken() {
		return token;
	}

	@Override
	public void login() throws DiscordException {
		try {
			if (ws != null) {
				ws.disconnect(DiscordDisconnectedEvent.Reason.RECONNECTING);
			}

			LoginResponse response = DiscordUtils.GSON.fromJson(Requests.POST.makeRequest(DiscordEndpoints.LOGIN,
					new StringEntity(DiscordUtils.GSON.toJson(new LoginRequest(email, password))),
					new BasicNameValuePair("content-type", "application/json")), LoginResponse.class);
			this.token = response.token;

			this.ws = new DiscordWS(this, new URI(obtainGateway(this.token)), timeoutTime, maxMissedPingCount);
		} catch (Exception e) {
			throw new DiscordException("Login error occurred! Are your login details correct?");
		}
	}

	@Override
	public void logout() throws HTTP429Exception, DiscordException {
		if (isReady()) {
			ws.disconnect(DiscordDisconnectedEvent.Reason.LOGGED_OUT);
			if (voiceWS != null)
				voiceWS.disconnect(VoiceDisconnectedEvent.Reason.LOGGED_OUT);

			Requests.POST.makeRequest(DiscordEndpoints.LOGOUT,
					new BasicNameValuePair("authorization", token));
		} else
			Discord4J.LOGGER.error("Bot has not signed in yet!");
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
			GatewayResponse response = DiscordUtils.GSON.fromJson(Requests.GET.makeRequest("https://discordapp.com/api/gateway",
					new BasicNameValuePair("authorization", token)), GatewayResponse.class);
			gateway = response.url;//.replaceAll("wss", "ws");
		} catch (HTTP429Exception | DiscordException e) {
			Discord4J.LOGGER.error("Discord4J Internal Exception", e);
		}
		Discord4J.LOGGER.debug("Obtained gateway {}.", gateway);
		return gateway;
	}

	@Override //TODO: Make private
	public void changeAccountInfo(Optional<String> username, Optional<String> email, Optional<String> password, Optional<Image> avatar) throws HTTP429Exception, DiscordException {
		Discord4J.LOGGER.debug("Changing account info.");

		if (!isReady()) {
			Discord4J.LOGGER.error("Bot has not signed in yet!");
			return;
		}

		try {
			AccountInfoChangeResponse response = DiscordUtils.GSON.fromJson(Requests.PATCH.makeRequest(DiscordEndpoints.USERS+"@me",
					new StringEntity(DiscordUtils.GSON.toJson(new AccountInfoChangeRequest(email.orElse(this.email),
							this.password, password.orElse(this.password), username.orElse(getOurUser().getName()),
							avatar == null ? Image.forUser(ourUser).getData() :
									(avatar.isPresent() ? avatar.get().getData() : Image.defaultAvatar().getData())))),
					new BasicNameValuePair("Authorization", token),
					new BasicNameValuePair("content-type", "application/json; charset=UTF-8")), AccountInfoChangeResponse.class);

			if (!this.token.equals(response.token)) {
				Discord4J.LOGGER.debug("Token changed, updating it.");
				this.token = response.token;
			}
		} catch (UnsupportedEncodingException e) {
			Discord4J.LOGGER.error("Discord4J Internal Exception", e);
		}
	}

	@Override
	public void changeUsername(String username) throws DiscordException, HTTP429Exception {
		changeAccountInfo(Optional.of(username), Optional.empty(), Optional.empty(), null);
	}

	@Override
	public void changeEmail(String email) throws DiscordException, HTTP429Exception {
		changeAccountInfo(Optional.empty(), Optional.of(email), Optional.empty(), null);
	}

	@Override
	public void changePassword(String password) throws DiscordException, HTTP429Exception {
		changeAccountInfo(Optional.empty(), Optional.empty(), Optional.of(password), null);
	}

	@Override
	public void changeAvatar(Image avatar) throws DiscordException, HTTP429Exception {
		changeAccountInfo(Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(avatar));
	}

	@Override
	public void updatePresence(boolean isIdle, Optional<String> game) {
		if (!isReady()) {
			Discord4J.LOGGER.error("Bot has not signed in yet!");
			return;
		}

		ws.send(DiscordUtils.GSON.toJson(new PresenceUpdateRequest(isIdle ? System.currentTimeMillis() : null, game.orElse(null))));

		((User) getOurUser()).setPresence(isIdle ? Presences.IDLE : Presences.ONLINE);
		((User) getOurUser()).setGame(game);
	}

	@Override
	public boolean isReady() {
		return isReady && ws != null;
	}

	@Override
	public IUser getOurUser() {
		if (!isReady()) {
			Discord4J.LOGGER.error("Bot has not signed in yet!");
			return null;
		}
		return ourUser;
	}

	@Override
	public Collection<IChannel> getChannels(boolean priv) {
		Collection<IChannel> channels = guildList.stream()
				.map(IGuild::getChannels)
				.flatMap(List::stream)
				.collect(Collectors.toList());
		if (priv)
			channels.addAll(privateChannels);
		return channels;
	}

	@Override
	public IChannel getChannelByID(String id) {
		return getChannels(true).stream()
				.filter(c -> c.getID().equalsIgnoreCase(id))
				.findAny().orElse(null);
	}

	@Override
	public Collection<IVoiceChannel> getVoiceChannels() {
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
	public IGuild getGuildByID(String guildID) {
		return guildList.stream()
				.filter(g -> g.getID().equalsIgnoreCase(guildID))
				.findAny().orElse(null);
	}

	@Override
	public List<IGuild> getGuilds() {
		return guildList;
	}

	@Override
	public IUser getUserByID(String userID) {
		IUser user = null;
		for (IGuild guild : guildList) {
			if (user == null)
				user = guild.getUserByID(userID);
			else
				break;
		}

		return ourUser != null && ourUser.getID().equals(userID) ? ourUser : user;
	}

	@Override
	public IPrivateChannel getOrCreatePMChannel(IUser user) throws DiscordException, HTTP429Exception {
		if (!isReady()) {
			Discord4J.LOGGER.error("Bot has not signed in yet!");
			return null;
		}

		Optional<IPrivateChannel> opt = privateChannels.stream()
				.filter(c -> c.getRecipient().getID().equalsIgnoreCase(user.getID()))
				.findAny();
		if (opt.isPresent())
			return opt.get();

		PrivateChannelResponse response = null;
		try {
			response = DiscordUtils.GSON.fromJson(Requests.POST.makeRequest(DiscordEndpoints.USERS+this.ourUser.getID()+"/channels",
					new StringEntity(DiscordUtils.GSON.toJson(new PrivateChannelRequest(user.getID()))),
					new BasicNameValuePair("authorization", this.token),
					new BasicNameValuePair("content-type", "application/json")), PrivateChannelResponse.class);
		} catch (UnsupportedEncodingException e) {
			Discord4J.LOGGER.error("Error creating creating a private channel!", e);
		}

		IPrivateChannel channel = DiscordUtils.getPrivateChannelFromJSON(this, response);
		privateChannels.add(channel);
		return channel;
	}

	@Override
	public IInvite getInviteForCode(String code) {
		if (!isReady()) {
			Discord4J.LOGGER.error("Bot has not signed in yet!");
			return null;
		}

		try {
			InviteJSONResponse response = DiscordUtils.GSON.fromJson(Requests.GET.makeRequest(DiscordEndpoints.INVITE+code,
					new BasicNameValuePair("authorization", token)), InviteJSONResponse.class);

			return DiscordUtils.getInviteFromJSON(this, response);
		} catch (HTTP429Exception | DiscordException e) {
			Discord4J.LOGGER.error("Discord4J Internal Exception", e);
		}
		return null;
	}

	@Override
	public List<IRegion> getRegions() throws HTTP429Exception, DiscordException {
		if (REGIONS.isEmpty()) {
			RegionResponse[] regions = DiscordUtils.GSON.fromJson(Requests.GET.makeRequest(
					DiscordEndpoints.VOICE+"regions",
					new BasicNameValuePair("authorization", this.token)),
					RegionResponse[].class);

			Arrays.stream(regions)
					.map(r -> DiscordUtils.getRegionFromJSON(this, r))
					.forEach(r -> REGIONS.add(r));
		}

		return REGIONS;
	}

	@Override
	public IRegion getRegionForID(String regionID) {
		return getRegionByID(regionID);
	}

	@Override
	public IRegion getRegionByID(String regionID) {
		try {
			return getRegions().stream()
					.filter(r -> r.getID().equals(regionID))
					.findAny().orElse(null);
		} catch (HTTP429Exception | DiscordException e) {
			Discord4J.LOGGER.error("Discord4J Internal Exception", e);
		}
		return null;
	}

	@Override
	public IGuild createGuild(String name, String regionID, Optional<Image> icon) throws HTTP429Exception, DiscordException {
		return createGuild(name, getRegionByID(regionID), icon);
	}

	@Override
	public IGuild createGuild(String name, IRegion region, Optional<Image> icon) throws HTTP429Exception, DiscordException {
		try {
			GuildResponse guildResponse = DiscordUtils.GSON.fromJson(Requests.POST.makeRequest(DiscordEndpoints.APIBASE+"/guilds",
					new StringEntity(DiscordUtils.GSON_NO_NULLS.toJson(
							new CreateGuildRequest(name, region.getID(), icon.orElse(null)))),
					new BasicNameValuePair("authorization", this.token),
					new BasicNameValuePair("content-type", "application/json")), GuildResponse.class);
			IGuild guild = DiscordUtils.getGuildFromJSON(this, guildResponse);
			guildList.add(guild);
			return guild;
		} catch (UnsupportedEncodingException e) {
			Discord4J.LOGGER.error("Discord4J Internal Exception", e);
		}
		return null;
	}

	@Override
	public long getResponseTime() {
		return ws.getResponseTime();
	}

	@Override
	public Optional<IVoiceChannel> getConnectedVoiceChannel() {
		return Optional.ofNullable(connectedVoiceChannel);
	}
}
