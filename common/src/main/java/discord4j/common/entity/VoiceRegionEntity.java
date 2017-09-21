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

/**
 * Represents a Voice Region Entity as defined by Discord.
 *
 * @see <a href="https://discordapp.com/developers/docs/resources/voice#voice-region">Voice Region Entity</a>
 */
@DiscordEntity
public class VoiceRegionEntity {

	private String id;
	private String name;
	@JsonProperty("sample_hostname")
	private String sampleHostname;
	@JsonProperty("sample_port")
	private String samplePort;
	private boolean vip;
	private boolean optimal;
	private boolean deprecated;
	private boolean custom;

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

	public String getSampleHostname() {
		return sampleHostname;
	}

	public void setSampleHostname(String sampleHostname) {
		this.sampleHostname = sampleHostname;
	}

	public String getSamplePort() {
		return samplePort;
	}

	public void setSamplePort(String samplePort) {
		this.samplePort = samplePort;
	}

	public boolean isVip() {
		return vip;
	}

	public void setVip(boolean vip) {
		this.vip = vip;
	}

	public boolean isOptimal() {
		return optimal;
	}

	public void setOptimal(boolean optimal) {
		this.optimal = optimal;
	}

	public boolean isDeprecated() {
		return deprecated;
	}

	public void setDeprecated(boolean deprecated) {
		this.deprecated = deprecated;
	}

	public boolean isCustom() {
		return custom;
	}

	public void setCustom(boolean custom) {
		this.custom = custom;
	}
}
