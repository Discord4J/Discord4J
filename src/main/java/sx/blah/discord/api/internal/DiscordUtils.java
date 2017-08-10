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
import sx.blah.discord.api.internal.json.objects.audit.AuditLogEntryObject;
import sx.blah.discord.api.internal.json.objects.audit.AuditLogObject;
import sx.blah.discord.handle.audit.AuditLog;
import sx.blah.discord.handle.audit.ActionType;
import sx.blah.discord.handle.audit.entry.AuditLogEntry;
import sx.blah.discord.handle.audit.entry.DiscordObjectEntry;
import sx.blah.discord.handle.audit.entry.TargetedEntry;
import sx.blah.discord.handle.audit.entry.change.ChangeMap;
import sx.blah.discord.handle.audit.entry.option.OptionMap;
import sx.blah.discord.handle.impl.obj.*;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.LogMarkers;
import sx.blah.discord.util.LongMapCollector;
import sx.blah.discord.util.MessageTokenizer;
import sx.blah.discord.util.RequestBuilder;
import sx.blah.discord.util.cache.Cache;
import sx.blah.discord.util.cache.LongMap;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.awt.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Collection of internal Discord4J utilities.
 */
public class DiscordUtils {

	/**
	 * The version of Discord's API and Gateway used by Discord4J.
	 */
	public static final String API_VERSION = "6";

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
	 * Used to determine age based on discord ids
	 */
	public static final long DISCORD_EPOCH = 1420070400000L;

	/**
	 * Pattern for Discord's custom emoji
	 */
	public static final Pattern CUSTOM_EMOJI_PATTERN = Pattern.compile("<?:[A-Za-z_0-9]+:\\d+>?");

	/**
	 * Pattern for Discord's emoji aliases (e.g. :heart: or :thinking:)
	 */
	public static final Pattern EMOJI_ALIAS_PATTERN = Pattern.compile(":.+:");

	/**
	 * Pattern for Discord's nsfw channel name indicator
	 */
	public static final Pattern NSFW_CHANNEL_PATTERN = Pattern.compile("^nsfw(-|$)");

	/**
	 * Pattern for Discord's valid streaming URL strings passed to {@link IShard#streaming(String, String)}
	 */
	public static final Pattern STREAM_URL_PATTERN = Pattern.compile("https?://(www\\.)?twitch\\.tv/.+");

	/**
	 * Gets a snowflake from a unix timestamp.
	 *
	 * <p>This snowflake only contains accurate information about the timestamp (not about other parts of the snowflake).
	 * The returned snowflake is only one of many that could exist at the given timestamp.
	 *
	 * @param unixTime The unix timestamp that should be used in the snowflake.
	 * @return A snowflake with the given timestamp.
	 */
	public static long getSnowflakeFromTimestamp(long unixTime) {
		return (unixTime - DISCORD_EPOCH) << 22;
	}

	/**
	 * Gets a snowflake from a unix timestamp.
	 *
	 * <p>This snowflake only contains accurate information about the timestamp (not about other parts of the snowflake).
	 * The returned snowflake is only one of many that could exist at the given timestamp.
	 *
	 * @param date The date that should be converted to a unix timestamp for use in the snowflake.
	 * @return A snowflake with the given timestamp.
	 */
	public static long getSnowflakeFromTimestamp(LocalDateTime date) {
		return getSnowflakeFromTimestamp(date.atZone(ZoneId.systemDefault()).toEpochSecond());
	}

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
		if (shard != null && (user = (User) shard.getUserByID(Long.parseUnsignedLong(response.id))) != null) {
			user.setAvatar(response.avatar);
			user.setName(response.username);
			user.setDiscriminator(response.discriminator);
		} else {
			user = new User(shard, response.username, Long.parseUnsignedLong(response.id), response.discriminator, response.avatar,
					new Presence(null, null, StatusType.OFFLINE), response.bot);
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
		return new Invite(client, json);
	}

	/**
	 * Creates a java {@link ExtendedInvite} object for a json response.
	 *
	 * @param client The discord client to use.
	 * @param json   The json response to use.
	 * @return The java extended invite object.
	 */
	public static IExtendedInvite getExtendedInviteFromJSON(IDiscordClient client, ExtendedInviteObject json) {
		return new ExtendedInvite(client, json);
	}

	/**
	 * Gets the users mentioned from a message json object.
	 *
	 * @param json The json response to use.
	 * @return The list of mentioned users.
	 */
	public static List<Long> getMentionsFromJSON(MessageObject json) {
		List<Long> mentions = new ArrayList<>();
		if (json.mentions != null)
			for (UserObject response : json.mentions)
				mentions.add(Long.parseUnsignedLong(response.id));

		return mentions;
	}

