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

import java.util.Optional;

/**
 * Represents a Game Object as defined by Discord.
 *
 * @see <a href="https://discordapp.com/developers/docs/topics/gateway#game-object">Game Object</a>
 */
@JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = DiscordPojoFilter.class)
public class GamePojo {

	private String name;
	private int type;
	private Optional<String> url;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public Optional<String> getUrl() {
		return url;
	}

	public void setUrl(Optional<String> url) {
		this.url = url;
	}
}
