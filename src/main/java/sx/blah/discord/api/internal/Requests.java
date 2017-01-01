package sx.blah.discord.api.internal;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.internal.json.responses.RateLimitResponse;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.LogMarkers;
import sx.blah.discord.util.RateLimitException;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

import static sx.blah.discord.Discord4J.*;

/**
 * Represents request types to be sent.
 */
public class Requests {

	/**
	 * This is used when the requests class must be used from a static context.
	 */
	public static final Requests GENERAL_REQUESTS = new Requests(null);

	/**
	 * Used to send POST Requests
	 */
	public final Request POST;
	/**
	 * Used to send GET requests
	 */
	public final Request GET;
	/**
	 * Used to send DELETE requests
	 */
	public final Request DELETE;
	/**
	 * Used to send PATCH requests
	 */
	public final Request PATCH;
	/**
	 * Used to send PUT requests
	 */
	public final Request PUT;

	/**
	 * The client used for these requests.
	 */
	private final DiscordClientImpl client;

	public Requests(DiscordClientImpl client) {
		this.client = client;

		POST = new Request(HttpPost.class, client);
		GET = new Request(HttpGet.class, client);
		DELETE = new Request(HttpDelete.class, client);
		PATCH = new Request(HttpPatch.class, client);
		PUT = new Request(HttpPut.class, client);
	}

	/**
	 * This represents a specific request.
	 */
	public final class Request {
		/**
		 * The user-agent, as per @Jake's request
		 */
		private final String userAgent = String.format("DiscordBot (%s v%s) - %s %s", URL, VERSION, NAME, DESCRIPTION);

		/**
		 * The client used for these requests.
		 */
		private final DiscordClientImpl client;

		//Same as HttpClients.createDefault() but with the proper user-agent
		private final CloseableHttpClient CLIENT = HttpClients.custom().setUserAgent(userAgent).build();

		/**
		 * The class of the request type used for the request
		 */
		final Class<? extends HttpUriRequest> requestClass;

		/**
		 * Keeps track of the time when the global rate limit retry-after interval is over
		 */
		private final AtomicLong globalRetryAfter = new AtomicLong(-1);

		/**
		 * Keeps track of per-method rate limits. Pair is method, path
		 */
		private final Map<Pair<String, String>, Long> retryAfters = new ConcurrentHashMap<>();

		private Request(Class<? extends HttpUriRequest> clazz, DiscordClientImpl client) {
			this.requestClass = clazz;
			this.client = client;
		}

		/**
		 * Makes a request.
		 *
		 * @param url     The url to make the request to.
		 * @param entity  Any data to send with the request.
		 * @param clazz   The class of the object to transform the json response into.
		 * @param headers The headers to include in the response.
		 * @param <T>     The type of the object to transform the json response into.
		 * @return The transformed object.
		 * @throws DiscordException
		 * @throws RateLimitException
		 */
		public <T> T makeRequest(String url, Object entity, Class<T> clazz, BasicNameValuePair... headers) throws DiscordException, RateLimitException {
			return makeRequest(url, DiscordUtils.GSON.toJson(entity), clazz, headers);
		}

		public <T> T makeRequest(String url, String entity, Class<T> clazz, BasicNameValuePair... headers) throws DiscordException, RateLimitException {
			return DiscordUtils.GSON.fromJson(makeRequest(url, entity, headers), clazz);
		}

		/**
		 * Makes a request.
		 *
		 * @param url     The url to make the request to.
		 * @param clazz   The class of the object to transform the json response into.
		 * @param headers The headers to include in the response.
		 * @param <T>     The type of the object to transform the json response into.
		 * @return The transformed object.
		 * @throws DiscordException
		 * @throws RateLimitException
		 */
		public <T> T makeRequest(String url, Class<T> clazz, BasicNameValuePair... headers) throws DiscordException, RateLimitException {
			return DiscordUtils.GSON.fromJson(makeRequest(url, headers), clazz);
		}

		/**
		 * Makes a request.
		 *
		 * @param url     The url to make the request to.
		 * @param entity  Any data to send with the request.
		 * @param headers The headers to include in the response.
		 * @return The result (if any) returned by the request.
		 * @throws DiscordException
		 * @throws RateLimitException
		 */
		public String makeRequest(String url, Object entity, BasicNameValuePair... headers) throws DiscordException, RateLimitException {
			return makeRequest(url, DiscordUtils.GSON.toJson(entity), headers);
		}

		/**
		 * Makes a request.
		 *
		 * @param url     The url to make the request to.
		 * @param entity  Any data to send with the request.
		 * @param headers The headers to include in the response.
		 * @return The result (if any) returned by the request.
		 * @throws DiscordException
		 * @throws RateLimitException
		 */
		public String makeRequest(String url, String entity, BasicNameValuePair... headers) throws DiscordException, RateLimitException {
			return makeRequest(url, new StringEntity(entity, "UTF-8"), headers);
		}

		/**
		 * Makes a request.
		 *
		 * @param url     The url to make the request to.
		 * @param headers The headers to include in the request.
		 * @return The result (if any) returned by the request.
		 * @throws RateLimitException
		 * @throws DiscordException
		 */
		public String makeRequest(String url, BasicNameValuePair... headers) throws DiscordException, RateLimitException {
			try {
				HttpUriRequest request = this.requestClass.getConstructor(String.class).newInstance(url);
				for (BasicNameValuePair header : headers) {
					request.addHeader(header.getName(), header.getValue());
				}

				return request(request);
			} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
				Discord4J.LOGGER.error(LogMarkers.API, "Discord4J Internal Exception", e);
				return null;
			}
		}


