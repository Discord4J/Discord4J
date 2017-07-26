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

package sx.blah.discord.api.internal.json.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import sx.blah.discord.Discord4J;

/**
 * Sent to trigger the initial handshake with the gateway.
 */
public class IdentifyRequest {
	/**
	 * The bot's authentication token.
	 */
	private final String token;
	/**
	 * Connection properties.
	 */
	private final Properties properties;
	/**
	 * Whether this connection supports compression of the initial ready packet.
	 */
	private final boolean compress;
	/**
	 * Total number of members at which the gateway will stop sending offline members in the guild member list. Must be
	 * between 50 and 250.
	 */
	private final int large_threshold;
	/**
	 * Sharding information.
	 */
	private final int[] shard;
	private final PresenceUpdateRequest presence;

	public IdentifyRequest(String token, int[] shard, PresenceUpdateRequest presence) {
		this(token, new Properties(), true, 250, shard, presence);
	}

	private IdentifyRequest(String token, Properties properties, boolean compress, int large_threshold, int[] shard, PresenceUpdateRequest presence) {
		this.token = token;
		this.properties = properties;
		this.compress = compress;
		this.large_threshold = large_threshold;
		this.shard = shard;
		this.presence = presence;
	}

	/**
	 * Inner connection properties object.
	 */
	private static class Properties {
		/**
		 * The operating system this program is running on.
		 */
		@JsonProperty("$os")
		private final String os;
		/**
		 * The name of the library. ("Discord4J")
		 */
		@JsonProperty("$browser")
		private final String browser;
		/**
		 * The name of the library. ("Discord4J")
		 */
		@JsonProperty("$device")
		private final String device;
		/**
		 * Always an empty string.
		 */
		@JsonProperty("$referrer")
		private final String referrer;
		/**
		 * Always an empty string.
		 */
		@JsonProperty("$referring_domain")
		private final String referring_domain;

		public Properties() {
			this(System.getProperty("os.name"), Discord4J.NAME, Discord4J.NAME, "", "");
		}

		private Properties(String os, String browser, String device, String referrer, String referring_domain) {
			this.os = os;
			this.browser = browser;
			this.device = device;
			this.referrer = referrer;
			this.referring_domain = referring_domain;
		}
	}
}
