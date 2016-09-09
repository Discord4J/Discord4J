package sx.blah.discord.api.internal.json.requests;

/**
 * This is sent to create a new Discord application.
 */
public class ApplicationCreateRequest {

	/**
	 * The application name.
	 */
	public String name;

	public ApplicationCreateRequest(String name) {
		this.name = name;
	}
}
