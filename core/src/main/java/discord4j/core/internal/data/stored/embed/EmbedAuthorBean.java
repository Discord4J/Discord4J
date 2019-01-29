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
package discord4j.core.internal.data.stored.embed;

import discord4j.common.json.EmbedAuthorResponse;

import java.io.Serializable;

public final class EmbedAuthorBean implements Serializable {

    private static final long serialVersionUID = 5914439836499107142L;

    private String name;
    private String url;
    private String iconUrl;
    private String proxyIconUrl;

    public EmbedAuthorBean(final EmbedAuthorResponse response) {
        name = response.getName();
        url = response.getUrl();
        iconUrl = response.getIconUrl();
        proxyIconUrl = response.getProxyIconUrl();
    }

    public EmbedAuthorBean() {}

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(final String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getProxyIconUrl() {
        return proxyIconUrl;
    }

    public void setProxyIconUrl(final String proxyIconUrl) {
        this.proxyIconUrl = proxyIconUrl;
    }

    @Override
    public String toString() {
        return "EmbedAuthorBean{" +
                "name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", iconUrl='" + iconUrl + '\'' +
                ", proxyIconUrl='" + proxyIconUrl + '\'' +
                '}';
    }
}
