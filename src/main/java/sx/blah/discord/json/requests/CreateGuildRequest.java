package sx.blah.discord.json.requests;

import sx.blah.discord.api.IDiscordClient;

/**
 * This is sent to create a new guild.
 */
public class CreateGuildRequest {
	
	/**
	 * The name of the guild.
	 */
	public String name;
	
	/**
	 * The region for this guild. (OPTIONAL)
	 */
	public String region;
	
	/**
	 * The encoded icon for this guild. (OPTIONAL)
	 */
	public String icon;
	
	public CreateGuildRequest(String name, String region, IDiscordClient.Image icon) {
		this.name = name;
		this.region = region;
		this.icon = icon == null ? null : icon.getData();
	}
}
