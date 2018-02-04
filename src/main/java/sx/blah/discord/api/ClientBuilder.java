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
import sx.blah.discord.api.internal.json.requests.PresenceUpdateRequest;
import sx.blah.discord.api.internal.json.responses.GatewayBotResponse;
import sx.blah.discord.handle.obj.ActivityType;
import sx.blah.discord.handle.obj.StatusType;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.cache.Cache;
import sx.blah.discord.util.cache.ICacheDelegateProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.TimeUnit;

/**
 * Used to configure and build a {@link IDiscordClient} instance.
 */
public class ClientBuilder {

	/**
	 * The default amount of messages which may be cached by channels.
	 * @see sx.blah.discord.util.MessageHistory
	 */
	public static final int DEFAULT_MESSAGE_CACHE_LIMIT = 256;

	private int[] shard = null;
	private boolean withRecommendedShardCount = false;
	private int maxMissedPings = -1;
	private String botToken;
	private boolean isDaemon = false;
	private int shardCount = 1;
	private int maxReconnectAttempts = 5;
	private int retryCount = 5;
	private int maxCacheCount = DEFAULT_MESSAGE_CACHE_LIMIT;
	private ICacheDelegateProvider provider = Cache.DEFAULT_PROVIDER;
	private RejectedExecutionHandler backpressureHandler = new EventDispatcher.CallerRunsPolicy();
	private int minimumPoolSize = 1;
	private int maximumPoolSize = Runtime.getRuntime().availableProcessors() * 4;
	private long eventThreadTimeout = 60L;
	private TimeUnit eventThreadTimeoutUnit = TimeUnit.SECONDS;
	private int overflowCapacity = 128;
	private StatusType status = StatusType.ONLINE;
	private ActivityType activity;
	private String text;
	private String streamUrl;
	//Early registered listeners:
	private final List<IListener> iListeners = new ArrayList<>();
	private final List<Object> listeners = new ArrayList<>();
	private final List<Class<?>> listenerClasses = new ArrayList<>();

	/**
	 * Configures the bot token used for authentication with Discord.
	 *
	 * @param token The bot's token.
	 * @return The builder instance.
	 */
	public ClientBuilder withToken(String token) {
		this.botToken = token;
		return this;
	}

	/**
	 * Gets the bot's authentication token.
	 *
	 * @return The bot's authentication token.
	 */
	public String getToken() {
		return botToken;
	}

	/**
	 * Configures the max number of heartbeats Discord can not respond to before a reconnect is initiated.
	 *
	 * @param maxMissedPings The max number of heartbeats Discord can not respond to before a reconnect is initiated.
	 * @return The builder instance.
	 */
	public ClientBuilder withPingTimeout(int maxMissedPings) {
		this.maxMissedPings = maxMissedPings;
		return this;
	}

	/**
	 * Configures whether the main client thread should be daemon. (It is non-daemon by default).
	 *
	 * @param isDaemon Whether the main client thread should be daemon.
	 * @return The builder instance.
	 */
	public ClientBuilder setDaemon(boolean isDaemon) {
		this.isDaemon = isDaemon;
		return this;
	}

	/**
	 * Configures the number of shards the client should create and manage.
	 *
	 * @param shardCount The number of shards the client should create and manage.
	 * @return The builder instance.
	 */
	public ClientBuilder withShards(int shardCount) {
		this.shardCount = shardCount;
		return this;
	}

	/**
	 * Configures whether the client should request the number of shards to login with from Discord.
	 *
	 * @param useRecommended Whether the bot should request the number of shards to login with from Discord.
	 * @return The builder instance.
	 */
	public ClientBuilder withRecommendedShardCount(boolean useRecommended) {
		this.withRecommendedShardCount = useRecommended;
		return this;
	}

	/**
	 * Configures the client to request the number of shards to login with from Discord.
	 *
	 * <p>Note: This method is incompatible with {@link #setShard(int, int)}.
	 *
	 * @return The builder instance.
	 */
	public ClientBuilder withRecommendedShardCount() {
		return withRecommendedShardCount(true);
	}

	/**
	 * Configures the max number of attempts shards managed by the client will make to reconnect to Discord.
	 *
	 * @param maxReconnectAttempts The max max number of attempts shards managed by the client will make to reconnect to
	 *                             Discord.
	 * @return The builder instance.
	 */
	public ClientBuilder setMaxReconnectAttempts(int maxReconnectAttempts) {
		this.maxReconnectAttempts = maxReconnectAttempts;
		return this;
	}

