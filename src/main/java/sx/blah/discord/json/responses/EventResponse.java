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
	 * The opcode for the event. From: https://github.com/RogueException/Discord.Net/blob/dev/src/Discord.Net/API/Client/GatewaySocket/OpCodes.cs
	 * 0 = To send events to client
	 * 1 = To send heartbeat
	 * 2 = To authenticate a token with the server
	 * 3 = To send an update on the client's idle and game playing status to the socket
	 * 4 = To send a voice state update to the socket TODO: Implement
	 * 5 = To ensure the voice server is still up, only use if the voice connection fails/drops TODO: Implement
	 * 6 = Resume a connection after a redirect occurs
	 * 7 = Notifies the client to redirect to another gateway
	 * 8 = To request all guild member withheld by large_threshold
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
