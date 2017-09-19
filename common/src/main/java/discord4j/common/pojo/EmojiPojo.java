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

import com.fasterxml.jackson.annotation.JsonProperty;
import discord4j.common.jackson.DiscordPojo;
import discord4j.common.jackson.Possible;

/**
 * Represents an Emoji Object as defined by Discord.
 *
 * @see <a href="https://discordapp.com/developers/docs/resources/emoji#emoji-object">Emoji Object</a>
 */
@DiscordPojo
public class EmojiPojo {

	private String id;
	private String name;
	private Possible<String[]> roles = Possible.absent();
	@JsonProperty("require_colons")
	private Possible<Boolean> requireColons = Possible.absent();
	private Possible<Boolean> managed = Possible.absent();

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

	public Possible<String[]> getRoles() {
		return roles;
	}

	public void setRoles(Possible<String[]> roles) {
		this.roles = roles;
	}

	public Possible<Boolean> getRequireColons() {
		return requireColons;
	}

	public void setRequireColons(Possible<Boolean> requireColons) {
		this.requireColons = requireColons;
	}

	public Possible<Boolean> getManaged() {
		return managed;
	}

	public void setManaged(Possible<Boolean> managed) {
		this.managed = managed;
	}
}
