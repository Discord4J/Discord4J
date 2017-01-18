package sx.blah.discord.api;

import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.api.internal.DiscordClientImpl;
import sx.blah.discord.util.DiscordException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import sx.blah.discord.modules.Configuration;
import sx.blah.discord.modules.ModuleLoader;

/**
 * Use this as a factory to create {@link IDiscordClient} instances
 */
public class ClientBuilder {

        private boolean recommendShardCount = false;
	private int maxMissedPings = -1;
	private String botToken;
	private boolean isDaemon = false;
	private int shardCount = 1;
	private int maxReconnectAttempts = 5;
	private int retryCount = 5;

	//Early registered listeners:
	private final List<IListener> iListeners = new ArrayList<>();
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
         * Gets the current ping timeout.
         * 
         * @return The ping timeout.
         */
        public int getPingTimeout() {
                return maxMissedPings;
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
         * Gets the current daemon state.
         * 
         * @return The daemon state.
         */
        public boolean isDaemon() {
                return isDaemon;
        }

	/**
	 * Sets the number of shards to use when the client logs in.
         * 
	 * @param shardCount The total number of shards that will be created.
	 * @return The instance of the builder.
	 */
	public ClientBuilder withShards(int shardCount) {
		this.shardCount = shardCount;
		return this;
	}
        
        /**
         * Gets the current shard count.
         * 
         * @return The shard count.
         */
        public int getShardCount() {
                return shardCount;
        }
        
        /**
         * Sets whether the client should use the recommended shard count received when logging in. Note: enabling this
         * option will ignore the shard count set with {@link #withShards(int shardCount)}. This is false by default.
         * 
         * @param recommendShardCount True to use to recommended shard count, false otherwise.
         * @return The instance of the builder.
         */
        public ClientBuilder useRecommendedShardCount(boolean recommendShardCount) {
                this.recommendShardCount = recommendShardCount;
                return this;
        }
        
        /**
         * Gets the current state of using a recommended shard count.
         * 
         * @return True if using recommended shard count, false otherwise.
         */
        public boolean isRecommendingShardCount() {
                return recommendShardCount;
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
         * Gets the current maximum reconnect attempts.
         * 
         * @return The maximum reconnect attempts.
         */
        public int getMaxReconnectAttempts() {
                return maxReconnectAttempts;
        }

	/**
	 * This registers event listeners before the client is logged in.
	 *
	 * @param listeners The listeners to register.
	 * @return The instance of the builder.
	 */
	public final ClientBuilder registerListeners(IListener... listeners) {
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
	public ClientBuilder registerListener(IListener listener) {
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
	 * Sets the 5xx retry count. Default: 5
	 *
	 * @param retryCount The new retry count.
	 * @return The instance of the builder.
	 */
	public ClientBuilder set5xxRetryCount(int retryCount) {
		this.retryCount = retryCount;
		return this;
	}
        
        /**
         * Gets the current 5xx retry count.
         * 
         * @return The 5xx retry count.
         */
        public int get5xxRetryCount() {
                return retryCount;
        }

	/**
	 * Creates the discord instance with the desired features.
	 *
	 * @return The discord instance.
	 *
	 * @throws DiscordException Thrown if the instance isn't built correctly.
	 */
	public IDiscordClient build() throws DiscordException {
		if (botToken == null)
			throw new DiscordException("No login info present!");

		final IDiscordClient client = new DiscordClientImpl(this);
                if (Configuration.AUTOMATICALLY_ENABLE_MODULES) { // Need to be sure a module instance is create.
                    ModuleLoader.getForClient(client);
                }

		//Registers events as soon as client is initialized
		final EventDispatcher dispatcher = client.getDispatcher();
		iListeners.forEach(dispatcher::registerListener);
		listeners.forEach(dispatcher::registerListener);
		listenerClasses.forEach(dispatcher::registerListener);

		return client;
	}

	/**
	 * Performs {@link #build()} and logs in automatically.
	 *
	 * @return The discord instance.
	 *
	 * @throws DiscordException Thrown if the instance isn't built correctly.
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
