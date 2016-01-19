package sx.blah.discord.json.requests;

/**
 * This is sent to request that a guild be edited.
 */
public class EditGuildRequest {
	
	/**
	 * The new name of the guild.
	 */
	public String name;
	
	/**
	 * The new region for the guild.
	 */
	public String region;
	
	/**
	 * The new icon.
	 */
	public String icon;
	
	/**
	 * The afk channel id.
	 */
	public String afk_channel_id;
	
	/**
	 * The afk timeout.
	 */
	public int afk_timeout;
	
	public EditGuildRequest(String name, String region, String icon, String afk_channel_id, int afk_timeout) {
		this.name = name;
		this.region = region;
		this.icon = icon;
		this.afk_channel_id = afk_channel_id;
		this.afk_timeout = afk_timeout;
	}
}
