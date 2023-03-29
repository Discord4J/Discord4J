package discord4j.core.object.entity.channel;

import discord4j.common.annotations.Experimental;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.ForumTag;
import discord4j.core.object.reaction.DefaultReaction;
import discord4j.discordjson.json.ChannelData;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A Discord guild channel that contains organized threads with labels.
 */
@Experimental
public final class ForumChannel extends BaseTopLevelGuildChannel implements CategorizableChannel {

    public ForumChannel(GatewayDiscordClient gateway, ChannelData data) {
        super(gateway, data);
    }

    /**
     * Gets the topic for this guild forum.
     *
     * @return the user defined topic for this guild forum channel (0-4096 characters).
     */
    public Optional<String> getTopic() {
        return getData().topic().toOptional()
            .flatMap(opt -> opt); // Value is present on guild forum type but nullable
    }

    /**
     * Gets whether this channel is considered NSFW (Not Safe For Work).
     *
     * @return {@code true} if this channel is considered NSFW (Not Safe For Work), {@code false} otherwise.
     */
    public boolean isNsfw() {
        return getData().nsfw().toOptional().orElse(false);
    }

    /* Represents guild forum channels flags */
    //TODO Should we create a special "Unknown flag" or warn about unknown flags ? Do they change frequently enough ?
    @Experimental
    public enum ForumChannelFlag {
        PINNED(1),
        REQUIRE_TAG(4);

        private final int shiftValue;
        private final int bitValue;

        ForumChannelFlag(int shiftValue) {
            this.shiftValue = shiftValue;
            this.bitValue = 1 << shiftValue;
        }

        /**
         * Gets the shift amount associated to this bit value
         *
         * @return N in 1 << N that is the bit value for this flag
         */
        public int getShiftValue() {
            return shiftValue;
        }

        /**
         * Gets the bit value associated to this flag
         *
         * @return The bit field value associated to this flag
         */
        public int getBitValue() {
            return bitValue;
        }

        /**
         * Translate a bitfield value into an {@link EnumSet< ForumChannelFlag >} related to known flags
         *
         * @param bitfield An integer representing the flags, one per bit
         * @return An {@link EnumSet< ForumChannelFlag >} of known flags associated to this bit field
         * @implNote This implementation ignores unknown flags
         */
        public static EnumSet<ForumChannelFlag> valueOf(final int bitfield) {
            EnumSet<ForumChannelFlag> returnSet = EnumSet.noneOf(ForumChannelFlag.class);
            for (ForumChannelFlag flag : ForumChannelFlag.values()) {
                if ((bitfield & flag.getBitValue()) != 0) {
                    returnSet.add(flag);
                }
            }
            return returnSet;
        }

    }

    /**
     * Gets the {@link ForumChannelFlag} flags associated to this forum channel
     * Unknown flags are currently ignored.
     *
     * @return An {@link EnumSet} representing the <b>known flags</b> for this forum channel.
     */
    public EnumSet<ForumChannelFlag> getFlags() {
        return getData().flags().toOptional()
            .map(ForumChannelFlag::valueOf)
            .orElseThrow(IllegalStateException::new); // Mandatory for Forum Channels
    }

    /**
     * Gets the default auto archive duration for threads in this forum channel
     *
     * @return Default client auto archive duration in minutes
     */
    public int getDefaultAutoArchiveDuration() {
        return getData().defaultAutoArchiveDuration().toOptional()
            .orElseThrow(IllegalStateException::new); // Mandatory for Forum channels
    }

    /**
     * Gets the available forum tags for this forum channel
     *
     * @return A list containing every available forum tag for this channel
     */
    public List<ForumTag> getAvailableTags() {
        return getData().availableTags().toOptional()
            .orElseThrow(IllegalStateException::new) // Mandatory for Forum channels
            .stream()
            .map(forumTagData -> new ForumTag(getClient(), forumTagData))
            .collect(Collectors.toList());
    }

    /**
     * Gets the default emoji to add in a reaction button on a thread for this forum channel
     *
     * @return An {@link Optional} that may contain the {@link DefaultReaction} emoji
     */
    public Optional<DefaultReaction> getDefaultReaction() {
        return getData().defaultReactionEmoji()
            .toOptional().orElseThrow(IllegalStateException::new) // Mandatory for Forum channels
            .map(defaultReactionData -> new DefaultReaction(getClient(), defaultReactionData));
    }

    /**
     * Gets the default thread rate limit duration per user.
     * This field is copied into a created thread and is not updated on the fly.
     *
     * @return The initial rate limit per user for newly created threads
     */
    public int getDefaultThreadRateLimitPerUser() {
        return getData().defaultThreadRateLimitPerUser().toOptional()
            .orElseThrow(IllegalStateException::new); // Mandatory for Forum channels
    }

    @Experimental
    public enum SortOrder {

        /**
         * Internal value only, for unknown values
         */
        UNKNOWN(-1),
        LATEST_ACTIVITY(0),
        CREATION_DATE(1);

        /**
         * Discord represented integer value
         */
        private final int value;

        SortOrder(int value) {
            this.value = value;
        }

        /**
         * Gets Discord represented value
         *
         * @return Discord integer represented value
         */
        public int getValue() {
            return value;
        }

        /**
         * Translates an integer value to a {@link SortOrder}
         *
         * @param value The integer value
         * @return The SortOrder if known, or the fallback UNKNOWN value
         */
        public static SortOrder valueOf(final int value) {
            /* We could exclude UNKNOWN from the lookup array using
            Arrays.copyOfRange(SortOrder.values(), 1, SortOrder.values().length) but this would just waste CPU time
            because if -1 is one day used by Discord we would need anyway to update the code here */
            for (SortOrder sortOrder : SortOrder.values()) {
                if (sortOrder.getValue() == value) {
                    return sortOrder;
                }
            }
            // Fallback to UNKNOWN as default value
            return SortOrder.UNKNOWN;
        }
    }

    /**
     * Gets the default associated {@link SortOrder} for this forum channel
     *
     * @return The {@link SortOrder}, wrapped in an {@link Optional}
     */
    public Optional<SortOrder> getSortOrder() {
        return getData().defaultSortOrder().toOptional()
            .orElseThrow(IllegalStateException::new) // Mandatory for Forum channels
            .map(SortOrder::valueOf);
    }

    @Experimental
    public enum LayoutType {
        /**
         * Internal value only, for unknown values
         */
        UNKNOWN(-1),
        NOT_SET(0),
        LIST_VIEW(1),
        GALLERY_VIEW(2);

        /**
         * Discord represented integer value
         */
        private final int value;

        LayoutType(int value) {
            this.value = value;
        }

        /**
         * Gets Discord represented value
         *
         * @return Discord integer represented value
         */
        public int getValue() {
            return value;
        }

        /**
         * Translates an integer value to a {@link LayoutType}
         *
         * @param value The integer value
         * @return The {@link LayoutType} if known, or the fallback UNKNOWN value
         */
        public static LayoutType valueOf(final int value) {
            // Same as SortOrder, we could exclude the UNKNOWN value
            for (LayoutType layoutType : LayoutType.values()) {
                if (layoutType.getValue() == value) {
                    return layoutType;
                }
            }
            // Fallback to UNKNOWN as default value
            return LayoutType.UNKNOWN;
        }
    }

    /**
     * Gets the associated {@link LayoutType} for this Forum channel
     *
     * @return The associated {@link LayoutType}
     */
    public LayoutType getDefaultForumLayout() {
        return getData().defaultForumLayout().toOptional()
            .map(LayoutType::valueOf)
            .orElseThrow(IllegalStateException::new); // Mandatory for Forum channels
    }
}
