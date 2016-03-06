package sx.blah.discord.handle.impl.obj;

import sx.blah.discord.api.DiscordEndpoints;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.internal.DiscordUtils;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Presences;

import java.time.LocalDateTime;
import java.util.*;

public class User implements IUser {

	/**
	 * Display name of the user.
	 */
	protected String name;

	/**
	 * The user's avatar location.
	 */
	protected String avatar;

	/**
	 * The game the user is playing.
	 */
	protected Optional<String> game;

	/**
	 * User ID.
	 */
	protected final String id;

	/**
	 * User discriminator.
	 * Distinguishes users with the same name.
	 * This is here in case it becomes necessary.
	 */
	protected final String discriminator;

	/**
	 * This user's presence.
	 * One of [online/idle/offline].
	 */
	protected Presences presence;

	/**
	 * The user's avatar in URL form.
	 */
	protected String avatarURL;

	/**
	 * The roles the user is a part of. (Key = guild id).
	 */
	protected HashMap<String, List<IRole>> roles;

	/**
	 * The client that created this object.
	 */
	protected final IDiscordClient client;

	public User(IDiscordClient client, String name, String id, String discriminator, String avatar, Presences presence) {
		this.client = client;
		this.id = id;
		this.name = name;
		this.discriminator = discriminator;
		this.avatar = avatar;
		this.avatarURL = String.format(DiscordEndpoints.AVATARS, this.id, this.avatar);
		this.presence = presence;
		this.roles = new HashMap<>();
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
		return game == null ? Optional.empty() : game;
	}

	/**
	 * Sets the user's CACHED game.
	 *
	 * @param game The game.
	 */
	public void setGame(Optional<String> game) {
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
	public String mention() {
		return "<@"+id+">";
	}

	@Override
	public String getDiscriminator() {
		return discriminator;
	}

	@Override
	public List<IRole> getRolesForGuild(String guildID) {
		return roles.getOrDefault(guildID, new ArrayList<>());
	}

	@Override
	public List<IRole> getRolesForGuild(IGuild guild) {
		return roles.getOrDefault(guild.getID(), new ArrayList<>());
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
	public LocalDateTime getCreationDate() {
		return DiscordUtils.getSnowflakeTimeFromID(id);
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
		return this.getClass().isAssignableFrom(other.getClass()) && ((IUser) other).getID().equals(getID());
	}
}
