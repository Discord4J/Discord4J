package sx.blah.discord.api.internal;

import org.apache.http.message.BasicNameValuePair;
import sx.blah.discord.api.DiscordEndpoints;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.Channel;
import sx.blah.discord.handle.obj.Message;
import sx.blah.discord.handle.obj.User;
import sx.blah.discord.json.responses.MessageResponse;
import sx.blah.discord.json.responses.UserResponse;
import sx.blah.discord.util.HTTP403Exception;
import sx.blah.discord.util.Presences;
import sx.blah.discord.util.Requests;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Collection of internal Discord4J utilities.
 */
public class DiscordUtils {
	
	/**
	 * Gets the last 50 messages from a given channel ID.
	 *
	 * @param client The discord client to use
	 * @param channel The channel to get messages from.
	 * @return Last 50 messages from the channel.
	 * 
	 * @throws IOException
	 */
	public static void getChannelMessages(IDiscordClient client, Channel channel) throws IOException, HTTP403Exception {
		String response = Requests.GET.makeRequest(DiscordEndpoints.CHANNELS + channel.getID() + "/messages?limit=50",
				new BasicNameValuePair("authorization", client.getToken()));
		MessageResponse[] messages = DiscordClientImpl.GSON.fromJson(response, MessageResponse[].class);
		
		for (MessageResponse message : messages) {
			channel.addMessage(new Message(client, message.id,
					message.content, client.getUserByID(message.author.id), channel, convertFromTimestamp(message.timestamp)));
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
		UserResponse response = DiscordClientImpl.GSON.fromJson(user, UserResponse.class);
		
		return constructUserFromJSON(client, response);
	}
	
	/**
	 * Returns a user from the java form of the raw JSON data.
	 */
	public static User constructUserFromJSON(IDiscordClient client, UserResponse response) {
		User ourUser = new User(client, response.username, response.id, response.avatar);
		ourUser.setPresence(Presences.ONLINE);
		
		return ourUser;
	}
}
