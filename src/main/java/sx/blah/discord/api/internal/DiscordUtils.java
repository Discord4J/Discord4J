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
import sx.blah.discord.handle.audit.ActionType;
import sx.blah.discord.handle.audit.AuditLog;
import sx.blah.discord.handle.audit.entry.AuditLogEntry;
import sx.blah.discord.handle.audit.entry.DiscordObjectEntry;
import sx.blah.discord.handle.audit.entry.TargetedEntry;
import sx.blah.discord.handle.audit.entry.change.ChangeMap;
import sx.blah.discord.handle.audit.entry.option.OptionMap;
import sx.blah.discord.handle.impl.obj.*;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.LogMarkers;
import sx.blah.discord.util.LongMapCollector;
import sx.blah.discord.util.RequestBuilder;
import sx.blah.discord.util.cache.Cache;
import sx.blah.discord.util.cache.LongMap;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.awt.Color;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicReference;
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
	 * The unix time that represents Discord's epoch. (January 1, 2015).
	 */
	public static final long DISCORD_EPOCH = 1420070400000L;

	/**
	 * Pattern for Discord's custom emoji.
	 */
	public static final Pattern CUSTOM_EMOJI_PATTERN = Pattern.compile("<?:[A-Za-z_0-9]+:\\d+>?");

	/**
 	 * Pattern for naming Discord's custom emoji.
 	 */
 	public static final Pattern EMOJI_NAME_PATTERN = Pattern.compile("([A-Za-z0-9_]{2,32})");

	/**
	 * Pattern for Discord's emoji aliases (e.g. :heart: or :thinking:).
	 */
	public static final Pattern EMOJI_ALIAS_PATTERN = Pattern.compile(":.+:");

	/**
	 * Pattern for Discord's nsfw channel name indicator.
	 */
	public static final Pattern NSFW_CHANNEL_PATTERN = Pattern.compile("^nsfw(-|$)");

	/**
	 * Pattern for Discord's valid streaming URL strings passed to {@link IShard#streaming(String, String)}.
	 */
	public static final Pattern STREAM_URL_PATTERN = Pattern.compile("https?://(www\\.)?twitch\\.tv/.+");

	/**
	 * Pattern for Discord's valid channel names.
	 */
	public static final Pattern CHANNEL_NAME_PATTERN = Pattern.compile("^[a-z0-9-_[^\\p{ASCII}]]{2,100}$");

	/**
	 * Gets a snowflake from a unix timestamp.
	 * <p>
	 * This snowflake only contains accurate information about the timestamp (not about other parts of the snowflake).
	 * The returned snowflake is only one of many that could exist at the given timestamp.
	 *
	 * @param date The date that should be converted to a unix timestamp for use in the snowflake.
	 * @return A snowflake with the given timestamp.
	 */
	public static long getSnowflakeFromTimestamp(Instant date) {
		return (date.toEpochMilli() - DISCORD_EPOCH) << 22;
	}

	/**
	 * Converts a String timestamp into a {@link Instant}.
	 *
	 * @param time The string timestamp.
	 * @return The LocalDateTime representing the timestamp.
	 */
	public static Instant convertFromTimestamp(String time) {
		return time == null ? Instant.now() : ZonedDateTime.parse(time).toInstant();
	}

	/**
	 * Converts a json {@link UserObject} to a {@link User}. This method first checks the internal user cache and returns
	 * that object with updated information if it exists. Otherwise, it constructs a new user.
	 *
	 * @param shard The shard the user belongs to.
	 * @param response The json object representing the user.
	 * @return The converted user object.
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
					new Presence(null, null, StatusType.OFFLINE, ActivityType.PLAYING), response.bot);
		}
		return user;
	}

	/**
	 * Converts a json {@link InviteObject} to an {@link IInvite}.
	 *
	 * @param client The client the invite belongs to.
	 * @param json   The json object representing the invite.
	 * @return The converted invite object.
	 */
	public static IInvite getInviteFromJSON(IDiscordClient client, InviteObject json) {
		return new Invite(client, json);
	}

	/**
	 * Converts a json {@link ExtendedInviteObject} to an {@link IExtendedInvite}.
	 *
	 * @param client The client the invite belongs to.
	 * @param json   The json object representing the invite.
	 * @return The converted extended invite object.
	 */
	public static IExtendedInvite getExtendedInviteFromJSON(IDiscordClient client, ExtendedInviteObject json) {
		return new ExtendedInvite(client, json);
	}

	/**
	 * Gets the users mentioned in a message.
	 *
	 * @param json The json response to use.
	 * @return The list of IDs of mentioned users.
	 */
	public static List<Long> getMentionsFromJSON(MessageObject json) {
		List<Long> mentions = new ArrayList<>();
		if (json.mentions != null)
			for (UserObject response : json.mentions)
				mentions.add(Long.parseUnsignedLong(response.id));

		return mentions;
	}

	/**
	 * Gets the roles mentioned in a message.
	 *
	 * @param json The json response to use.
	 * @return The list IDs of mentioned roles.
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
	 * @return The attachments.
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
	 * Gets the embeds on a message.
	 *
	 * @param json The json response to use.
	 * @return The embeds.
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
	 * Converts a json {@link GuildObject} to a {@link IGuild}. This method first checks the internal guild cache and returns
	 * that object with updated information if it exists. Otherwise, it constructs a new guild.
	 *
	 * @param shard The shard the guild belongs to.
	 * @param json The json object representing the guild.
	 * @return The converted guild object.
	 */
	public static IGuild getGuildFromJSON(IShard shard, GuildObject json) {
		Guild guild;

		long guildId = Long.parseUnsignedLong(json.id);
		long systemChannelId = json.system_channel_id == null ? 0L : Long.parseUnsignedLong(json.system_channel_id);

		if ((guild = (Guild) shard.getGuildByID(guildId)) != null) {
			guild.setIcon(json.icon);
			guild.setName(json.name);
			guild.setOwnerID(Long.parseUnsignedLong(json.owner_id));
			guild.setAFKChannel(json.afk_channel_id == null ? 0 : Long.parseUnsignedLong(json.afk_channel_id));
			guild.setAfkTimeout(json.afk_timeout);
			guild.setRegionID(json.region);
			guild.setVerificationLevel(json.verification_level);
			guild.setTotalMemberCount(json.member_count);
			guild.setSystemChannelId(systemChannelId);

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
					json.afk_channel_id == null ? 0 : Long.parseUnsignedLong(json.afk_channel_id), json.afk_timeout,
					json.region, json.verification_level, systemChannelId);

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
					} else if (channelJSON.type == ChannelObject.Type.GUILD_CATEGORY) {
						guild.categories.put(DiscordUtils.getCategoryFromJSON(shard, guild, channelJSON));
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
	 * Converts a json {@link MemberObject} to a {@link IUser}. This method uses {@link #getUserFromJSON(IShard, UserObject)}
	 * to get or create a {@link IUser} and then updates the guild's appropriate member caches for that user.
	 *
	 * @param guild The guild the member belongs to.
	 * @param json The json object representing the member.
	 * @return The converted user object.
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
	 * Converts a json {@link MessageObject} to a {@link IMessage}. This method first checks the internal message cache
	 * and returns that object with updated information if it exists. Otherwise, it constructs a new message.
	 *
	 * @param channel The channel the message belongs to.
	 * @param json The json object representing the message.
	 * @return The converted message object.
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

			IMessage.Type type = Arrays.stream(IMessage.Type.values())
					.filter(t -> t.getValue() == json.type)
					.findFirst()
					.orElse(IMessage.Type.UNKNOWN);

			Message message = new Message(channel.getClient(), Long.parseUnsignedLong(json.id), json.content,
					author, channel, convertFromTimestamp(json.timestamp),
					json.edited_timestamp == null ? null : convertFromTimestamp(json.edited_timestamp),
					json.mention_everyone, getMentionsFromJSON(json), getRoleMentionsFromJSON(json),
					getAttachmentsFromJSON(json), Boolean.TRUE.equals(json.pinned), getEmbedsFromJSON(json),
					json.webhook_id != null ? Long.parseUnsignedLong(json.webhook_id) : 0, type);
			message.setReactions(getReactionsFromJSON(message, json.reactions));

			return message;
		}
	}

	/**
	 * Updates a {@link IMessage} object with the non-null or non-empty contents of a json {@link MessageObject}.
	 *
	 * @param client The client this message belongs to.
	 * @param toUpdate The message to update.
	 * @param json The json object representing the message.
	 * @return The updated message object.
	 */
	public static IMessage getUpdatedMessageFromJSON(IDiscordClient client, IMessage toUpdate, MessageObject json) {
		if (toUpdate == null) {
			Channel channel = (Channel) client.getChannelByID(Long.parseUnsignedLong(json.channel_id));
			return channel == null ? null : channel.getMessageByID(Long.parseUnsignedLong(json.id));
		}

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
	 * Converts a json {@link WebhookObject} to a {@link IWebhook}. This method first checks the internal webhook cache
	 * and returns that object with updated information if it exists. Otherwise, it constructs a new webhook.
	 *
	 * @param channel The channel the webhook belongs to.
	 * @param json The json object representing the webhook.
	 * @return The converted webhook object.
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
	 * Converts a json {@link ChannelObject} to a {@link IChannel}. This method first checks the internal channel cache
	 * and returns that object with updated information if it exists. Otherwise, it constructs a new channel.
	 *
	 * @param shard The shard the channel belongs to.
	 * @param json The json object representing the channel.
	 * @return The converted channel object.
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
			Pair<Cache<PermissionOverride>, Cache<PermissionOverride>> overrides =
					getPermissionOverwritesFromJSONs(client, json.permission_overwrites);
			long categoryID = json.parent_id == null ? 0L : Long.parseUnsignedLong(json.parent_id);

			if (channel != null) {
				channel.setName(json.name);
				channel.setPosition(json.position);
				channel.setNSFW(json.nsfw);
				channel.userOverrides.clear();
				channel.roleOverrides.clear();
				channel.userOverrides.putAll(overrides.getLeft());
				channel.roleOverrides.putAll(overrides.getRight());
				channel.setCategoryID(categoryID);

				if (json.type == ChannelObject.Type.GUILD_TEXT) {
					channel.setTopic(json.topic);
				} else {
					VoiceChannel vc = (VoiceChannel) channel;
					vc.setUserLimit(json.user_limit);
					vc.setBitrate(json.bitrate);
				}
			} else if (json.type == ChannelObject.Type.GUILD_TEXT) {
				channel = new Channel(client, json.name, id, guild, json.topic, json.position, json.nsfw, categoryID,
						overrides.getRight(), overrides.getLeft());
			} else if (json.type == ChannelObject.Type.GUILD_VOICE) {
				channel = new VoiceChannel(client, json.name, id, guild, json.topic, json.position, json.nsfw,
						json.user_limit, json.bitrate, categoryID, overrides.getRight(), overrides.getLeft());
			}
		}

		return channel;
	}

	/**
	 * Converts an array of json {@link OverwriteObject}s to sets of user and role overrides.
	 *
	 * @param overwrites The array of json overwrite objects.
	 * @return A pair representing the overwrites per id; left value = user overrides and right value = role overrides.
	 */
	public static Pair<Cache<PermissionOverride>, Cache<PermissionOverride>>
	getPermissionOverwritesFromJSONs(DiscordClientImpl client, OverwriteObject[] overwrites) {
		Cache<PermissionOverride> userOverrides = new Cache<>(client, PermissionOverride.class);
		Cache<PermissionOverride> roleOverrides = new Cache<>(client, PermissionOverride.class);

		for (OverwriteObject overrides : overwrites) {
			if (overrides.type.equalsIgnoreCase("role")) {
				roleOverrides.put(new PermissionOverride(Permissions.getAllowedPermissionsForNumber(overrides.allow),
								Permissions.getDeniedPermissionsForNumber(overrides.deny), Long.parseUnsignedLong(overrides.id)));
			} else if (overrides.type.equalsIgnoreCase("member")) {
				userOverrides.put(new PermissionOverride(Permissions.getAllowedPermissionsForNumber(overrides.allow),
								Permissions.getDeniedPermissionsForNumber(overrides.deny), Long.parseUnsignedLong(overrides.id)));
			} else {
				Discord4J.LOGGER.warn(LogMarkers.API, "Unknown permissions overwrite type \"{}\"!", overrides.type);
			}
		}

		return Pair.of(userOverrides, roleOverrides);
	}

	/**
	 * Converts a json {@link RoleObject} to a {@link IRole}. This method first checks the internal role cache
	 * and returns that object with updated information if it exists. Otherwise, it constructs a new role.
	 *
	 * @param guild The guild the role belongs to.
	 * @param json The json object representing the role.
	 * @return The converted role object.
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
	 * Converts a json {@link VoiceRegionObject} to an {@link IRegion}.
	 *
	 * @param json The json object representing the region.
	 * @return The converted region object.
	 */
	public static IRegion getRegionFromJSON(VoiceRegionObject json) {
		return new Region(json.id, json.name, json.vip);
	}

	/**
	 * Converts a json {@link VoiceStateObject} to a {@link IVoiceState}.
	 *
	 * @param guild The guild the voice state is in.
	 * @param json The json object representing the voice state.
	 * @return The converted voice state object.
	 */
	public static IVoiceState getVoiceStateFromJson(IGuild guild, VoiceStateObject json) {
		IVoiceChannel channel = json.channel_id != null ? guild.getVoiceChannelByID(Long.parseUnsignedLong(json.channel_id)) : null;
		return new VoiceState(guild, channel, guild.getUserByID(Long.parseUnsignedLong(json.user_id)),
				json.session_id, json.deaf, json.mute, json.self_deaf, json.self_mute, json.suppress);
	}

	/**
	 * Converts a json {@link PresenceObject} to a {@link IPresence}.
	 *
	 * @param presence The json object representing the presence.
	 * @return The converted presence object.
	 */
	public static IPresence getPresenceFromJSON(PresenceObject presence) {
		return getPresenceFromJSON(presence.game, presence.status);
	}

	/**
	 * Converts a json {@link PresenceUpdateEventResponse} to a {@link IPresence}.
	 *
	 * @param response The event response with presence information.
	 * @return The converted presence object.
	 */
	public static IPresence getPresenceFromJSON(PresenceUpdateEventResponse response) {
		return getPresenceFromJSON(response.game, response.status);
	}

	/**
	 * Creates a {@link IPresence} from a {@link GameObject game} and status type.
	 *
	 * @param game The game of the presence.
	 * @param status The status of the presence.
	 * @return The presence object with the given game and status.
	 */
	private static IPresence getPresenceFromJSON(GameObject game, String status) {
		return new Presence(
				game == null ? null : game.name,
				game == null ? null : game.url,
				StatusType.get(status),
				game == null ? ActivityType.PLAYING : ActivityType.values()[game.type]);
	}

	/**
	 * Converts a json {@link EmojiObject} to a {@link IEmoji}.
	 *
	 * @param guild The guild the emoji belongs to.
	 * @param json The json object representing the emoji.
	 * @return The converted emoji object.
	 */
	public static IEmoji getEmojiFromJSON(IGuild guild, EmojiObject json) {
		long id = Long.parseUnsignedLong(json.id);
		List<IRole> roles = Arrays.stream(json.roles)
				.map(role -> guild.getRoleByID(Long.parseUnsignedLong(role)))
				.collect(Collectors.toList());

		EmojiImpl emoji = (EmojiImpl) guild.getEmojiByID(id);
		if (emoji != null) {
			emoji.setName(json.name);
			emoji.setRoles(roles);
			return emoji;
		}

		Cache<IRole> roleCache = new Cache<>((DiscordClientImpl) guild.getClient(), IRole.class);
		roleCache.putAll(roles);
		return new EmojiImpl(id, guild, json.name, roleCache, json.require_colons, json.managed, json.animated);
	}

	/**
	 * Converts an array of json {@link MessageObject.ReactionObject}s to a list of {@link IReaction}s.
	 *
	 * @param message The message the reactions belong to.
	 * @param json The json objects representing the reactions.
	 * @return The converted reaction objects.
	 */
	public static List<IReaction> getReactionsFromJSON(IMessage message, MessageObject.ReactionObject[] json) {
		List<IReaction> reactions = new CopyOnWriteArrayList<>();
		if (json != null)
			for (MessageObject.ReactionObject object : json) {
				long id = object.emoji.id == null ? 0 : Long.parseUnsignedLong(object.emoji.id);
				ReactionEmoji emoji = ReactionEmoji.of(object.emoji.name, id, object.emoji.animated);
				reactions.add(new Reaction(message, object.count, emoji));
			}

		return reactions;
	}

	/**
	 * Converts a json {@link AuditLogObject} to a {@link AuditLog}.
	 *
	 * @param guild The guild the audit log belongs to.
	 * @param json The json object representing the audit log.
	 * @return The converted audit log object.
	 */
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

	/**
	 * Converts a json {@link AuditLogEntry} to a {@link AuditLogEntry}.
	 *
	 * @param guild The guild the entry belongs to.
	 * @param users The users of the parent audit log.
	 * @param webhooks The webhooks of the parent audit log.
	 * @param json The converted audit log entry object.
	 * @return The converted audit log entry.
	 */
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

	public static ICategory getCategoryFromJSON(IShard shard, IGuild guild, ChannelObject json) {
		Pair<Cache<PermissionOverride>, Cache<PermissionOverride>> permissionOverwrites =
				getPermissionOverwritesFromJSONs((DiscordClientImpl) shard.getClient(), json.permission_overwrites);

		Category category = (Category) shard.getCategoryByID(Long.parseUnsignedLong(json.id));
		if (category != null) {
			category.setName(json.name);
			category.setPosition(json.position);
			category.setNSFW(json.nsfw);
			category.userOverrides.clear();
			category.roleOverrides.clear();
			category.userOverrides.putAll(permissionOverwrites.getLeft());
			category.roleOverrides.putAll(permissionOverwrites.getRight());

		} else {
			category = new Category(shard, json.name, Long.parseUnsignedLong(json.id), guild, json.position, json.nsfw,
					permissionOverwrites.getLeft(), permissionOverwrites.getRight());
		}

		return category;
	}

	/**
	 * Gets the timestamp portion of a Snowflake ID as a {@link Instant} using the system's default timezone.
	 *
	 * @param id The Snowflake ID.
	 * @return The timestamp portion of the ID.
	 */
	public static Instant getSnowflakeTimeFromID(long id) {
		return Instant.ofEpochMilli(DISCORD_EPOCH + (id >>> 22));
	}

	/**
	 * Converts an {@link AudioInputStream} to 48000Hz 16 bit stereo signed Big Endian PCM format.
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
	 * Creates a {@link ThreadFactory} which produces threads which run as daemons.
	 *
	 * @return The new daemon thread factory.
	 */
	public static ThreadFactory createDaemonThreadFactory() {
		return createDaemonThreadFactory(null);
	}

	/**
	 * This creates a {@link ThreadFactory} which produces threads which run as daemons.
	 *
	 * @param threadName The name of threads created by the returned factory.
	 * @return The new daemon thread factory.
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
	 * Checks equality between two {@link IDiscordObject}s using their IDs.
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
