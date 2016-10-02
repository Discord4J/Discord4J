package sx.blah.discord.api.internal.json.objects;

public class GuildObject {
	public String id;
	public String name;
	public String icon;
	public String owner_id;
	public String region;
	public String afk_channel_id;
	public int afk_timeout;
	public boolean embed_enabled;
	public String embed_channel_id;
	public int verification_level;
	public int default_messages_notifications;
	public RoleObject[] roles;
	public EmojiObject[] emojis;
	public String[] features;
	public int mfa_level;
	public String joined_at;
	public boolean large;
	public boolean unavailable;
	public int member_count;
	public VoiceStateObject[] voice_states;
	public MemberObject[] members;
	public ChannelObject[] channels;
	public PresenceObject[] presences;
}
