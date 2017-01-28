package sx.blah.discord.handle.impl.events;

import sx.blah.discord.handle.impl.events.guild.member.NicknameChangedEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

/**
 * This is fired once a nickname is changed.
 * @deprecated Use {@link NicknameChangedEvent} instead.
 */
@Deprecated
public class NickNameChangeEvent extends NicknameChangedEvent {

	public NickNameChangeEvent(IGuild guild, IUser user, String oldNickname, String newNickname) {
		super(guild, user, oldNickname, newNickname);
	}
}
