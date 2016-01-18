package sx.blah.discord.api.internal;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.message.BasicNameValuePair;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.DiscordEndpoints;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.obj.*;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.json.generic.PermissionOverwrite;
import sx.blah.discord.json.generic.RoleResponse;
import sx.blah.discord.json.requests.GuildMembersRequest;
import sx.blah.discord.json.responses.*;
import sx.blah.discord.util.HTTP403Exception;
import sx.blah.discord.util.Requests;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
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
	 * Used to find urls in order to not escape them
	 */
	private static final Pattern urlPattern = Pattern.compile(
			"(?:^|[\\W])((ht|f)tp(s?):\\/\\/|www\\.)"
					+"(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*"
					+"[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)",
			Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
	
	/**
	 * Gets the last 50 messages from a given channel ID.
	 *
	 * @param client The discord client to use
	 * @param channel The channel to get messages from.
	 * @throws IOException
	 */
	public static void getChannelMessages(IDiscordClient client, Channel channel) throws IOException, HTTP403Exception {
		String response = Requests.GET.makeRequest(DiscordEndpoints.CHANNELS+channel.getID()+"/messages?limit=50",
				new BasicNameValuePair("authorization", client.getToken()));
		MessageResponse[] messages = GSON.fromJson(response, MessageResponse[].class);
		
		for (MessageResponse message : messages) {
			channel.addMessage(new Message(client, message.id,
					message.content, client.getUserByID(message.author.id), channel,
					convertFromTimestamp(message.timestamp), message.mention_everyone, getMentionsFromJSON(client, message), getAttachmentsFromJSON(message)));
		}
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
		return LocalDateTime.parse(time.split("\\+")[0]).atZone(ZoneId.of("UTC+00:00")).withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
	}
	
	/**
	 * Returns a user from raw JSON data.
	 */
	public static IUser getUserFromJSON(IDiscordClient client, String user) {
		UserResponse response = GSON.fromJson(user, UserResponse.class);
		
		return getUserFromJSON(client, response);
	}
	
	/**
	 * Returns a user from the java form of the raw JSON data.
	 */
	public static User getUserFromJSON(IDiscordClient client, UserResponse response) {
		User user;
		if ((user = (User) client.getUserByID(response.id)) != null) {
			user.setAvatar(response.avatar);
			user.setName(response.username);
		} else {
			user = new User(client, response.username, response.id, response.discriminator, response.avatar, Presences.OFFLINE);
		}
		return user;
	}
	
	/**
	 * Escapes a string to ensure that the Discord websocket receives it correctly.
	 *
	 * @param string The string to escape
	 * @return The escaped string
	 *
	 * @deprecated No longer required for discord to handle special characters
	 */
	@Deprecated
	public static String escapeString(String string) {
		//All this weird regex stuff is to prevent any urls from being escaped and therefore breaking them
		List<String> urls = new ArrayList<>();
		Matcher matcher = urlPattern.matcher(string);
		while (matcher.find()) {
			int matchStart = matcher.start(1);
			int matchEnd = matcher.end();
			String url = string.substring(matchStart, matchEnd);
			urls.add(url);
			string = matcher.replaceFirst("@@URL"+(urls.size()-1)+"@@");//Hopefully no one will ever want to send a message with @@URL#@@
		}
		
		string = StringEscapeUtils.escapeJson(string);
		
		for (int i = 0; i < urls.size(); i++) {
			string = string.replace("@@URL"+i+"@@", " "+urls.get(i));
		}
		
		return string;
	}
	
	/**
	 * Creates a java {@link Invite} object for a json response.
	 *
	 * @param client The discord client to use.
	 * @param json The json response to use.
	 * @return The java invite object.
	 */
	public static IInvite getInviteFromJSON(IDiscordClient client, InviteJSONResponse json) {
		return new Invite(client, json.code, json.xkcdpass);
	}
	
	/**
	 * Gets the users mentioned from a message json object.
	 *
	 * @param client The discord client to use.
	 * @param json The json response to use.
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
	 * Creates a guild object from a json response.
	 *
	 * @param client The discord client.
	 * @param json The json response.
	 * @return The guild object.
	 */
	public static IGuild getGuildFromJSON(IDiscordClient client, GuildResponse json) {
		Guild guild;
		
		if ((guild = (Guild) client.getGuildByID(json.id)) != null) {
			guild.setIcon(json.icon);
			guild.setName(json.name);
			
			List<IRole> newRoles = new ArrayList<>();
			for (RoleResponse roleResponse : json.roles) {
				newRoles.add(getRoleFromJSON(guild, roleResponse));
			}
			guild.getRoles().clear();
			guild.getRoles().addAll(newRoles);
			
			for (IUser user : guild.getUsers()) { //Removes all deprecated roles
				for (IRole role : user.getRolesForGuild(guild.getID())) {
					if (guild.getRoleForId(role.getID()) == null) {
						user.getRolesForGuild(guild.getID()).remove(role);
					}
				}
			}
		} else {
			guild = new Guild(client, json.name, json.id, json.icon, json.owner_id);
			
			for (RoleResponse roleResponse : json.roles) {
				guild.addRole(getRoleFromJSON(guild, roleResponse));
			}
			
			for (GuildResponse.MemberResponse member : json.members) {
				guild.addUser(getUserFromGuildMemberResponse(client, guild, member));
			}
			
			if (json.large) { //The guild is large, we have to send a request to get the offline users
				((DiscordClientImpl) client).ws.send(DiscordUtils.GSON.toJson(new GuildMembersRequest(json.id)));
			}
			
			for (PresenceResponse presence : json.presences) {
				User user = (User) guild.getUserByID(presence.user.id);
				user.setPresence(Presences.valueOf((presence.status).toUpperCase()));
				user.setGame(Optional.ofNullable(presence.game == null ? null : presence.game.name));
			}
			
			for (ChannelResponse channelResponse : json.channels) {
				String channelType = channelResponse.type;
				if ("text".equalsIgnoreCase(channelType)) {
					guild.addChannel(getChannelFromJSON(client, guild, channelResponse));
				}
			}
		}
		
		return guild;
	}
	
	/**
	 * Creates a user object from a guild member json response.
	 *
	 * @param client The discord client.
	 * @param guild The guild the member belongs to.
	 * @param json The json response.
	 * @return The user object.
	 */
	public static IUser getUserFromGuildMemberResponse(IDiscordClient client, IGuild guild, GuildResponse.MemberResponse json) {
		User user = getUserFromJSON(client, json.user);
		for (String role : json.roles) {
			Role roleObj = (Role) guild.getRoleForId(role);
			if (roleObj != null && !user.getRolesForGuild(guild.getID()).contains(roleObj))
				user.addRole(guild.getID(), roleObj);
		}
		user.addRole(guild.getID(), guild.getRoleForId(guild.getID())); //@everyone role
		return user;
	}
	
	/**
	 * Creates a private channel object from a json response.
	 *
	 * @param client The discord client.
	 * @param json The json response.
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
			try {
				DiscordUtils.getChannelMessages(client, channel);
			} catch (HTTP403Exception e) {
				Discord4J.LOGGER.error("No permission for the private channel for \"{}\". Are you logged in properly?", channel.getRecipient().getName());
			} catch (Exception e) {
				Discord4J.LOGGER.error("Unable to get messages for the private channel for \"{}\" (Cause: {}).", channel.getRecipient().getName(), e.getClass().getSimpleName());
				e.printStackTrace();
			}
		}
		
		channel.setLastReadMessageID(json.last_message_id);
		
		return channel;
	}
	
	/**
	 * Creates a message object from a json response.
	 *
	 * @param client The discord client.
	 * @param channel The channel.
	 * @param json The json response.
	 * @return The message object.
	 */
	public static IMessage getMessageFromJSON(IDiscordClient client, IChannel channel, MessageResponse json) {
		Message message;
		if ((message = (Message) channel.getMessageByID(json.id)) != null) {
			message.setAttachments(getAttachmentsFromJSON(json));
			message.setContent(json.content);
			message.setMentionsEveryone(json.mention_everyone);
			message.setMentions(getMentionsFromJSON(client, json));
			message.setTimestamp(convertFromTimestamp(json.edited_timestamp == null ? json.timestamp : json.edited_timestamp));
			return message;
		} else
			return new Message(client, json.id, json.content, client.getUserByID(json.author.id),
					channel, convertFromTimestamp(json.timestamp), json.mention_everyone, getMentionsFromJSON(client, json),
					getAttachmentsFromJSON(json));
	}
	
	/**
	 * Creates a channel object from a json response.
	 *
	 * @param client The discord client.
	 * @param guild the guild.
	 * @param json The json response.
	 * @return The channel object.
	 */
	public static IChannel getChannelFromJSON(IDiscordClient client, IGuild guild, ChannelResponse json) {
		Channel channel;
		
		if ((channel = (Channel) guild.getChannelByID(json.id)) != null) {
			channel.setName(json.name);
			channel.setPosition(json.position);
			channel.setTopic(json.topic);
			HashMap<String, IChannel.PermissionOverride> userOverrides = new HashMap<>();
			HashMap<String, IChannel.PermissionOverride> roleOverrides = new HashMap<>();
			for (PermissionOverwrite overrides : json.permission_overwrites) {
				if (overrides.type.equalsIgnoreCase("role")) {
					if (channel.getRoleOverrides().containsKey(overrides.id)) {
						roleOverrides.put(overrides.id, channel.getRoleOverrides().get(overrides.id));
					} else {
						roleOverrides.put(overrides.id, new IChannel.PermissionOverride(
								Permissions.getAllPermissionsForNumber(overrides.allow),
								Permissions.getAllPermissionsForNumber(overrides.deny)));
					}
				} else if (overrides.type.equalsIgnoreCase("member")) {
					if (channel.getUserOverrides().containsKey(overrides.id)) {
						userOverrides.put(overrides.id, channel.getUserOverrides().get(overrides.id));
					} else {
						userOverrides.put(overrides.id, new IChannel.PermissionOverride(
								Permissions.getAllPermissionsForNumber(overrides.allow),
								Permissions.getAllPermissionsForNumber(overrides.deny)));
					}
				} else {
					Discord4J.LOGGER.warn("Unknown permissions overwrite type \"{}\"!", overrides.type);
				}
			}
			channel.getUserOverrides().clear();
			channel.getUserOverrides().putAll(userOverrides);
			channel.getRoleOverrides().clear();
			channel.getRoleOverrides().putAll(roleOverrides);
		} else {
			channel = new Channel(client, json.name, json.id, guild, json.topic, json.position);
			
			try {
				DiscordUtils.getChannelMessages(client, channel);
			} catch (HTTP403Exception e) {
				Discord4J.LOGGER.error("No permission for channel \"{}\" in guild \"{}\". Are you logged in properly?", json.name, guild.getName());
			} catch (Exception e) {
				Discord4J.LOGGER.error("Unable to get messages for channel \"{}\" in guild \"{}\" (Cause: {}).", json.name, guild.getName(), e.getClass().getSimpleName());
				e.printStackTrace();
			}
			
			for (PermissionOverwrite overrides : json.permission_overwrites) {
				IChannel.PermissionOverride override = new IChannel.PermissionOverride(
						Permissions.getAllPermissionsForNumber(overrides.allow),
						Permissions.getAllPermissionsForNumber(overrides.deny));
				if (overrides.type.equalsIgnoreCase("role")) {
					channel.addRoleOverride(overrides.id, override);
				} else if (overrides.type.equalsIgnoreCase("member")) {
					channel.addUserOverride(overrides.id, override);
				} else {
					Discord4J.LOGGER.warn("Unknown permissions overwrite type \"{}\"!", overrides.type);
				}
			}
		}
		
		channel.setLastReadMessageID(json.last_message_id);
		
		return channel;
	}
	
	public static IRole getRoleFromJSON(IGuild guild, RoleResponse json) {
		Role role;
		if ((role = (Role) guild.getRoleForId(json.id)) != null) {
			role.setColor(json.color);
			role.setHoist(json.hoist);
			role.setName(json.name);
			role.setPermissions(json.permissions);
			role.setPosition(json.position);
		} else {
			role = new Role(json.position, json.permissions, json.name, json.managed, json.id, json.hoist, json.color, guild);
		}
		return role;
	}
}
