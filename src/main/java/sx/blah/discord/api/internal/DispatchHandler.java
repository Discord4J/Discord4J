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

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.builder.ToStringBuilder;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.internal.json.event.*;
import sx.blah.discord.api.internal.json.objects.*;
import sx.blah.discord.api.internal.json.requests.GuildMembersRequest;
import sx.blah.discord.api.internal.json.responses.ReadyResponse;
import sx.blah.discord.api.internal.json.responses.voice.VoiceUpdateResponse;
import sx.blah.discord.handle.impl.events.guild.*;
import sx.blah.discord.handle.impl.events.guild.category.CategoryCreateEvent;
import sx.blah.discord.handle.impl.events.guild.category.CategoryDeleteEvent;
import sx.blah.discord.handle.impl.events.guild.category.CategoryUpdateEvent;
import sx.blah.discord.handle.impl.events.guild.channel.*;
import sx.blah.discord.handle.impl.events.guild.channel.message.*;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionAddEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionRemoveEvent;
import sx.blah.discord.handle.impl.events.guild.member.*;
import sx.blah.discord.handle.impl.events.guild.role.RoleCreateEvent;
import sx.blah.discord.handle.impl.events.guild.role.RoleDeleteEvent;
import sx.blah.discord.handle.impl.events.guild.role.RoleUpdateEvent;
import sx.blah.discord.handle.impl.events.guild.voice.VoiceChannelCreateEvent;
import sx.blah.discord.handle.impl.events.guild.voice.VoiceChannelDeleteEvent;
import sx.blah.discord.handle.impl.events.guild.voice.VoiceChannelUpdateEvent;
import sx.blah.discord.handle.impl.events.guild.voice.VoiceDisconnectedEvent;
import sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelJoinEvent;
import sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelLeaveEvent;
import sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelMoveEvent;
import sx.blah.discord.handle.impl.events.shard.LoginEvent;
import sx.blah.discord.handle.impl.events.shard.ResumedEvent;
import sx.blah.discord.handle.impl.events.shard.ShardReadyEvent;
import sx.blah.discord.handle.impl.events.user.PresenceUpdateEvent;
import sx.blah.discord.handle.impl.events.user.UserUpdateEvent;
import sx.blah.discord.handle.impl.obj.*;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.LogMarkers;
import sx.blah.discord.util.PermissionUtils;
import sx.blah.discord.util.RequestBuilder;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static sx.blah.discord.api.internal.DiscordUtils.MAPPER;

/**
 * Handles {@link GatewayOps#DISPATCH} payloads on the Gateway.
 */
class DispatchHandler {
	/**
	 * The associated websocket connection.
	 */
	private DiscordWS ws;
	/**
	 * The associated shard.
	 */
	private ShardImpl shard;
	/**
	 * The associated client.
	 */
	private DiscordClientImpl client;
	/**
	 * The thread on which every payload is handled.
	 */
	private final ExecutorService dispatchExecutor = new ThreadPoolExecutor(2, Runtime.getRuntime().availableProcessors() * 4, 60L,
			TimeUnit.SECONDS, new SynchronousQueue<>(false),
			DiscordUtils.createDaemonThreadFactory("Dispatch Handler"), new ThreadPoolExecutor.CallerRunsPolicy());
	/**
	 * Lock used to synchronize initialization
	 */
	private final Lock startupLock = new ReentrantLock(true);

	DispatchHandler(DiscordWS ws, ShardImpl shard) {
		this.ws = ws;
		this.shard = shard;
		this.client = (DiscordClientImpl) shard.getClient();
	}