	/**
	 * Gets the roles mentioned from a message json object.
	 *
	 * @param json The json response to use.
	 * @return The list of mentioned roles.
	 */
	public static List<Long> getRoleMentionsFromJSON(MessageObject json) {
		List<Long> mentions = new ArrayList<>();
		if (json.mention_roles != null)
			for (String role : json.mention_roles)
				mentions.add(Long.parseUnsignedLong(role));

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
				attachments.add(new IMessage.Attachment(response.filename, response.size, Long.parseUnsignedLong(response.id), response.url));
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

		long guildId = Long.parseUnsignedLong(json.id);
		if ((guild = (Guild) shard.getGuildByID(guildId)) != null) {
			guild.setIcon(json.icon);
			guild.setName(json.name);
			guild.setOwnerID(Long.parseUnsignedLong(json.owner_id));
			guild.setAFKChannel(json.afk_channel_id == null ? 0 : Long.parseUnsignedLong(json.afk_channel_id));
			guild.setAfkTimeout(json.afk_timeout);
			guild.setRegion(json.region);
			guild.setVerificationLevel(json.verification_level);
			guild.setTotalMemberCount(json.member_count);

			List<IRole> newRoles = new ArrayList<>();
			for (RoleObject roleResponse : json.roles) {
				newRoles.add(getRoleFromJSON(guild, roleResponse));
			}
			guild.roles.clear();
			guild.roles.putAll(newRoles);

			for (IUser user : guild.getUsers()) { //Removes all deprecated roles
				for (IRole role : user.getRolesForGuild(guild)) {
					if (guild.getRoleByID(role.getLongID()) == null) {
						user.getRolesForGuild(guild).remove(role);
					}
				}
			}
		} else {
			guild = new Guild(shard, json.name, guildId, json.icon, Long.parseUnsignedLong(json.owner_id),
					json.afk_channel_id == null ? 0 : Long.parseUnsignedLong(json.afk_channel_id), json.afk_timeout, json.region, json.verification_level);

			if (json.roles != null)
				for (RoleObject roleResponse : json.roles) {
					getRoleFromJSON(guild, roleResponse); //Implicitly adds the role to the guild.
				}

			guild.setTotalMemberCount(json.member_count);
			if (json.members != null) {
				for (MemberObject member : json.members) {
					IUser user = getUserFromGuildMemberResponse(guild, member);
					guild.users.put(user);
				}
			}

			if (json.presences != null)
				for (PresenceObject presence : json.presences) {
					User user = (User) guild.getUserByID(Long.parseUnsignedLong(presence.user.id));
					if (user != null) {
						user.setPresence(DiscordUtils.getPresenceFromJSON(presence));
					}
				}

			if (json.channels != null)
				for (ChannelObject channelJSON : json.channels) {
					IChannel channel = getChannelFromJSON(shard, guild, channelJSON);
					if (channelJSON.type == ChannelObject.Type.GUILD_TEXT) {
						guild.channels.put(channel);
					} else if (channelJSON.type == ChannelObject.Type.GUILD_VOICE) {
						guild.voiceChannels.put((IVoiceChannel) channel);
					}
				}

			if (json.voice_states != null) {
				for (VoiceStateObject voiceState : json.voice_states) {
					final AtomicReference<IUser> user = new AtomicReference<>(guild.getUserByID(Long.parseUnsignedLong(voiceState.user_id)));
					if (user.get() == null) {
						new RequestBuilder(shard.getClient()).shouldBufferRequests(true).doAction(() -> {
							if (user.get() == null) user.set(shard.fetchUser(Long.parseUnsignedLong(voiceState.user_id)));
							return true;
						}).execute();
					}
					if (user.get()!= null)
						((User) user.get()).voiceStates.put(DiscordUtils.getVoiceStateFromJson(guild, voiceState));
				}
			}
		}

		// emoji are always updated
		guild.emojis.clear();
		for (EmojiObject obj : json.emojis) {
			guild.emojis.put(DiscordUtils.getEmojiFromJSON(guild, obj));
		}

		return guild;
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
			Role roleObj = (Role) guild.getRoleByID(Long.parseUnsignedLong(role));
			if (roleObj != null && !user.getRolesForGuild(guild).contains(roleObj))
				user.addRole(guild.getLongID(), roleObj);
		}
		user.addRole(guild.getLongID(), guild.getRoleByID(guild.getLongID())); //@everyone role
		user.addNick(guild.getLongID(), json.nick);

