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

package sx.blah.discord.api.internal;

import com.fasterxml.jackson.core.JsonProcessingException;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.internal.http.HttpMethod;
import okhttp3.logging.HttpLoggingInterceptor;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.message.BasicNameValuePair;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.internal.json.responses.RateLimitResponse;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.LogMarkers;
import sx.blah.discord.util.RateLimitException;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

import static sx.blah.discord.Discord4J.LOGGER;
import static sx.blah.discord.Discord4J.URL;
import static sx.blah.discord.Discord4J.VERSION;

/**
 * Used to send HTTP requests to Discord.
 */
public class Requests {

	/**
	 * The user agent used for requests.
	 */
	public static final String USER_AGENT = String.format("DiscordBot (%s, %s)", URL, VERSION);

	/**
	 * A Requests instance that has no client associated with it.
	 */
	public static final Requests GENERAL_REQUESTS = new Requests(null);

	/**
	 * The HTTP client requests are made on.
	 */
	private final OkHttpClient HTTP;

	/**
	 * Used to send POST requests.
	 */
	public final DiscordRequest POST;
	/**
	 * Used to send GET requests.
	 */
	public final DiscordRequest GET;
	/**
	 * Used to send DELETE requests.
	 */
	public final DiscordRequest DELETE;
	/**
	 * Used to send PATCH requests.
	 */
	public final DiscordRequest PATCH;
	/**
	 * Used to send PUT requests.
	 */
	public final DiscordRequest PUT;

	public Requests(DiscordClientImpl client) {
		HttpLoggingInterceptor httpLogger = new HttpLoggingInterceptor(message -> Discord4J.LOGGER.debug(LogMarkers.API, message)).setLevel(HttpLoggingInterceptor.Level.HEADERS);
		httpLogger.redactHeader("Set-Cookie");
		httpLogger.redactHeader("Cookie");
		httpLogger.redactHeader("Authorization");
		HTTP = new OkHttpClient.Builder().addInterceptor(httpLogger).build();

		POST = new DiscordRequest(RequestMethod.POST, client);
		GET = new DiscordRequest(RequestMethod.GET, client);
		DELETE = new DiscordRequest(RequestMethod.DELETE, client);
		PATCH = new DiscordRequest(RequestMethod.PATCH, client);
		PUT = new DiscordRequest(RequestMethod.PUT, client);
	}

	/**
	 * The method types available for a request.
	 */
	public enum RequestMethod {
		POST, GET, DELETE, PATCH, PUT
	}

	/**
	 * A specific HTTP method request type.
	 */
	public final class DiscordRequest {

		/**
		 * The client used for these requests.
		 */
		private final DiscordClientImpl client;

		/**
		 * The method type used for the request
		 */
		final RequestMethod method;

		/**
		 * Keeps track of the time when the global rate limit retry-after interval is over
		 */
		private final AtomicLong globalRetryAfter = new AtomicLong(-1);

		/**
		 * Keeps track of per-method rate limits. Pair is method, path
		 */
		private final Map<Pair<String, String>, Long> retryAfters = new ConcurrentHashMap<>();

		private DiscordRequest(RequestMethod method, DiscordClientImpl client) {
			this.method = method;
			this.client = client;
		}

		/**
		 * Makes a request.
		 *
		 * @param url The url to make the request to.
		 * @param entity Any data to serialize and send in the body of the request.
		 * @param clazz The class of the object to deserialize the json response into.
		 * @param headers The headers to include in the request.
		 * @param <T> The type of the object to deserialize the json response into.
		 * @return The deserialized response.
		 */
		public <T> T makeRequest(String url, Object entity, Class<T> clazz, BasicNameValuePair... headers) {
			try {
				return makeRequest(url, DiscordUtils.MAPPER.writeValueAsString(entity), clazz, headers);
			} catch (JsonProcessingException e) {
				throw new DiscordException("Unable to serialize request!", e);
			}
		}

		/**
		 * Makes a request.
		 *
		 * @param url The url to make the request to.
		 * @param entity Any data to serialize and send in the body of the request.
		 * @param clazz The class of the object to deserialize the json response into.
		 * @param headers The headers to include in the request.
		 * @param <T> The type of the object to deserialize the json response into.
		 * @return The deserialized response.
		 */
		public <T> T makeRequest(String url, String entity, Class<T> clazz, BasicNameValuePair... headers) {
			try {
				String response = makeRequest(url, entity, headers);
				return response == null ? null : DiscordUtils.MAPPER.readValue(response, clazz);
			} catch (IOException e) {
				throw new DiscordException("Unable to serialize request!", e);
			}
		}

		/**
		 * Makes a request.
		 *
		 * @param url The url to make the request to.
		 * @param clazz The class of the object to deserialize the json response into.
		 * @param headers The headers to include in the request.
		 * @param <T> The type of the object to deserialize the json response into.
		 * @return The deserialized response.
		 */
		public <T> T makeRequest(String url, Class<T> clazz, BasicNameValuePair... headers) {
			try {
				Request.Builder builder = new Request.Builder().url(url).header("User-Agent", USER_AGENT);
				for (BasicNameValuePair header : headers) {
					builder.header(header.getName(), header.getValue());
				}
				if (HttpMethod.requiresRequestBody(method.toString()))
					builder.method(method.toString(), RequestBody.create(MediaType.parse("charset=utf-8"), ""));
				else
					builder.method(method.toString(), null);
				String response = request(builder);
				return response == null ? null : DiscordUtils.MAPPER.readValue(response, clazz);
			} catch (IOException e) {
				throw new DiscordException("Unable to serialize request!", e);
			}
		}

