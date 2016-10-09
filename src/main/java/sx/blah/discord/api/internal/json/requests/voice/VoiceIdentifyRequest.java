package sx.blah.discord.api.internal.json.requests.voice;

public class VoiceIdentifyRequest {
	private String server_id;
	private String user_id;
	private String session_id;
	private String token;

	public VoiceIdentifyRequest(String server_id, String user_id, String session_id, String token) {
		this.server_id = server_id;
		this.user_id = user_id;
		this.session_id = session_id;
		this.token = token;
	}
}
