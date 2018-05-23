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

package discord4j.rest.util;

import discord4j.rest.json.request.MessageCreateRequest;

import javax.annotation.Nullable;
import java.io.InputStream;

public class MultipartRequest {

    private final MessageCreateRequest createRequest;
    private final String fileName;
    private final InputStream file;

    public MultipartRequest(MessageCreateRequest createRequest) {
        this(createRequest, null, null);
    }

    public MultipartRequest(MessageCreateRequest createRequest, @Nullable String fileName, @Nullable InputStream file) {
        this.createRequest = createRequest;
        this.fileName = fileName;
        this.file = file;
    }

    @Nullable
    public MessageCreateRequest getCreateRequest() {
        return createRequest;
    }

    @Nullable
    public String getFileName() {
        return fileName;
    }

    @Nullable
    public InputStream getFile() {
        return file;
    }
}
