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

import discord4j.common.json.EmbedVideoResponse;

public class EmbedVideoData {

    private final String url;
    private final String proxyUrl;
    private final int height;
    private final int width;

    public EmbedVideoData(EmbedVideoResponse response) {
        url = response.getUrl();
        proxyUrl = response.getProxyUrl();
        height = response.getHeight();
        width = response.getWidth();
    }

    public String getUrl() {
        return url;
    }

    public String getProxyUrl() {
        return proxyUrl;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    @Override
    public String toString() {
        return "EmbedVideoData{" +
                "url='" + url + '\'' +
                ", proxyUrl='" + proxyUrl + '\'' +
                ", height=" + height +
                ", width=" + width +
                '}';
    }
}
