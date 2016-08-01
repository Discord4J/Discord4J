package sx.blah.discord.api.internal;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import sx.blah.discord.Discord4J;
import sx.blah.discord.json.responses.RateLimitResponse;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RateLimitException;
import sx.blah.discord.util.LogMarkers;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Locale;

import static sx.blah.discord.Discord4J.*;

/**
 * Represents request types to be sent.
 */
public enum Requests {

	/**
	 * Used to send POST Requests
	 */
	POST(HttpPost.class),
	/**
	 * Used to send GET requests
	 */
	GET(HttpGet.class),
	/**
	 * Used to send DELETE requests
	 */
	DELETE(HttpDelete.class),
	/**
	 * Used to send PATCH requests
	 */
	PATCH(HttpPatch.class),
	/**
	 * Used to send PUT requests
	 */
	PUT(HttpPut.class);
	/**
	 * The user-agent, as per @Jake's request
	 */
	private static final String userAgent = String.format("DiscordBot (%s v%s) - %s %s", URL, VERSION, NAME, DESCRIPTION);

	//Same as HttpClients.createDefault() but with the proper user-agent
	static final CloseableHttpClient CLIENT = HttpClients.custom().setUserAgent(userAgent).build();

	final Class<? extends HttpUriRequest> requestClass;

	Requests(Class<? extends HttpUriRequest> clazz) {
		this.requestClass = clazz;
	}

	/**
	 * Gets the HttpREQUEST.class represented by the enum.
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
		try (CloseableHttpResponse response = CLIENT.execute(request)){
			int responseCode = response.getStatusLine().getStatusCode();

			String message = "";
			if (response.getEntity() != null)
				message = EntityUtils.toString(response.getEntity());

			if (responseCode == 404) {
				if (!request.getURI().toString().contains("invite"))
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
				throw new RateLimitException(DiscordUtils.GSON.fromJson(element, RateLimitResponse.class));
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