	/**
	 * Deserializes the given payload and passes it to the appropriate method depending on the event name.
	 *
	 * @param event The json payload.
	 */
	public void handle(final JsonNode event) {
		dispatchExecutor.submit(() -> {
			boolean locked = false;
			if (!client.isReady()) {
				startupLock.lock();
				locked = true;
			}
			try {
				String type = event.get("t").asText();
				JsonNode json = event.get("d");
				switch (type) {
					case "RESUMED":
						resumed();
						break;
					case "READY":
						ready(MAPPER.treeToValue(json, ReadyResponse.class));
						break;
					case "MESSAGE_CREATE":
						messageCreate(MAPPER.treeToValue(json, MessageObject.class));
						break;
					case "TYPING_START":
						typingStart(MAPPER.treeToValue(json, TypingEventResponse.class));
						break;
					case "GUILD_CREATE":
						guildCreate(MAPPER.treeToValue(json, GuildObject.class));
						break;
					case "GUILD_MEMBER_ADD":
						guildMemberAdd(MAPPER.treeToValue(json, GuildMemberAddEventResponse.class));
						break;
					case "GUILD_MEMBER_REMOVE":
						guildMemberRemove(MAPPER.treeToValue(json, GuildMemberRemoveEventResponse.class));
						break;
					case "GUILD_MEMBER_UPDATE":
						guildMemberUpdate(MAPPER.treeToValue(json, GuildMemberUpdateEventResponse.class));
						break;
					case "MESSAGE_UPDATE":
						messageUpdate(MAPPER.treeToValue(json, MessageObject.class));
						break;
					case "MESSAGE_DELETE":
						messageDelete(MAPPER.treeToValue(json, MessageDeleteEventResponse.class));
						break;
					case "MESSAGE_DELETE_BULK":
						messageDeleteBulk(MAPPER.treeToValue(json, MessageDeleteBulkEventResponse.class));
						break;
					case "PRESENCE_UPDATE":
						presenceUpdate(MAPPER.treeToValue(json, PresenceUpdateEventResponse.class));
						break;
					case "GUILD_DELETE":
						guildDelete(MAPPER.treeToValue(json, GuildObject.class));
						break;
					case "CHANNEL_CREATE":
						channelCreate(MAPPER.treeToValue(json, ChannelObject.class));
						break;
					case "CHANNEL_DELETE":
						channelDelete(MAPPER.treeToValue(json, ChannelObject.class));
						break;
					case "CHANNEL_PINS_UPDATE": /* Implemented in MESSAGE_UPDATE. Ignored */
						break;
					case "CHANNEL_PINS_ACK": /* Ignored */
						break;
					case "USER_UPDATE":
						userUpdate(MAPPER.treeToValue(json, UserUpdateEventResponse.class));
						break;
					case "CHANNEL_UPDATE":
						channelUpdate(MAPPER.treeToValue(json, ChannelObject.class));
						break;
					case "GUILD_MEMBERS_CHUNK":
						guildMembersChunk(MAPPER.treeToValue(json, GuildMemberChunkEventResponse.class));
						break;
					case "GUILD_UPDATE":
						guildUpdate(MAPPER.treeToValue(json, GuildObject.class));
						break;
					case "GUILD_ROLE_CREATE":
						guildRoleCreate(MAPPER.treeToValue(json, GuildRoleEventResponse.class));
						break;
					case "GUILD_ROLE_UPDATE":
						guildRoleUpdate(MAPPER.treeToValue(json, GuildRoleEventResponse.class));
						break;
					case "GUILD_ROLE_DELETE":
						guildRoleDelete(MAPPER.treeToValue(json, GuildRoleDeleteEventResponse.class));
						break;
					case "GUILD_BAN_ADD":
						guildBanAdd(MAPPER.treeToValue(json, GuildBanEventResponse.class));
						break;
					case "GUILD_BAN_REMOVE":
						guildBanRemove(MAPPER.treeToValue(json, GuildBanEventResponse.class));
						break;
					case "GUILD_EMOJIS_UPDATE":
						guildEmojisUpdate(MAPPER.treeToValue(json, GuildEmojiUpdateResponse.class));
						break;
					case "GUILD_INTEGRATIONS_UPDATE": /* TODO: Impl Guild integrations */
						break;
					case "VOICE_STATE_UPDATE":
						voiceStateUpdate(MAPPER.treeToValue(json, VoiceStateObject.class));
						break;
					case "VOICE_SERVER_UPDATE":
						voiceServerUpdate(MAPPER.treeToValue(json, VoiceUpdateResponse.class));
						break;
					case "MESSAGE_REACTION_ADD":
						reactionAdd(MAPPER.treeToValue(json, ReactionEventResponse.class));
						break;
					case "MESSAGE_REACTION_REMOVE":
						reactionRemove(MAPPER.treeToValue(json, ReactionEventResponse.class));
						break;
					case "MESSAGE_REACTION_REMOVE_ALL": /* REMOVE_ALL is 204 empty but REACTION_REMOVE is sent anyway */
						break;
					case "WEBHOOKS_UPDATE":
						webhookUpdate(MAPPER.treeToValue(json, WebhookObject.class));
						break;
					case "PRESENCES_REPLACE": /* Ignored. Not meant for bot accounts. */
						break;

					default:
						Discord4J.LOGGER.warn(LogMarkers.WEBSOCKET, "Unknown message received: {}, REPORT THIS TO THE DISCORD4J DEV!", type);
				}
			} catch (Exception e) {
				Discord4J.LOGGER.error(LogMarkers.WEBSOCKET, "Unable to process JSON!", e);
			} finally {
				if (locked)
					startupLock.unlock();
			}
		});
	}

