package sx.blah.discord.api.internal.json.responses.voice;

public class VoiceReadyResponse {
	public int ssrc;
	public int port;
	public String[] modes;
	public int heartbeat_interval;
}
