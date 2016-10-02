package sx.blah.discord.api.internal.json.requests.voice;

/**
 * Requests send to the server to tell it's about to receive audio, or telling it the client is going to stop sending audio
 */
public class VoiceSpeakingRequest {

	public int delay = 0;
	public boolean speaking;

	public VoiceSpeakingRequest(boolean speaking) {
		this.speaking = speaking;
	}
}
