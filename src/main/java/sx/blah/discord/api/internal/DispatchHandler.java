package sx.blah.discord.api.internal;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.internal.json.event.*;
import sx.blah.discord.api.internal.json.objects.*;
import sx.blah.discord.api.internal.json.responses.ReadyResponse;
import sx.blah.discord.api.internal.json.responses.voice.VoiceUpdateResponse;
import sx.blah.discord.handle.impl.events.*;
import sx.blah.discord.handle.impl.obj.*;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static sx.blah.discord.api.internal.DiscordUtils.GSON;

class DispatchHandler {
	private DiscordWS ws;
	private ShardImpl shard;
	private DiscordClientImpl client;

	DispatchHandler(DiscordWS ws, ShardImpl shard) {
		this.ws = ws;
		this.shard = shard;
		this.client = (DiscordClientImpl) shard.getClient();
	}

	public void handle(JsonObject event) {
		String type = event.get("t").getAsString();
		JsonElement json = event.get("d");
		switch (type) {
			case "RESUMED": resumed(); break;
			case "READY": ready(GSON.fromJson(json, ReadyResponse.class)); break;
			case "MESSAGE_CREATE": messageCreate(GSON.fromJson(json, MessageObject.class)); break;
			case "TYPING_START": typingStart(GSON.fromJson(json, TypingEventResponse.class)); break;
			case "GUILD_CREATE": guildCreate(GSON.fromJson(json, GuildObject.class)); break;
			case "GUILD_MEMBER_ADD": guildMemberAdd(GSON.fromJson(json, GuildMemberAddEventResponse.class)); break;
			case "GUILD_MEMBER_REMOVE": guildMemberRemove(GSON.fromJson(json, GuildMemberRemoveEventResponse.class)); break;
			case "GUILD_MEMBER_UPDATE": guildMemberUpdate(GSON.fromJson(json, GuildMemberUpdateEventResponse.class)); break;
			case "MESSAGE_UPDATE": messageUpdate(GSON.fromJson(json, MessageObject.class)); break;
			case "MESSAGE_DELETE": messageDelete(GSON.fromJson(json, MessageDeleteEventResponse.class)); break;
			case "MESSAGE_DELETE_BULK": messageDeleteBulk(GSON.fromJson(json, MessageDeleteBulkEventResponse.class)); break;
			case "PRESENCE_UPDATE": presenceUpdate(GSON.fromJson(json, PresenceUpdateEventResponse.class)); break;
			case "GUILD_DELETE": guildDelete(GSON.fromJson(json, GuildObject.class)); break;
			case "CHANNEL_CREATE": channelCreate(json.getAsJsonObject()); break;
			case "CHANNEL_DELETE": channelDelete(GSON.fromJson(json, ChannelObject.class)); break;
			case "CHANNEL_PINS_UPDATE": /* Implemented in MESSAGE_UPDATE. Ignored */ break;
			case "CHANNEL_PINS_ACK": /* Ignored */ break;
			case "USER_UPDATE": userUpdate(GSON.fromJson(json, UserUpdateEventResponse.class)); break;
			case "CHANNEL_UPDATE": channelUpdate(GSON.fromJson(json, ChannelObject.class)); break;
			case "GUILD_MEMBERS_CHUNK": guildMembersChunk(GSON.fromJson(json, GuildMemberChunkEventResponse.class)); break;
			case "GUILD_UPDATE": guildUpdate(GSON.fromJson(json, GuildObject.class)); break;
			case "GUILD_ROLE_CREATE": guildRoleCreate(GSON.fromJson(json, GuildRoleEventResponse.class)); break;
			case "GUILD_ROLE_UPDATE": guildRoleUpdate(GSON.fromJson(json, GuildRoleEventResponse.class)); break;
			case "GUILD_ROLE_DELETE": guildRoleDelete(GSON.fromJson(json, GuildRoleDeleteEventResponse.class)); break;
			case "GUILD_BAN_ADD": guildBanAdd(GSON.fromJson(json, GuildBanEventResponse.class)); break;
			case "GUILD_BAN_REMOVE": guildBanRemove(GSON.fromJson(json, GuildBanEventResponse.class)); break;
			case "GUILD_EMOJIS_UPDATE": guildEmojisUpdate(GSON.fromJson(json, GuildEmojiUpdateResponse.class)); break;
			case "GUILD_INTEGRATIONS_UPDATE": /* TODO: Impl Guild integrations */ break;
			case "VOICE_STATE_UPDATE": voiceStateUpdate(GSON.fromJson(json, VoiceStateObject.class)); break;
			case "VOICE_SERVER_UPDATE": voiceServerUpdate(GSON.fromJson(json, VoiceUpdateResponse.class)); break;
			case "MESSAGE_REACTION_ADD": reactionAdd(GSON.fromJson(json, ReactionEventResponse.class)); break;
			case "MESSAGE_REACTION_REMOVE": reactionRemove(GSON.fromJson(json, ReactionEventResponse.class)); break;
			case "MESSAGE_REACTION_REMOVE_ALL": /* REMOVE_ALL is 204 empty but REACTION_REMOVE is sent anyway */ break;
			case "WEBHOOKS_UPDATE": webhookUpdate(GSON.fromJson(json, WebhookObject.class)); break;

			default:
				Discord4J.LOGGER.warn(LogMarkers.WEBSOCKET, "Unknown message received: {}, REPORT THIS TO THE DISCORD4J DEV!", type);
		}
	}

