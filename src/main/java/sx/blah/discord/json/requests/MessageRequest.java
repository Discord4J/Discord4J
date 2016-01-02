package sx.blah.discord.json.requests;

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
	 * The users mentioned in the message TODO: Remove
	 */
	@Deprecated
	public String[] mentions;
	
	/**
	 * Whether or not the message should use tts.
	 */
	public boolean tts = false;
	
	public MessageRequest(String content, String[] mentions, boolean tts) {
		this.content = content;
		this.mentions = mentions;
		this.tts = tts;
	}
}