	/**
	 * Configures the max number of messages which are cached for each channel.
	 *
	 * @param maxCacheCount The maximum number of messages which are cached for each channel. A negative value indicates
	 *                      infinite caching while <code>0</code> indicates no caching.
	 * @return The builder instance.
	 */
	public ClientBuilder setMaxMessageCacheCount(int maxCacheCount) {
		this.maxCacheCount = maxCacheCount;
		return this;
	}

	/**
	 * Configures the {@link ICacheDelegateProvider} used by the client to create
	 * {@link sx.blah.discord.util.cache.ICacheDelegate}s to store cached objects.
	 *
	 * @param provider The cache delegate provider used by the client to create cache delegates.
	 * @return The builder instance.
	 */
	public ClientBuilder setCacheProvider(ICacheDelegateProvider provider) {
		this.provider = provider;
		return this;
	}

	/**
	 * Configures listeners to immediately register with the client's {@link EventDispatcher} before logging in.
	 *
	 * @param listeners The listeners to register.
	 * @return The builder instance.
	 */
	public final ClientBuilder registerListeners(IListener... listeners) {
		iListeners.addAll(Arrays.asList(listeners));
		return this;
	}

	/**
	 * Configures listeners to immediately register with the client's {@link EventDispatcher} before logging in.
	 *
	 * @param listeners The listeners to register.
	 * @return The builder instance.
	 */
	public ClientBuilder registerListeners(Object... listeners) {
		this.listeners.addAll(Arrays.asList(listeners));
		return this;
	}

	/**
	 * Configures listeners to immediately register with the client's {@link EventDispatcher} before logging in.
	 *
	 * @param listeners The listeners to register.
	 * @return The builder instance.
	 */
	public ClientBuilder registerListeners(Class<?>... listeners) {
		listenerClasses.addAll(Arrays.asList(listeners));
		return this;
	}

	/**
	 * Configures a listener to immediately register with the client's {@link EventDispatcher} before logging in.
	 *
	 * @param listener The listener to register.
	 * @return The builder instance.
	 */
	public ClientBuilder registerListener(IListener listener) {
		return registerListeners(listener);
	}

	/**
	 * Configures a listener to immediately register with the client's {@link EventDispatcher} before logging in.
	 *
	 * @param listener The listener to register.
	 * @return The builder instance.
	 */
	public ClientBuilder registerListener(Object listener) {
		return registerListeners(listener);
	}

	/**
	 * Configures a listener to immediately register with the client's {@link EventDispatcher} before logging in.
	 *
	 * @param listener The listener to register.
	 * @return The builder instance.
	 */
	public ClientBuilder registerListener(Class<?> listener) {
		return registerListeners(listener);
	}

	/**
	 * Configures the number of retries that should be attempted for HTTP requests to Discord in the case of a 5xx
	 * response code.
	 *
	 * @param retryCount The number of retries that should be made.
	 * @return The builder instance.
	 */
	public ClientBuilder set5xxRetryCount(int retryCount) {
		this.retryCount = retryCount;
		return this;
	}

	/**
	 * Configures a <b>single</b> shard for this client to manage.
	 *
	 * <p>Note: This is incompatible with {@link #withShards(int)}.
	 *
	 * @param shardIndex The index of the shard to create.
	 * @param totalShards The total of number of shards being created.
	 * @return The builder instance.
	 *
	 * @see <a href=https://discordapp.com/developers/docs/topics/gateway#sharding>Sharding</a>
	 */
	public ClientBuilder setShard(int shardIndex, int totalShards) {
		if (shardIndex < 0) throw new IllegalArgumentException("The shard index must be greater than or equal to 0!");
		if (totalShards <= shardIndex) throw new IllegalArgumentException("The shard index is out of bounds for the provided total shard count!");

		this.shard = new int[]{shardIndex, totalShards};
		return this;
	}

	/**
	 * Configures the handler to use if the client's {@link EventDispatcher} thread pool cannot keep up with the volume
	 * of events being received.
	 *
	 * @param handler The handler to call when this occurs.
	 * @return The builder instance.
	 */
	public ClientBuilder withEventBackpressureHandler(RejectedExecutionHandler handler) {
		this.backpressureHandler = handler;
		return this;
	}

	/**
	 * Configures the maximum number of threads which must be alive at any given time in the client's
	 * {@link EventDispatcher}. Higher values are more expensive overall but lead to quicker availability of threads.
	 *
	 * @param minimumDispatchThreads The minimum number of threads to keep alive.
	 * @return The builder instance.
	 */
	public ClientBuilder withMinimumDispatchThreads(int minimumDispatchThreads) {
		this.minimumPoolSize = minimumDispatchThreads;
		return this;
	}