	private void ready(ReadyResponse ready) {
		Discord4J.LOGGER.info(LogMarkers.WEBSOCKET, "Connected to Discord Gateway v{}. Receiving {} guilds.", ready.v, ready.guilds.length);

		ws.state = DiscordWS.State.READY;
		ws.hasReceivedReady = true; // Websocket received actual ready event
		if (client.ourUser == null) client.ourUser = DiscordUtils.getUserFromJSON(shard, ready.user);
		client.getDispatcher().dispatch(new LoginEvent(shard));

		new RequestBuilder(client).setAsync(true).doAction(() -> {
			ws.sessionId = ready.session_id;

			Set<UnavailableGuildObject> waitingGuilds = ConcurrentHashMap.newKeySet(ready.guilds.length);
			waitingGuilds.addAll(Arrays.asList(ready.guilds));

			final AtomicInteger loadedGuilds = new AtomicInteger(0);
			client.getDispatcher().waitFor((GuildCreateEvent e) -> {
				waitingGuilds.removeIf(g -> g.id.equals(e.getGuild().getStringID()));
				return loadedGuilds.incrementAndGet() >= ready.guilds.length;
			}, (long) Math.ceil(Math.sqrt(2 * ready.guilds.length)), TimeUnit.SECONDS);

			waitingGuilds.forEach(guild -> client.getDispatcher().dispatch(new GuildUnavailableEvent(Long.parseUnsignedLong(guild.id))));
			return true;
		}).andThen(() -> {
			if (this.shard.getInfo()[0] == 0) { // pms are only sent to shard 0
				for (ChannelObject pmObj : ready.private_channels) {
					IPrivateChannel pm = (IPrivateChannel) DiscordUtils.getChannelFromJSON(shard, null, pmObj);
					shard.privateChannels.put(pm);
				}
			}

			ws.isReady = true;
			client.getDispatcher().dispatch(new ShardReadyEvent(shard)); // All information for this shard has been received
			return true;
		}).execute();
	}

	private void resumed() {
		Discord4J.LOGGER.info(LogMarkers.WEBSOCKET, "Session resumed on shard " + shard.getInfo()[0]);
		ws.hasReceivedReady = true; // Technically a lie but irrelevant in the case of a resume.
		ws.isReady = true;          //
		client.getDispatcher().dispatch(new ResumedEvent(shard));
	}

	private void messageCreate(MessageObject json) {
		boolean mentioned = json.mention_everyone;

		Channel channel = (Channel) client.getChannelByID(Long.parseUnsignedLong(json.channel_id));

		if (null != channel) {

			// check if our user is mentioned directly
			if (!mentioned) { //Not worth checking if already mentioned
				for (UserObject user : json.mentions) { //Check mention array for a mention
					if (client.getOurUser().getLongID() == Long.parseUnsignedLong(user.id)) {
						mentioned = true;
						break;
					}
				}
			}

			// check if our user is mentioned through role mentions
			if (!mentioned) { //Not worth checking if already mentioned
				for (String role : json.mention_roles) { //Check roles for a mention
					if (client.getOurUser().getRolesForGuild(channel.getGuild()).contains(channel.getGuild().getRoleByID(Long.parseUnsignedLong(role)))) {
						mentioned = true;
						break;
					}
				}
			}

			IMessage message = DiscordUtils.getMessageFromJSON(channel, json);

			if (!channel.getMessageHistory().contains(message)) {
				Discord4J.LOGGER.debug(LogMarkers.MESSAGES, "Message from: {} ({}) in channel ID {}: {}", message.getAuthor().getName(),
						json.author.id, json.channel_id, json.content);

				if (mentioned) {
					client.dispatcher.dispatch(new MentionEvent(message));
				}

				channel.addToCache(message);

				if (message.getAuthor().equals(client.getOurUser())) {
					client.dispatcher.dispatch(new MessageSendEvent(message));
					message.getChannel().setTypingStatus(false); //Messages being sent should stop the bot from typing
				} else {
					client.dispatcher.dispatch(new MessageReceivedEvent(message));
					if (!message.getEmbeds().isEmpty()) {
						client.dispatcher.dispatch(new MessageEmbedEvent(null, message, new ArrayList<>()));
					}
				}
			}
		}
	}

	private void typingStart(TypingEventResponse event) {
		User user;
		Channel channel = (Channel) client.getChannelByID(Long.parseUnsignedLong(event.channel_id));
		if (channel != null) {
			if (channel.isPrivate()) {
				user = (User) ((IPrivateChannel) channel).getRecipient();
			} else {
				user = (User) channel.getGuild().getUserByID(Long.parseUnsignedLong(event.user_id));
			}

			if (user != null) {
				client.dispatcher.dispatch(new TypingEvent(user, channel));
			}
		}
	}

	private void guildCreate(GuildObject json) {
		if (json.unavailable) { //Guild can't be reached, so we ignore it
			Discord4J.LOGGER.warn(LogMarkers.WEBSOCKET, "Guild with id {} is unavailable, ignoring it. Is there an outage?", json.id);
			return;
		}

		Guild guild = (Guild) DiscordUtils.getGuildFromJSON(shard, json);
		shard.guildCache.put(guild);

		new RequestBuilder(client).setAsync(true).doAction(() -> {
			try {
				if (json.large) {
					shard.ws.send(GatewayOps.REQUEST_GUILD_MEMBERS, new GuildMembersRequest(json.id));
					client.getDispatcher().waitFor((AllUsersReceivedEvent e) ->
							e.getGuild().getLongID() == guild.getLongID()
					);
				}
			} catch (InterruptedException e) {
				Discord4J.LOGGER.error(LogMarkers.EVENTS, "Wait for AllUsersReceivedEvent on guild create was interrupted.", e);
			}
			return true;
		}).andThen(() -> {
			guild.loadWebhooks();
			client.dispatcher.dispatch(new GuildCreateEvent(guild));
			Discord4J.LOGGER.debug(LogMarkers.EVENTS, "New guild has been created/joined! \"{}\" with ID {} on shard {}.", guild.getName(), guild.getStringID(), shard.getInfo()[0]);
			return true;
		}).execute();
	}

