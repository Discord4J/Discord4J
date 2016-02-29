package sx.blah.discord.json.requests;

/**
 * This represents a single object which composes the reorder roles operation array parameter.
 */
public class ReorderRolesRequest {

	/**
	 * The id of the role.
	 */
	public String id;

	/**
	 * The new position of this role.
	 */
	public int position;

	public ReorderRolesRequest(String id, int position) {
		this.id = id;
		this.position = position;
	}
}
