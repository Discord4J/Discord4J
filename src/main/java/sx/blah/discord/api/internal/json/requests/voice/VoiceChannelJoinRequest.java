package sx.blah.discord.api.internal.json.requests.voice;

public class VoiceChannelJoinRequest {

	public String guild_id;
	public String channel_id;
	public boolean self_mute;
	public boolean self_deaf;

	public VoiceChannelJoinRequest(String guild_id, String channel_id, boolean self_mute, boolean self_deaf) {
		this.guild_id = guild_id;
		this.channel_id = channel_id;
		this.self_mute = self_mute;
		this.self_deaf = self_deaf;
	}
}
