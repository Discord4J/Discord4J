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
	private final String oldGame, newGame;
	
	public GameChangeEvent(Guild guild, User user, String oldGame, String newGame) {
		this.guild = guild;
		this.user = user;
		this.oldGame = oldGame;
		this.newGame = newGame;
	}
	
	public String getNewGame() {
		return newGame;
	}
	
	public User getUser() {
		return user;
	}
	
	public String getOldGame() {
		return oldGame;
	}
	
	public Guild getGuild() {
		return guild;
	}
	
	
}