		VoiceState voiceState = (VoiceState) user.getVoiceStateForGuild(guild);
		voiceState.setDeafened(json.deaf);
		voiceState.setMuted(json.mute);

		((Guild) guild).joinTimes.put(new Guild.TimeStampHolder(user.getLongID(), convertFromTimestamp(json.joined_at)));
		return user;
	}

	/**
	 * Creates a message object from a json response.
	 *
	 * @param channel The channel.
	 * @param json    The json response.
	 * @return The message object.
	 */
	public static IMessage getMessageFromJSON(Channel channel, MessageObject json) {
		if (json == null)
			return null;

		if (channel.messages.containsKey(json.id)) {
			Message message = (Message) channel.getMessageByID(Long.parseUnsignedLong(json.id));
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
			long authorId = Long.parseUnsignedLong(json.author.id);
			IGuild guild = channel.isPrivate() ? null : channel.getGuild();
			IUser author = guild == null ? getUserFromJSON(channel.getShard(), json.author) : guild
					.getUsers()
					.stream()
					.filter(it -> it.getLongID() == authorId)
					.findAny()
					.orElseGet(() -> getUserFromJSON(channel.getShard(), json.author));

			Message message = new Message(channel.getClient(), Long.parseUnsignedLong(json.id), json.content,
					author, channel, convertFromTimestamp(json.timestamp),
					json.edited_timestamp == null ? null : convertFromTimestamp(json.edited_timestamp),
					json.mention_everyone, getMentionsFromJSON(json), getRoleMentionsFromJSON(json),
					getAttachmentsFromJSON(json), Boolean.TRUE.equals(json.pinned), getEmbedsFromJSON(json),
					json.webhook_id != null ? Long.parseUnsignedLong(json.webhook_id) : 0);
			message.setReactions(getReactionsFromJSON(message, json.reactions));

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
		long webhookId = Long.parseUnsignedLong(json.id);
		if (channel.getWebhookByID(webhookId) != null) {
			Webhook webhook = (Webhook) channel.getWebhookByID(webhookId);
			webhook.setName(json.name);
			webhook.setAvatar(json.avatar);

			return webhook;
		} else {
			long userId = Long.parseUnsignedLong(json.user.id);
			IUser author = channel.getGuild()
					.getUsers()
					.stream()
					.filter(it -> it.getLongID() == userId)
					.findAny()
					.orElseGet(() -> getUserFromJSON(channel.getShard(), json.user));
			return new Webhook(channel.getClient(), json.name, Long.parseUnsignedLong(json.id), channel, author, json.avatar, json.token);
		}
	}

	/**
	 * Creates a channel object from a json response.
	 *
	 * @param shard the shard.
	 * @param json  The json response.
	 * @return The channel object.
	 */
	public static IChannel getChannelFromJSON(IShard shard, IGuild guild, ChannelObject json) {
		DiscordClientImpl client = (DiscordClientImpl) shard.getClient();
		long id = Long.parseUnsignedLong(json.id);
		Channel channel = (Channel) shard.getChannelByID(id);
		if (channel == null) channel = (Channel) shard.getVoiceChannelByID(id);

		if (json.type == ChannelObject.Type.PRIVATE) {
			if (channel == null) {
				User recipient = getUserFromJSON(shard, json.recipients[0]);
				channel = new PrivateChannel(client, recipient, id);
			}
		} else if (json.type == ChannelObject.Type.GUILD_TEXT || json.type == ChannelObject.Type.GUILD_VOICE) {
			Pair<Cache<IChannel.PermissionOverride>, Cache<IChannel.PermissionOverride>> overrides =
					getPermissionOverwritesFromJSONs(client, json.permission_overwrites);

			if (channel != null) {
				channel.setName(json.name);
				channel.setPosition(json.position);
				channel.setNSFW(json.nsfw);
				channel.userOverrides.clear();
				channel.roleOverrides.clear();
				channel.userOverrides.putAll(overrides.getLeft());
				channel.roleOverrides.putAll(overrides.getRight());

				if (json.type == ChannelObject.Type.GUILD_TEXT) {
					channel.setTopic(json.topic);
				} else {
					VoiceChannel vc = (VoiceChannel) channel;
					vc.setUserLimit(json.user_limit);
					vc.setBitrate(json.bitrate);
				}
			} else if (json.type == ChannelObject.Type.GUILD_TEXT) {
				channel = new Channel(client, json.name, id, guild, json.topic, json.position, json.nsfw,
						overrides.getRight(), overrides.getLeft());
			} else if (json.type == ChannelObject.Type.GUILD_VOICE) {
				channel = new VoiceChannel(client, json.name, id, guild, json.topic, json.position, json.nsfw,
						json.user_limit, json.bitrate, overrides.getRight(), overrides.getLeft());
			}
		}

		return channel;
	}

	/**
	 * Generates permission override sets from an array of json responses.
	 *
	 * @param overwrites The overwrites.
	 * @return A pair representing the overwrites per id; left value = user overrides and right value = role overrides.
	 */
	public static Pair<Cache<IChannel.PermissionOverride>, Cache<IChannel.PermissionOverride>>
	getPermissionOverwritesFromJSONs(DiscordClientImpl client, OverwriteObject[] overwrites) {
		Cache<IChannel.PermissionOverride> userOverrides = new Cache<>(client, IChannel.PermissionOverride.class);
		Cache<IChannel.PermissionOverride> roleOverrides = new Cache<>(client, IChannel.PermissionOverride.class);

		for (OverwriteObject overrides : overwrites) {
			if (overrides.type.equalsIgnoreCase("role")) {
				roleOverrides.put(new IChannel.PermissionOverride(Permissions.getAllowedPermissionsForNumber(overrides.allow),
								Permissions.getDeniedPermissionsForNumber(overrides.deny), Long.parseUnsignedLong(overrides.id)));
			} else if (overrides.type.equalsIgnoreCase("member")) {
				userOverrides.put(new IChannel.PermissionOverride(Permissions.getAllowedPermissionsForNumber(overrides.allow),
								Permissions.getDeniedPermissionsForNumber(overrides.deny), Long.parseUnsignedLong(overrides.id)));
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
		if ((role = (Role) guild.getRoleByID(Long.parseUnsignedLong(json.id))) != null) {
			role.setColor(json.color);
			role.setHoist(json.hoist);
			role.setName(json.name);
			role.setPermissions(json.permissions);
			role.setPosition(json.position);
			role.setMentionable(json.mentionable);
		} else {
			role = new Role(json.position, json.permissions, json.name, json.managed, Long.parseUnsignedLong(json.id), json.hoist, json.color,
					json.mentionable, guild);
			((Guild) guild).roles.put(role);
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
	 * Creates a voice state object from a json response.
	 *
	 * @param guild The guild the voice state is in.
	 * @param json The json response.
	 * @return The voice state object.
	 */
	public static IVoiceState getVoiceStateFromJson(IGuild guild, VoiceStateObject json) {
		IVoiceChannel channel = json.channel_id != null ? guild.getVoiceChannelByID(Long.parseUnsignedLong(json.channel_id)) : null;
		return new VoiceState(guild, channel, guild.getUserByID(Long.parseUnsignedLong(json.user_id)),
				json.session_id, json.deaf, json.mute, json.self_deaf, json.self_mute, json.suppress);
	}

	public static IPresence getPresenceFromJSON(PresenceObject presence) {
		return getPresenceFromJSON(presence.game, presence.status);
	}

	public static IPresence getPresenceFromJSON(PresenceUpdateEventResponse response) {
		return getPresenceFromJSON(response.game, response.status);
	}

	private static IPresence getPresenceFromJSON(GameObject game, String status) {
		return new Presence(
				game == null ? null : game.name,
				game == null ? null : game.url,
				StatusType.get(status));
	}



	public static IEmoji getEmojiFromJSON(IGuild guild, EmojiObject json) {
		long id = Long.parseUnsignedLong(json.id);
		List<IRole> roles = Arrays.stream(json.roles)
				.map(role -> guild.getRoleByID(Long.parseUnsignedLong(role)))
				.collect(Collectors.toList());

		return new EmojiImpl(id, guild, json.name, roles, json.require_colons, json.managed);
	}

	public static List<IReaction> getReactionsFromJSON(IMessage message, MessageObject.ReactionObject[] json) {
		List<IReaction> reactions = new ArrayList<>();
		if (json != null)
			for (MessageObject.ReactionObject object : json) {
				long id = object.emoji.id == null ? 0 : Long.parseUnsignedLong(object.emoji.id);
				ReactionEmoji emoji = ReactionEmoji.of(object.emoji.name, id);
				reactions.add(new Reaction(message, object.count, emoji));
			}

		return reactions;
	}

	public static AuditLog getAuditLogFromJSON(IGuild guild, AuditLogObject json) {
		LongMap<IUser> users = Arrays.stream(json.users)
				.map(u -> DiscordUtils.getUserFromJSON(guild.getShard(), u))
				.collect(LongMapCollector.toLongMap());

		LongMap<IWebhook> webhooks = Arrays.stream(json.webhooks)
				.map(w -> DiscordUtils.getWebhookFromJSON(guild.getChannelByID(Long.parseUnsignedLong(w.channel_id)), w))
				.collect(LongMapCollector.toLongMap());

		LongMap<AuditLogEntry> entries = Arrays.stream(json.audit_log_entries)
				.map(e -> DiscordUtils.getAuditLogEntryFromJSON(guild, users, webhooks, e))
				.collect(LongMapCollector.toLongMap());

		return new AuditLog(entries);
	}

	public static AuditLogEntry getAuditLogEntryFromJSON(IGuild guild, LongMap<IUser> users, LongMap<IWebhook> webhooks, AuditLogEntryObject json) {
		long targetID = json.target_id == null ? 0 : Long.parseUnsignedLong(json.target_id);
		long id = Long.parseUnsignedLong(json.id);
		IUser user = users.get(Long.parseUnsignedLong(json.user_id));

		ChangeMap changes = json.changes == null ? new ChangeMap() : Arrays.stream(json.changes).collect(ChangeMap.Collector.toChangeMap());

		OptionMap options = new OptionMap(json.options);

		ActionType actionType = ActionType.fromRaw(json.action_type);
		switch (actionType) {
			case GUILD_UPDATE:
				return new DiscordObjectEntry<>(guild, id, user, changes, json.reason, actionType, options);
			case CHANNEL_CREATE:
			case CHANNEL_UPDATE:
			case CHANNEL_OVERWRITE_CREATE:
			case CHANNEL_OVERWRITE_UPDATE:
			case CHANNEL_OVERWRITE_DELETE:
				IChannel channel = guild.getChannelByID(targetID);
				if (channel == null) channel = guild.getVoiceChannelByID(targetID);

				if (channel == null) {
					return new TargetedEntry(id, user, changes, json.reason, actionType, options, targetID);
				}
				return new DiscordObjectEntry<>(channel, id, user, changes, json.reason, actionType, options);
			case MEMBER_KICK:
			case MEMBER_BAN_ADD:
			case MEMBER_BAN_REMOVE:
			case MEMBER_UPDATE:
			case MEMBER_ROLE_UPDATE:
			case MESSAGE_DELETE: // message delete target is the author of the message
				IUser target = users.get(targetID);

				if (target == null) {
					return new TargetedEntry(id, user, changes, json.reason, actionType, options, targetID);
				}
				return new DiscordObjectEntry<>(target, id, user, changes, json.reason, actionType, options);
			case ROLE_CREATE:
			case ROLE_UPDATE:
				IRole role = guild.getRoleByID(targetID);

				if (role == null) {
					return new TargetedEntry(id, user, changes, json.reason, actionType, options, targetID);
				}
				return new DiscordObjectEntry<>(role, id, user, changes, json.reason, actionType, options);
			case WEBHOOK_CREATE:
			case WEBHOOK_UPDATE:
				IWebhook webhook = webhooks.get(targetID);

				if (webhook == null) {
					return new TargetedEntry(id, user, changes, json.reason, actionType, options, targetID);
				}
				return new DiscordObjectEntry<>(webhook, id, user, changes, json.reason, actionType, options);
			case EMOJI_CREATE:
			case EMOJI_UPDATE:
				IEmoji emoji = guild.getEmojiByID(targetID);

				if (emoji == null) {
					return new TargetedEntry(id, user, changes, json.reason, actionType, options, targetID);
				}
				return new DiscordObjectEntry<>(emoji, id, user, changes, json.reason, actionType, options);
			case CHANNEL_DELETE:
			case ROLE_DELETE:
			case WEBHOOK_DELETE:
			case EMOJI_DELETE:
				return new TargetedEntry(id, user, changes, json.reason, actionType, options, targetID);
			case INVITE_CREATE:
			case INVITE_DELETE:
			case INVITE_UPDATE:
			case MEMBER_PRUNE:
				return new AuditLogEntry(id, user, changes, json.reason, actionType, options);
		}

		return null;
	}

	/**
	 * Gets the time at which a discord id was created.
	 *
	 * @param id The id.
	 * @return The time the id was created.
	 */
	public static LocalDateTime getSnowflakeTimeFromID(long id) {
		long milliseconds = DISCORD_EPOCH + (id >>> 22);
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

		if (((IDiscordObject) a).getLongID() != ((IDiscordObject) b).getLongID()) return false;

		return true;
	}
}
