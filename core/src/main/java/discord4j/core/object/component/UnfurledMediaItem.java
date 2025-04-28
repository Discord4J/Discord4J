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

import discord4j.core.object.entity.Attachment;
import discord4j.core.spec.MessageCreateFields;
import discord4j.discordjson.json.UnfurledMediaItemData;
import discord4j.discordjson.possible.Possible;

import java.io.InputStream;
import java.util.Optional;

/**
 * Represents an unfurled media item.
 */
public class UnfurledMediaItem {

    private static final String PREFIX_DISCORD_ATTACHMENT_REFERENCE = "attachment://";

    /**
     * Creates an {@link UnfurledMediaItem} with a given url.
     *
     * @param url The url to use
     * @return An {@link UnfurledMediaItem}
     */
    public static UnfurledMediaItem of(String url) {
        return new UnfurledMediaItem(UnfurledMediaItemData.builder()
            .url(url)
            .build());
    }

    /**
     * Creates an {@link UnfurledMediaItem} with a given {@link MessageCreateFields.File file}.
     *
     * @apiNote The file passed is supposed to be used in the creation of the message for use with the format {@code attachment://}
     * @param file The file to use
     * @return An {@link UnfurledMediaItem}
     * @see MessageCreateFields.File#of(String, InputStream)
     */
    public static UnfurledMediaItem of(MessageCreateFields.File file) {
        return UnfurledMediaItem.of(PREFIX_DISCORD_ATTACHMENT_REFERENCE.concat(file.name()));
    }

    /**
     * Creates an {@link UnfurledMediaItem} with a given {@link Attachment}.
     *
     * @apiNote The attachment passed is supposed to be in the message
     * @param attachment The attachment to use
     * @return An {@link UnfurledMediaItem}
     * @see Attachment#getUrl()
     */
    public static UnfurledMediaItem of(Attachment attachment) {
        return UnfurledMediaItem.of(attachment.getUrl());
    }

    private final UnfurledMediaItemData data;

    public UnfurledMediaItem(UnfurledMediaItemData data) {
        this.data = data;
    }

    /**
     * Get the raw data of this item
     *
     * @return the raw data of this item
     */
    public UnfurledMediaItemData getData() {
        return this.data;
    }

    /**
     * Gets the URL for this item.
     *
     * @return The url
     */
    public String getURL() {
        return this.getData().url();
    }

    /**
     * Gets the proxy url for this item
     *
     * @return The proxy url for this item if it exists
     */
    public Optional<String> getProxyUrl() {
        return this.getData().proxyUrl().toOptional();
    }

    /**
     * Get the resolved width of this item
     *
     * @return the resolved with or 0 if it failed
     */
    public int getWidth() {
        return Possible.flatOpt(this.getData().width()).orElse(0);
    }

    /**
     * Get the resolved height of this item
     *
     * @return the resolved height or 0 if it failed
     */
    public int getHeight() {
        return Possible.flatOpt(this.getData().height()).orElse(0);
    }

    /**
     * Get the content type of this item
     *
     * @return the content type of this item
     */
    public String getContentType() {
        return Possible.flatOpt(this.getData().contentType()).orElse("");
    }

    /**
     * Get the loading state of this item
     *
     * @return the loading state of this item
     */
    public Optional<LoadingState> getLoadingState() {
        return Possible.flatOpt(this.getData().loadingState()).map(LoadingState::of);
    }

    enum LoadingState {
        UNKNOWN(0),
        LOADING(1),
        LOADED_SUCCESS(2),
        LOADED_NOT_FOUND(3);

        private final int value;

        LoadingState(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }

        public static LoadingState of(int value) {
            switch (value) {
                case 1: return LOADING;
                case 2: return LOADED_SUCCESS;
                case 3: return LOADED_NOT_FOUND;

                case 0:
                default: return UNKNOWN;
            }
        }
    }

}
