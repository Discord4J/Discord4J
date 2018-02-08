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
package discord4j.core.object.data;

import javax.annotation.Nullable;

public class AttachmentData {

	private final long id;
	private final String fileName;
	private final int size;
	private final String url;
	private final String proxyUrl;
	@Nullable
	private final Integer height;
	@Nullable
	private final Integer width;

	public AttachmentData(long id, String fileName, int size, String url, String proxyUrl, @Nullable Integer height,
			@Nullable Integer width) {
		this.id = id;
		this.fileName = fileName;
		this.size = size;
		this.url = url;
		this.proxyUrl = proxyUrl;
		this.height = height;
		this.width = width;
	}

	public long getId() {
		return id;
	}

	public String getFileName() {
		return fileName;
	}

	public int getSize() {
		return size;
	}

	public String getUrl() {
		return url;
	}

	public String getProxyUrl() {
		return proxyUrl;
	}

	@Nullable
	public Integer getHeight() {
		return height;
	}

	@Nullable
	public Integer getWidth() {
		return width;
	}
}
