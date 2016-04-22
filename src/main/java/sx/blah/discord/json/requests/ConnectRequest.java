package sx.blah.discord.json.requests;

import com.google.gson.annotations.SerializedName;

/**
 * This is used to request a connection to the discord websocket
 */
public class ConnectRequest {

	/**
	 * The opcode, always 2
	 */
	public int op = 2;

	/**
	 * The event object
	 */
	public EventObject d;

	public ConnectRequest(String token, String os, String browser, String device, String referrer, String referring_domain, int large_threshold, boolean compress) {
		d = new EventObject(token, new PropertiesObject(os, browser, device, referrer, referring_domain), large_threshold, compress);
	}

	/**
	 * The event object for this operation
	 */
	public static class EventObject {

		/**
		 * The authorization token for the connection
		 */
		public String token;

		/**
		 * The client's properties
		 */
		public PropertiesObject properties;

		/**
		 * The version?? of the event, always 3
		 */
		public int v = 3;

		/**
		 * The amount of users in a guild before the guild is perceived as "large"
		 */
		public int large_threshold;

		/**
		 * Whether events should be compressed with a gzip format
		 */
		public boolean compress;

		public EventObject(String token, PropertiesObject properties, int large_threshold, boolean compress) {
			this.token = token;
			this.properties = properties;
			this.large_threshold = large_threshold;
			this.compress = compress;
		}
	}

	/**
	 * The object representing a client's properties
	 */
	public static class PropertiesObject {

		/**
		 * The os of the client
		 */
		@SerializedName("$os")
		public String os;

		/**
		 * The browser of the client
		 */
		@SerializedName("$browser")
		public String browser;

		/**
		 * The device of the client
		 */
		@SerializedName("$device")
		public String device;

		/**
		 * The referrer of the client, can be empty
		 */
		@SerializedName("$referrer")
		public String referrer;

		/**
		 * The referring domain of the client, can be empty
		 */
		@SerializedName("$referring_domain")
		public String referring_domain;

		public PropertiesObject(String os, String browser, String device, String referrer, String referring_domain) {
			this.os = os;
			this.browser = browser;
			this.device = device;
			this.referrer = referrer;
			this.referring_domain = referring_domain;
		}
	}
}
