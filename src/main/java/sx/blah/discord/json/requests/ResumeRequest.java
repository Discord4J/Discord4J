package sx.blah.discord.json.requests;

/**
 * This request is sent to resume a connection after being redirected.
 */
public class ResumeRequest {
	
	/**
	 * The session id to resume.
	 */
	public String session_id;
	
	/**
	 * This is the last cached value of {@link sx.blah.discord.json.responses.EventResponse#s}.
	 */
	public long seq;
	
	public ResumeRequest(String session_id, long seq) {
		this.session_id = session_id;
		this.seq = seq;
	}
}
