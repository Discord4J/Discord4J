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

import discord4j.common.json.EmbedFooterResponse;

public class EmbedFooterData {

    private final String text;
    private final String iconUrl;
    private final String proxyIconUrl;

    public EmbedFooterData(EmbedFooterResponse response) {
        text = response.getText();
        iconUrl = response.getIconUrl();
        proxyIconUrl = response.getProxyIconUrl();
    }

    public String getText() {
        return text;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public String getProxyIconUrl() {
        return proxyIconUrl;
    }

    @Override
    public String toString() {
        return "EmbedFooterData{" +
                "text='" + text + '\'' +
                ", iconUrl='" + iconUrl + '\'' +
                ", proxyIconUrl='" + proxyIconUrl + '\'' +
                '}';
    }
}
