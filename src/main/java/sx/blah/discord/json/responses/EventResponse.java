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
	 * Should be 1 FIXME ???
	 */
	public int s;
	
	/**
	 * Should be 0 FIXME ???
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
