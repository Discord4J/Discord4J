package sx.blah.discord.api.internal.json.requests.voice;

/**
 * Used to indicate whether or not the bot is transmitting audio.
 */
public class VoiceSpeakingRequest {

	public int delay = 0;
	public boolean speaking;

	public VoiceSpeakingRequest(boolean speaking) {
		this.speaking = speaking;
	}
}