		/**
		 * Makes a request.
		 *
		 * @param url The url to make the request to.
		 * @param entity Any data to serialize and send in the body of the request.
		 * @param headers The headers to include in the request.
		 */
		public void makeRequest(String url, Object entity, BasicNameValuePair... headers) {
			try {
				makeRequest(url, DiscordUtils.MAPPER.writeValueAsString(entity), headers);
			} catch (IOException e) {
				throw new DiscordException("Unable to serialize request!", e);
			}
		}

		/**
		 * Makes a request.
		 *
		 * @param url The url to make the request to.
		 * @param entity Any data to serialize and send in the body of the request.
		 * @param headers The headers to include in the request.
		 * @return The response as a string.
		 */
		public String makeRequest(String url, String entity, BasicNameValuePair... headers) {
			Request.Builder builder = new Request.Builder().url(url).header("User-Agent", USER_AGENT);
			for (BasicNameValuePair header : headers) {
				builder.header(header.getName(), header.getValue());
			}
			builder.method(method.toString(), RequestBody.create(MediaType.parse("charset=utf-8"), entity));
			return request(builder);
		}

		/**
		 * Makes a request.
		 *
		 * @param url The url to make the request to.
		 * @param headers The headers to include in the request.
		 * @return The response as a string.
		 */
		public String makeRequest(String url, BasicNameValuePair... headers) {
			Request.Builder builder = new Request.Builder().url(url).header("User-Agent", USER_AGENT);
			for (BasicNameValuePair header : headers) {
				builder.header(header.getName(), header.getValue());
			}
			if (HttpMethod.requiresRequestBody(method.toString()))
				builder.method(method.toString(), RequestBody.create(MediaType.parse("charset=utf-8"), ""));
			else
				builder.method(method.toString(), null);
			return request(builder);
		}

		private String request(Request.Builder builder) {
			return request(builder, 1, client == null ? 0 : client.getRetryCount());
		}

		private String request(Request.Builder builder, long sleepTime, int retry) {
			if (client != null)
				builder.header("Authorization", client.getToken());

			String content = builder.build().header("Content-Type");
			if (content != null) {
				if (content.equals("multipart/form-data")) {
					builder.removeHeader("Content-Type");
				}
			} else {
				builder.header("Content-Type", "application/json; charset=utf-8");
			}

			if (globalRetryAfter.get() != -1) {
				if (System.currentTimeMillis() > globalRetryAfter.get())
					globalRetryAfter.set(-1);
				else
					throw new RateLimitException("Global rate limit exceeded.",
							globalRetryAfter.get() - System.currentTimeMillis(), method.toString(), true);
			}

			Pair<String, String> methodRequestPair = Pair.of(method.toString(), builder.build().url().uri().getPath());

			if (retryAfters.containsKey(methodRequestPair)) {
				if (System.currentTimeMillis() > retryAfters.get(methodRequestPair))
					retryAfters.remove(methodRequestPair);
				else
					throw new RateLimitException("Rate limit exceeded.",
							retryAfters.get(methodRequestPair) - System.currentTimeMillis(),
							String.format("%s %s", methodRequestPair.getLeft(), methodRequestPair.getRight()), false);
			}

			Request request = builder.build();
			try (Response response = HTTP.newCall(request).execute()) {
				int responseCode = response.code();

				String header = response.header("X-RateLimit-Remaining");
				if (header != null) {
					int remaining = Integer.parseInt(header);
					if (remaining == 0) {
						retryAfters.put(methodRequestPair, Long.parseLong(header) * 1000);
					}
				}

				String data = null;
				if (response.body() != null)
					data = response.body().string();

				if (responseCode == 404) {
					if (!request.url().toString().contains("invite") && !request.url().toString().contains("messages") && !request.url().toString().contains("users")) //Suppresses common 404s which are a result on queries to verify if something exists or not
						LOGGER.error(LogMarkers.API, "Received 404 error, please notify the developer and include the URL ({})", request.url().uri());
					return null;
				} else if (responseCode == 403) {
					LOGGER.error(LogMarkers.API, "Received 403 forbidden error for url {}. If you believe this is a Discord4J error, report this!", request.url().uri());
					return null;
				} else if (responseCode == 204) { //There is a no content response when deleting messages
					return null;
				} else if ((responseCode >= 500 && responseCode < 600) || (responseCode == 400 && data != null && data.contains("cloudflare"))) {
					if (retry == 0)
						throw new DiscordException(String.format("Failed to make a %s failed request after %s tries!",
								responseCode, client == null ? 0 : client.getRetryCount()));
					try {
						Thread.sleep(sleepTime);
					} catch (InterruptedException e) {
						throw new DiscordException("Interrupted while waiting to retry a 5xx response!", e);
					}
					return request(builder, (long) (Math.pow(sleepTime, 2) * ThreadLocalRandom.current().nextLong(5)), retry - 1);

				} else if ((responseCode < 200 || responseCode > 299) && responseCode != 429) {
					throw new DiscordException("Error on request to " + request.url().uri() + ". Received response code " + responseCode + ". With response text: " + data);
				}

				if (responseCode == 429) {
					RateLimitResponse rateLimitResponse = DiscordUtils.MAPPER.readValue(data, RateLimitResponse.class);

					if (rateLimitResponse.global) {
						globalRetryAfter.set(System.currentTimeMillis() + rateLimitResponse.retry_after);
					} else {
						retryAfters.put(methodRequestPair, System.currentTimeMillis() + rateLimitResponse.retry_after);
					}

					throw new RateLimitException(rateLimitResponse.message, rateLimitResponse.retry_after,
							String.format("%s %s", methodRequestPair.getLeft(), methodRequestPair.getRight()),
							rateLimitResponse.global);
				}

				return data;
			} catch (IOException e) {
				Discord4J.LOGGER.error(LogMarkers.API, "Discord4J Internal Exception", e);
				return null;
			}
		}
	}
}
