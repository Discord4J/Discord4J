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
package discord4j.rest.json.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nullable;

public class EmbedAuthorRequest {

    private final String name;
    @Nullable
    private final String url;
    @JsonProperty("icon_url")
    @Nullable
    private final String iconUrl;

    public EmbedAuthorRequest(String name, @Nullable String url, @Nullable String iconUrl) {
        this.name = name;
        this.url = url;
        this.iconUrl = iconUrl;
    }

    @Override
    public String toString() {
        return "EmbedAuthorRequest{" +
                "name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", iconUrl='" + iconUrl + '\'' +
                '}';
    }
}
