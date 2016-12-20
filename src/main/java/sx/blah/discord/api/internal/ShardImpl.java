package sx.blah.discord.api.internal;

import org.apache.http.entity.StringEntity;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.IShard;
import sx.blah.discord.api.internal.json.objects.InviteObject;
import sx.blah.discord.api.internal.json.objects.PrivateChannelObject;
import sx.blah.discord.api.internal.json.requests.PresenceUpdateRequest;
import sx.blah.discord.api.internal.json.requests.PrivateChannelCreateRequest;
import sx.blah.discord.handle.impl.events.DisconnectedEvent;
import sx.blah.discord.handle.impl.events.PresenceUpdateEvent;
import sx.blah.discord.handle.impl.events.StatusChangeEvent;
import sx.blah.discord.handle.impl.obj.User;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.LogMarkers;
import sx.blah.discord.util.RateLimitException;
import sx.blah.discord.util.RequestBuffer;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class ShardImpl implements IShard {

	public volatile DiscordWS ws;

	private final String gateway;
	private final int[] info;

	private final DiscordClientImpl client;
	final List<IGuild> guildList = new CopyOnWriteArrayList<>();
	final List<IPrivateChannel> privateChannels = new CopyOnWriteArrayList<>();

	ShardImpl(IDiscordClient client, String gateway, int[] info) {
		this.client = (DiscordClientImpl) client;
		this.gateway = gateway;
		this.info = info;
	}

	@Override
	public IDiscordClient getClient() {
		return this.client;
	}

	@Override
	public int[] getInfo() {
		return this.info;
	}

	@Override
	public void login() throws DiscordException {
		Discord4J.LOGGER.trace(LogMarkers.API, "Shard logging in.");
		this.ws = new DiscordWS(this, gateway, client.maxMissedPings);
		this.ws.connect();
	}

	@Override
	public void logout() throws DiscordException {
		if (isLoggedIn()) {
			Discord4J.LOGGER.info(LogMarkers.API, "Shard {} logging out.", getInfo()[0]);
			getConnectedVoiceChannels().forEach(channel -> {
				RequestBuffer.RequestFuture<IVoiceChannel> request = RequestBuffer.request(() -> {
					channel.leave();
					return channel;
				});
				request.get();
			});
			getClient().getDispatcher().dispatch(new DisconnectedEvent(DisconnectedEvent.Reason.LOGGED_OUT, this));
			ws.shutdown();
		} else {
			Discord4J.LOGGER.error(LogMarkers.API, "Attempt to logout before bot has logged in!");
		}
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
	public long getResponseTime() {
		return ws.heartbeatHandler.getAckResponseTime();
	}

	@Override
	public void changePresence(boolean isIdle) {
		updatePresence(isIdle, getClient().getOurUser().getStatus());
	}

	@Override
	public void changeStatus(Status status) {
		updatePresence(getClient().getOurUser().getPresence() == Presences.IDLE, status);
	}

	private void updatePresence(boolean isIdle, Status status) {
		if (!isLoggedIn()) {
			Discord4J.LOGGER.error(LogMarkers.API, "Attempt to change presence before bot has logged in!");
			return;
		}

		IUser ourUser = getClient().getOurUser();

		if (!status.equals(ourUser.getStatus())) {
			Status oldStatus = ourUser.getStatus();
			((User) ourUser).setStatus(status);
			getClient().getDispatcher().dispatch(new StatusChangeEvent(ourUser, oldStatus, status));
		}

		if ((ourUser.getPresence() != Presences.IDLE && isIdle)
				|| (ourUser.getPresence() == Presences.IDLE && !isIdle)
				|| (ourUser.getPresence() != Presences.STREAMING && status.getType() == Status.StatusType.STREAM)) {
			Presences oldPresence = ourUser.getPresence();
			Presences newPresence = isIdle ? Presences.IDLE :
					(status.getType() == Status.StatusType.STREAM ? Presences.STREAMING : Presences.ONLINE);
			((User) ourUser).setPresence(newPresence);
			getClient().getDispatcher().dispatch(new PresenceUpdateEvent(ourUser, oldPresence, newPresence));
		}

		ws.send(GatewayOps.STATUS_UPDATE, new PresenceUpdateRequest(isIdle ? System.currentTimeMillis() : null, status));
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
	public List<IVoiceChannel> getConnectedVoiceChannels() {
		return getClient().getOurUser().getConnectedVoiceChannels().stream().filter(vc -> vc.getShard().equals(this)).collect(Collectors.toList());
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
		IUser ourUser = getClient().getOurUser();

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
			Discord4J.LOGGER.error(LogMarkers.API, "Attempt to get PM channel before bot is ready!");
			return null;
		}

		if (user.equals(getClient().getOurUser()))
			throw new DiscordException("Cannot PM yourself!");

		Optional<IPrivateChannel> opt = privateChannels.stream()
				.filter(c -> c.getRecipient().getID().equalsIgnoreCase(user.getID()))
				.findAny();
		if (opt.isPresent())
			return opt.get();

		PrivateChannelObject pmChannel;
		try {
			pmChannel = DiscordUtils.GSON.fromJson(client.REQUESTS.POST.makeRequest(DiscordEndpoints.USERS+getClient().getOurUser().getID()+"/channels",
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
}
