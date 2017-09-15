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
package discord4j.common.pojo.embed;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents an Embed Author Object as defined by Discord.
 *
 * @see <a href="https://discordapp.com/developers/docs/resources/channel#embed-object-embed-author-structure">Embed Author Object</a>
 */
public class EmbedAuthorPojo {

	private String name;
	private String url;
	@JsonProperty("icon_url")
	private String iconUrl;
	@JsonProperty("proxy_icon_url")
	private String proxyIconUrl;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getIconUrl() {
		return iconUrl;
	}

	public void setIconUrl(String iconUrl) {
		this.iconUrl = iconUrl;
	}

	public String getProxyIconUrl() {
		return proxyIconUrl;
	}

	public void setProxyIconUrl(String proxyIconUrl) {
		this.proxyIconUrl = proxyIconUrl;
	}
}
