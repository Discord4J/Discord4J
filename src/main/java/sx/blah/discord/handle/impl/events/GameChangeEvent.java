package sx.blah.discord.handle.impl.events;

import sx.blah.discord.handle.Event;
import sx.blah.discord.handle.obj.Guild;
import sx.blah.discord.handle.obj.User;

import java.util.Optional;

/**
 * This event is dispatched when the game a user is playing is changed.
 */
public class GameChangeEvent extends Event {
	
	private final Guild guild;
	private final User user;
	private final Optional<String>  oldGame, newGame;
	
	public GameChangeEvent(Guild guild, User user, Optional<String> oldGame, Optional<String>  newGame) {
		this.guild = guild;
		this.user = user;
		this.oldGame = oldGame;
		this.newGame = newGame;
	}
	
	/**
	 * Gets the new game played.
	 * 
	 * @return The new game, or no value if the user isn't play a game.
	 */
	public Optional<String>  getNewGame() {
		return newGame;
	}
	
	/**
	 * Gets the user involved.
	 * 
	 * @return The user.
	 */
	public User getUser() {
		return user;
	}
	
	/**
	 * Gets the old game played.
	 * 
	 * @return The old game, or no value if the user wasn't plaing a game.
	 */
	public Optional<String>  getOldGame() {
		return oldGame;
	}
	
	/**
	 * Gets the guild involved.
	 * 
	 * @return The guild.
	 */
	public Guild getGuild() {
		return guild;
	}
	
	
}
