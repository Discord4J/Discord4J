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
package discord4j.common.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import discord4j.common.jackson.DiscordPojoFilter;

/**
 * Represents a Connection Object as defined by Discord.
 *
 * @see <a href="https://discordapp.com/developers/docs/resources/user#connection-object">Connection Object</a>
 */
@JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = DiscordPojoFilter.class)
public class ConnectionPojo {

	private String id;
	private String name;
	private String type;
	private boolean revoked;
	private IntegrationPojo[] integrations;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean isRevoked() {
		return revoked;
	}

	public void setRevoked(boolean revoked) {
		this.revoked = revoked;
	}

	public IntegrationPojo[] getIntegrations() {
		return integrations;
	}

	public void setIntegrations(IntegrationPojo[] integrations) {
		this.integrations = integrations;
	}
}