	private void guildMemberAdd(GuildMemberAddEventResponse event) {
		long guildID = Long.parseUnsignedLong(event.guild_id);
		Guild guild = (Guild) client.getGuildByID(guildID);
		if (guild != null) {
			User user = (User) DiscordUtils.getUserFromGuildMemberResponse(guild, new MemberObject(event.user, event.roles));
			guild.users.put(user);
			guild.setTotalMemberCount(guild.getTotalMemberCount() + 1);
			Instant timestamp = DiscordUtils.convertFromTimestamp(event.joined_at);
			Discord4J.LOGGER.debug(LogMarkers.EVENTS, "User \"{}\" joined guild \"{}\".", user.getName(), guild.getName());
			client.dispatcher.dispatch(new UserJoinEvent(guild, user, timestamp));
		}
	}

	private void guildMemberRemove(GuildMemberRemoveEventResponse event) {
		long guildID = Long.parseUnsignedLong(event.guild_id);
		Guild guild = (Guild) client.getGuildByID(guildID);
		if (guild != null) {
			User user = (User) guild.getUserByID(Long.parseUnsignedLong(event.user.id));
			if (user != null) {
				guild.users.remove(user);
				guild.joinTimes.remove(user);
				user.roles.remove(guild);
				guild.setTotalMemberCount(guild.getTotalMemberCount() - 1);
				Discord4J.LOGGER.debug(LogMarkers.EVENTS, "User \"{}\" has been removed from or left guild \"{}\".", user.getName(), guild.getName());
				client.dispatcher.dispatch(new UserLeaveEvent(guild, user));
			}
		}
	}

	private void guildMemberUpdate(GuildMemberUpdateEventResponse event) {
		Guild guild = (Guild) client.getGuildByID(Long.parseUnsignedLong(event.guild_id));
		User user = (User) client.getUserByID(Long.parseUnsignedLong(event.user.id));

		if (guild != null && user != null) {
			List<IRole> oldRoles = user.getRolesForGuild(guild);
			boolean rolesChanged = oldRoles.size() != event.roles.length + 1;//Add one for the @everyone role
			if (!rolesChanged) {
				rolesChanged = oldRoles.stream().filter(role -> {
					if (role.equals(guild.getEveryoneRole()))
						return false;

					for (String roleID : event.roles) {
						if (role.getLongID() == Long.parseUnsignedLong(roleID)) {
							return false;
						}
					}

					return true;
				}).collect(Collectors.toList()).size() > 0;
			}

			if (rolesChanged) {
				user.roles.remove(guild);

				for (String role : event.roles)
					user.addRole(guild.getLongID(), guild.getRoleByID(Long.parseUnsignedLong(role)));

				user.addRole(guild.getLongID(), guild.getEveryoneRole());

				client.dispatcher.dispatch(new UserRoleUpdateEvent(guild, user, oldRoles, user.getRolesForGuild(guild)));

				if (user.equals(client.getOurUser()))
					guild.loadWebhooks();
			}

			String oldNick = user.getNicknameForGuild(guild);
			if ((oldNick == null ^ event.nick == null)
					|| (oldNick != null && !oldNick.equals(event.nick))
					|| event.nick != null && !event.nick.equals(oldNick)) {
				user.addNick(guild.getLongID(), event.nick);
				client.dispatcher.dispatch(new NicknameChangedEvent(guild, user, oldNick, event.nick));
			}
		}
	}

	private void messageUpdate(MessageObject json) {
		Channel channel = (Channel) client.getChannelByID(Long.parseUnsignedLong(json.channel_id));
		if (channel == null)
			return;

		IMessage toUpdate = channel.messages.get(json.id);

		if (toUpdate == null) { // Cannot resolve update type. MessageObject is incomplete, so we'll have to request for the full message.
			if (channel.isPrivate() ||
					PermissionUtils.hasHierarchicalPermissions(channel, client.ourUser, channel.getGuild().getRolesForUser(client.ourUser), Permissions.READ_MESSAGE_HISTORY))
				client.dispatcher.dispatch(new MessageUpdateEvent(null, channel.fetchMessage(Long.parseUnsignedLong(json.id))));
//			else
//FIXME: unable to fire message update events when the user doesn't have the read message history permission
		} else {
			IMessage oldMessage = toUpdate.copy();
			IMessage updatedMessage = DiscordUtils.getUpdatedMessageFromJSON(client, toUpdate, json);
			if (json.pinned != null && oldMessage.isPinned() && !json.pinned) {
				client.dispatcher.dispatch(new MessageUnpinEvent(oldMessage, updatedMessage));
			} else if (json.pinned != null && !oldMessage.isPinned() && json.pinned) {
				client.dispatcher.dispatch(new MessagePinEvent(oldMessage, updatedMessage));
			} else if (oldMessage.getEmbeds().size() < updatedMessage.getEmbeds().size()) {
				client.dispatcher.dispatch(new MessageEmbedEvent(oldMessage, updatedMessage, oldMessage.getEmbeds()));
			} else if (json.content != null && !oldMessage.getContent().equals(json.content)) {
				client.dispatcher.dispatch(new MessageEditEvent(oldMessage, updatedMessage));
			} else {
				client.dispatcher.dispatch(new MessageUpdateEvent(oldMessage, updatedMessage));
			}
		}
	}