	private void ready(ReadyResponse ready) {
		Discord4J.LOGGER.info(LogMarkers.WEBSOCKET, "Connected to Discord Gateway v{}. Receiving {} guilds.", ready.v, ready.guilds.length);

		ws.state = DiscordWS.State.READY;
		ws.hasReceivedReady = true; // Websocket received actual ready event
		client.getDispatcher().dispatch(new LoginEvent(shard));

		new RequestBuilder(client).setAsync(true).doAction(() -> {
			if (client.ourUser == null) client.ourUser = new User(shard, ready.user.username, ready.user.id,
					ready.user.discriminator, ready.user.avatar, Presences.OFFLINE, ready.user.bot);

			ws.sessionId = ready.session_id;

			//Disable initial caching for performance
			if (ready.guilds.length > MessageList.MAX_GUILD_COUNT)
				MessageList.shouldDownloadHistoryAutomatically(false);

			ArrayList<UnavailableGuildObject> waitingGuilds = new ArrayList<>(Arrays.asList(ready.guilds));
			for (int i = 0; i < ready.guilds.length; i++) {
				client.getDispatcher().waitFor((GuildCreateEvent e) -> {
					synchronized (waitingGuilds) {
						waitingGuilds.removeIf(g -> g.id.equals(e.getGuild().getID()));
					}
					return true;
				}, 10, TimeUnit.SECONDS);
			}

			waitingGuilds.forEach(guild ->
					client.getDispatcher().dispatch(new GuildUnavailableEvent(guild.id))
			);
			return true;
		}).andThen(() -> {
			if (this.shard.getInfo()[0] == 0) { // pms are only sent to shard one
				Arrays.stream(ready.private_channels)
						.map(pm -> DiscordUtils.getPrivateChannelFromJSON(shard, pm))
						.forEach(shard.privateChannels::add);
			}

			ws.isReady = true;
			client.getDispatcher().dispatch(new ShardReadyEvent(shard)); // All information for this shard has been received
			return true;
		}).execute();
	}

	private void resumed() {
		Discord4J.LOGGER.info(LogMarkers.WEBSOCKET, "Session resumed on shard " + shard.getInfo()[0]);
		client.getDispatcher().dispatch(new ResumedEvent(shard));
	}

