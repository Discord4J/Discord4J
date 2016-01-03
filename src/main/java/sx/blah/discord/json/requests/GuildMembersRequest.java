package sx.blah.discord.json.requests;

/**
 * Used to request the offline guild members if a guild is "large"
 */
public class GuildMembersRequest {
	
	/**
	 * The opcode for this request, always 8
	 */
	public int op = 8;
	
	/**
	 * The object containing all the request data
	 */
	public RequestObject d;
	
	public GuildMembersRequest(String guild_id) {
		d = new RequestObject(guild_id);
	}
	
	public static class RequestObject {
		
		/**
		 * The guild's id
		 */
		public String guild_id;
		
		/**
		 * FIXME ??
		 */
		public String query = "";
		
		/**
		 * The limit on users to receive??
		 */
		public int limit = 0;
		
		public RequestObject(String guild_id) {
			this.guild_id = guild_id;
		}
	}
}