	private void messageDelete(MessageDeleteEventResponse event) {
		long id = Long.parseUnsignedLong(event.id);
		Channel channel = (Channel) client.getChannelByID(Long.parseUnsignedLong(event.channel_id));
		IMessage message = null;

		if (channel != null) {
			message = channel.messages.get(id);
		}

		if (message == null) { // we dont have the message cached. The only thing we know about the message is its ID and its channel's ID.
			client.dispatcher.dispatch(new MessageDeleteEvent(channel, id));
		} else {
			channel.messages.remove(id);
			client.dispatcher.dispatch(new MessageDeleteEvent(message));
		}
	}

	private void messageDeleteBulk(MessageDeleteBulkEventResponse event) { //TODO: maybe add a separate event for this?
		for (String id : event.ids) {
			messageDelete(new MessageDeleteEventResponse(id, event.channel_id));
		}
	}

	private void presenceUpdate(PresenceUpdateEventResponse event) {
		IPresence presence = DiscordUtils.getPresenceFromJSON(event);
		Guild guild = event.guild_id == null ? null : (Guild) client.getGuildByID(Long.parseUnsignedLong(event.guild_id));
		if (guild != null) {
			User user = (User) guild.getUserByID(Long.parseUnsignedLong(event.user.id));
			if (user != null) {
				if (event.user.username != null) { //Full object was sent so there is a user change, otherwise all user fields but id would be null
					IUser oldUser = user.copy();
					user = DiscordUtils.getUserFromJSON(shard, event.user);
					client.dispatcher.dispatch(new UserUpdateEvent(oldUser, user));
				}

				if (!user.getPresence().equals(presence)) {
					IPresence oldPresence = user.getPresence();
					user.setPresence(presence);
					client.dispatcher.dispatch(new PresenceUpdateEvent(user, oldPresence, presence));
					Discord4J.LOGGER.debug(LogMarkers.PRESENCES, "User \"{}\" changed presence to {}", user.getName(), user.getPresence());
				}
			}
		}
	}

	private void guildDelete(GuildObject json) {
		long guildId = Long.parseUnsignedLong(json.id);
		Guild guild = (Guild) client.getGuildByID(guildId);

		// Clean up cache
		if (guild != null) {
			((ShardImpl) guild.getShard()).guildCache.remove(guild);
			((User) client.getOurUser()).voiceStates.remove(guild.getLongID());
			DiscordVoiceWS vWS = shard.voiceWebSockets.get(guildId);
			if (vWS != null) {
				vWS.disconnect(VoiceDisconnectedEvent.Reason.LEFT_CHANNEL);
				shard.voiceWebSockets.remove(guildId);
			}
		}

		if (json.unavailable) { //Guild can't be reached
			Discord4J.LOGGER.warn(LogMarkers.WEBSOCKET, "Guild with id {} is unavailable, is there an outage?", json.id);
			client.dispatcher.dispatch(new GuildUnavailableEvent(guildId));
		} else {
			Discord4J.LOGGER.debug(LogMarkers.EVENTS, "You have been kicked from or left \"{}\"! :O", guild.getName());
			client.dispatcher.dispatch(new GuildLeaveEvent(guild));
		}
	}

	private void channelCreate(ChannelObject json) {
		if (json.type == ChannelObject.Type.PRIVATE) {
			if (!shard.privateChannels.containsKey(json.id)) {
				shard.privateChannels.put((IPrivateChannel) DiscordUtils.getChannelFromJSON(shard, null, json));
			}
		} else {
			Guild guild = (Guild) shard.getGuildByID(Long.parseUnsignedLong(json.guild_id));
			if (guild != null) {
				IChannel channel = DiscordUtils.getChannelFromJSON(shard, guild, json);
				if (json.type == ChannelObject.Type.GUILD_TEXT) {
					guild.channels.put(channel);
					client.dispatcher.dispatch(new ChannelCreateEvent(channel));
				} else if (json.type == ChannelObject.Type.GUILD_VOICE) {
					guild.voiceChannels.put((IVoiceChannel) channel);
					client.dispatcher.dispatch(new VoiceChannelCreateEvent((IVoiceChannel) channel));
				} else if (json.type == ChannelObject.Type.GUILD_CATEGORY) {
					ICategory category = DiscordUtils.getCategoryFromJSON(shard, guild, json);
					guild.categories.put(category);
					client.dispatcher.dispatch(new CategoryCreateEvent(category));
				}
			}
		}
	}

