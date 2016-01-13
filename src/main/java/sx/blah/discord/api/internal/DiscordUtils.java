package sx.blah.discord.api.internal;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.message.BasicNameValuePair;
import sx.blah.discord.api.DiscordEndpoints;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.Channel;
import sx.blah.discord.handle.obj.Invite;
import sx.blah.discord.handle.obj.Message;
import sx.blah.discord.handle.obj.User;
import sx.blah.discord.json.responses.InviteJSONResponse;
import sx.blah.discord.json.responses.MessageResponse;
import sx.blah.discord.json.responses.UserResponse;
import sx.blah.discord.util.HTTP403Exception;
import sx.blah.discord.util.Presences;
import sx.blah.discord.util.Requests;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
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
					+ "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*"
					+ "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)",
			Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
	
	/**
	 * Gets the last 50 messages from a given channel ID.
	 *
	 * @param client The discord client to use
	 * @param channel The channel to get messages from.
	 * 
	 * @throws IOException
	 */
	public static void getChannelMessages(IDiscordClient client, Channel channel) throws IOException, HTTP403Exception {
		String response = Requests.GET.makeRequest(DiscordEndpoints.CHANNELS + channel.getID() + "/messages?limit=50",
				new BasicNameValuePair("authorization", client.getToken()));
		MessageResponse[] messages = GSON.fromJson(response, MessageResponse[].class);
		
		for (MessageResponse message : messages) {
			channel.addMessage(new Message(client, message.id,
					message.content, client.getUserByID(message.author.id), channel, 
					convertFromTimestamp(message.timestamp), mentionsFromJSON(client, message), attachmentsFromJSON(message)));
		}
	}
	
	/**
	 * Converts a String timestamp into a java object timestamp.
	 *
	 * @param time The String timestamp.
	 * @return The java object representing the timestamp.
	 */
	public static LocalDateTime convertFromTimestamp(String time) {
		return LocalDateTime.parse(time.split("\\+")[0]).atZone(ZoneId.of("UTC+00:00")).withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
	}
	
	/**
	 * Returns a user from raw JSON data.
	 */
	public static User constructUserFromJSON(IDiscordClient client, String user) {
		UserResponse response = GSON.fromJson(user, UserResponse.class);
		
		return constructUserFromJSON(client, response);
	}
	
	/**
	 * Returns a user from the java form of the raw JSON data.
	 */
	public static User constructUserFromJSON(IDiscordClient client, UserResponse response) {
		User ourUser = new User(client, response.username, response.id, response.discriminator, response.avatar);
		ourUser.setPresence(Presences.ONLINE);
		
		return ourUser;
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
	public static Invite getInviteFromJSON(IDiscordClient client, InviteJSONResponse json) {
		return new Invite(client, json.code, json.xkcdpass);
	}
	
	/**
	 * Gets the users mentioned from a message json object.
	 * 
	 * @param client The discord client to use.
	 * @param json The json response to use.
	 * @return The list of mentioned users.
	 */
	public static List<User> mentionsFromJSON(IDiscordClient client, MessageResponse json) {
		List<User> mentions = new ArrayList<>();
		if (json.mention_everyone) {
			mentions = client.getChannelByID(json.channel_id).getGuild().getUsers();
		} else {
			for (UserResponse response : json.mentions)
				mentions.add(client.getUserByID(response.id));
		}
		
		return mentions;
	}
	
	/**
	 * Gets the attachments on a message.
	 * 
	 * @param json The json response to use.
	 * @return The attached messages.
	 */
	public static List<Message.Attachment> attachmentsFromJSON(MessageResponse json) {
		List<Message.Attachment> attachments = new ArrayList<>();
		for (MessageResponse.AttachmentResponse response : json.attachments) {
			attachments.add(new Message.Attachment(response.filename, response.size, response.id, response.url));
		}
		
		return attachments;
	}
}
