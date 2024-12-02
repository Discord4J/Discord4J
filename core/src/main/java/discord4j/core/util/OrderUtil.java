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
package discord4j.core.util;

import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.channel.*;
import discord4j.common.util.Snowflake;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.function.Function;

/** A utility class for the sorting of {@link Role roles} and {@link GuildChannel guild channels}. */
public final class OrderUtil {

    /**
     * The ordering of Discord {@link Role roles}.
     * <p>
     * In Discord, two orderable entities may have the same "raw position," the position as reported by the "position" field.
     * This conflict is resolved by comparing the creation time of the entities, reflected in their {@link Snowflake IDs}.
     */
    public static final Comparator<Role> ROLE_ORDER =
            Comparator.comparing(Role::getRawPosition).thenComparing(Role::getId);

    /**
     * The base ordering of Discord {@link GuildChannel guild channels}.
     * <p>
     * In Discord, two orderable entities may have the same "raw position," the position as reported by the "position" field.
     * This conflict is resolved by comparing the creation time of the entities, reflected in their {@link Snowflake IDs}.
     * <p>
     * Note that this order is only applicable to channels if they are of the same type and in the same category. See
     * {@link #BUCKETED_CHANNEL_ORDER} for ordering between different channel types.
     */
    public static final Comparator<TopLevelGuildChannel> CHANNEL_ORDER =
            Comparator.comparing(TopLevelGuildChannel::getRawPosition).thenComparing(GuildChannel::getId);

    /**
     * The ordering of {@link GuildChannel guild channels} which considers channel type.
     * <p>
     * Guild channels are first ordered by "bucket" which is determined by the type of the channels. Then,
     * {@link #CHANNEL_ORDER} is used to determine order within a buvket. Effectively, this only means that voice
     * channels always appear below other types of channels.
     * <p>
     * Note that this order is only applicable to channels if they are in the same category.
     */
    public static final Comparator<CategorizableChannel> BUCKETED_CHANNEL_ORDER =
            Comparator.<CategorizableChannel>comparingInt(c -> {
                if (c instanceof VoiceChannel) {
                    return 1;
                }
                return 0;
            }).thenComparing(CHANNEL_ORDER);

    /**
     * Sorts {@link GuildChannel guild channels} according to visual ordering in Discord. Channels at the top of the
     * list are first. This sorts channels within the same category according to {@link #BUCKETED_CHANNEL_ORDER} and
     * then sorts those categories according to {@link #CHANNEL_ORDER}.
     * <p>
     * This function can be used with {@link Flux#transform(Function)} for better chaining:
     * <pre>
     * {@code
     * guild.getChannels()
     *   .transform(OrderUtil::orderGuildChannels)
     * }
     * </pre>
     *
     * @param channels The guild channels to sort.
     * @return The sorted guild channels.
     */
    public static Flux<GuildChannel> orderGuildChannels(Flux<GuildChannel> channels) {
        return channels
                .collectMap(GuildChannel::getId) // associate channels to ids
                .flatMapIterable(OrderUtil::orderGuildChannels);
    }

    /**
     * Sorts {@link Role roles} according to visual ordering in Discord. Roles at the bottom of the list are first. This
     * sorts roles according to {@link #ROLE_ORDER}.
     * <p>
     * This function can be used with {@link Flux#transform(Function)} for better chaining:
     * <pre>
     * {@code
     * guild.getRoles()
     *   .transform(OrderUtil::orderRoles)
     * }
     * </pre>
     *
     * @param roles The roles to sort.
     * @return The sorted roles.
     */
    public static Flux<Role> orderRoles(Flux<Role> roles) {
        return roles.sort(OrderUtil.ROLE_ORDER);
    }

    private static List<GuildChannel> orderGuildChannels(Map<Snowflake, GuildChannel> channels) {
        // associate channels to their parent category
        // sorted by raw position then ID
        // channels not in a category always appear before all other channels, so nulls are first
        Map<Category, SortedSet<CategorizableChannel>> byCategory = new TreeMap<>(Comparator.nullsFirst(CHANNEL_ORDER));
        channels.forEach((id, channel) -> {
            if (channel instanceof CategorizableChannel) {
                CategorizableChannel categorizable = (CategorizableChannel) channel;
                Category parent = (Category) channels.get(categorizable.getCategoryId().orElse(null));

                // add the channel to the set of channels in the category (creating the set if necessary)
                // sorted by sort bucket, then raw position, then ID
                // sort bucket is determined by channel type. Voice channels always appear below other types
                byCategory.computeIfAbsent(parent, __ -> new TreeSet<>(BUCKETED_CHANNEL_ORDER)).add(categorizable);
            } else {
                // to account for empty categories
                byCategory.putIfAbsent(((Category) channel), new TreeSet<>(BUCKETED_CHANNEL_ORDER));
            }
        });

        // flatten the map into a list which must be sorted
        List<GuildChannel> sorted = new ArrayList<>();
        byCategory.forEach((category, children) -> {
            if (category != null) { // don't add the "parent" of channels which don't have one
                sorted.add(category);
            }
            sorted.addAll(children);
        });

        return sorted;
    }
}
