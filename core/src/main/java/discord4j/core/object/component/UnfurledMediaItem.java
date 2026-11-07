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
import discord4j.discordjson.json.ContentScanMetadata;
import discord4j.discordjson.json.UnfurledMediaItemData;
import discord4j.discordjson.possible.Possible;

import java.io.InputStream;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

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
     * Creates an {@link UnfurledMediaItem} with a given {@link discord4j.core.spec.MessageCreateFields.File}.
     *
     * @apiNote The file passed is supposed to be used in the creation of the message for use with the format {@code attachment://}
     * @param file The file to use
     * @return An {@link UnfurledMediaItem}
     * @see discord4j.core.spec.MessageCreateFields.File#of(String, InputStream)
     * @see discord4j.core.spec.MessageCreateSpec#withFiles(discord4j.core.spec.MessageCreateFields.File...) MessageCreateSpec#withFiles(MessageCreateFields.File...)
     * @see discord4j.core.spec.InteractionApplicationCommandCallbackReplyMono#withFiles(discord4j.core.spec.MessageCreateFields.File...) InteractionApplicationCommandCallbackReplyMono#withFiles(MessageCreateFields.File...)
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

    /**
     * Returns the flags of this {@link UnfurledMediaItem}.
     *
     * @return A {@code EnumSet} with the flags
     */
    public Set<Flag> getFlags() {
        return Possible.flatOpt(this.getData().flags())
                .map(UnfurledMediaItem.Flag::of)
                .orElse(EnumSet.noneOf(UnfurledMediaItem.Flag.class));
    }

    /**
     * Returns the results of Discord’s content scanning, if the media item has been scanned.
     *
     * @return A {@code EnumSet} with the flags
     */
    public Set<ContentScanMetadataFlag> getContentScanMetadataFlags() {
        return Possible.flatOpt(this.getData().contentScanMetadata())
                .map(ContentScanMetadata::flags)
                .map(UnfurledMediaItem.ContentScanMetadataFlag::of)
                .orElse(EnumSet.noneOf(UnfurledMediaItem.ContentScanMetadataFlag.class));
    }

    public enum LoadingState {
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

    /**
     * Represent the {@link UnfurledMediaItem} flags.
     *
     * @see <a href="https://docs.discord.com/developers/components/reference#unfurled-media-item-unfurled-media-item-flags">https://docs.discord.com/developers/components/reference#unfurled-media-item-unfurled-media-item-flags</a>
     */
    public enum Flag {
        /**
         * This image is animated.
         */
        IS_ANIMATED(0);

        /**
         * The underlying value as represented by Discord.
         */
        private final int value;

        /**
         * The flag value as represented by Discord.
         */
        private final int flag;

        /**
         * Constructs a {@code UnfurledMediaItem.Flag}.
         */
        Flag(final int value) {
            this.value = value;
            this.flag = 1 << value;
        }

        /**
         * Gets the underlying value as represented by Discord.
         *
         * @return The underlying value as represented by Discord
         */
        public int getValue() {
            return this.value;
        }

        /**
         * Gets the flag value as represented by Discord.
         *
         * @return The flag value as represented by Discord
         */
        public int getFlag() {
            return this.flag;
        }

        /**
         * Gets the flags of {@link UnfurledMediaItem}. It is guaranteed that invoking {@link #getValue()} from the returned enum will be
         * equal ({@code ==}) to the supplied {@code value}.
         *
         * @param value The flags value as represented by Discord
         * @return The {@link EnumSet} of flags
         */
        public static Set<Flag> of(final int value) {
            final EnumSet<Flag> flags = EnumSet.noneOf(Flag.class);
            for (final Flag flag : Flag.values()) {
                final long flagValue = flag.getFlag();
                if ((flagValue & value) == flagValue) {
                    flags.add(flag);
                }
            }
            return flags;
        }

    }

    /**
     * Represent the content scan metadata flags of {@link UnfurledMediaItem}.
     *
     * @see <a href="https://docs.discord.com/developers/components/reference#unfurled-media-item-content-scan-metadata-structure">https://docs.discord.com/developers/components/reference#unfurled-media-item-content-scan-metadata-structure</a>
     */
    public enum ContentScanMetadataFlag {
        /**
         * This media was flagged as explicit by Discord’s content scanning.
         */
        IS_EXPLICIT_CONTENT(0),
        /**
         * This media was flagged as gore by Discord’s content scanning.
         */
        IS_GORE_CONTENT(1),
        /**
         * This media was flagged as self-harm by Discord’s content scanning.
         */
        IS_SELF_HARM_CONTENT(2);

        /**
         * The underlying value as represented by Discord.
         */
        private final int value;

        /**
         * The flag value as represented by Discord.
         */
        private final int flag;

        /**
         * Constructs a {@code ContentScanMetadataFlag}.
         */
        ContentScanMetadataFlag(final int value) {
            this.value = value;
            this.flag = 1 << value;
        }

        /**
         * Gets the underlying value as represented by Discord.
         *
         * @return The underlying value as represented by Discord
         */
        public int getValue() {
            return this.value;
        }

        /**
         * Gets the flag value as represented by Discord.
         *
         * @return The flag value as represented by Discord
         */
        public int getFlag() {
            return this.flag;
        }

        /**
         * Gets the flags of {@link ContentScanMetadata}. It is guaranteed that invoking {@link #getValue()} from the returned enum will be
         * equal ({@code ==}) to the supplied {@code value}.
         *
         * @param value The flags value as represented by Discord
         * @return The {@link EnumSet} of flags
         */
        public static Set<ContentScanMetadataFlag> of(final int value) {
            final EnumSet<ContentScanMetadataFlag> flags = EnumSet.noneOf(ContentScanMetadataFlag.class);
            for (final ContentScanMetadataFlag flag : ContentScanMetadataFlag.values()) {
                final long flagValue = flag.getFlag();
                if ((flagValue & value) == flagValue) {
                    flags.add(flag);
                }
            }
            return flags;
        }
    }

}
