/*
 * This file is part of Discord4J.
 *
 * Discord4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Discord4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J. If not, see <http://www.gnu.org/licenses/>.
 */
package discord4j.common.json.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Identify implements Payload {

	private String token;
	private IdentifyProperties properties;
	private boolean compress;
	@JsonProperty("large_threshold")
	private int largeThreshold;
	private int[] shard;
	private StatusUpdate presence;

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public IdentifyProperties getProperties() {
		return properties;
	}

	public void setProperties(IdentifyProperties properties) {
		this.properties = properties;
	}

	public boolean isCompress() {
		return compress;
	}

	public void setCompress(boolean compress) {
		this.compress = compress;
	}

	public int getLargeThreshold() {
		return largeThreshold;
	}

	public void setLargeThreshold(int largeThreshold) {
		this.largeThreshold = largeThreshold;
	}

	public int[] getShard() {
		return shard;
	}

	public void setShard(int[] shard) {
		this.shard = shard;
	}

	public StatusUpdate getPresence() {
		return presence;
	}

	public void setPresence(StatusUpdate presence) {
		this.presence = presence;
	}
}
