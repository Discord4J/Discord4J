package sx.blah.discord.handle.impl.events.guild.member;

import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

import java.util.Optional;

/**
 * This is fired once a nickname is changed.
 */
public class NickNameChangeEvent extends GuildMemberEvent {
	
	private final String oldNickname, newNickname;
	
	public NickNameChangeEvent(IGuild guild, IUser user, String oldNickname, String newNickname) {
		super(guild, user);
		this.oldNickname = oldNickname;
		this.newNickname = newNickname;
	}
	
	/**
	 * This gets the old nickname.
	 *
	 * @return The old nickname.
	 */
	public Optional<String> getOldNickname() {
		return Optional.ofNullable(oldNickname);
	}
	
	/**
	 * This gets the new nickname.
	 *
	 * @return The new nickname.
	 */
	public Optional<String> getNewNickname() {
		return Optional.ofNullable(newNickname);
	}
}