	/**
	 * Configures the maximum amount of threads which may be alive at any given time in the client's
	 * {@link EventDispatcher}. Higher values are more expensive overall but lead to quicker availability of threads.
	 *
	 * @param maximumDispatchThreads The maximum number of threads to keep alive.
	 * @return The builder instance.
	 */
	public ClientBuilder withMaximumDispatchThreads(int maximumDispatchThreads) {
		this.maximumPoolSize = maximumDispatchThreads;
		return this;
	}

	/**
	 * Configures the amount of time extra threads in the client's {@link EventDispatcher} are allowed to be idle before
	 * they are killed.
	 *
	 * @param time The amount of allowed idle time.
	 * @param unit The unit of time to use.
	 * @return The builder instance.
	 */
	public ClientBuilder withIdleDispatchThreadTimeout(long time, TimeUnit unit) {
		this.eventThreadTimeout = time;
		this.eventThreadTimeoutUnit = unit;
		return this;
	}

	/**
	 * Configures the number of events the client's {@link EventDispatcher} is allowed to overflow by without calling
	 * the backpressure handler. This allows for easy recovery in the case that there is a sudden, unexpected burst of
	 * events which the dispatcher cannot handle.
	 *
	 * @param overflowCapacity The overflow capacity.
	 * @return The builder instance.
	 */
	public ClientBuilder withEventOverflowCapacity(int overflowCapacity) {
		this.overflowCapacity = overflowCapacity;
		return this;
	}

	/**
	 * Sets the presence of the bot when it logs in.
	 *
	 * @param status The status to display.
	 * @param activity The type of activity to display.
	 * @param text The text to display.
	 * @return The builder instance.
	 *
	 * @throws IllegalArgumentException If activity is {@link ActivityType#STREAMING}.
	 * Use {@link #setStreamingPresence(StatusType, String, String)} instead.
	 */
	public ClientBuilder setPresence(StatusType status, ActivityType activity, String text) {
		if (activity == ActivityType.STREAMING) throw new IllegalArgumentException("Invalid ActivityType");
		return setPresence(status, activity, text, null);
	}

	/**
	 * Sets the presence of the bot when it logs in.
	 *
	 * @param status The status to display.
	 * @return The builder instance.
	 */
	public ClientBuilder setPresence(StatusType status) {
		return setPresence(status, null, null, null);
	}

	/**
	 * Sets the presence of the bot when it logs in to streaming.
	 *
	 * @param status The status to display.
	 * @param text The text to display, may be null.
	 * @param streamUrl The valid twitch.tv streaming url.
	 * @return The builder instance.
	 */
	public ClientBuilder setStreamingPresence(StatusType status, String text, String streamUrl) {
		return setPresence(status, ActivityType.STREAMING, text, streamUrl);
	}

	/**
	 * Sets the presence of the bot when it logs in.
	 *
	 * @param status The status to display.
	 * @param activity The type of activity to display.
	 * @param text The text to display.
	 * @param streamUrl The valid twitch.tv url.
	 * @return The builder instance.
	 */
	private ClientBuilder setPresence(StatusType status, ActivityType activity, String text, String streamUrl) {
		this.status = status;
		this.activity = activity;
		this.text = text;
		this.streamUrl = streamUrl;
		return this;
	}

	/**
	 * Creates a {@link IDiscordClient} with the configuration specified by this builder.
	 *
	 * @return The new client with the configuration specified by this builder.
	 */
	public IDiscordClient build() {
		if (botToken == null)
			throw new DiscordException("No login info present!");

		if (withRecommendedShardCount && shard != null)
			throw new DiscordException("Cannot use recommend shard count options with a specific shard!");

		if (withRecommendedShardCount){
			GatewayBotResponse response = Requests.GENERAL_REQUESTS.GET.makeRequest(DiscordEndpoints.GATEWAY + "/bot", GatewayBotResponse.class, new BasicNameValuePair("Authorization", "Bot " + botToken), new BasicNameValuePair("Content-Type", "application/json"));
			shardCount = response.shards;
		}

		final IDiscordClient client = new DiscordClientImpl(botToken, shard != null ? -1 : shardCount, isDaemon,
				maxMissedPings, maxReconnectAttempts, retryCount, maxCacheCount, provider, shard, backpressureHandler,
				minimumPoolSize, maximumPoolSize, overflowCapacity, eventThreadTimeout, eventThreadTimeoutUnit,
				new PresenceUpdateRequest(status, activity, text, streamUrl));

		//Registers events as soon as client is initialized
		final EventDispatcher dispatcher = client.getDispatcher();
		iListeners.forEach(dispatcher::registerListener);
		listeners.forEach(dispatcher::registerListener);
		listenerClasses.forEach(dispatcher::registerListener);

		return client;
	}

	/**
	 * Builds and logs in the new client instance.
	 *
	 * @return The new client which has begun its connection process.
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
