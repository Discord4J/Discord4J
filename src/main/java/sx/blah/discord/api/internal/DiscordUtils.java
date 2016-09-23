package sx.blah.discord.api.internal;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.tuple.Pair;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.internal.json.generic.PermissionOverwrite;
import sx.blah.discord.api.internal.json.generic.RoleResponse;
import sx.blah.discord.api.internal.json.generic.StatusObject;
import sx.blah.discord.api.internal.json.requests.GuildMembersRequest;
import sx.blah.discord.api.internal.json.responses.*;
import sx.blah.discord.handle.audio.impl.AudioManager;
import sx.blah.discord.handle.impl.obj.*;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.LogMarkers;
import sx.blah.discord.util.MissingPermissionsException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Collection of internal Discord4J utilities.
 */
public class DiscordUtils {

	/**
	 * Re-usable instance of Gson.
	 */
	public static final Gson GSON = new GsonBuilder().serializeNulls().create();
	/**
	 * Like {@link #GSON} but it doesn't serialize nulls.
	 */
	public static final Gson GSON_NO_NULLS = new GsonBuilder().create();

	/**
	 * Used to find urls in order to not escape them
	 */
	public static final Pattern URL_PATTERN = Pattern.compile(
			"(?:^|[\\W])((ht|f)tp(s?):\\/\\/|www\\.)" + "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*" +
					"[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)",
			Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

	/**
	 * Used in order to find the invite code from a passed message.
	 */
	public static final Pattern INVITE_PATTERN = Pattern.compile(
			"(?:https://discordapp.com/invite/|https://discord.gg/)" + "(\\w+-\\w+-\\w+|[^\\W1ilo0]{5})"); //Thanks
	// dec!

	/**
	 * Used to determine age based on discord ids
	 */
	public static final BigInteger DISCORD_EPOCH = new BigInteger("1420070400000");

	/**
	 * Converts a String timestamp into a java object timestamp.
	 *
	 * @param time The String timestamp.
	 * @return The java object representing the timestamp.
	 */
	public static LocalDateTime convertFromTimestamp(String time) {
		if (time == null) {
			return LocalDateTime.now();
		}
		return LocalDateTime.parse(time.split("\\+")[0]).atZone(ZoneId.of("UTC+00:00"))
				.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
	}

	/**
	 * Returns a user from the java form of the raw JSON data.
	 */
	public static User getUserFromJSON(IDiscordClient client, UserResponse response) {
		if (response == null)
			return null;

		User user;
		if ((user = (User) client.getUserByID(response.id)) != null) {
			user.setAvatar(response.avatar);
			user.setName(response.username);
			user.setDiscriminator(response.discriminator);
			if (response.bot && !user.isBot())
				user.convertToBot();
		} else {
			user = new User(client, response.username, response.id, response.discriminator, response.avatar,
					Presences.OFFLINE, response.bot);
		}
		return user;
	}

	/**
	 * Escapes a string to ensure that the Discord websocket receives it correctly.
	 *
	 * @param string The string to escape
	 * @return The escaped string
	 * @deprecated No longer required for discord to handle special characters
	 */
	@Deprecated
	public static String escapeString(String string) {
		//All this weird regex stuff is to prevent any urls from being escaped and therefore breaking them
		List<String> urls = new ArrayList<>();
		Matcher matcher = URL_PATTERN.matcher(string);
		while (matcher.find()) {
			int matchStart = matcher.start(1);
			int matchEnd = matcher.end();
			String url = string.substring(matchStart, matchEnd);
			urls.add(url);
			string = matcher.replaceFirst("@@URL" + (urls.size() - 1) +
					"@@");//Hopefully no one will ever want to send a message with @@URL#@@
		}

		string = StringEscapeUtils.escapeJson(string);

		for (int i = 0; i < urls.size(); i++) {
			string = string.replace("@@URL" + i + "@@", " " + urls.get(i));
		}

		return string;
	}

	/**
	 * Creates a java {@link Invite} object for a json response.
	 *
	 * @param client The discord client to use.
	 * @param json   The json response to use.
	 * @return The java invite object.
	 */
	public static IInvite getInviteFromJSON(IDiscordClient client, InviteJSONResponse json) {
		return new Invite(client, json.code);
	}

	/**
	 * Gets the users mentioned from a message json object.
	 *
	 * @param client The discord client to use.
	 * @param json   The json response to use.
	 * @return The list of mentioned users.
	 */
	public static List<String> getMentionsFromJSON(IDiscordClient client, MessageResponse json) {
		List<String> mentions = new ArrayList<>();
		if (json.mentions != null)
			for (UserResponse response : json.mentions)
				mentions.add(response.id);

		return mentions;
	}

	/**
	 * Gets the roles mentioned from a message json object.
	 *
	 * @param client The discord client to use.
	 * @param json   The json response to use.
	 * @return The list of mentioned roles.
	 */
	public static List<String> getRoleMentionsFromJSON(IDiscordClient client, MessageResponse json) {
		List<String> mentions = new ArrayList<>();
		if (json.mention_roles != null)
			for (String role : json.mention_roles)
				mentions.add(role);

		return mentions;
	}

	/**
	 * Gets the attachments on a message.
	 *
	 * @param json The json response to use.
	 * @return The attached messages.
	 */
	public static List<IMessage.Attachment> getAttachmentsFromJSON(MessageResponse json) {
		List<IMessage.Attachment> attachments = new ArrayList<>();
		if (json.attachments != null)
			for (MessageResponse.AttachmentResponse response : json.attachments) {
				attachments.add(new IMessage.Attachment(response.filename, response.size, response.id, response.url));
			}

		return attachments;
	}

	/**
	 * Gets the embedded attachments on a message.
	 *
	 * @param json The json response to use.
	 * @return The embedded messages.
	 */
	public static List<Embedded> getEmbedsFromJSON(MessageResponse json) {
		List<Embedded> embeds = new ArrayList<>();
		if (json.embeds != null)
			for (MessageResponse.EmbedResponse response : json.embeds) {
				embeds.add(new Embedded(response.title, response.type, response.description, response.url,
						response.thumbnail, response.provider));
			}

		return embeds;
	}

	/**
	 * Creates a guild object from a json response.
	 *
	 * @param client The discord client.
	 * @param json   The json response.
	 * @return The guild object.
	 */
	public static IGuild getGuildFromJSON(IDiscordClient client, GuildResponse json) {
		Guild guild;

		if ((guild = (Guild) client.getGuildByID(json.id)) != null) {
			guild.setIcon(json.icon);
			guild.setName(json.name);
			guild.setOwnerID(json.owner_id);
			guild.setAFKChannel(json.afk_channel_id);
			guild.setAfkTimeout(json.afk_timeout);
			guild.setRegion(json.region);

			List<IRole> newRoles = new ArrayList<>();
			for (RoleResponse roleResponse : json.roles) {
				newRoles.add(getRoleFromJSON(guild, roleResponse));
			}
			guild.getRoles().clear();
			guild.getRoles().addAll(newRoles);

			for (IUser user : guild.getUsers()) { //Removes all deprecated roles
				for (IRole role : user.getRolesForGuild(guild)) {
					if (guild.getRoleByID(role.getID()) == null) {
						user.getRolesForGuild(guild).remove(role);
					}
				}
			}
		} else {
			guild = new Guild(client, json.name, json.id, json.icon, json.owner_id, json.afk_channel_id,
					json.afk_timeout, json.region);

			if (json.roles != null)
				for (RoleResponse roleResponse : json.roles) {
					getRoleFromJSON(guild, roleResponse); //Implicitly adds the role to the guild.
				}

			if (json.members != null)
				for (GuildResponse.MemberResponse member : json.members) {
					IUser user = getUserFromGuildMemberResponse(client, guild, member);
					guild.addUser(user);
				}

			if (json.large) { //The guild is large, we have to send a request to get the offline users
				((DiscordClientImpl) client).getWebSocket(json.id).send(DiscordUtils.GSON.toJson(new GuildMembersRequest(json.id)));
			}

			if (json.presences != null)
				for (PresenceResponse presence : json.presences) {
					User user = (User) guild.getUserByID(presence.user.id);
					if (user != null) {
						Status status = getStatusFromJSON(presence.game);
						if (status.getType() == Status.StatusType.STREAM) {
							user.setPresence(Presences.STREAMING);
						} else {
							user.setPresence(Presences.valueOf((presence.status).toUpperCase()));
						}
						user.setStatus(status);
					}
				}

			if (json.channels != null)
				for (ChannelResponse channelResponse : json.channels) {
					String channelType = channelResponse.type;
					if (channelType.equalsIgnoreCase("text")) {
						guild.addChannel(getChannelFromJSON(client, guild, channelResponse));
					} else if (channelType.equalsIgnoreCase("voice")) {
						guild.addVoiceChannel(getVoiceChannelFromJSON(client, guild, channelResponse));
					}
				}

			if (json.voice_states != null) {
				for (VoiceStateResponse voiceState : json.voice_states) {
					((User) guild.getUserByID(voiceState.user_id)).getConnectedVoiceChannels()
							.add(guild.getVoiceChannelByID(voiceState.channel_id));
				}
			}
		}

		return guild;
	}

	/**
	 * Creates a {@link Status} object from a json response.
	 *
	 * @param json The json status object.
	 * @return The Status instance.
	 */
	public static Status getStatusFromJSON(StatusObject json) {
		if (json == null) {
			return Status.empty();
		} else if (json.type == 0) {
			return Status.game(json.name);
		} else if (json.type == 1) {
			return Status.stream(json.name, json.url);
		} else {
			return Status.empty();
		}
	}

	/**
	 * Creates a user object from a guild member json response.
	 *
	 * @param client The discord client.
	 * @param guild  The guild the member belongs to.
	 * @param json   The json response.
	 * @return The user object.
	 */
	public static IUser getUserFromGuildMemberResponse(IDiscordClient client, IGuild guild,
													   GuildResponse.MemberResponse json) {
		User user = getUserFromJSON(client, json.user);
		for (String role : json.roles) {
			Role roleObj = (Role) guild.getRoleByID(role);
			if (roleObj != null && !user.getRolesForGuild(guild).contains(roleObj))
				user.addRole(guild.getID(), roleObj);
		}
		user.addRole(guild.getID(), guild.getRoleByID(guild.getID())); //@everyone role

		user.addNick(guild.getID(), json.nick);

		user.setIsDeaf(guild.getID(), json.deaf);
		user.setIsMute(guild.getID(), json.mute);

		((Guild) guild).getJoinTimes().put(user, convertFromTimestamp(json.joined_at));
		return user;
	}

	/**
	 * Creates a private channel object from a json response.
	 *
	 * @param client The discord client.
	 * @param json   The json response.
	 * @return The private channel object.
	 */
	public static IPrivateChannel getPrivateChannelFromJSON(IDiscordClient client, PrivateChannelResponse json) {
		String id = json.id;
		User recipient = (User) client.getUserByID(id);
		if (recipient == null)
			recipient = getUserFromJSON(client, json.recipient);

		PrivateChannel channel = null;
		for (IPrivateChannel privateChannel : ((DiscordClientImpl) client).privateChannels) {
			if (privateChannel.getRecipient().equals(recipient)) {
				channel = (PrivateChannel) privateChannel;
				break;
			}
		}
		if (channel == null) {
			channel = new PrivateChannel(client, recipient, id);
		}

		return channel;
	}

	/**
	 * Creates a message object from a json response.
	 *
	 * @param client  The discord client.
	 * @param channel The channel.
	 * @param json    The json response.
	 * @return The message object.
	 */
	public static IMessage getMessageFromJSON(IDiscordClient client, IChannel channel, MessageResponse json) {
		if (channel.getMessages() != null && channel.getMessages().contains(json.id)) {
			Message message = (Message) channel.getMessageByID(json.id);
			message.setAttachments(getAttachmentsFromJSON(json));
			message.setEmbedded(getEmbedsFromJSON(json));
			message.setContent(json.content);
			message.setMentionsEveryone(json.mention_everyone);
			message.setMentions(getMentionsFromJSON(client, json), getRoleMentionsFromJSON(client, json));
			message.setTimestamp(convertFromTimestamp(json.timestamp));
			message.setEditedTimestamp(
					json.edited_timestamp == null ? null : convertFromTimestamp(json.edited_timestamp));
			message.setPinned(json.pinned);
			message.setChannelMentions();

			return message;
		} else
			return new Message(client, json.id, json.content, getUserFromJSON(client, json.author), channel,
					convertFromTimestamp(json.timestamp),
					json.edited_timestamp == null ? null : convertFromTimestamp(json.edited_timestamp),
					json.mention_everyone, getMentionsFromJSON(client, json), getRoleMentionsFromJSON(client, json),
					getAttachmentsFromJSON(json), json.pinned, getEmbedsFromJSON(json));
	}

	/**
	 * Creates a channel object from a json response.
	 *
	 * @param client The discord client.
	 * @param guild  the guild.
	 * @param json   The json response.
	 * @return The channel object.
	 */
	public static IChannel getChannelFromJSON(IDiscordClient client, IGuild guild, ChannelResponse json) {
		Channel channel;

		Pair<Map<String, IChannel.PermissionOverride>, Map<String, IChannel.PermissionOverride>> overrides =
				getPermissionOverwritesFromJSONs(
				json.permission_overwrites);
		Map<String, IChannel.PermissionOverride> userOverrides = overrides.getLeft();
		Map<String, IChannel.PermissionOverride> roleOverrides = overrides.getRight();

		if ((channel = (Channel) guild.getChannelByID(json.id)) != null) {
			channel.setName(json.name);
			channel.setPosition(json.position);
			channel.setTopic(json.topic);
			channel.getUserOverrides().clear();
			channel.getUserOverrides().putAll(userOverrides);
			channel.getRoleOverrides().clear();
			channel.getRoleOverrides().putAll(roleOverrides);
		} else {
			channel = new Channel(client, json.name, json.id, guild, json.topic, json.position, roleOverrides,
					userOverrides);
		}

		return channel;
	}

	/**
	 * Generates permission override sets from an array of json responses.
	 *
	 * @param overwrites The overwrites.
	 * @return A pair representing the overwrites per id; left value = user overrides and right value = role overrides.
	 */
	public static Pair<Map<String, IChannel.PermissionOverride>, Map<String, IChannel.PermissionOverride>>
	getPermissionOverwritesFromJSONs(
			PermissionOverwrite[] overwrites) {
		Map<String, IChannel.PermissionOverride> userOverrides = new ConcurrentHashMap<>();
		Map<String, IChannel.PermissionOverride> roleOverrides = new ConcurrentHashMap<>();

		for (PermissionOverwrite overrides : overwrites) {
			if (overrides.type.equalsIgnoreCase("role")) {
				roleOverrides.put(overrides.id,
						new IChannel.PermissionOverride(Permissions.getAllowedPermissionsForNumber(overrides.allow),
								Permissions.getDeniedPermissionsForNumber(overrides.deny)));
			} else if (overrides.type.equalsIgnoreCase("member")) {
				userOverrides.put(overrides.id,
						new IChannel.PermissionOverride(Permissions.getAllowedPermissionsForNumber(overrides.allow),
								Permissions.getDeniedPermissionsForNumber(overrides.deny)));
			} else {
				Discord4J.LOGGER.warn(LogMarkers.API, "Unknown permissions overwrite type \"{}\"!", overrides.type);
			}
		}

		return Pair.of(userOverrides, roleOverrides);
	}

	/**
	 * Creates a role object from a json response.
	 *
	 * @param guild the guild.
	 * @param json  The json response.
	 * @return The role object.
	 */
	public static IRole getRoleFromJSON(IGuild guild, RoleResponse json) {
		Role role;
		if ((role = (Role) guild.getRoleByID(json.id)) != null) {
			role.setColor(json.color);
			role.setHoist(json.hoist);
			role.setName(json.name);
			role.setPermissions(json.permissions);
			role.setPosition(json.position);
			role.setMentionable(json.mentionable);
		} else {
			role = new Role(json.position, json.permissions, json.name, json.managed, json.id, json.hoist, json.color,
					json.mentionable, guild);
			((Guild) guild).addRole(role);
		}
		return role;
	}

	/**
	 * Creates a region object from a json response.
	 *
	 * @param json The json response.
	 * @return The region object.
	 */
	public static IRegion getRegionFromJSON(RegionResponse json) {
		return new Region(json.id, json.name, json.vip);
	}

	/**
	 * Creates a channel object from a json response.
	 *
	 * @param client The discord client.
	 * @param guild  the guild.
	 * @param json   The json response.
	 * @return The channel object.
	 */
	public static IVoiceChannel getVoiceChannelFromJSON(IDiscordClient client, IGuild guild, ChannelResponse json) {
		VoiceChannel channel;

		Pair<Map<String, IChannel.PermissionOverride>, Map<String, IChannel.PermissionOverride>> overrides =
				getPermissionOverwritesFromJSONs(
				json.permission_overwrites);
		Map<String, IChannel.PermissionOverride> userOverrides = overrides.getLeft();
		Map<String, IChannel.PermissionOverride> roleOverrides = overrides.getRight();

		if ((channel = (VoiceChannel) guild.getVoiceChannelByID(json.id)) != null) {
			channel.setUserLimit(json.user_limit);
			channel.setBitrate(json.bitrate);
			channel.setName(json.name);
			channel.setPosition(json.position);
			channel.getUserOverrides().clear();
			channel.getUserOverrides().putAll(userOverrides);
			channel.getRoleOverrides().clear();
			channel.getRoleOverrides().putAll(roleOverrides);
		} else {
			channel = new VoiceChannel(client, json.name, json.id, guild, json.topic, json.position, json.user_limit,
					json.bitrate, roleOverrides, userOverrides);
		}

		return channel;
	}

	/**
	 * Checks a set of permissions provided by a guild against required permissions and a user's role hierarchy
	 * position.
	 *
	 * @param user     The user.
	 * @param guild    The guild.
	 * @param roles    The roles.
	 * @param required The permissions required.
	 * @throws MissingPermissionsException This is thrown if the permissions required aren't present.
	 */
	public static void checkPermissions(IUser user, IGuild guild, List<IRole> roles,
										EnumSet<Permissions> required) throws MissingPermissionsException {
		try {
			if (!isUserHigher(guild, user, roles))
				throw new MissingPermissionsException("Edited roles hierarchy is too high.");

			checkPermissions(user, guild, required);
		} catch (UnsupportedOperationException e) {
		}
	}

	/**
	 * Checks a set of permissions provided by a guild against required permissions and a user's role hierarchy
	 * position.
	 *
	 * @param user     The user.
	 * @param channel  The channel.
	 * @param roles    The roles.
	 * @param required The permissions required.
	 * @throws MissingPermissionsException This is thrown if the permissions required aren't present.
	 */
	public static void checkPermissions(IUser user, IChannel channel, List<IRole> roles,
										EnumSet<Permissions> required) throws MissingPermissionsException {
		try {
			if (!isUserHigher(channel.getGuild(), user, roles))
				throw new MissingPermissionsException("Edited roles hierarchy is too high.");

			checkPermissions(user, channel, required);
		} catch (UnsupportedOperationException e) {
		}
	}

	/**
	 * Checks a set of permissions provided by a channel against required permissions.
	 *
	 * @param user     The user.
	 * @param channel  The channel.
	 * @param required The permissions required.
	 * @throws MissingPermissionsException This is thrown if the permissions required aren't present.
	 */
	public static void checkPermissions(IUser user, IChannel channel, EnumSet<Permissions> required) throws
			MissingPermissionsException {
		try {
			EnumSet<Permissions> contained = channel.getModifiedPermissions(user);
			checkPermissions(contained, required);
		} catch (UnsupportedOperationException e) {
		}
	}

	/**
	 * Checks a set of permissions provided by a guild against required permissions.
	 *
	 * @param user     The user.
	 * @param guild    The guild.
	 * @param required The permissions required.
	 * @throws MissingPermissionsException This is thrown if the permissions required aren't present.
	 */
	public static void checkPermissions(IUser user, IGuild guild, EnumSet<Permissions> required) throws
			MissingPermissionsException {
		try {
			EnumSet<Permissions> contained = EnumSet.noneOf(Permissions.class);
			List<IRole> roles = user.getRolesForGuild(guild);
			for (IRole role : roles) {
				contained.addAll(role.getPermissions());
			}
			checkPermissions(contained, required);
		} catch (UnsupportedOperationException e) {
		}
	}

	/**
	 * Checks a set of permissions provided by a guild against required permissions and a user's role hierarchy
	 * position.
	 *
	 * @param client   The client.
	 * @param guild    The guild.
	 * @param roles    The roles.
	 * @param required The permissions required.
	 * @throws MissingPermissionsException This is thrown if the permissions required aren't present.
	 */
	public static void checkPermissions(IDiscordClient client, IGuild guild, List<IRole> roles,
										EnumSet<Permissions> required) throws MissingPermissionsException {
		checkPermissions(client.getOurUser(), guild, roles, required);
	}

	/**
	 * Checks a set of permissions provided by a guild against required permissions and a user's role hierarchy
	 * position.
	 *
	 * @param client   The client.
	 * @param channel  The channel.
	 * @param roles    The roles.
	 * @param required The permissions required.
	 * @throws MissingPermissionsException This is thrown if the permissions required aren't present.
	 */
	public static void checkPermissions(IDiscordClient client, IChannel channel, List<IRole> roles,
										EnumSet<Permissions> required) throws MissingPermissionsException {
		checkPermissions(client.getOurUser(), channel, roles, required);
	}

	/**
	 * Checks a set of permissions provided by a channel against required permissions.
	 *
	 * @param client   The client.
	 * @param channel  The channel.
	 * @param required The permissions required.
	 * @throws MissingPermissionsException This is thrown if the permissions required aren't present.
	 */
	public static void checkPermissions(IDiscordClient client, IChannel channel, EnumSet<Permissions> required) throws
			MissingPermissionsException {
		checkPermissions(client.getOurUser(), channel, required);
	}

	/**
	 * Checks a set of permissions provided by a guild against required permissions.
	 *
	 * @param client   The client.
	 * @param guild    The guild.
	 * @param required The permissions required.
	 * @throws MissingPermissionsException This is thrown if the permissions required aren't present.
	 */
	public static void checkPermissions(IDiscordClient client, IGuild guild, EnumSet<Permissions> required) throws
			MissingPermissionsException {
		checkPermissions(client.getOurUser(), guild, required);
	}

	/**
	 * Checks a set of permissions against required permissions.
	 *
	 * @param contained The permissions contained.
	 * @param required  The permissions required.
	 * @throws MissingPermissionsException This is thrown if the permissions required aren't present.
	 */
	public static void checkPermissions(EnumSet<Permissions> contained, EnumSet<Permissions> required) throws
			MissingPermissionsException {
		if (contained.contains(Permissions.ADMINISTRATOR))
			return;

		EnumSet<Permissions> missing = EnumSet.noneOf(Permissions.class);

		for (Permissions requiredPermission : required) {
			if (!contained.contains(requiredPermission))
				missing.add(requiredPermission);
		}
		if (missing.size() > 0)
			throw new MissingPermissionsException(missing);
	}

	/**
	 * Gets the time at which a discord id was created.
	 *
	 * @param id The id.
	 * @return The time the id was created.
	 */
	public static LocalDateTime getSnowflakeTimeFromID(String id) {
		long milliseconds = DISCORD_EPOCH.add(new BigInteger(id).shiftRight(22)).longValue();
		return LocalDateTime.ofInstant(Instant.ofEpochMilli(milliseconds), ZoneId.systemDefault());
	}

	/**
	 * Gets an application from a json response.
	 *
	 * @param client   The discord client owner.
	 * @param response The json representation of an application.
	 * @return The application object.
	 */
	public static IApplication getApplicationFromJSON(IDiscordClient client, ApplicationResponse response) {
		return new Application(client, response.secret, response.redirect_uris, response.description, response.name,
				response.id, response.icon, DiscordUtils.getUserFromJSON(client, response.bot),
				response.bot == null ? null : response.bot.token);
	}

	/**
	 * Gets invite codes from a message if it exists.
	 *
	 * @param message The message to parse.
	 * @return The codes or empty if none are found.
	 */
	public static List<String> getInviteCodesFromMessage(String message) {
		Matcher matcher = INVITE_PATTERN.matcher(message);
		List<String> strings = new ArrayList<>();
		while (matcher.find()) {
			strings.add(matcher.group(1));
			matcher = INVITE_PATTERN.matcher(matcher.replaceFirst(""));
		}

		return strings;
	}

	/**
	 * This checks if user1 can interact with the set of provided roles by checking their role hierarchies.
	 *
	 * @param guild The guild to check from.
	 * @param user1 The first user to check.
	 * @param roles The roles to check.
	 * @return True if user1's role hierarchy position > provided roles hierarchy.
	 */
	public static boolean isUserHigher(IGuild guild, IUser user1, List<IRole> roles) {
		List<IRole> user1Roles = guild.getRolesForUser(user1);
		int user1Position = 0;
		int rolesPosition = 0;
		for (IRole role : user1Roles)
			if (user1Position < role.getPosition())
				user1Position = role.getPosition();

		for (IRole role : roles)
			if (rolesPosition < role.getPosition())
				rolesPosition = role.getPosition();

		return user1Position > rolesPosition;
	}

	/**
	 * This takes in an {@link AudioInputStream} and guarantees that it is PCM encoded.
	 *
	 * @param stream The original stream.
	 * @return The PCM encoded stream.
	 */
	public static AudioInputStream getPCMStream(AudioInputStream stream) {
		AudioFormat baseFormat = stream.getFormat();

		//Converts first to PCM data. If the data is already PCM data, this will not change anything.
		AudioFormat toPCM = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, baseFormat.getSampleRate(),
//AudioConnection.OPUS_SAMPLE_RATE,
				baseFormat.getSampleSizeInBits() != -1 ? baseFormat.getSampleSizeInBits() : 16,
				baseFormat.getChannels(),
				//If we are given a frame size, use it. Otherwise, assume 16 bits (2 8bit shorts) per channel.
				baseFormat.getFrameSize() != -1 ? baseFormat.getFrameSize() : 2 * baseFormat.getChannels(),
				baseFormat.getFrameRate() != -1 ? baseFormat.getFrameRate() : baseFormat.getSampleRate(),
				baseFormat.isBigEndian());
		AudioInputStream pcmStream = AudioSystem.getAudioInputStream(toPCM, stream);

		//Then resamples to a sample rate of 48000hz and ensures that data is Big Endian.
		AudioFormat audioFormat = new AudioFormat(toPCM.getEncoding(), AudioManager.OPUS_SAMPLE_RATE,
				toPCM.getSampleSizeInBits(), toPCM.getChannels(), toPCM.getFrameSize(), toPCM.getFrameRate(), true);

		return AudioSystem.getAudioInputStream(audioFormat, pcmStream);
	}

	public static int getShard(IDiscordClient client, IGuild parent) {
		return getShard(client, parent.getID());
	}

	public static int getShard(IDiscordClient client, String guildID) {
		return Math.toIntExact(((Long.parseLong(guildID) >> 22) % client.getShardCount()));
	}
}
