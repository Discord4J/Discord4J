package sx.blah.discord.api.internal.json.objects;

public class ChannelObject {
	public String id;
	public String guild_id;
	public String name;
	public String type;
	public int position;
	public boolean is_private;
	public OverwriteObject[] permission_overwrites;
	public String topic;
	public String last_message_id;
	public String last_pin_timestamp;
	public int bitrate;
	public int user_limit;
	public UserObject[] recipients;
}
