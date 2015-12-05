package sx.blah.discord.handle.impl.events;

import sx.blah.discord.handle.IEvent;
import sx.blah.discord.handle.obj.Guild;
import sx.blah.discord.handle.obj.User;

/**
 * @author austinv11
 * @since 9:10 PM, 12/4/15
 * Project: Discord4J
 */
public class GameChangeEvent implements IEvent {
	private final Guild guild;
	private final User user;
	private final Long oldGameID, newGameID;
	
	public GameChangeEvent(Guild guild, User user, Long oldGameID, Long newGameID) {
		this.guild = guild;
		this.user = user;
		this.oldGameID = oldGameID;
		this.newGameID = newGameID;
	}
	
	public long getNewGameID() {
		return newGameID;
	}
	
	public User getUser() {
		return user;
	}
	
	public long getOldGameID() {
		return oldGameID;
	}
	
	public Guild getGuild() {
		return guild;
	}
	
	
}
