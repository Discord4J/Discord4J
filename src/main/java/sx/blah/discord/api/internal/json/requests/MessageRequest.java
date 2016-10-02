package sx.blah.discord.api.internal.json.requests;

/**
 * This request is sent to send a message
 */
public class MessageRequest {

	/**
	 * A unique ID assigned to this message. Has no real purpose and can be null.
	 */
	public String nonce;

	/**
	 * The content of the message
	 */
	public String content;

	/**
	 * Whether or not the message should use tts.
	 */
	public boolean tts = false;

	public MessageRequest(String content, boolean tts) {
		this.content = content;
		this.tts = tts;
	}
}
