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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import org.apache.commons.lang3.tuple.Pair;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.IShard;
import sx.blah.discord.api.internal.json.event.PresenceUpdateEventResponse;
import sx.blah.discord.api.internal.json.objects.*;
import sx.blah.discord.api.internal.json.requests.GuildMembersRequest;
import sx.blah.discord.handle.impl.obj.*;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.LogMarkers;
import sx.blah.discord.util.MessageTokenizer;
import sx.blah.discord.util.MissingPermissionsException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.awt.*;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Collection of internal Discord4J utilities.
 */
public class DiscordUtils {

	/**
	 * Re-usable instance of jackson.
	 */
	public static final ObjectMapper MAPPER = new ObjectMapper()
			.registerModule(new AfterburnerModule())
			.enable(SerializationFeature.USE_EQUALITY_FOR_OBJECT_ID)
			.enable(SerializationFeature.WRITE_NULL_MAP_VALUES)
			.enable(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY)
			.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
			.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
			.enable(JsonParser.Feature.ALLOW_COMMENTS)
			.enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES)
			.enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES)
			.enable(JsonParser.Feature.ALLOW_MISSING_VALUES)
			.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
			.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
	/**
	 * Like {@link #MAPPER} but it doesn't serialize nulls.
	 */
	public static final ObjectMapper MAPPER_NO_NULLS = new ObjectMapper()
			.registerModule(new AfterburnerModule())
			.setSerializationInclusion(JsonInclude.Include.NON_NULL)
			.enable(SerializationFeature.USE_EQUALITY_FOR_OBJECT_ID)
			.disable(SerializationFeature.WRITE_NULL_MAP_VALUES)
			.enable(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY)
			.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
			.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
			.enable(JsonParser.Feature.ALLOW_COMMENTS)
			.enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES)
			.enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES)
			.enable(JsonParser.Feature.ALLOW_MISSING_VALUES)
			.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
			.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

	/**
	 * Used to find urls in order to not escape them
	 */
	public static final Pattern URL_PATTERN = Pattern.compile(
			"(?:^|[\\W])((ht|f)tp(s?):\\/\\/|www\\.)" + "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*" +
					"[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)",
			Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

	/**
	 * Used to determine age based on discord ids
	 */
	public static final BigInteger DISCORD_EPOCH = new BigInteger("1420070400000");

	/**
	 * If the input matches the toString() result of IEmoji.
	 */
	public static final Pattern IEMOJI_TOSTRING_RESULT = Pattern.compile("<?:[A-Za-z_0-9]+:\\d+>?");

	/**
	 * If the input matches an emoji alias (:alias:)
	 */
	public static final Pattern EMOJI_ALIAS = Pattern.compile(":.+:");

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
	public static User getUserFromJSON(IShard shard, UserObject response) {
		if (response == null)
			return null;

		User user;
		if (shard != null && (user = (User) shard.getUsers().stream().filter(it -> it.getID().equals(response.id)).findFirst().orElse(null)) != null) {
			user.setAvatar(response.avatar);
			user.setName(response.username);
			user.setDiscriminator(response.discriminator);
		} else {
			user = new User(shard, response.username, response.id, response.discriminator, response.avatar,
					new PresenceImpl(Optional.empty(), Optional.empty(), StatusType.OFFLINE), response.bot);
		}
		return user;
	}

	/**
	 * Creates a java {@link Invite} object for a json response.
	 *
	 * @param client The discord client to use.
	 * @param json   The json response to use.
	 * @return The java invite object.
	 */
	public static IInvite getInviteFromJSON(IDiscordClient client, InviteObject json) {
		return new Invite(client, json.code);
	}

	/**
	 * Gets the users mentioned from a message json object.
	 *
	 * @param json The json response to use.
	 * @return The list of mentioned users.
	 */
	public static List<String> getMentionsFromJSON(MessageObject json) {
		List<String> mentions = new ArrayList<>();
		if (json.mentions != null)
			for (UserObject response : json.mentions)
				mentions.add(response.id);

		return mentions;
	}

	/**
	 * Gets the roles mentioned from a message json object.
	 *
	 * @param json The json response to use.
	 * @return The list of mentioned roles.
	 */
	public static List<String> getRoleMentionsFromJSON(MessageObject json) {
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
	public static List<IMessage.Attachment> getAttachmentsFromJSON(MessageObject json) {
		List<IMessage.Attachment> attachments = new ArrayList<>();
		if (json.attachments != null)
			for (MessageObject.AttachmentObject response : json.attachments) {
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
	public static List<Embed> getEmbedsFromJSON(MessageObject json) {
		List<Embed> embeds = new ArrayList<>();
		if (json.embeds != null)
			for (EmbedObject response : json.embeds) {
				embeds.add(new Embed(response.title, response.type, response.description, response.url,
						response.thumbnail, response.provider, convertFromTimestamp(response.timestamp),
						new Color(response.color), response.footer, response.image, response.video,
						response.author, response.fields));
			}

		return embeds;
	}

	/**
	 * Creates a guild object from a json response.
	 *
	 * @param shard The shard this guild is on
	 * @param json  The json response.
	 * @return The guild object.
	 */
	public static IGuild getGuildFromJSON(IShard shard, GuildObject json) {
		Guild guild;

		if ((guild = (Guild) shard.getGuildByID(json.id)) != null) {
			guild.setIcon(json.icon);
			guild.setName(json.name);
			guild.setOwnerID(json.owner_id);
			guild.setAFKChannel(json.afk_channel_id);
			guild.setAfkTimeout(json.afk_timeout);
			guild.setRegion(json.region);
			guild.setVerificationLevel(json.verification_level);
			guild.setTotalMemberCount(json.member_count);

			List<IRole> newRoles = new ArrayList<>();
			for (RoleObject roleResponse : json.roles) {
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
			guild = new Guild(shard, json.name, json.id, json.icon, json.owner_id, json.afk_channel_id,
					json.afk_timeout, json.region, json.verification_level);

			if (json.roles != null)
				for (RoleObject roleResponse : json.roles) {
					getRoleFromJSON(guild, roleResponse); //Implicitly adds the role to the guild.
				}

			guild.setTotalMemberCount(json.member_count);
			if (json.members != null) {
				for (MemberObject member : json.members) {
					IUser user = getUserFromGuildMemberResponse(guild, member);
					guild.addUser(user);
				}
			}

			if (json.large) { //The guild is large, we have to send a request to get the offline users
				((ShardImpl) shard).ws.send(GatewayOps.REQUEST_GUILD_MEMBERS, new GuildMembersRequest(json.id));
			}

			if (json.presences != null)
				for (PresenceObject presence : json.presences) {
					User user = (User) guild.getUserByID(presence.user.id);
					if (user != null) {
						user.setPresence(
								DiscordUtils.getPresenceFromJSON(presence));
					}
				}

			if (json.channels != null)
				for (ChannelObject channelResponse : json.channels) {
					String channelType = channelResponse.type;
					if (channelType.equalsIgnoreCase("text")) {
						guild.addChannel(getChannelFromJSON(guild, channelResponse));
					} else if (channelType.equalsIgnoreCase("voice")) {
						guild.addVoiceChannel(getVoiceChannelFromJSON(guild, channelResponse));
					}
				}

			if (json.voice_states != null) {
				for (VoiceStateObject voiceState : json.voice_states) {
					guild.getUserByID(voiceState.user_id).getVoiceStates().put(
							guild.getID(), DiscordUtils.getVoiceStateFromJson(guild, voiceState));
				}
			}
		}

		guild.getEmojis().clear();
		for (EmojiObject obj : json.emojis) {
			guild.getEmojis().add(DiscordUtils.getEmojiFromJSON(guild, obj));
		}

		return guild;
	}

	/**
	 * Creates an IEmoji object from the EmojiObj json data.
	 *
	 * @param guild The guild.
	 * @param json  The json object data.
	 * @return The emoji object.
	 */
	public static IEmoji getEmojiFromJSON(IGuild guild, EmojiObject json) {
		EmojiImpl emoji = new EmojiImpl(guild, json.id, json.name, json.require_colons, json.managed, json.roles);

		return emoji;
	}

	public static IReaction getReactionFromJSON(IShard shard, MessageObject.ReactionObject object) {
		Reaction reaction = new Reaction(shard, object.count, new CopyOnWriteArrayList<>(),
				object.emoji.id != null
						? object.emoji.id
						: object.emoji.name, object.emoji.id != null);


		return reaction;
	}

	public static List<IReaction> getReactionsFromJson(IShard shard, MessageObject.ReactionObject[] objects) {
		List<IReaction> reactions = new CopyOnWriteArrayList<>();

		if (objects != null) {
			for (MessageObject.ReactionObject obj : objects) {
				IReaction r = getReactionFromJSON(shard, obj);
				if (r != null)
					reactions.add(r);
			}
		}

		return reactions;
	}

	/**
	 * Creates a user object from a guild member json response.
	 *
	 * @param guild The guild the member belongs to.
	 * @param json  The json response.
	 * @return The user object.
	 */
	public static IUser getUserFromGuildMemberResponse(IGuild guild, MemberObject json) {
		User user = getUserFromJSON(guild.getShard(), json.user);
		for (String role : json.roles) {
			Role roleObj = (Role) guild.getRoleByID(role);
			if (roleObj != null && !user.getRolesForGuild(guild).contains(roleObj))
				user.addRole(guild.getID(), roleObj);
		}
		user.addRole(guild.getID(), guild.getRoleByID(guild.getID())); //@everyone role
		user.addNick(guild.getID(), json.nick);

		VoiceState voiceState = (VoiceState) user.getVoiceStateForGuild(guild);
		voiceState.setDeafened(json.deaf);
		voiceState.setMuted(json.mute);

		((Guild) guild).getJoinTimes().put(user, convertFromTimestamp(json.joined_at));
		return user;
	}

	/**
	 * Creates a private channel object from a json response.
	 *
	 * @param shard The shard this channel is on.
	 * @param json  The json response.
	 * @return The private channel object.
	 */
	public static IPrivateChannel getPrivateChannelFromJSON(IShard shard, PrivateChannelObject json) {
		String id = json.id;
		User recipient = (User) shard.getUserByID(id);
		if (recipient == null)
			recipient = getUserFromJSON(shard, json.recipient);

		PrivateChannel channel = null;
		for (IPrivateChannel privateChannel : ((ShardImpl) shard).privateChannels) {
			if (privateChannel.getRecipient().equals(recipient)) {
				channel = (PrivateChannel) privateChannel;
				break;
			}
		}
		if (channel == null) {
			channel = new PrivateChannel((DiscordClientImpl) shard.getClient(), recipient, id);
		}

		return channel;
	}

	/**
	 * Creates a message object from a json response.
	 *
	 * @param channel The channel.
	 * @param json    The json response.
	 * @return The message object.
	 */
	public static IMessage getMessageFromJSON(Channel channel, MessageObject json) {
		if (channel.messages.stream().anyMatch(msg -> msg.getID().equals(json.id))) {
			Message message = (Message) channel.getMessageByID(json.id);
			message.setAttachments(getAttachmentsFromJSON(json));
			message.setEmbeds(getEmbedsFromJSON(json));
			message.setContent(json.content);
			message.setMentionsEveryone(json.mention_everyone);
			message.setMentions(getMentionsFromJSON(json), getRoleMentionsFromJSON(json));
			message.setTimestamp(convertFromTimestamp(json.timestamp));
			message.setEditedTimestamp(
					json.edited_timestamp == null ? null : convertFromTimestamp(json.edited_timestamp));
			message.setPinned(Boolean.TRUE.equals(json.pinned));
			message.setChannelMentions();

			return message;
		} else {
			Message message = new Message(channel.getClient(), json.id, json.content,
					channel.getGuild().getUserByID(json.author.id), channel, convertFromTimestamp(json.timestamp),
					json.edited_timestamp == null ? null : convertFromTimestamp(json.edited_timestamp),
					json.mention_everyone, getMentionsFromJSON(json), getRoleMentionsFromJSON(json),
					getAttachmentsFromJSON(json), Boolean.TRUE.equals(json.pinned), getEmbedsFromJSON(json),
					getReactionsFromJson(channel.getShard(), json.reactions), json.webhook_id);

			for (IReaction reaction : message.getReactions()) {
				((Reaction) reaction).setMessage(message);
			}

			return message;
		}
	}

	/**
	 * Updates a message object with the non-null or non-empty contents of a json response.
	 *
	 * @param toUpdate The message.
	 * @param json     The json response.
	 * @return The message object.
	 */
	public static IMessage getUpdatedMessageFromJSON(IMessage toUpdate, MessageObject json) {
		if (toUpdate == null)
			return null;

		Message message = (Message) toUpdate;
		List<IMessage.Attachment> attachments = getAttachmentsFromJSON(json);
		List<Embed> embeds = getEmbedsFromJSON(json);
		if (!attachments.isEmpty())
			message.setAttachments(attachments);
		if (!embeds.isEmpty())
			message.setEmbeds(embeds);
		if (json.content != null) {
			message.setContent(json.content);
			message.setMentions(getMentionsFromJSON(json), getRoleMentionsFromJSON(json));
			message.setMentionsEveryone(json.mention_everyone);
			message.setChannelMentions();
		}
		if (json.timestamp != null)
			message.setTimestamp(convertFromTimestamp(json.timestamp));
		if (json.edited_timestamp != null)
			message.setEditedTimestamp(convertFromTimestamp(json.edited_timestamp));
		if (json.pinned != null)
			message.setPinned(json.pinned);

		return message;
	}

	/**
	 * Creates a webhook object from a json response.
	 *
	 * @param channel The webhook.
	 * @param json The json response.
	 * @return The message object.
	 */
	public static IWebhook getWebhookFromJSON(IChannel channel, WebhookObject json) {
		if (channel.getWebhookByID(json.id) != null) {
			Webhook webhook = (Webhook) channel.getWebhookByID(json.id);
			webhook.setName(json.name);
			webhook.setAvatar(json.avatar);

			return webhook;
		} else {
			return new Webhook(channel.getClient(), json.name, json.id, channel, getUserFromJSON(channel.getShard(), json.user), json.avatar, json.token);
		}
	}

	/**
	 * Creates a channel object from a json response.
	 *
	 * @param guild the guild.
	 * @param json  The json response.
	 * @return The channel object.
	 */
	public static IChannel getChannelFromJSON(IGuild guild, ChannelObject json) {
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
			channel = new Channel((DiscordClientImpl) guild.getClient(), json.name, json.id, guild, json.topic, json.position,
					roleOverrides, userOverrides);
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
	getPermissionOverwritesFromJSONs(OverwriteObject[] overwrites) {
		Map<String, IChannel.PermissionOverride> userOverrides = new ConcurrentHashMap<>();
		Map<String, IChannel.PermissionOverride> roleOverrides = new ConcurrentHashMap<>();

		for (OverwriteObject overrides : overwrites) {
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
	public static IRole getRoleFromJSON(IGuild guild, RoleObject json) {
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
	public static IRegion getRegionFromJSON(VoiceRegionObject json) {
		return new Region(json.id, json.name, json.vip);
	}

	/**
	 * Creates a channel object from a json response.
	 *
	 * @param guild the guild.
	 * @param json  The json response.
	 * @return The channel object.
	 */
	public static IVoiceChannel getVoiceChannelFromJSON(IGuild guild, ChannelObject json) {
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
			channel = new VoiceChannel((DiscordClientImpl) guild.getClient(), json.name, json.id, guild, json.topic, json.position,
					json.user_limit, json.bitrate, roleOverrides, userOverrides);
		}

		return channel;
	}

	/**
	 * Creates a voice state object from a json response.
	 *
	 * @param guild The guild the voice state is in.
	 * @param json The json response.
	 * @return The voice state object.
	 */
	public static IVoiceState getVoiceStateFromJson(IGuild guild, VoiceStateObject json) {
		return new VoiceState(guild, guild.getVoiceChannelByID(json.channel_id), guild.getUserByID(json.user_id),
				json.session_id, json.deaf, json.mute, json.self_deaf, json.self_mute, json.suppress);
	}

	public static IPresence getPresenceFromJSON(PresenceObject presence) {
		return new PresenceImpl(Optional.ofNullable(presence.game == null ? null : presence.game.name),
				Optional.ofNullable(presence.game == null ? null : presence.game.url),
				((presence.game != null && presence.game.type == GameObject.STREAMING)
						? StatusType.STREAMING
						: (StatusType.get(presence.status))));
	}

	public static IPresence getPresenceFromJSON(PresenceUpdateEventResponse response) {
		PresenceObject obj = new PresenceObject();
		obj.game = response.game;
		obj.status = response.status;
		return getPresenceFromJSON(obj);
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
	public static void checkPermissions(IUser user, IGuild guild, List<IRole> roles, EnumSet<Permissions> required) {
		try {
			if (!isUserHigher(guild, user, roles))
				throw new MissingPermissionsException("Edited roles hierarchy is too high.", EnumSet.noneOf(Permissions.class));

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
	public static void checkPermissions(IUser user, IChannel channel, List<IRole> roles, EnumSet<Permissions> required) {
		try {
			if (!isUserHigher(channel.getGuild(), user, roles))
				throw new MissingPermissionsException("Edited roles hierarchy is too high.", EnumSet.noneOf(Permissions.class));

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
	public static void checkPermissions(IUser user, IChannel channel, EnumSet<Permissions> required) {
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
	public static void checkPermissions(IUser user, IGuild guild, EnumSet<Permissions> required) {
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
	public static void checkPermissions(IDiscordClient client, IGuild guild, List<IRole> roles, EnumSet<Permissions> required) {
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
	public static void checkPermissions(IDiscordClient client, IChannel channel, List<IRole> roles, EnumSet<Permissions> required)  {
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
	public static void checkPermissions(IDiscordClient client, IChannel channel, EnumSet<Permissions> required) {
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
	public static void checkPermissions(IDiscordClient client, IGuild guild, EnumSet<Permissions> required) {
		checkPermissions(client.getOurUser(), guild, required);
	}

	/**
	 * Checks a set of permissions against required permissions.
	 *
	 * @param contained The permissions contained.
	 * @param required  The permissions required.
	 * @throws MissingPermissionsException This is thrown if the permissions required aren't present.
	 */
	public static void checkPermissions(EnumSet<Permissions> contained, EnumSet<Permissions> required) {
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
	 * Gets invite codes from a message if it exists.
	 *
	 * @param message The message to parse.
	 * @return The codes or empty if none are found.
	 */
	public static List<String> getInviteCodesFromMessage(String message) {
		Matcher matcher = MessageTokenizer.INVITE_PATTERN.matcher(message);
		List<String> strings = new ArrayList<>();
		while (matcher.find()) {
			strings.add(matcher.group(1));
			matcher = MessageTokenizer.INVITE_PATTERN.matcher(matcher.replaceFirst(""));
		}
		return strings;
	}

	/**
	 * This checks if user can interact with the set of provided roles by checking their role hierarchies.
	 *
	 * @param guild The guild to check from.
	 * @param user The first user to check.
	 * @param roles The roles to check.
	 * @return True if user's role hierarchy position > provided roles hierarchy.
	 */
	public static boolean isUserHigher(IGuild guild, IUser user, List<IRole> roles) {
		OptionalInt userPos = user.getRolesForGuild(guild).stream().mapToInt(IRole::getPosition).max();
		OptionalInt rolesPos = roles.stream().mapToInt(IRole::getPosition).max();
		return (userPos.isPresent() ? userPos.getAsInt() : 0) > (rolesPos.isPresent() ? rolesPos.getAsInt() : 0);
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
		AudioFormat audioFormat = new AudioFormat(toPCM.getEncoding(), OpusUtil.OPUS_SAMPLE_RATE,
				toPCM.getSampleSizeInBits(), toPCM.getChannels(), toPCM.getFrameSize(), toPCM.getFrameRate(), true);

		return AudioSystem.getAudioInputStream(audioFormat, pcmStream);
	}

	/**
	 * This creates a {@link ThreadFactory} which produces threads which run as daemons.
	 *
	 * @return The new thread factory.
	 */
	public static ThreadFactory createDaemonThreadFactory() {
		return createDaemonThreadFactory(null);
	}

	/**
	 * This creates a {@link ThreadFactory} which produces threads which run as daemons.
	 *
	 * @param threadName The name to place on created threads.
	 * @return The new thread factory.
	 */
	public static ThreadFactory createDaemonThreadFactory(String threadName) {
		return (runnable) -> { //Ensures all threads are daemons
			Thread thread = Executors.defaultThreadFactory().newThread(runnable);
			if (threadName != null)
				thread.setName(threadName);
			thread.setDaemon(true);
			return thread;
		};
	}

	/**
	 * Used to check equality between two {@link IDiscordObject}s using their IDs.
	 * If one of the given objects is not a discord object, it will use the {@link Object#equals(Object)} method of that
	 * object instead.
	 *
	 * @param a The first object.
	 * @param b The second object.
	 * @return If the two objects are equal.
	 */
	public static boolean equals(Object a, Object b) {
		if (a == b) return true;
		if (a == null || b == null) return false;

		if (!IDiscordObject.class.isAssignableFrom(a.getClass())) {
			return a.equals(b);
		}

		if (!IDiscordObject.class.isAssignableFrom(b.getClass())) {
			return b.equals(a);
		}

		if (!a.getClass().isAssignableFrom(b.getClass())) return false;

		if (!((IDiscordObject) a).getID().equals(((IDiscordObject) b).getID())) return false;

		return true;
	}
}
