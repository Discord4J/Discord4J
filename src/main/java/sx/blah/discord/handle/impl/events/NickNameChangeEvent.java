package sx.blah.discord.handle.impl.events;

import sx.blah.discord.api.events.Event;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

import java.util.Optional;

/**
 * This is fired once a nickname is changed.
 */
public class NickNameChangeEvent extends Event {
	
	private final IGuild guild;
	private final IUser user;
	private final String oldNickname, newNickname;
	
	public NickNameChangeEvent(IGuild guild, IUser user, String oldNickname, String newNickname) {
		this.guild = guild;
		this.user = user;
		this.oldNickname = oldNickname;
		this.newNickname = newNickname;
	}
	
	/**
	 * This gets the guild involved.
	 *
	 * @return The guild.
	 */
	public IGuild getGuild() {
		return guild;
	}
	
	/**
	 * This gets the user involved.
	 *
	 * @return The user.
	 */
	public IUser getUser() {
		return user;
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
