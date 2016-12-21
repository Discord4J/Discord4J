package sx.blah.discord.api.internal.json.requests;

import sx.blah.discord.api.internal.json.objects.EmbedObject;

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

	public EmbedObject embed;

	public MessageRequest(String content, EmbedObject embed, boolean tts) {
		this.content = content;
		this.tts = tts;
		this.embed = embed;
	}
}
