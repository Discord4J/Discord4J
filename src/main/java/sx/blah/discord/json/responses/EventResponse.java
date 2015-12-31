package sx.blah.discord.json.responses;

/**
 * A generic json for receiving events
 */
public class EventResponse {
	
	/**
	 * The event type
	 */
	public String t;
	
	/**
	 * From @Voltana "s is the sequence value - it's used for getting all messages you've missed after a gateway redirect"
	 */
	public int s;
	
	/**
	 * The opcode for the event, TODO: list all the opcode meanings
	 */
	public int op;
	
	/**
	 * The error message, or null if no error exists
	 */
	public String message;
	
	/**
	 * The event object
	 */
	public Object d;
}
