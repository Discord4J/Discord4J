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
package discord4j.core.object.component;

import discord4j.common.util.Snowflake;
import discord4j.discordjson.json.ComponentData;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A File Upload component for modals.
 *
 * @see <a href="https://discord.com/developers/docs/components/reference#file-upload">File Upload</a>
 */
public class FileUpload extends MessageComponent implements ICanBeUsedInLabelComponent {

    /**
     * Creates a {@link FileUpload}.
     *
     * @param customId A developer-defined identifier
     * @return A {@link FileUpload}
     */
    public static FileUpload of(String customId) {
        return new FileUpload(MessageComponent.getBuilder(Type.FILE_UPLOAD).customId(customId).build());
    }

    /**
     * Creates a {@link FileUpload}.
     *
     * @param customId A developer-defined identifier
     * @param id the component id
     * @return A {@link FileUpload}
     */
    public static FileUpload of(String customId, int id) {
        return new FileUpload(MessageComponent.getBuilder(Type.FILE_UPLOAD).id(id).customId(customId).build());
    }

    FileUpload(ComponentData data) {
        super(data);
    }

    /**
     * Get this file upload's custom id.
     *
     * @return A developer-defined custom id
     */
    public String getCustomId() {
        return this.getData().customId().toOptional().orElseThrow(IllegalStateException::new);
    }

    /**
     * Gets the file upload values, if any. Can be present with an empty list if no elements were added.
     *
     * @return the file upload's values
     */
    public Optional<List<Snowflake>> getValues() {
        return this.getData().values().toOptional().map(strings -> strings.stream().map(Snowflake::of).collect(Collectors.toList()));
    }

    /**
     * Creates a new file upload with the same data as this one, but depending on the value param, it may be
     * required or not.
     *
     * @param value True if the component should be required otherwise False.
     * @return A new possibly required select menu with the same data as this one.
     */
    public FileUpload required(boolean value) {
        return new FileUpload(ComponentData.builder().from(this.getData()).required(value).build());
    }

    /**
     * Creates a new file upload with the same data as this one, but with the given minimum values.
     *
     * @param minValues The new minimum values (0-10)
     * @return A new file upload with the given minimum values.
     */
    public FileUpload withMinValues(int minValues) {
        return new FileUpload(ComponentData.builder().from(this.getData()).minValues(minValues).build());
    }

    /**
     * Creates a new file upload with the same data as this one, but with the given maximum values.
     *
     * @param maxValues The new maximum values (1-10)
     * @return A new file upload with the given maximum values.
     */
    public FileUpload withMaxValues(int maxValues) {
        return new FileUpload(ComponentData.builder().from(this.getData()).maxValues(maxValues).build());
    }
}
