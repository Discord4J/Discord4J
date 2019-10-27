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

package discord4j.rest.entity.data;

import discord4j.common.json.EmbedAuthorResponse;

public class EmbedAuthorData {

    private final String name;
    private final String url;
    private final String iconUrl;
    private final String proxyIconUrl;

    public EmbedAuthorData(EmbedAuthorResponse response) {
        name = response.getName();
        url = response.getUrl();
        iconUrl = response.getIconUrl();
        proxyIconUrl = response.getProxyIconUrl();
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public String getProxyIconUrl() {
        return proxyIconUrl;
    }

    @Override
    public String toString() {
        return "EmbedAuthorData{" +
                "name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", iconUrl='" + iconUrl + '\'' +
                ", proxyIconUrl='" + proxyIconUrl + '\'' +
                '}';
    }
}