	private void channelDelete(ChannelObject json) {
		if (json.type == ChannelObject.Type.GUILD_TEXT) {
			Channel channel = (Channel) client.getChannelByID(Long.parseUnsignedLong(json.id));
			if (channel != null) {
				if (!channel.isPrivate())
					((Guild) channel.getGuild()).channels.remove(channel);
				else
					shard.privateChannels.remove(channel);
				client.dispatcher.dispatch(new ChannelDeleteEvent(channel));
			}
		} else if (json.type == ChannelObject.Type.GUILD_VOICE) {
			VoiceChannel channel = (VoiceChannel) client.getVoiceChannelByID(Long.parseUnsignedLong(json.id));
			if (channel != null) {
				((Guild) channel.getGuild()).voiceChannels.remove(channel);
				client.dispatcher.dispatch(new VoiceChannelDeleteEvent(channel));
			}
		} else if (json.type == ChannelObject.Type.GUILD_CATEGORY) {
			ICategory category = client.getCategoryByID(Long.parseUnsignedLong(json.id));
			if (category != null) {
				((Guild) category.getGuild()).categories.remove(category);
				client.dispatcher.dispatch(new CategoryDeleteEvent(category));
			}
		}
	}

	private void userUpdate(UserUpdateEventResponse event) {
		User newUser = (User) client.getUserByID(Long.parseUnsignedLong(event.id));
		if (newUser != null) {
			IUser oldUser = newUser.copy();
			newUser = DiscordUtils.getUserFromJSON(shard, event);
			client.dispatcher.dispatch(new UserUpdateEvent(oldUser, newUser));
		}
	}

	private void channelUpdate(ChannelObject json) {
		if (json.type == ChannelObject.Type.GUILD_TEXT) {
			Channel toUpdate = (Channel) shard.getChannelByID(Long.parseUnsignedLong(json.id));
			if (toUpdate != null) {
				IChannel oldChannel = toUpdate.copy();
				toUpdate = (Channel) DiscordUtils.getChannelFromJSON(shard, toUpdate.getGuild(), json);
				toUpdate.loadWebhooks();

				if (!Objects.equals(oldChannel.getCategory(), toUpdate.getCategory())) {
					client.dispatcher.dispatch(new ChannelCategoryUpdateEvent(oldChannel, toUpdate, oldChannel.getCategory(), toUpdate.getCategory()));
				} else {
					client.dispatcher.dispatch(new ChannelUpdateEvent(oldChannel, toUpdate));
				}
			}
		} else if (json.type == ChannelObject.Type.GUILD_VOICE) {
			IVoiceChannel toUpdate = shard.getVoiceChannelByID(Long.parseUnsignedLong(json.id));
			if (toUpdate != null) {
				IVoiceChannel oldChannel = toUpdate.copy();
				toUpdate = (IVoiceChannel) DiscordUtils.getChannelFromJSON(shard, toUpdate.getGuild(), json);

				if (!Objects.equals(oldChannel.getCategory(), toUpdate.getCategory())) {
					client.dispatcher.dispatch(new ChannelCategoryUpdateEvent(oldChannel, toUpdate, oldChannel.getCategory(), toUpdate.getCategory()));
				} else {
					client.dispatcher.dispatch(new VoiceChannelUpdateEvent(oldChannel, toUpdate));
				}
			}
		} else if (json.type == ChannelObject.Type.GUILD_CATEGORY) {
			ICategory toUpdate = shard.getCategoryByID(Long.parseUnsignedLong(json.id));
			if (toUpdate != null) {
				ICategory oldCategory = toUpdate.copy();
				toUpdate = DiscordUtils.getCategoryFromJSON(shard, toUpdate.getGuild(), json);
				client.dispatcher.dispatch(new CategoryUpdateEvent(oldCategory, toUpdate));
			}
		}
	}

	private void guildMembersChunk(GuildMemberChunkEventResponse event) {
		Guild guildToUpdate = (Guild) client.getGuildByID(Long.parseUnsignedLong(event.guild_id));
		if (guildToUpdate == null) {
			Discord4J.LOGGER.warn(LogMarkers.WEBSOCKET, "Can't receive guild members chunk for guild id {}, the guild is null!", event.guild_id);
			return;
		}

		for (MemberObject member : event.members) {
			IUser user = DiscordUtils.getUserFromGuildMemberResponse(guildToUpdate, member);
			guildToUpdate.users.put(user);
		}
		if (guildToUpdate.getUsers().size() >= guildToUpdate.getTotalMemberCount()) {
			client.getDispatcher().dispatch(new AllUsersReceivedEvent(guildToUpdate));
		}
	}

