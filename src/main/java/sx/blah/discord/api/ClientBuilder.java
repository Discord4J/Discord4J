/*
 *     This file is part of Discord4J.
 *
 *     Discord4J is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Discord4J is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */

package sx.blah.discord.api;

import org.apache.http.message.BasicNameValuePair;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.api.internal.DiscordClientImpl;
import sx.blah.discord.api.internal.DiscordEndpoints;
import sx.blah.discord.api.internal.Requests;
import sx.blah.discord.api.internal.json.responses.GatewayBotResponse;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.cache.Cache;
import sx.blah.discord.util.cache.ICacheDelegateProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Use this as a factory to create {@link IDiscordClient} instances
 */
public class ClientBuilder {

	/**
	 * This represents the default amount of messages which may be cached by channels.
	 * @see sx.blah.discord.util.MessageHistory
	 */
	public static final int DEFAULT_MESSAGE_CACHE_LIMIT = 256;

	private int[] shard = null;
	private boolean withRecomendedShardCount = false;
	private int maxMissedPings = -1;
	private String botToken;
	private boolean isDaemon = false;
	private int shardCount = 1;
	private int maxReconnectAttempts = 5;
	private int retryCount = 5;
	private int maxCacheCount = DEFAULT_MESSAGE_CACHE_LIMIT;
	private ICacheDelegateProvider provider = Cache.DEFAULT_PROVIDER;

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
	 *
	 * @param shardCount The total number of shards that will be created.
	 * @return The instance of the builder.
	 */
	public ClientBuilder withShards(int shardCount) {
		this.shardCount = shardCount;
		return this;
	}

	/**
	 * Sets whether the bot should use Discord's recommended number of shards on login.
	 *
	 * @param useRecommended If the bot is to use the recommended number of shards.
	 * @return The instance of the builder.
	 */
	public ClientBuilder withRecommendedShardCount(boolean useRecommended) {
		this.withRecomendedShardCount = useRecommended;
		return this;
	}

	/**
	 * Sets the bot to use Discord's recommended number of shards on login. NOTE: This is incompatible with {@link #setShard(int, int)}.
	 *
	 * @return The instance of the builder.
	 */
	public ClientBuilder withRecommendedShardCount() {
		return withRecommendedShardCount(true);
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
	 * Sets the max amount of messages which are cached from message received events (for better message history
	 * efficiency).
	 *
	 * @param maxCacheCount The maximum amount of messages. Setting this to a negative number makes it infinite while
	 *                      setting it to 0 makes it disable caching.
	 * @return The instance of the builder.
	 */
	public ClientBuilder setMaxMessageCacheCount(int maxCacheCount) {
		this.maxCacheCount = maxCacheCount;
		return this;
	}

	/**
	 * Sets the {@link ICacheDelegateProvider} used to create {@link sx.blah.discord.util.cache.ICacheDelegate}s to store cached
	 * objects.
	 *
	 * @param provider The cache provider for this client to use.
	 * @return The instance of the builder.
	 */
	public ClientBuilder setCacheProvider(ICacheDelegateProvider provider) {
		this.provider = provider;
		return this;
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
	 * Sets the shard for this client to run on. NOTE: This is incompatible with {@link #withRecommendedShardCount()}.
	 *
	 * @param shardIndex The shard to run on.
	 * @param totalShards The total of number of shards being run.
	 * @return The instance of the builder.
	 */
	public ClientBuilder setShard(int shardIndex, int totalShards) {
		if (totalShards >= shardIndex) throw new IllegalArgumentException("The shard index is out of bounds for the provided total shard count!");

		this.shard = new int[]{shardIndex, totalShards};
		return this;
	}

	/**
	 * Creates the discord instance with the desired features
	 *
	 * @return The discord instance
	 *
	 * @throws DiscordException Thrown if the instance isn't built correctly
	 */
	public IDiscordClient build() {
		if (botToken == null)
			throw new DiscordException("No login info present!");

		if (withRecomendedShardCount && shard != null)
			throw new DiscordException("Cannot use recommend shard count options with a specific shard!");

		if (withRecomendedShardCount){
			GatewayBotResponse response = Requests.GENERAL_REQUESTS.GET.makeRequest(DiscordEndpoints.GATEWAY + "/bot", GatewayBotResponse.class, new BasicNameValuePair("Authorization", "Bot " + botToken), new BasicNameValuePair("Content-Type", "application/json"));
			shardCount = response.shards;
		}

		final IDiscordClient client = new DiscordClientImpl(botToken, shard != null ? -1 : shardCount, isDaemon, maxMissedPings,
				maxReconnectAttempts, retryCount, maxCacheCount, provider, shard);

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
	public IDiscordClient login() {
		IDiscordClient client = build();
		try {
			client.login();
		} catch (Exception e) {
			throw new DiscordException("Exception ("+e.getClass().getSimpleName()+") occurred while logging in: "+e.getMessage());
		}
		return client;
	}
}
