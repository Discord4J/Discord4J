package sx.blah.discord.handle.impl.obj;

import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.internal.DiscordClientImpl;
import sx.blah.discord.api.internal.DiscordEndpoints;
import sx.blah.discord.api.internal.DiscordUtils;
import sx.blah.discord.api.internal.json.requests.MemberEditRequest;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.LogMarkers;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class User implements IUser {

	/**
	 * User ID.
	 */
	protected final String id;
	/**
	 * The roles the user is a part of. (Key = guild id).
	 */
	protected final Map<String, List<IRole>> roles;
	/**
	 * The nicknames this user has. (Key = guild id).
	 */
	protected final Map<String, String> nicks;
	/**
	 * The voice channels this user is in.
	 */
	protected final List<IVoiceChannel> channels = new CopyOnWriteArrayList<>();
	/**
	 * The client that created this object.
	 */
	protected final IDiscordClient client;
	/**
	 * The muted status of this user. (Key = guild id).
	 */
	private final Map<String, Boolean> isMuted = new ConcurrentHashMap<>();
	/**
	 * The deafened status of this user. (Key = guild id).
	 */
	private final Map<String, Boolean> isDeaf = new ConcurrentHashMap<>();
	/**
	 * Display name of the user.
	 */
	protected volatile String name;
	/**
	 * The user's avatar location.
	 */
	protected volatile String avatar;
	/**
	 * The user's statuses.
	 */
	protected final Map<Integer, Status> statuses = new ConcurrentHashMap<>();
	/**
	 * User discriminator.
	 * Distinguishes users with the same name.
	 */
	protected volatile String discriminator;
	/**
	 * Whether this user is a bot or not.
	 */
	protected volatile boolean isBot;
	/**
	 * This user's presences.
	 * [online/idle/offline].
	 */
	protected final Map<Integer, Presences> presences = new ConcurrentHashMap<>();
	/**
	 * The user's avatar in URL form.
	 */
	protected volatile String avatarURL;
	/**
	 * The local muted status of this user.
	 */
	private volatile boolean isMutedLocally;
	/**
	 * The local deafened status of this user.
	 */
	private volatile boolean isDeafLocally;

	public User(IDiscordClient client, String name, String id, String discriminator, String avatar, Presences presence,
				boolean isBot) {
		this.client = client;
		this.id = id;
		this.name = name;
		this.discriminator = discriminator;
		this.avatar = avatar;
		this.avatarURL = String.format(DiscordEndpoints.AVATARS, this.id, this.avatar);
		this.presences.clear();
		this.presences.put(0, presence);
		this.statuses.clear();
		this.statuses.put(0, Status.empty());
		this.roles = new ConcurrentHashMap<>();
		this.nicks = new ConcurrentHashMap<>();
		this.isBot = isBot;
	}

	@Override
	public String getID() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	/**
	 * Sets the user's CACHED username.
	 *
	 * @param name The username.
	 */
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public Status getStatus(int index) {
		if (!this.equals(getClient().getOurUser())) {
			if (index != 0) {
				return null;
			}
		} else {
			if (index < 0 || index >= getClient().getShardCount())
				throw new IllegalArgumentException(
						"Index out of bounds. Cannot be less than zero, or higher than the shard count.");
		}

		return statuses.putIfAbsent(index, Status.empty());
	}

	@Override
	public Status getStatus() {
		return getStatus(0);
	}

	/**
	 * Sets the user's CACHED status.
	 *
	 * @param num The index.
	 * @param status The status.
	 */
	public void setStatus(Status status, int num) {
		if (!this.equals(getClient().getOurUser()))
			num = 0;

		this.statuses.put(num, status);

		trimPresencesAndStatuses();
	}

	@Override
	public String getAvatar() {
		return avatar;
	}

	/**
	 * Sets the user's CACHED avatar id.
	 *
	 * @param avatar The user's avatar id.
	 */
	public void setAvatar(String avatar) {
		this.avatar = avatar;
		this.avatarURL = String.format(DiscordEndpoints.AVATARS, this.id, this.avatar);
	}

	@Override
	public String getAvatarURL() {
		return avatarURL;
	}

	@Override
	public Presences getPresence(int index) {
		if (!this.equals(getClient().getOurUser())) {
			if (index != 0) {
				return null;
			}
		} else {
			if (index < 0 || index >= getClient().getShardCount())
				if (index < 0 || index >= getClient().getShardCount())
					throw new IllegalArgumentException(
							"Index out of bounds. Cannot be less than zero, or higher than the shard count.");
		}

		return presences.putIfAbsent(index, Presences.ONLINE);
	}

	@Override
	public Presences getPresence() {
		return getPresence(0);
	}

	/**
	 * Sets the CACHED presence of the user.
	 *
	 * @param num      The index.
	 * @param presence The new presence.
	 */
	public void setPresence(Presences presence, int num) {
		if (!this.equals(getClient().getOurUser()))
			num = 0;

		this.presences.put(num, presence);

		trimPresencesAndStatuses();
	}

	/**
	 * Removes presences and statuses outside of the shard range (which is 1 for non-client users)
	 */
	private void trimPresencesAndStatuses() {
		if (this.equals(getClient().getOurUser())) {
			for (int i = 0; i < Math.max(statuses.size(), presences.size()); i++) {
				if (i < getClient().getShardCount()) {
					statuses.putIfAbsent(i, Status.empty());
					presences.put(i, Presences.ONLINE);
				} else {
					statuses.remove(i);
					presences.remove(i);
				}
			}
		} else {
			final Presences firstPresence = getPresence();
			final Status firstStatus = getStatus();

			presences.clear();
			presences.put(0, firstPresence);

			statuses.clear();
			statuses.put(0, firstStatus);
		}
	}

	@Override
	public String getDisplayName(IGuild guild) {
		if (guild == null)
			return getName();

		return getNicknameForGuild(guild).isPresent() ? getNicknameForGuild(guild).get() : getName();
	}

	@Override
	public String mention() {
		return mention(true);
	}

	@Override
	public String mention(boolean mentionWithNickname) {
		return "<@" + (mentionWithNickname ? "!" : "") + id + ">";
	}

	@Override
	public String getDiscriminator() {
		return discriminator;
	}

	/**
	 * Sets the CACHED discriminator for the user.
	 *
	 * @param discriminator The user's new discriminator.
	 */
	public void setDiscriminator(String discriminator) {
		this.discriminator = discriminator;
	}

	@Override
	public List<IRole> getRolesForGuild(IGuild guild) {
		return roles.getOrDefault(guild.getID(), new ArrayList<>());
	}

	@Override
	public Optional<String> getNicknameForGuild(IGuild guild) {
		return Optional.ofNullable(nicks.containsKey(guild.getID()) ? nicks.get(guild.getID()) : null);
	}

	/**
	 * CACHES a nickname to the user.
	 *
	 * @param guildID The guild the nickname is for.
	 * @param nick    The nickname, or null to remove it.
	 */
	public void addNick(String guildID, String nick) {
		if (nick == null) {
			if (nicks.containsKey(guildID))
				nicks.remove(guildID);
		} else {
			nicks.put(guildID, nick);
		}
	}

	/**
	 * CACHES a role to the user.
	 *
	 * @param guildID The guild the role is for.
	 * @param role    The role.
	 */
	public void addRole(String guildID, IRole role) {
		if (!roles.containsKey(guildID)) {
			roles.put(guildID, new ArrayList<>());
		}

		roles.get(guildID).add(role);
	}

	@Override
	public IUser copy() {
		User newUser = new User(client, name, id, discriminator, avatar, this.getPresence(), isBot);
		newUser.presences.clear();
		for (int i = 0; i < this.presences.size(); i++) {
			newUser.setPresence(this.presences.get(i), i);
		}
		newUser.statuses.clear();
		for (int i = 0; i < this.statuses.size(); i++) {
			newUser.setStatus(this.statuses.get(i), i);
		}
		for (String key : isMuted.keySet())
			newUser.setIsMute(key, isMuted.get(key));
		for (String key : isDeaf.keySet())
			newUser.setIsDeaf(key, isDeaf.get(key));
		newUser.nicks.putAll(nicks);
		newUser.roles.putAll(roles);
		newUser.channels.addAll(channels);
		return newUser;
	}

	@Override
	public boolean isBot() {
		return isBot;
	}

	/**
	 * Sets the CACHED isBot status to true.
	 */
	public void convertToBot() {
		isBot = true;
	}

	@Override
	public void moveToVoiceChannel(IVoiceChannel newChannel) throws DiscordException, RateLimitException,
			MissingPermissionsException {
		// can this user go to this channel?
		DiscordUtils.checkPermissions(this, newChannel, EnumSet.of(Permissions.VOICE_CONNECT));

		// in order to move a member:
		// both users have to be able to access the new VC (half of it is covered above)
		// the client must have either Move Members or Administrator

		// this isn't the client, so the client is moving this uesr
		if (!this.equals(client.getOurUser())) {
			// can the client go to this channel?
			DiscordUtils.checkPermissions(client.getOurUser(), newChannel, EnumSet.of(Permissions.VOICE_CONNECT));

			DiscordUtils.checkPermissions(newChannel.getModifiedPermissions(client.getOurUser()),
					EnumSet.of(Permissions.VOICE_MOVE_MEMBERS));
		}

		try {
			((DiscordClientImpl) client).REQUESTS.PATCH
					.makeRequest(DiscordEndpoints.GUILDS + newChannel.getGuild().getID() + "/members/" + id,
							new StringEntity(
									DiscordUtils.GSON_NO_NULLS.toJson(new MemberEditRequest(newChannel.getID()))),
							new BasicNameValuePair("authorization", client.getToken()),
							new BasicNameValuePair("content-type", "application/json"));
		} catch (UnsupportedEncodingException e) {
			Discord4J.LOGGER.error(LogMarkers.HANDLE, "Discord4J Internal Exception", e);
		}
	}

	@Override
	public List<IVoiceChannel> getConnectedVoiceChannels() {
		return channels;
	}

	@Override
	public IPrivateChannel getOrCreatePMChannel() throws RateLimitException, DiscordException {
		return client.getOrCreatePMChannel(this);
	}

	/**
	 * Sets whether the user is muted or not. This value is CACHED.
	 *
	 * @param guildID The guild in which this is the case.
	 * @param isMuted Whether the user is muted or not.
	 */
	public void setIsMute(String guildID, boolean isMuted) {
		this.isMuted.put(guildID, isMuted);
	}

	/**
	 * Sets whether the user is deafened or not. This value is CACHED.
	 *
	 * @param guildID The guild in which this is the case.
	 * @param isDeaf  Whether the user is deafened or not.
	 */
	public void setIsDeaf(String guildID, boolean isDeaf) {
		this.isDeaf.put(guildID, isDeaf);
	}

	@Override
	public boolean isDeaf(IGuild guild) {
		return isDeaf.getOrDefault(guild.getID(), false);
	}

	@Override
	public boolean isMuted(IGuild guild) {
		return isMuted.getOrDefault(guild.getID(), false);
	}

	/**
	 * Sets whether the user is muted locally or not (meaning they muted themselves). This value is CACHED.
	 *
	 * @param isMuted Whether the user is muted or not.
	 */
	public void setIsMutedLocally(boolean isMuted) {
		this.isMutedLocally = isMuted;
	}

	/**
	 * Sets whether the user is deafened locally or not (meaning they deafened themselves). This value is CACHED.
	 *
	 * @param isDeaf Whether the user is deafened or not.
	 */
	public void setIsDeafLocally(boolean isDeaf) {
		this.isDeafLocally = isDeaf;
	}

	@Override
	public boolean isDeafLocally() {
		return isDeafLocally;
	}

	@Override
	public boolean isMutedLocally() {
		return isMutedLocally;
	}

	@Override
	public IDiscordClient getClient() {
		return client;
	}

	@Override
	public String toString() {
		return mention();
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object other) {
		if (other == null)
			return false;

		return this.getClass().isAssignableFrom(other.getClass()) && ((IUser) other).getID().equals(getID());
	}

	@Override
	public void addRole(IRole role) throws MissingPermissionsException, RateLimitException, DiscordException {
		IGuild guild = role.getGuild();
		List<IRole> roleList = new ArrayList<>(getRolesForGuild(guild));
		roleList.add(role);
		guild.editUserRoles(this, roleList.toArray(new IRole[roleList.size()]));
	}

	@Override
	public void removeRole(IRole role) throws MissingPermissionsException, RateLimitException, DiscordException {
		IGuild guild = role.getGuild();
		List<IRole> roleList = new ArrayList<>(getRolesForGuild(guild));
		roleList.remove(role);
		guild.editUserRoles(this, roleList.toArray(new IRole[roleList.size()]));
	}

	@Override
	public int getNumberOfPresences() {
		return presences.size();
	}

	@Override
	public int getNumberOfStatuses() {
		return statuses.size();
	}
}
