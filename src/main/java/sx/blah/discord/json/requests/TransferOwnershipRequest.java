package sx.blah.discord.json.requests;

/**
 * This request is sent to transfer the ownership of a guild.
 */
public class TransferOwnershipRequest {
	
	/**
	 * The new owner's id.
	 */
	public String owner_id;
	
	public TransferOwnershipRequest(String owner_id) {
		this.owner_id = owner_id;
	}
}