	private void guildUpdate(GuildObject json) {
		Guild toUpdate = (Guild) client.getGuildByID(Long.parseUnsignedLong(json.id));

		if (toUpdate != null) {
			IGuild oldGuild = toUpdate.copy();

			toUpdate = (Guild) DiscordUtils.getGuildFromJSON(shard, json);

			if (toUpdate.getOwnerLongID() != oldGuild.getOwnerLongID()) {
				client.dispatcher.dispatch(new GuildTransferOwnershipEvent(oldGuild.getOwner(), toUpdate.getOwner(), toUpdate));
			} else {
				client.dispatcher.dispatch(new GuildUpdateEvent(oldGuild, toUpdate));
			}
		}
	}

	private void guildRoleCreate(GuildRoleEventResponse event) {
		IGuild guild = client.getGuildByID(Long.parseUnsignedLong(event.guild_id));
		if (guild != null) {
			IRole role = DiscordUtils.getRoleFromJSON(guild, event.role);
			client.dispatcher.dispatch(new RoleCreateEvent(role));
		}
	}

	private void guildRoleUpdate(GuildRoleEventResponse event) {
		IGuild guild = client.getGuildByID(Long.parseUnsignedLong(event.guild_id));
		if (guild != null) {
			IRole toUpdate = guild.getRoleByID(Long.parseUnsignedLong(event.role.id));
			if (toUpdate != null) {
				IRole oldRole = toUpdate.copy();
				toUpdate = DiscordUtils.getRoleFromJSON(guild, event.role);
				client.dispatcher.dispatch(new RoleUpdateEvent(oldRole, toUpdate));

				if (guild.getRolesForUser(client.getOurUser()).contains(toUpdate))
					((Guild) guild).loadWebhooks();
			}
		}
	}

	private void guildRoleDelete(GuildRoleDeleteEventResponse event) {
		Guild guild = (Guild) client.getGuildByID(Long.parseUnsignedLong(event.guild_id));
		if (guild != null) {
			IRole role = guild.getRoleByID(Long.parseUnsignedLong(event.role_id));
			if (role != null) {
				guild.roles.remove(role);
				client.dispatcher.dispatch(new RoleDeleteEvent(role));
			}
		}
	}

	private void guildBanAdd(GuildBanEventResponse event) {
		Guild guild = (Guild) client.getGuildByID(Long.parseUnsignedLong(event.guild_id));
		if (guild != null) {
			IUser user = DiscordUtils.getUserFromJSON(shard, event.user);
			if (guild.getUserByID(user.getLongID()) != null) {
				guild.users.remove(user);
				guild.joinTimes.remove(user);
			}

			client.dispatcher.dispatch(new UserBanEvent(guild, user));
		}
	}

	private void guildBanRemove(GuildBanEventResponse event) {
		IGuild guild = client.getGuildByID(Long.parseUnsignedLong(event.guild_id));
		if (guild != null) {
			IUser user = DiscordUtils.getUserFromJSON(shard, event.user);

			client.dispatcher.dispatch(new UserPardonEvent(guild, user));
		}
	}

	private void voiceStateUpdate(VoiceStateObject json) {
		User user = (User) shard.getUserByID(Long.parseUnsignedLong(json.user_id));

		if (user != null) {
			IVoiceState curVoiceState = user.voiceStates.get(json.guild_id);

			IVoiceChannel channel = json.channel_id != null ? shard.getVoiceChannelByID(Long.parseUnsignedLong(json.channel_id)) : null;
			IVoiceChannel oldChannel = curVoiceState == null ? null : curVoiceState.getChannel();

			user.voiceStates.put(DiscordUtils.getVoiceStateFromJson(shard.getGuildByID(Long.parseUnsignedLong(json.guild_id)), json));

			if (oldChannel != channel) {
				if (channel == null) {
					client.getDispatcher().dispatch(new UserVoiceChannelLeaveEvent(oldChannel, user));
				} else if (oldChannel == null) {
					((Guild) channel.getGuild()).connectingVoiceChannelID = 0;
					client.getDispatcher().dispatch(new UserVoiceChannelJoinEvent(channel, user));
				} else if (oldChannel.getGuild().equals(channel.getGuild())) {
					client.getDispatcher().dispatch(new UserVoiceChannelMoveEvent(user, oldChannel, channel));
				}
			}
		}
	}

	private void voiceServerUpdate(VoiceUpdateResponse event) {
		DiscordVoiceWS oldWS = shard.voiceWebSockets.get(event.guild_id);
		if (oldWS != null) {
			oldWS.disconnect(VoiceDisconnectedEvent.Reason.SERVER_UPDATE);
		}

		if (event.endpoint == null) {
			Discord4J.LOGGER.debug(LogMarkers.VOICE, "Awaiting endpoint to join voice channel in guild id {}...", event.guild_id);
		} else {
			Discord4J.LOGGER.trace(LogMarkers.VOICE, "Voice endpoint received! Connecting to {}...", event.endpoint);
			DiscordVoiceWS vWS = new DiscordVoiceWS(shard, event);
			shard.voiceWebSockets.put(vWS);
			vWS.connect();
		}
	}

