package sx.blah.discord.api;

import sx.blah.discord.api.events.Event;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.api.internal.DiscordClientImpl;
import sx.blah.discord.util.DiscordException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Use this as a factory to create {@link IDiscordClient} instances
 */
public class ClientBuilder {
	
	private int maxMissedPings = -1;
	private String botToken;
	private boolean isDaemon = false;
	private int shardCount = 1;
	private int maxReconnectAttempts = 5;
	
	//Early registered listeners:
	private final List<IListener<? extends Event>> iListeners = new ArrayList<>();
	private final List<Object> listeners = new ArrayList<>();
	private final List<Class<?>> listenerClasses = new ArrayList<>();
	
	/**
	 * Provides the login info for the client.
	 *
	 * @param token The bot's token.
	 * @return The instance of the builder.
	 */
	public ClientBuilder withToken(String token) {
		this.botToken = token;
		return this;
	}
	
	/**
	 * Gets the provided token.
	 *
	 * @return The provided token.
	 */
	public String getToken() {
		return botToken;
	}
	
	/**
	 * Makes the client have a ping timeout.
	 *
	 * @param maxMissedPings The maximum amount of pings that discord can not respond to before a new session is created.
	 * @return The instance of the builder.
	 */
	public ClientBuilder withPingTimeout(int maxMissedPings) {
		this.maxMissedPings = maxMissedPings;
		return this;
	}
	
	/**
	 * Sets whether the client should act as a daemon (it is NOT a daemon by default).
	 *
	 * @param isDaemon If true, the client will not stop the JVM from closing until the client is logged out from. If false
	 * the client will stop the JVM from closing until logged out from.
	 * @return The instance of the builder.
	 */
	public ClientBuilder setDaemon(boolean isDaemon) {
		this.isDaemon = isDaemon;
		return this;
	}
	
	/**
	 * Sets the sharding information for the client.
	 * @param shardCount The total number of shards that will be created.
	 * @return The instance of the builder.
	 */
	public ClientBuilder withShards(int shardCount) {
		this.shardCount = shardCount;
		return this;
	}
	
	/**
	 * Sets the max amount of attempts shards managed by this client will make to reconnect in the event of an
	 * unexpected disconnection.
	 *
	 * @param maxReconnectAttempts The max amount of attempts before the shard is abandoned.
	 * @return The instance of the builder.
	 */
	public ClientBuilder setMaxReconnectAttempts(int maxReconnectAttempts) {
		this.maxReconnectAttempts = maxReconnectAttempts;
		return this;
	}
	
	/**
	 * This registers event listeners before the client is logged in.
	 *
	 * @param listeners The listeners to register.
	 * @return The instance of the builder.
	 */
	@SafeVarargs
	public final ClientBuilder registerListeners(IListener<? extends Event>... listeners) {
		iListeners.addAll(Arrays.asList(listeners));
		return this;
	}
	
	/**
	 * This registers event listeners before the client is logged in.
	 *
	 * @param listeners The listeners to register.
	 * @return The instance of the builder.
	 */
	public ClientBuilder registerListeners(Object... listeners) {
		this.listeners.addAll(Arrays.asList(listeners));
		return this;
	}
	
	/**
	 * This registers event listeners before the client is logged in.
	 *
	 * @param listeners The listeners to register.
	 * @return The instance of the builder.
	 */
	public ClientBuilder registerListeners(Class<?>... listeners) {
		listenerClasses.addAll(Arrays.asList(listeners));
		return this;
	}
	
	/**
	 * This registers an event listeners before the client is logged in.
	 *
	 * @param listener The listener to register.
	 * @return The instance of the builder.
	 */
	public ClientBuilder registerListener(IListener<? extends Event> listener) {
		return registerListeners(listener);
	}
	
	/**
	 * This registers an event listeners before the client is logged in.
	 *
	 * @param listener The listener to register.
	 * @return The instance of the builder.
	 */
	public ClientBuilder registerListener(Object listener) {
		return registerListeners(listener);
	}
	
	/**
	 * This registers an event listeners before the client is logged in.
	 *
	 * @param listener The listener to register.
	 * @return The instance of the builder.
	 */
	public ClientBuilder registerListener(Class<?> listener) {
		return registerListeners(listener);
	}
	
	/**
	 * Creates the discord instance with the desired features
	 *
	 * @return The discord instance
	 *
	 * @throws DiscordException Thrown if the instance isn't built correctly
	 */
	public IDiscordClient build() throws DiscordException {
		if (botToken == null)
			throw new DiscordException("No login info present!");
		
		final IDiscordClient client = new DiscordClientImpl(botToken, shardCount, isDaemon, maxMissedPings, maxReconnectAttempts);
		
		//Registers events as soon as client is initialized
		final EventDispatcher dispatcher = client.getDispatcher();
		iListeners.forEach(dispatcher::registerListener);
		listeners.forEach(dispatcher::registerListener);
		listenerClasses.forEach(dispatcher::registerListener);
		
		return client;
	}
	
	/**
	 * Performs {@link #build()} and logs in automatically
	 *
	 * @return The discord instance
	 *
	 * @throws DiscordException Thrown if the instance isn't built correctly
	 */
	public IDiscordClient login() throws DiscordException {
		IDiscordClient client = build();
		try {
			client.login();
		} catch (Exception e) {
			throw new DiscordException("Exception ("+e.getClass().getSimpleName()+") occurred while logging in: "+e.getMessage());
		}
		return client;
	}
}
