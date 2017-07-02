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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.internal.json.event.*;
import sx.blah.discord.api.internal.json.objects.*;
import sx.blah.discord.api.internal.json.requests.GuildMembersRequest;
import sx.blah.discord.api.internal.json.responses.ReadyResponse;
import sx.blah.discord.api.internal.json.responses.voice.VoiceUpdateResponse;
import sx.blah.discord.handle.impl.events.*;
import sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelJoinEvent;
import sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelLeaveEvent;
import sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelMoveEvent;
import sx.blah.discord.handle.impl.obj.*;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.LogMarkers;
import sx.blah.discord.util.MessageList;
import sx.blah.discord.util.RequestBuffer;
import sx.blah.discord.util.RequestBuilder;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static sx.blah.discord.api.internal.DiscordUtils.MAPPER;

class DispatchHandler {
	private DiscordWS ws;
	private ShardImpl shard;
	private DiscordClientImpl client;
	private final ExecutorService dispatchExecutor = Executors.newSingleThreadExecutor(DiscordUtils.createDaemonThreadFactory("Dispatch Handler"));

	DispatchHandler(DiscordWS ws, ShardImpl shard) {
		this.ws = ws;
		this.shard = shard;
		this.client = (DiscordClientImpl) shard.getClient();
	}

	public void handle(final JsonNode event) {
		dispatchExecutor.submit(() -> {
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
						channelCreate(json);
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

			if (MessageList.getEfficiency(client) == null) //User did not manually set the efficiency
				MessageList.setEfficiency(client, MessageList.EfficiencyLevel.getEfficiencyForGuilds(ready.guilds.length));

			Set<UnavailableGuildObject> waitingGuilds = ConcurrentHashMap.newKeySet(ready.guilds.length);
			waitingGuilds.addAll(Arrays.asList(ready.guilds));

			final AtomicInteger loadedGuilds = new AtomicInteger(0);
			client.getDispatcher().waitFor((GuildCreateEvent e) -> {
				waitingGuilds.removeIf(g -> g.id.equals(e.getGuild().getStringID()));
				return loadedGuilds.incrementAndGet() >= ready.guilds.length;
			}, 10, TimeUnit.SECONDS);

			waitingGuilds.forEach(guild -> client.getDispatcher().dispatch(new GuildUnavailableEvent(Long.parseUnsignedLong(guild.id))));
			return true;
		}).andThen(() -> {
			if (this.shard.getInfo()[0] == 0) { // pms are only sent to shard 0
				for (PrivateChannelObject pmObj : ready.private_channels) {
					IPrivateChannel pm = DiscordUtils.getPrivateChannelFromJSON(shard, pmObj);
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

				//TODO remove
				List<String> inviteCodes = DiscordUtils.getInviteCodesFromMessage(json.content);
				if (!inviteCodes.isEmpty()) {
					List<IInvite> invites = inviteCodes.stream()
							.map(s -> client.getInviteForCode(s))
							.filter(Objects::nonNull)
							.collect(Collectors.toList());
					if (!invites.isEmpty()) client.getDispatcher().dispatch(new InviteReceivedEvent(invites.toArray(new IInvite[invites.size()]), message));
				}

				if (mentioned) {
					client.dispatcher.dispatch(new MentionEvent(message));
				}

				channel.addToCache(message);

				if (message.getAuthor().equals(client.getOurUser())) {
					client.dispatcher.dispatch(new MessageSendEvent(message));
					message.getChannel().setTypingStatus(false); //Messages being sent should stop the bot from typing
				} else {
					client.dispatcher.dispatch(new MessageReceivedEvent(message));
					if (!message.getEmbedded().isEmpty()) {
						client.dispatcher.dispatch(new MessageEmbedEvent(message, new ArrayList<>()));
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
			LocalDateTime timestamp = DiscordUtils.convertFromTimestamp(event.joined_at);
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
				client.dispatcher.dispatch(new NickNameChangeEvent(guild, user, oldNick, event.nick));
			}
		}
	}

	private void messageUpdate(MessageObject json) {
		String id = json.id;
		String channelID = json.channel_id;

		Channel channel = (Channel) client.getChannelByID(Long.parseUnsignedLong(channelID));
		if (channel == null)
			return;

		Message toUpdate = (Message) channel.getMessageByID(Long.parseUnsignedLong(id));
		if (toUpdate == null) return;

		IMessage oldMessage = toUpdate.copy();
		toUpdate = (Message) DiscordUtils.getUpdatedMessageFromJSON(toUpdate, json);

		if (json.pinned != null && oldMessage.isPinned() && !json.pinned) {
			client.dispatcher.dispatch(new MessageUnpinEvent(toUpdate));
		} else if (json.pinned != null && !oldMessage.isPinned() && json.pinned) {
			client.dispatcher.dispatch(new MessagePinEvent(toUpdate));
		} else if (oldMessage.getEmbedded().size() < toUpdate.getEmbedded().size()) {
			client.dispatcher.dispatch(new MessageEmbedEvent(toUpdate, oldMessage.getEmbedded()));
		} else if (json.content != null && !oldMessage.getContent().equals(json.content)) {
			client.dispatcher.dispatch(new MessageUpdateEvent(oldMessage, toUpdate));
		}
	}

	private void messageDelete(MessageDeleteEventResponse event) {
		String id = event.id;
		String channelID = event.channel_id;
		Channel channel = (Channel) client.getChannelByID(Long.parseUnsignedLong(channelID));

		if (channel != null) {
			Message message = (Message) channel.getMessageByID(Long.parseUnsignedLong(id));
			if (message != null) {
				if (message.isPinned()) {
					message.setPinned(false); //For consistency with the event
					client.dispatcher.dispatch(new MessageUnpinEvent(message));
				}
				message.setDeleted(true);
				channel.messages.remove(message);
				client.dispatcher.dispatch(new MessageDeleteEvent(message));
			}
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

	private void channelCreate(JsonNode json) throws JsonProcessingException {
		boolean isPrivate = json.get("is_private").asBoolean(false);

		if (isPrivate) { // PM channel.
			PrivateChannelObject event = MAPPER.treeToValue(json, PrivateChannelObject.class);
			if (shard.privateChannels.containsKey(event.id))
				return; // we already have this PM channel; no need to create another.

			shard.privateChannels.put(DiscordUtils.getPrivateChannelFromJSON(shard, event));

		} else { // Regular channel.
			ChannelObject event = MAPPER.treeToValue(json, ChannelObject.class);
			String type = event.type;
			Guild guild = (Guild) client.getGuildByID(Long.parseUnsignedLong(event.guild_id));
			if (guild != null) {
				if (type.equalsIgnoreCase("text")) { //Text channel
					Channel channel = (Channel) DiscordUtils.getChannelFromJSON(guild, event);
					guild.channels.put(channel);
					client.dispatcher.dispatch(new ChannelCreateEvent(channel));
				} else if (type.equalsIgnoreCase("voice")) {
					VoiceChannel channel = (VoiceChannel) DiscordUtils.getVoiceChannelFromJSON(guild, event);
					guild.voiceChannels.put(channel);
					client.dispatcher.dispatch(new VoiceChannelCreateEvent(channel));
				}
			}
		}
	}

	private void channelDelete(ChannelObject json) {
		if (json.type.equalsIgnoreCase("text")) {
			Channel channel = (Channel) client.getChannelByID(Long.parseUnsignedLong(json.id));
			if (channel != null) {
				if (!channel.isPrivate())
					((Guild) channel.getGuild()).channels.remove(channel);
				else
					shard.privateChannels.remove(channel);
				client.dispatcher.dispatch(new ChannelDeleteEvent(channel));
			}
		} else if (json.type.equalsIgnoreCase("voice")) {
			VoiceChannel channel = (VoiceChannel) client.getVoiceChannelByID(Long.parseUnsignedLong(json.id));
			if (channel != null) {
				((Guild) channel.getGuild()).voiceChannels.remove(channel);
				client.dispatcher.dispatch(new VoiceChannelDeleteEvent(channel));
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
		if (!json.is_private) {
			if (json.type.equalsIgnoreCase("text")) {
				Channel toUpdate = (Channel) client.getChannelByID(Long.parseUnsignedLong(json.id));
				if (toUpdate != null) {
					IChannel oldChannel = toUpdate.copy();

					toUpdate = (Channel) DiscordUtils.getChannelFromJSON(toUpdate.getGuild(), json);

					toUpdate.loadWebhooks();

					client.getDispatcher().dispatch(new ChannelUpdateEvent(oldChannel, toUpdate));
				}
			} else if (json.type.equalsIgnoreCase("voice")) {
				VoiceChannel toUpdate = (VoiceChannel) client.getVoiceChannelByID(Long.parseUnsignedLong(json.id));
				if (toUpdate != null) {
					VoiceChannel oldChannel = (VoiceChannel) toUpdate.copy();

					toUpdate = (VoiceChannel) DiscordUtils.getVoiceChannelFromJSON(toUpdate.getGuild(), json);

					client.getDispatcher().dispatch(new VoiceChannelUpdateEvent(oldChannel, toUpdate));
				}
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
			if (client.getUserByID(user.getID()) != null) {
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
		Guild guild = (Guild) client.getGuildByID(Long.parseUnsignedLong(event.guild_id));
		if (guild != null) {
			List<IEmoji> oldList = guild.getEmojis().stream().map(IEmoji::copy)
					.collect(Collectors.toCollection(CopyOnWriteArrayList::new));

			guild.emojis.clear();
			for (EmojiObject obj : event.emojis) {
				guild.emojis.put(DiscordUtils.getEmojiFromJSON(guild, obj));
			}

			client.dispatcher.dispatch(new GuildEmojisUpdateEvent(guild, oldList, guild.getEmojis()));
		}
	}

	private void reactionAdd(ReactionEventResponse event) {
		IChannel channel = client.getChannelByID(Long.parseUnsignedLong(event.channel_id));
		if (!channel.getModifiedPermissions(client.getOurUser()).contains(Permissions.READ_MESSAGES))
			return;
		if (channel != null) {
			IMessage message = RequestBuffer.request(() -> {
				return channel.getMessageByID(Long.parseUnsignedLong(event.message_id));
			}).get();

			if (message != null) {
				Reaction reaction = (Reaction) (event.emoji.id == null
						? message.getReactionByUnicode(event.emoji.name)
						: message.getReactionByIEmoji(message.getGuild().getEmojiByID(Long.parseUnsignedLong(event.emoji.id))));
				IUser user = message.getClient().getUserByID(Long.parseUnsignedLong(event.user_id));

				if (reaction == null) {
					List<IUser> list = new CopyOnWriteArrayList<>();
					list.add(user);

					reaction = new Reaction(message.getShard(), 1, list,
							event.emoji.id != null ? event.emoji.id : event.emoji.name, event.emoji.id != null);

					message.getReactions().add(reaction);
				} else if (channel.getMessageHistory().contains(message.getLongID())) { //If the message is in the internal cache then it doesn't have the most up to date reaction count
					reaction.setCount(reaction.getCount() + 1);
					reaction.getCachedUsers().add(user);
				}

				reaction.setMessage(message);

				client.dispatcher.dispatch(
						new ReactionAddEvent(message, reaction, user));
			}
		}
	}

	private void reactionRemove(ReactionEventResponse event) {
		IChannel channel = client.getChannelByID(Long.parseUnsignedLong(event.channel_id));
		if (!channel.getModifiedPermissions(client.getOurUser()).contains(Permissions.READ_MESSAGES))
			return;
		if (channel != null) {
			IMessage message = channel.getMessageByID(Long.parseUnsignedLong(event.message_id));

			if (message != null) {
				Reaction reaction = (Reaction) (event.emoji.id == null
						? message.getReactionByUnicode(event.emoji.name)
						: message.getReactionByIEmoji(message.getGuild().getEmojiByID(Long.parseUnsignedLong(event.emoji.id))));
				IUser user = message.getClient().getUserByID(Long.parseUnsignedLong(event.user_id));

				if (reaction != null) {
					reaction.setMessage(message); // safeguard
					reaction.setCount(reaction.getCount() - 1);
					reaction.getCachedUsers().remove(user);

					if (reaction.getCount() <= 0) {
						message.getReactions().remove(reaction);
					}
				} else {
					IEmoji custom = event.emoji.id == null ? null : channel.getGuild().getEmojiByID(Long.parseUnsignedLong(event.emoji.id));
					reaction = new Reaction(channel.getShard(), 0, new ArrayList<>(), custom != null ? custom.getStringID() : event.emoji.name, custom != null);
				}

				client.dispatcher.dispatch(new ReactionRemoveEvent(message, reaction, user));
			}
		}
	}

	private void webhookUpdate(WebhookObject event) {
		Channel channel = (Channel) client.getChannelByID(Long.parseUnsignedLong(event.channel_id));
		if (channel != null)
			channel.loadWebhooks();
	}
}
