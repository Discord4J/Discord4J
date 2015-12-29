package sx.blah.discord.json.requests;

/**
 * This request is sent to send a message
 */
public class MessageRequest {
	
	/**
	 * The content of the message
	 */
	public String content;
	
	/**
	 * The users mentioned in the message TODO: Remove
	 */
	@Deprecated
	public String[] mentions;
	
	public MessageRequest(String content, String[] mentions) {
		this.content = content;
		this.mentions = mentions;
	}
}
