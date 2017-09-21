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
package discord4j.common.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import discord4j.common.jackson.DiscordEntity;
import discord4j.common.jackson.Possible;

/**
 * Represents an User Entity as defined by Discord.
 *
 * @see <a href="https://discordapp.com/developers/docs/resources/user#user-object">User Entity</a>
 */
@DiscordEntity
public class UserEntity {

	private String id;
	private String username;
	private String discriminator;
	private String avatar;
	private boolean bot;
	@JsonProperty("mfa_enabled")
	private boolean mfaEnabled;
	private Possible<Boolean> verified = Possible.absent();
	private Possible<String> email = Possible.absent();

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getDiscriminator() {
		return discriminator;
	}

	public void setDiscriminator(String discriminator) {
		this.discriminator = discriminator;
	}

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

	public boolean isBot() {
		return bot;
	}

	public void setBot(boolean bot) {
		this.bot = bot;
	}

	public boolean isMfaEnabled() {
		return mfaEnabled;
	}

	public void setMfaEnabled(boolean mfaEnabled) {
		this.mfaEnabled = mfaEnabled;
	}

	public Possible<Boolean> getVerified() {
		return verified;
	}

	public void setVerified(Possible<Boolean> verified) {
		this.verified = verified;
	}

	public Possible<String> getEmail() {
		return email;
	}

	public void setEmail(Possible<String> email) {
		this.email = email;
	}
}
