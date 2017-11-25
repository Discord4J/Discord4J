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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */
package discord4j.common.json.response;

import discord4j.common.jackson.UnsignedJson;

import java.util.Arrays;

public class ConnectionResponse {

	@UnsignedJson
	private long id;
	private String name;
	private String type;
	private boolean revoked;
	private IntegrationResponse[] integrations;

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public boolean isRevoked() {
		return revoked;
	}

	public IntegrationResponse[] getIntegrations() {
		return integrations;
	}

	@Override
	public String toString() {
		return "ConnectionResponse[" +
				"id=" + id +
				", name='" + name + '\'' +
				", type='" + type + '\'' +
				", revoked=" + revoked +
				", integrations=" + Arrays.toString(integrations) +
				']';
	}
}
