package sx.blah.discord.api.internal.json.requests;

import sx.blah.discord.handle.obj.IRole;

/**
 * This request is sent to modify a user's roles.
 * Note: all fields are optional and do not all need to be serialized if they are not being changed.
 */
public class MemberEditRequest {

	/**
	 * Roles for the user to have.
	 */
	public String[] roles;

	/**
	 * Changes the user's nickname.
	 */
	public String nick;

	/**
	 * Whether to mute the user.
	 */
	public Boolean mute;

	/**
	 * Whether to deafen the user.
	 */
	public Boolean deaf;

	/**
	 * The voice channel to move the user to.
	 */
	public String channel_id;

	public MemberEditRequest(String[] roles) {
		this.roles = roles;
	}

	public MemberEditRequest(IRole[] roles) {
		this.roles = new String[roles.length];
		for (int i = 0; i < roles.length; i++)
			this.roles[i] = roles[i].getID();
	}

	public MemberEditRequest(String channelID) {
		this.channel_id = channelID;
	}

	public MemberEditRequest(boolean deafen) {
		this.deaf = deafen;
	}

	public MemberEditRequest(boolean mute, boolean overloadsSuck) {
		this.mute = mute;
	}

	public MemberEditRequest(String nick, boolean overloadsSuck) {
		this.nick = nick;
	}
}
