package sx.blah.discord.api.internal;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.internal.json.responses.RateLimitResponse;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RateLimitException;
import sx.blah.discord.util.LogMarkers;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
	private final IDiscordClient client;

	public Requests(IDiscordClient client) {
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
		private final IDiscordClient client;

		//Same as HttpClients.createDefault() but with the proper user-agent
		private final CloseableHttpClient CLIENT = HttpClients.custom().setUserAgent(userAgent).build();

		final Class<? extends HttpUriRequest> requestClass;

		/**
		 * Keeps track of the time when the global rate limit retry-after interval is over
		 */
		private final AtomicLong globalRetryAfter = new AtomicLong(-1);
		/**
		 * Keeps track of per-method rate limits. Pair is method, path
		 */
		private final Map<Pair<String, String>, Long> retryAfters = new ConcurrentHashMap<>();

		private Request(Class<? extends HttpUriRequest> clazz, IDiscordClient client) {
			this.requestClass = clazz;
			this.client = client;
		}

		/**
		 * Gets the HttpRequest.class represented by the enum.
		 *
		 * @return The Http request class.
		 */
		public Class<? extends HttpUriRequest> getRequestClass() {
			return requestClass;
		}

		/**
		 * Makes a request.
		 *
		 * @param url The url to make the request to.
		 * @param headers The headers to include in the request.
		 * @return The result (if any) returned by the request.
		 *
		 * @throws RateLimitException
		 * @throws DiscordException
		 */
		public String makeRequest(String url, BasicNameValuePair... headers) throws RateLimitException, DiscordException {
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
		 * @param entity Any data to send with the request.
		 * @param url The url to make the request to.
		 * @param headers The headers to include in the request.
		 * @return The result (if any) returned by the request.
		 *
		 * @throws RateLimitException
		 * @throws DiscordException
		 */
		public String makeRequest(String url, HttpEntity entity, BasicNameValuePair... headers) throws RateLimitException, DiscordException {
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
					LOGGER.error(LogMarkers.API, "Tried to attach HTTP entity to invalid type! ({})",
							this.requestClass.getSimpleName());
				}
			} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
				Discord4J.LOGGER.error(LogMarkers.API, "Discord4J Internal Exception", e);
			}
			return null;
		}

		private String request(HttpUriRequest request) throws DiscordException, RateLimitException {
			request.addHeader("Authorization", client.getToken());

			if (request.containsHeader("Content-Type")) {
				if (request.getFirstHeader("Content-Type").getValue().equals("multipart/form-data")) {
					request.removeHeaders("Content-Type");
				}
			} else {
				request.addHeader("Content-Type", "application/json");
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
								Long.parseLong(response.getFirstHeader("X-RateLimit-Reset").getValue())*1000);
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
				} else if (responseCode == 502) {
					LOGGER.trace(LogMarkers.API, "502 response on request to {}, response text: {}", request.getURI(), message); //This can be used to verify if it was cloudflare causing the 502.

					if (message.toLowerCase(Locale.ROOT).contains("cloudflare")) {
						throw new DiscordException("502 error on request to " + request.getURI()
								+ ". This is due to CloudFlare.");
					}

					throw new DiscordException("502 error on request to "+request.getURI()+". With response text: "+message);
				} else if ((responseCode < 200 || responseCode > 299) && responseCode != 429) {
					throw new DiscordException("Error on request to "+request.getURI()+". Received response code "+responseCode+". With response text: "+message);
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
						globalRetryAfter.set(System.currentTimeMillis()+rateLimitResponse.retry_after);
					} else {
						retryAfters.put(methodRequestPair, System.currentTimeMillis()+rateLimitResponse.retry_after);
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
