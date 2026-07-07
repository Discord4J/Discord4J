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
package discord4j.core.object.entity;

import discord4j.discordjson.json.AttachmentData;
import discord4j.core.GatewayDiscordClient;
import discord4j.common.util.Snowflake;
import discord4j.core.util.EntityUtil;
import discord4j.discordjson.possible.Possible;
import org.jspecify.annotations.Nullable;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * A Discord attachment.
 *
 * @see <a href="https://discord.com/developers/docs/resources/channel#attachment-object">Attachment Object</a>
 */
public final class Attachment implements Entity {

    /**
     * The prefix of the name of files which are displayed as spoilers.
     *
     * @deprecated Discord now uses {@code Attachment.AttachmentFlags#IS_SPOILER} flag on the attachment object instead.
     * The {@code SPOILER_} prefix still marks a file as spoiler, but not all spoilered attachments use it.
     **/
    @Deprecated
    public static final String SPOILER_PREFIX = "SPOILER_";

    /** The gateway associated to this object. */
    private final GatewayDiscordClient gateway;

    /** The raw data as represented by Discord. */
    private final AttachmentData data;

    /**
     * Constructs an {@code Attachment} with an associated {@link GatewayDiscordClient} and Discord data.
     *
     * @param gateway The {@link GatewayDiscordClient} associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    public Attachment(final GatewayDiscordClient gateway, final AttachmentData data) {
        this.gateway = Objects.requireNonNull(gateway);
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public GatewayDiscordClient getClient() {
        return this.gateway;
    }

    @Override
    public Snowflake getId() {
        return Snowflake.of(this.getData().id());
    }

    /**
     * Gets the data of the attachment.
     *
     * @return The data of the attachment.
     */
    public AttachmentData getData() {
        return this.data;
    }

    /**
     * Gets the name of the file attached.
     *
     * @return The name of the file attached.
     */
    public String getFilename() {
        return this.getData().filename();
    }

    /**
     * Gets the title of the file attached, if present.
     *
     * @return The title of the file attached, if present.
     */
    public Optional<String> getTitle() {
        return this.getData().title().toOptional();
    }

    /**
     * Gets the description of the file attached, if present.
     *
     * @return The description of the file attached, if present.
     */
    public Optional<String> getDescription() {
        return this.getData().description().toOptional();
    }

    /**
     * Gets the size of the file in bytes.
     *
     * @return The size of the file in bytes.
     */
    public int getSize() {
        return this.getData().size();
    }

    /**
     * Gets the source URL of the file.
     *
     * @return The source URL of the file.
     */
    public String getUrl() {
        return this.getData().url();
    }

    /**
     * Gets a proxied URL of the file.
     *
     * @return A proxied URL of the file.
     */
    public String getProxyUrl() {
        return this.getData().proxyUrl();
    }

    /**
     * Gets the height of the file, if present.
     *
     * @return The height of the file, if present.
     */
    public OptionalInt getHeight() {
        return Possible.flatOpt(this.getData().height())
            .map(OptionalInt::of)
            .orElse(OptionalInt.empty());
    }

    /**
     * Gets the width of the file, if present.
     *
     * @return The width of the file, if present.
     */
    public OptionalInt getWidth() {
        return Possible.flatOpt(this.getData().width())
            .map(OptionalInt::of)
            .orElse(OptionalInt.empty());
    }

    /**
     * Gets whether the attachment is a spoiler.
     *
     * @return {@code true} if the attachment is a spoiler, {@code false} otherwise.
     */
    public boolean isSpoiler() {
        return this.getFlags().contains(AttachmentFlags.IS_SPOILER);
    }

    /**
     * Gets the attachment's media type, if present.
     *
     * @return The attachment's media type, if present.
     */
    public Optional<String> getContentType() {
        return this.getData().contentType().toOptional();
    }

    /**
     * Gets the attachment's duration in seconds, if present.
     * Note that this is currently only present for voice messages.
     *
     * @return The attachment's duration in seconds, if present.
     */
    public Optional<Float> getDurationSeconds() {
        return this.getData().durationSeconds().toOptional();
    }

    /**
     * Gets the attachment's wave form, if present.
     * Note that this is currently only present for voice messages.
     *
     * @return A base64 encoded bytearray representing a sampled waveform, if present.
     */
    public Optional<String> getWaveform() {
        return this.getData().waveform().toOptional();
    }

    /**
     * Returns the flags of the attachment.
     *
     * @return A {@code EnumSet} with the flags of the attachment.
     */
    public EnumSet<AttachmentFlags> getFlags() {
        return AttachmentFlags.of(this.getData().flags().toOptional().orElse(0));
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return EntityUtil.equals(this, obj);
    }

    @Override
    public int hashCode() {
        return EntityUtil.hashCode(this);
    }

    @Override
    public String toString() {
        return "Attachment{" +
                "data=" + this.data +
                '}';
    }

    /**
     * A set of flags for an {@link Attachment attachment}.
     */
    public enum AttachmentFlags {

        /**
         * This attachment is a Clip from a stream.
         */
        IS_CLIP(1),

        /**
         * This attachment has been edited using the remix feature on mobile (deprecated).
         */
        @Deprecated
        IS_THUMBNAIL(1 << 1),

        /**
         * This attachment has been edited using the remix feature on mobile.
         */
        IS_REMIX(1 << 2),

        /**
         * This attachment was marked as a spoiler and is blurred until clicked.
         */
        IS_SPOILER(1 << 3),

        /**
         * This attachment is an animated image.
         */
        IS_ANIMATED(1 << 5),;

        /** The flag value as represented by Discord. */
        private final int value;

        /**
         * Constructs an {@code AttachmentFlags} with a value.
         *
         * @param value The value of the attachment flag.
         */
        AttachmentFlags(int value) {
            this.value = value;
        }

        /**
         * Gets the flag value as represented by Discord.
         *
         * @return The flag value as represented by Discord.
         */
        public int getValue() {
            return this.value;
        }

        /**
         * Gets the flags of the attachment. It is guaranteed that invoking {@link #getValue()} from the returned enum
         * will be equal ({@code ==}) to the supplied {@code value}.
         *
         * @param value The flags value as represented by Discord.
         * @return The {@link EnumSet} of flags.
         */
        public static EnumSet<AttachmentFlags> of(final int value) {
            final EnumSet<AttachmentFlags> flags = EnumSet.noneOf(AttachmentFlags.class);

            for (final AttachmentFlags flag : AttachmentFlags.values()) {
                final long flagValue = flag.getValue();

                if ((flagValue & value) == flagValue) {
                    flags.add(flag);
                }
            }

            return flags;
        }
    }
}
