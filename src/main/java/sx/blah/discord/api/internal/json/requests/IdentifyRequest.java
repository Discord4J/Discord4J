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

public class IdentifyRequest {
	private final String token;
	private final Properties properties;
	private final boolean compress;
	private final int large_threshold;
	private final int[] shard;

	public IdentifyRequest(String token, int[] shard) {
		this(token, new Properties(), true, 250, shard);
	}

	private IdentifyRequest(String token, Properties properties, boolean compress, int large_threshold, int[] shard) {
		this.token = token;
		this.properties = properties;
		this.compress = compress;
		this.large_threshold = large_threshold;
		this.shard = shard;
	}

	private static class Properties {
		@JsonProperty("$os")
		private final String os;
		@JsonProperty("$browser")
		private final String browser;
		@JsonProperty("$device")
		private final String device;
		@JsonProperty("$referrer")
		private final String referrer;
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