		/**
		 * Makes a request.
		 *
		 * @param url     The url to make the request to.
		 * @param entity  Any data to send with the request.
		 * @param headers The headers to include in the request.
		 * @return The result (if any) returned by the request.
		 * @throws RateLimitException
		 * @throws DiscordException
		 */
		public String makeRequest(String url, HttpEntity entity, BasicNameValuePair... headers) throws DiscordException, RateLimitException {
			try {
				if (HttpEntityEnclosingRequestBase.class.isAssignableFrom(this.requestClass)) {
					HttpEntityEnclosingRequestBase request = (HttpEntityEnclosingRequestBase)
							this.requestClass.getConstructor(String.class).newInstance(url);
					for (BasicNameValuePair header : headers) {
						request.addHeader(header.getName(), header.getValue());
					}
					request.setEntity(entity);
					return request(request);
				} else {
					LOGGER.error(LogMarkers.API, "Tried to attach HTTP entity to invalid type! ({})", this.requestClass.getSimpleName());
				}
			} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
				Discord4J.LOGGER.error(LogMarkers.API, "Discord4J Internal Exception", e);
			}
			return null;
		}

		private String request(HttpUriRequest request) throws RateLimitException, DiscordException {
			return request(request, 1, client.getRetryCount());
		}

		private String request(HttpUriRequest request, long sleepTime, int retry) throws DiscordException, RateLimitException {
			request.addHeader("Authorization", client.getToken());

			if (request.containsHeader("Content-Type")) {
				if (request.getFirstHeader("Content-Type").getValue().equals("multipart/form-data")) {
					request.removeHeaders("Content-Type");
				}
			} else {
				request.addHeader("Content-Type", "application/json; charset=utf-8");
			}

			if (globalRetryAfter.get() != -1) {
				if (System.currentTimeMillis() > globalRetryAfter.get())
					globalRetryAfter.set(-1);
				else
					throw new RateLimitException("Global rate limit exceeded.",
							globalRetryAfter.get() - System.currentTimeMillis(), request.getMethod(), true);
			}

			Pair<String, String> methodRequestPair = Pair.of(request.getMethod(), request.getURI().getPath());

			if (retryAfters.containsKey(methodRequestPair)) {
				if (System.currentTimeMillis() > retryAfters.get(methodRequestPair))
					retryAfters.remove(methodRequestPair);
				else
					throw new RateLimitException("Rate limit exceeded.",
							retryAfters.get(methodRequestPair) - System.currentTimeMillis(),
							String.format("%s %s", methodRequestPair.getLeft(), methodRequestPair.getRight()), false);
			}

			try (CloseableHttpResponse response = CLIENT.execute(request)) {
				int responseCode = response.getStatusLine().getStatusCode();

				if (response.containsHeader("X-RateLimit-Remaining")) {
					int remaining = Integer.parseInt(response.getFirstHeader("X-RateLimit-Remaining").getValue());
					if (remaining == 0) {
						retryAfters.put(methodRequestPair,
								Long.parseLong(response.getFirstHeader("X-RateLimit-Reset").getValue()) * 1000);
					}
				}

				String message = "";
				if (response.getEntity() != null)
					message = EntityUtils.toString(response.getEntity());

				if (responseCode == 404) {
					if (!request.getURI().toString().contains("invite") && !request.getURI().toString().contains("messages")) //Suppresses common 404s which are a result on queries to verify if something exists or not
						LOGGER.error(LogMarkers.API, "Received 404 error, please notify the developer and include the URL ({})", request.getURI());
					return null;
				} else if (responseCode == 403) {
					LOGGER.error(LogMarkers.API, "Received 403 forbidden error for url {}. If you believe this is a Discord4J error, report this!", request.getURI());
					return null;
				} else if (responseCode == 204) { //There is a no content response when deleting messages
					return null;
				} else if (responseCode >= 500 && responseCode < 600) {
					if(retry == 0)
						throw new DiscordException(String.format("Failed to make a 5xx failed request after %s tries!",
								client.getRetryCount()));
					try {
						Thread.sleep(sleepTime);
					} catch (InterruptedException e) {
						throw new DiscordException("Interrupted while waiting to retry a 5xx response!", e);
					}
					return request(request, (long) (Math.pow(sleepTime, 2) * ThreadLocalRandom.current().nextLong(5)), retry - 1);

				} else if ((responseCode < 200 || responseCode > 299) && responseCode != 429) {
					throw new DiscordException("Error on request to " + request.getURI() + ". Received response code " + responseCode + ". With response text: " + message);
				}

				JsonParser parser = new JsonParser();
				JsonElement element;
				try {
					element = parser.parse(message);
				} catch (JsonParseException e) {
					return null;
				}

				if (responseCode == 429) {
					RateLimitResponse rateLimitResponse = DiscordUtils.GSON.fromJson(element, RateLimitResponse.class);

					if (rateLimitResponse.global) {
						globalRetryAfter.set(System.currentTimeMillis() + rateLimitResponse.retry_after);
					} else {
						retryAfters.put(methodRequestPair, System.currentTimeMillis() + rateLimitResponse.retry_after);
					}

					throw new RateLimitException(rateLimitResponse.message, rateLimitResponse.retry_after,
							String.format("%s %s", methodRequestPair.getLeft(), methodRequestPair.getRight()),
							rateLimitResponse.global);
				}

				if (element.isJsonObject() && parser.parse(message).getAsJsonObject().has("message"))
					throw new DiscordException(element.getAsJsonObject().get("message").getAsString());

				return message;
			} catch (IOException e) {
				Discord4J.LOGGER.error(LogMarkers.API, "Discord4J Internal Exception", e);
				return null;
			}
		}
	}
}
