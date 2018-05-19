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
package discord4j.core.object.data.stored;

import discord4j.common.json.AttachmentResponse;

import javax.annotation.Nullable;
import java.io.Serializable;

public final class AttachmentBean implements Serializable {

    private static final long serialVersionUID = -5000709886925013964L;

    private long id;
    private String fileName;
    private int size;
    private String url;
    private String proxyUrl;
    @Nullable
    private Integer height;
    @Nullable
    private Integer width;

    public AttachmentBean(final AttachmentResponse response) {
        id = response.getId();
        fileName = response.getFileName();
        size = response.getSize();
        url = response.getUrl();
        proxyUrl = response.getProxyUrl();
        height = response.getHeight();
        width = response.getWidth();
    }

    public AttachmentBean() {}

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    public int getSize() {
        return size;
    }

    public void setSize(final int size) {
        this.size = size;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public String getProxyUrl() {
        return proxyUrl;
    }

    public void setProxyUrl(final String proxyUrl) {
        this.proxyUrl = proxyUrl;
    }

    @Nullable
    public Integer getHeight() {
        return height;
    }

    public void setHeight(@Nullable final Integer height) {
        this.height = height;
    }

    @Nullable
    public Integer getWidth() {
        return width;
    }

    public void setWidth(@Nullable final Integer width) {
        this.width = width;
    }
}