	private void messageCreate(MessageObject json) {
		boolean mentioned = json.mention_everyone;

		Channel channel = (Channel) client.getChannelByID(json.channel_id);

		if (null != channel) {
			if (!mentioned) { //Not worth checking if already mentioned
				for (UserObject user : json.mentions) { //Check mention array for a mention
					if (client.getOurUser().getID().equals(user.id)) {
						mentioned = true;
						break;
					}
				}
			}

			if (!mentioned) { //Not worth checking if already mentioned
				for (String role : json.mention_roles) { //Check roles for a mention
					if (client.getOurUser().getRolesForGuild(channel.getGuild()).contains(channel.getGuild().getRoleByID(role))) {
						mentioned = true;
						break;
					}
				}
			}

			IMessage message = DiscordUtils.getMessageFromJSON(channel, json);

			if (!channel.getMessages().contains(message)) {
				Discord4J.LOGGER.debug(LogMarkers.EVENTS, "Message from: {} ({}) in channel ID {}: {}", message.getAuthor().getName(),
						json.author.id, json.channel_id, json.content);

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
		Channel channel = (Channel) client.getChannelByID(event.channel_id);
		if (channel != null) {
			if (channel.isPrivate()) {
				user = (User) ((IPrivateChannel) channel).getRecipient();
			} else {
				user = (User) channel.getGuild().getUserByID(event.user_id);
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
		shard.guildList.add(guild);
		guild.loadWebhooks();
		client.dispatcher.dispatch(new GuildCreateEvent(guild));
		Discord4J.LOGGER.debug(LogMarkers.EVENTS, "New guild has been created/joined! \"{}\" with ID {} on shard {}.", guild.getName(), guild.getID(), shard.getInfo()[0]);
	}

	private void guildMemberAdd(GuildMemberAddEventResponse event) {
		String guildID = event.guild_id;
		Guild guild = (Guild) client.getGuildByID(guildID);
		if (guild != null) {
			User user = (User) DiscordUtils.getUserFromGuildMemberResponse(guild, new MemberObject(event.user, event.roles));
			guild.addUser(user);
			guild.setTotalMemberCount(guild.getTotalMemberCount() + 1);
			LocalDateTime timestamp = DiscordUtils.convertFromTimestamp(event.joined_at);
			Discord4J.LOGGER.debug(LogMarkers.EVENTS, "User \"{}\" joined guild \"{}\".", user.getName(), guild.getName());
			client.dispatcher.dispatch(new UserJoinEvent(guild, user, timestamp));
		}
	}

	private void guildMemberRemove(GuildMemberRemoveEventResponse event) {
		String guildID = event.guild_id;
		Guild guild = (Guild) client.getGuildByID(guildID);
		if (guild != null) {
			User user = (User) guild.getUserByID(event.user.id);
			if (user != null) {
				guild.getUsers().remove(user);
				guild.getJoinTimes().remove(user);
				guild.setTotalMemberCount(guild.getTotalMemberCount() - 1);
				Discord4J.LOGGER.debug(LogMarkers.EVENTS, "User \"{}\" has been removed from or left guild \"{}\".", user.getName(), guild.getName());
				client.dispatcher.dispatch(new UserLeaveEvent(guild, user));
			}
		}
	}

	private void guildMemberUpdate(GuildMemberUpdateEventResponse event) {
		Guild guild = (Guild) client.getGuildByID(event.guild_id);
		User user = (User) client.getUserByID(event.user.id);

		if (guild != null && user != null) {
			List<IRole> oldRoles = new ArrayList<>(user.getRolesForGuild(guild));
			boolean rolesChanged = oldRoles.size() != event.roles.length + 1;//Add one for the @everyone role
			if (!rolesChanged) {
				rolesChanged = oldRoles.stream().filter(role -> {
					if (role.equals(guild.getEveryoneRole()))
						return false;

					for (String roleID : event.roles) {
						if (role.getID().equals(roleID)) {
							return false;
						}
					}

					return true;
				}).collect(Collectors.toList()).size() > 0;
			}

			if (rolesChanged) {
				user.getRolesForGuild(guild).clear();
				for (String role : event.roles)
					user.addRole(guild.getID(), guild.getRoleByID(role));

				user.addRole(guild.getID(), guild.getEveryoneRole());

				client.dispatcher.dispatch(new UserRoleUpdateEvent(oldRoles, user.getRolesForGuild(guild), user, guild));

				if (user.equals(client.getOurUser()))
					guild.loadWebhooks();
			}

			if (!user.getNicknameForGuild(guild).equals(Optional.ofNullable(event.nick))) {
				String oldNick = user.getNicknameForGuild(guild).orElse(null);
				user.addNick(guild.getID(), event.nick);

				client.dispatcher.dispatch(new NickNameChangeEvent(guild, user, oldNick, event.nick));
			}
		}
	}

	private void messageUpdate(MessageObject json) {
		String id = json.id;
		String channelID = json.channel_id;

		Channel channel = (Channel) client.getChannelByID(channelID);
		if (channel == null)
			return;

		Message toUpdate = (Message) channel.getMessageByID(id);
		if (toUpdate != null) {
			IMessage oldMessage = toUpdate.copy();

			toUpdate = (Message) DiscordUtils.getMessageFromJSON(channel, json);

			if (oldMessage.isPinned() && !json.pinned) {
				client.dispatcher.dispatch(new MessageUnpinEvent(toUpdate));
			} else if (!oldMessage.isPinned() && json.pinned) {
				client.dispatcher.dispatch(new MessagePinEvent(toUpdate));
			} else if (oldMessage.getEmbedded().size() < toUpdate.getEmbedded().size()) {
				client.dispatcher.dispatch(new MessageEmbedEvent(toUpdate, oldMessage.getEmbedded()));
			} else {
				client.dispatcher.dispatch(new MessageUpdateEvent(oldMessage, toUpdate));
			}
		}
	}

	private void messageDelete(MessageDeleteEventResponse event) {
		String id = event.id;
		String channelID = event.channel_id;
		Channel channel = (Channel) client.getChannelByID(channelID);

		if (channel != null) {
			Message message = (Message) channel.getMessageByID(id);
			if (message != null) {
				if (message.isPinned()) {
					message.setPinned(false); //For consistency with the event
					client.dispatcher.dispatch(new MessageUnpinEvent(message));
				}
				message.setDeleted(true);
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
		Status status = DiscordUtils.getStatusFromJSON(event.game);
		Presences presence = status.getType() == Status.StatusType.STREAM ?
				Presences.STREAMING : Presences.get(event.status);
		Guild guild = (Guild) client.getGuildByID(event.guild_id);
		if (guild != null) {
			User user = (User) guild.getUserByID(event.user.id);
			if (user != null) {
				if (event.user.username != null) { //Full object was sent so there is a user change, otherwise all user fields but id would be null
					IUser oldUser = user.copy();
					user = DiscordUtils.getUserFromJSON(shard, event.user);
					client.dispatcher.dispatch(new UserUpdateEvent(oldUser, user));
				}

				if (!user.getPresence().equals(presence)) {
					Presences oldPresence = user.getPresence();
					user.setPresence(presence);
					client.dispatcher.dispatch(new PresenceUpdateEvent(user, oldPresence, presence));
					Discord4J.LOGGER.debug(LogMarkers.EVENTS, "User \"{}\" changed presence to {}", user.getName(), user.getPresence());
				}
				if (!user.getStatus().equals(status)) {
					Status oldStatus = user.getStatus();
					user.setStatus(status);
					client.dispatcher.dispatch(new StatusChangeEvent(user, oldStatus, status));
					Discord4J.LOGGER.debug(LogMarkers.EVENTS, "User \"{}\" changed status to {}.", user.getName(), status);
				}
			}
		}
	}

	private void guildDelete(GuildObject json) {
		Guild guild = (Guild) client.getGuildByID(json.id);
		guild.getShard().getGuilds().remove(guild);
		if (json.unavailable) { //Guild can't be reached
			Discord4J.LOGGER.warn(LogMarkers.WEBSOCKET, "Guild with id {} is unavailable, is there an outage?", json.id);
			client.dispatcher.dispatch(new GuildUnavailableEvent(json.id));
		} else {
			Discord4J.LOGGER.debug(LogMarkers.EVENTS, "You have been kicked from or left \"{}\"! :O", guild.getName());
			client.dispatcher.dispatch(new GuildLeaveEvent(guild));
		}
	}

	private void channelCreate(JsonObject json) {
		boolean isPrivate = json.get("is_private").getAsBoolean();

		if (isPrivate) { // PM channel.
			PrivateChannelObject event = GSON.fromJson(json, PrivateChannelObject.class);
			String id = event.id;
			boolean contained = false;
			for (IPrivateChannel privateChannel : shard.privateChannels) {
				if (privateChannel.getID().equalsIgnoreCase(id))
					contained = true;
			}

			if (contained)
				return; // we already have this PM channel; no need to create another.

			shard.privateChannels.add(DiscordUtils.getPrivateChannelFromJSON(shard, event));

		} else { // Regular channel.
			ChannelObject event = GSON.fromJson(json, ChannelObject.class);
			String type = event.type;
			Guild guild = (Guild) client.getGuildByID(event.guild_id);
			if (guild != null) {
				if (type.equalsIgnoreCase("text")) { //Text channel
					Channel channel = (Channel) DiscordUtils.getChannelFromJSON(guild, event);
					guild.addChannel(channel);
					client.dispatcher.dispatch(new ChannelCreateEvent(channel));
				} else if (type.equalsIgnoreCase("voice")) {
					VoiceChannel channel = (VoiceChannel) DiscordUtils.getVoiceChannelFromJSON(guild, event);
					guild.addVoiceChannel(channel);
					client.dispatcher.dispatch(new VoiceChannelCreateEvent(channel));
				}
			}
		}
	}

	private void channelDelete(ChannelObject json) {
		if (json.type.equalsIgnoreCase("text")) {
			Channel channel = (Channel) client.getChannelByID(json.id);
			if (channel != null) {
				if (!channel.isPrivate())
					channel.getGuild().getChannels().remove(channel);
				else
					shard.privateChannels.remove(channel);
				client.dispatcher.dispatch(new ChannelDeleteEvent(channel));
			}
		} else if (json.type.equalsIgnoreCase("voice")) {
			VoiceChannel channel = (VoiceChannel) client.getVoiceChannelByID(json.id);
			if (channel != null) {
				channel.getGuild().getVoiceChannels().remove(channel);
				client.dispatcher.dispatch(new VoiceChannelDeleteEvent(channel));
			}
		}
	}

	private void userUpdate(UserUpdateEventResponse event) {
		User newUser = (User) client.getUserByID(event.id);
		if (newUser != null) {
			IUser oldUser = newUser.copy();
			newUser = DiscordUtils.getUserFromJSON(shard, event);
			client.dispatcher.dispatch(new UserUpdateEvent(oldUser, newUser));
		}
	}

	private void channelUpdate(ChannelObject json) {
		if (!json.is_private) {
			if (json.type.equalsIgnoreCase("text")) {
				Channel toUpdate = (Channel) client.getChannelByID(json.id);
				if (toUpdate != null) {
					IChannel oldChannel = toUpdate.copy();

					toUpdate = (Channel) DiscordUtils.getChannelFromJSON(toUpdate.getGuild(), json);

					toUpdate.loadWebhooks();

					client.getDispatcher().dispatch(new ChannelUpdateEvent(oldChannel, toUpdate));
				}
			} else if (json.type.equalsIgnoreCase("voice")) {
				VoiceChannel toUpdate = (VoiceChannel) client.getVoiceChannelByID(json.id);
				if (toUpdate != null) {
					VoiceChannel oldChannel = (VoiceChannel) toUpdate.copy();

					toUpdate = (VoiceChannel) DiscordUtils.getVoiceChannelFromJSON(toUpdate.getGuild(), json);

					client.getDispatcher().dispatch(new VoiceChannelUpdateEvent(oldChannel, toUpdate));
				}
			}
		}
	}

	private void guildMembersChunk(GuildMemberChunkEventResponse event) {
		Guild guildToUpdate = (Guild) client.getGuildByID(event.guild_id);
		if (guildToUpdate == null) {
			Discord4J.LOGGER.warn(LogMarkers.WEBSOCKET, "Can't receive guild members chunk for guild id {}, the guild is null!", event.guild_id);
			return;
		}

		for (MemberObject member : event.members) {
			IUser user = DiscordUtils.getUserFromGuildMemberResponse(guildToUpdate, member);
			guildToUpdate.addUser(user);
		}
		if (guildToUpdate.getUsers().size() == guildToUpdate.getTotalMemberCount()) {
			client.getDispatcher().dispatch(new AllUsersReceivedEvent(guildToUpdate));
		}
	}

	private void guildUpdate(GuildObject json) {
		Guild toUpdate = (Guild) client.getGuildByID(json.id);

		if (toUpdate != null) {
			IGuild oldGuild = toUpdate.copy();

			toUpdate = (Guild) DiscordUtils.getGuildFromJSON(shard, json);

			if (!toUpdate.getOwnerID().equals(oldGuild.getOwnerID())) {
				client.dispatcher.dispatch(new GuildTransferOwnershipEvent(oldGuild.getOwner(), toUpdate.getOwner(), toUpdate));
			} else {
				client.dispatcher.dispatch(new GuildUpdateEvent(oldGuild, toUpdate));
			}
		}
	}

	private void guildRoleCreate(GuildRoleEventResponse event) {
		IGuild guild = client.getGuildByID(event.guild_id);
		if (guild != null) {
			IRole role = DiscordUtils.getRoleFromJSON(guild, event.role);
			client.dispatcher.dispatch(new RoleCreateEvent(role, guild));
		}
	}

	private void guildRoleUpdate(GuildRoleEventResponse event) {
		IGuild guild = client.getGuildByID(event.guild_id);
		if (guild != null) {
			IRole toUpdate = guild.getRoleByID(event.role.id);
			if (toUpdate != null) {
				IRole oldRole = toUpdate.copy();
				toUpdate = DiscordUtils.getRoleFromJSON(guild, event.role);
				client.dispatcher.dispatch(new RoleUpdateEvent(oldRole, toUpdate, guild));

				if (guild.getRolesForUser(client.getOurUser()).contains(toUpdate))
					((Guild) guild).loadWebhooks();
			}
		}
	}

	private void guildRoleDelete(GuildRoleDeleteEventResponse event) {
		IGuild guild = client.getGuildByID(event.guild_id);
		if (guild != null) {
			IRole role = guild.getRoleByID(event.role_id);
			if (role != null) {
				guild.getRoles().remove(role);
				client.dispatcher.dispatch(new RoleDeleteEvent(role, guild));
			}
		}
	}

	private void guildBanAdd(GuildBanEventResponse event) {
		IGuild guild = client.getGuildByID(event.guild_id);
		if (guild != null) {
			IUser user = DiscordUtils.getUserFromJSON(shard, event.user);
			if (client.getUserByID(user.getID()) != null) {
				guild.getUsers().remove(user);
				((Guild) guild).getJoinTimes().remove(user);
			}

			client.dispatcher.dispatch(new UserBanEvent(user, guild));
		}
	}

	private void guildBanRemove(GuildBanEventResponse event) {
		IGuild guild = client.getGuildByID(event.guild_id);
		if (guild != null) {
			IUser user = DiscordUtils.getUserFromJSON(shard, event.user);

			client.dispatcher.dispatch(new UserPardonEvent(user, guild));
		}
	}

	private void voiceStateUpdate(VoiceStateObject json) {
		IGuild guild = client.getGuildByID(json.guild_id);

		if (guild != null) {
			IVoiceChannel channel = guild.getVoiceChannelByID(json.channel_id);
			User user = (User) guild.getUserByID(json.user_id);
			if (user != null) {
				user.setIsDeaf(guild.getID(), json.deaf);
				user.setIsMute(guild.getID(), json.mute);
				user.setIsDeafLocally(json.self_deaf);
				user.setIsMutedLocally(json.self_mute);

				IVoiceChannel oldChannel = user.getConnectedVoiceChannels()
						.stream()
						.filter(vChannel -> vChannel.getGuild().getID().equals(json.guild_id))
						.findFirst()
						.orElse(null);
				if (oldChannel == null)
					oldChannel = user.getConnectedVoiceChannels()
							.stream()
							.findFirst()
							.orElse(null);
				if (channel != oldChannel) {
					if (channel == null) {
						client.dispatcher.dispatch(new UserVoiceChannelLeaveEvent(user, oldChannel));
						user.getConnectedVoiceChannels().remove(oldChannel);
					} else if (oldChannel != null && oldChannel.getGuild().equals(channel.getGuild())) {
						client.dispatcher.dispatch(new UserVoiceChannelMoveEvent(user, oldChannel, channel));
						user.getConnectedVoiceChannels().remove(oldChannel);
						if (!user.getConnectedVoiceChannels().contains(channel))
							user.getConnectedVoiceChannels().add(channel);
					} else {
						client.dispatcher.dispatch(new UserVoiceChannelJoinEvent(user, channel));
						if (!user.getConnectedVoiceChannels().contains(channel))
							user.getConnectedVoiceChannels().add(channel);
					}
				}
			}
		}
	}

	private void voiceServerUpdate(VoiceUpdateResponse event) {
		try {
			event.endpoint = event.endpoint.substring(0, event.endpoint.indexOf(":"));
			client.voiceConnections.put(client.getGuildByID(event.guild_id), new DiscordVoiceWS(event, shard));
		} catch (Exception e) {
			Discord4J.LOGGER.error(LogMarkers.VOICE_WEBSOCKET, "Discord4J Internal Exception", e);
		}
	}

	private void guildEmojisUpdate(GuildEmojiUpdateResponse event) {
		IGuild guild = client.getGuildByID(event.guild_id);
		if (guild != null) {
			List<IEmoji> oldList = guild.getEmojis().stream().map(IEmoji::copy)
					.collect(Collectors.toCollection(CopyOnWriteArrayList::new));

			guild.getEmojis().clear();
			for (EmojiObject obj : event.emojis) {
				guild.getEmojis().add(DiscordUtils.getEmojiFromJSON(guild, obj));
			}

			client.dispatcher.dispatch(new GuildEmojisUpdateEvent(guild, oldList, guild.getEmojis()));
		}
	}

	private void reactionAdd(ReactionEventResponse event) {
		IChannel channel = client.getChannelByID(event.channel_id);
		if (channel != null) {
			IMessage message = channel.getMessageByID(event.message_id);

			if (message != null) {
				Reaction reaction = (Reaction) (event.emoji.id == null
						? message.getReactionByName(event.emoji.name)
						: message.getReactionByIEmoji(message.getGuild().getEmojiByID(event.emoji.id)));
				IUser user = message.getClient().getUserByID(event.user_id);

				if (reaction == null) {
					List<IUser> list = new CopyOnWriteArrayList<>();
					list.add(user);

					reaction = new Reaction(message.getShard(), 1, list,
							event.emoji.id != null ? event.emoji.id : event.emoji.name, event.emoji.id != null);

					message.getReactions().add(reaction);
				} else {
					reaction.getCachedUsers().add(user);
					reaction.setCount(reaction.getCount() + 1);
				}

				reaction.setMessage(message);

				client.dispatcher.dispatch(
						new ReactionAddEvent(message, reaction, user));
			}
		}
	}

	private void reactionRemove(ReactionEventResponse event) {
		IChannel channel = client.getChannelByID(event.channel_id);
		if (channel != null) {
			IMessage message = channel.getMessageByID(event.message_id);

			if (message != null) {
				Reaction reaction = (Reaction) (event.emoji.id == null
						? message.getReactionByName(event.emoji.name)
						: message.getReactionByIEmoji(message.getGuild().getEmojiByID(event.emoji.id)));
				IUser user = message.getClient().getUserByID(event.user_id);

				if (reaction != null) {
					reaction.setMessage(message); // safeguard
					reaction.setCount(reaction.getCount() - 1);
					reaction.getCachedUsers().remove(user);

					if (reaction.getCount() <= 0) {
						message.getReactions().remove(reaction);
					}

					client.dispatcher.dispatch(new ReactionRemoveEvent(message, reaction, user));
				}
			}
		}
	}

	private void webhookUpdate(WebhookObject event) {
		Channel channel = (Channel) client.getChannelByID(event.channel_id);
		if (channel != null)
			channel.loadWebhooks();
	}
}
