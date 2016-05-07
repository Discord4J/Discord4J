package sx.blah.discord.handle.impl.obj;

import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.internal.DiscordEndpoints;
import sx.blah.discord.api.internal.DiscordUtils;
import sx.blah.discord.api.internal.Requests;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.json.requests.MemberEditRequest;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.HTTP429Exception;
import sx.blah.discord.util.MissingPermissionsException;

import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class User implements IUser {

	/**
	 * Display name of the user.
	 */
	protected volatile String name;

	/**
	 * The user's avatar location.
	 */
	protected volatile String avatar;

	/**
	 * The game the user is playing.
	 */
	protected volatile String game;

	/**
	 * User ID.
	 */
	protected final String id;

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
	 * This user's presence.
	 * One of [online/idle/offline].
	 */
	protected volatile Presences presence;

	/**
	 * The user's avatar in URL form.
	 */
	protected volatile String avatarURL;

	/**
	 * The roles the user is a part of. (Key = guild id).
	 */
	protected final Map<String, List<IRole>> roles;

	/**
	 * The nicknames this user has. (Key = guild id).
	 */
	protected final Map<String, String> nicks;

	/**
	 * The muted status of this user. (Key = guild id).
	 */
	private final Map<String, Boolean> isMuted = new ConcurrentHashMap<>();

	/**
	 * The deafened status of this user. (Key = guild id).
	 */
	private final Map<String, Boolean> isDeaf = new ConcurrentHashMap<>();

	/**
	 * The voice channels this user is in.
	 */
	protected final List<IVoiceChannel> channels = new CopyOnWriteArrayList<>();

	/**
	 * The client that created this object.
	 */
	protected final IDiscordClient client;

	public User(IDiscordClient client, String name, String id, String discriminator, String avatar, Presences presence, boolean isBot) {
		this.client = client;
		this.id = id;
		this.name = name;
		this.discriminator = discriminator;
		this.avatar = avatar;
		this.avatarURL = String.format(DiscordEndpoints.AVATARS, this.id, this.avatar);
		this.presence = presence;
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

	@Override
	public Optional<String> getGame() {
		return Optional.ofNullable(game);
	}

	/**
	 * Sets the user's CACHED game.
	 *
	 * @param game The game.
	 */
	public void setGame(String game) {
		this.game = game;
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
	public String getAvatar() {
		return avatar;
	}

	@Override
	public String getAvatarURL() {
		return avatarURL;
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
	public Presences getPresence() {
		return presence;
	}

	/**
	 * Sets the CACHED presence of the user.
	 *
	 * @param presence The new presence.
	 */
	public void setPresence(Presences presence) {
		this.presence = presence;
	}

	@Override
	public String getDisplayName(IGuild guild) {
		return getNicknameForGuild(guild).isPresent() ? getNicknameForGuild(guild).get() : getName();
	}

	@Override
	public String mention() {
		return mention(true);
	}

	@Override
	public String mention(boolean mentionWithNickname) {
		return "<@"+(mentionWithNickname ? "!" : "")+id+">";
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
	 * @param nick The nickname, or null to remove it.
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
	 * @param role The role.
	 */
	public void addRole(String guildID, IRole role) {
		if (!roles.containsKey(guildID)) {
			roles.put(guildID, new ArrayList<>());
		}

		roles.get(guildID).add(role);
	}

	@Override
	public IUser copy() {
		return new User(client, name, id, discriminator, avatar, presence, isBot);
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
	public void moveToVoiceChannel(IVoiceChannel newChannel) throws DiscordException, HTTP429Exception, MissingPermissionsException {
		if (!client.getOurUser().equals(this))
			DiscordUtils.checkPermissions(client, newChannel.getGuild(), this.getRolesForGuild(newChannel.getGuild()), EnumSet.of(Permissions.VOICE_MOVE_MEMBERS));

		try {
			Requests.PATCH.makeRequest(DiscordEndpoints.GUILDS + newChannel.getGuild().getID() + "/members/" + id,
					new StringEntity(DiscordUtils.GSON_NO_NULLS.toJson(new MemberEditRequest(newChannel.getID()))),
					new BasicNameValuePair("authorization", client.getToken()),
					new BasicNameValuePair("content-type", "application/json"));
		} catch (UnsupportedEncodingException e) {
			Discord4J.LOGGER.error("Discord4J Internal Exception", e);
		}
	}

	@Override
	public Optional<IVoiceChannel> getVoiceChannel() {
		return channels.stream().findFirst();
	}

	@Override
	public List<IVoiceChannel> getConnectedVoiceChannels() {
		return channels;
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
	 * @param isDeaf Whether the user is deafened or not.
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
}