	private void guildEmojisUpdate(GuildEmojiUpdateResponse event) {
		Guild guild = (Guild) shard.getGuildByID(Long.parseUnsignedLong(event.guild_id));

		if (guild != null) {
			List<IEmoji> oldEmoji = guild.getEmojis();
			List<IEmoji> newEmoji = Arrays.stream(event.emojis)
					.map(e -> DiscordUtils.getEmojiFromJSON(guild, e))
					.collect(Collectors.toList());

			guild.emojis.clear();
			guild.emojis.putAll(newEmoji);

			client.dispatcher.dispatch(new GuildEmojisUpdateEvent(guild, oldEmoji, newEmoji));
		}
	}

	private void reactionAdd(ReactionEventResponse event) {
		IChannel channel = shard.getChannelByID(Long.parseUnsignedLong(event.channel_id));
		if (channel == null) return;
		if (!PermissionUtils.hasPermissions(channel, client.ourUser, Permissions.READ_MESSAGES, Permissions.READ_MESSAGE_HISTORY)) return; // Discord sends this event no matter our permissions for some reason.

		boolean cached = ((Channel) channel).messages.containsKey(Long.parseUnsignedLong(event.message_id));
		IMessage message = channel.fetchMessage(Long.parseUnsignedLong(event.message_id));
		if (message == null) {
			Discord4J.LOGGER.debug("Unable to fetch the message specified by a reaction add event\nObject={}", ToStringBuilder.reflectionToString(event));
			return;
		}
		IReaction reaction = event.emoji.id == null
				? message.getReactionByUnicode(event.emoji.name)
				: message.getReactionByID(Long.parseUnsignedLong(event.emoji.id));
		message.getReactions().remove(reaction);

		if (reaction == null) { // Only happens in the case of a cached message with a new reaction
			long id = event.emoji.id == null ? 0 : Long.parseUnsignedLong(event.emoji.id);
			reaction = new Reaction(message, 1, ReactionEmoji.of(event.emoji.name, id));
		} else if (cached) {
			reaction = new Reaction(message, reaction.getCount() + 1, reaction.getEmoji());
		}
		message.getReactions().add(reaction);

		IUser user;
		if (channel.isPrivate()) {
			user = channel.getUsersHere().get(channel.getUsersHere().get(0).getLongID() == Long.parseUnsignedLong(event.user_id) ? 0 : 1);
		}
		else {
			user = channel.getGuild().getUserByID(Long.parseUnsignedLong(event.user_id));
		}

		client.dispatcher.dispatch(new ReactionAddEvent(message, reaction, user));
	}

	private void reactionRemove(ReactionEventResponse event) {
		IChannel channel = shard.getChannelByID(Long.parseUnsignedLong(event.channel_id));
		if (channel == null)
			return;
		if (!PermissionUtils.hasPermissions(channel, client.ourUser, Permissions.READ_MESSAGES, Permissions.READ_MESSAGE_HISTORY)) return; // Discord sends this event no matter our permissions for some reason.

		boolean cached = ((Channel) channel).messages.containsKey(Long.parseUnsignedLong(event.message_id));
		IMessage message = channel.fetchMessage(Long.parseUnsignedLong(event.message_id));
		if (message == null) {
			Discord4J.LOGGER.debug("Unable to fetch the message specified by a reaction remove event\nObject={}", ToStringBuilder.reflectionToString(event));
			return;
		}
		IReaction reaction = event.emoji.id == null
				? message.getReactionByUnicode(event.emoji.name)
				: message.getReactionByID(Long.parseUnsignedLong(event.emoji.id));
		message.getReactions().remove(reaction);

		if (reaction == null) { // the last reaction of the emoji was removed
			long id = event.emoji.id == null ? 0 : Long.parseUnsignedLong(event.emoji.id);
			reaction = new Reaction(message, 0, ReactionEmoji.of(event.emoji.name, id));
		}
		else {
			reaction = new Reaction(message, !cached ? reaction.getCount() : reaction.getCount() - 1, reaction.getEmoji());
		}

		if (reaction.getCount() > 0) {
			message.getReactions().add(reaction);
		}


		IUser user;
		if (channel.isPrivate()) {
			user = channel.getUsersHere().get(channel.getUsersHere().get(0).getLongID() == Long.parseUnsignedLong(event.user_id) ? 0 : 1);
		}
		else {
			user = channel.getGuild().getUserByID(Long.parseUnsignedLong(event.user_id));
		}

		client.dispatcher.dispatch(new ReactionRemoveEvent(message, reaction, user));
	}


	private void webhookUpdate(WebhookObject event) {
		Channel channel = (Channel) client.getChannelByID(Long.parseUnsignedLong(event.channel_id));
		if (channel != null)
			channel.loadWebhooks();
	}
}
