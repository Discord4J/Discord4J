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
package discord4j.core.spec.legacy;

import discord4j.common.util.Snowflake;
import discord4j.core.object.Region;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.Channel;
import discord4j.discordjson.Id;
import discord4j.discordjson.json.GuildCreateRequest;
import discord4j.discordjson.json.ImmutableGuildCreateRequest;
import discord4j.discordjson.json.PartialChannelCreateRequest;
import discord4j.discordjson.json.RoleCreateRequest;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.Image;
import reactor.util.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * A spec used to configure and create a {@link Guild}. <b>This can only be used for bots in less than 10 guilds.</b>
 * <p>
 * This spec also has some limitations to it.
 *  - The first role added, either from {@link #addEveryoneRole} or {@link #addRole}, will automatically be set as the
 *  default @everyone role. Each subsequent call to {@link #addEveryoneRole} will not override the first role but shift
 *  all other roles down.
 *  - When using the channels parameter, the position field is ignored, and none of the default channels are created.
 *
 * @see <a href="https://discord.com/developers/docs/resources/guild#create-guild">Create Guild</a>
 */
public class LegacyGuildCreateSpec implements LegacySpec<GuildCreateRequest> {

    private final ImmutableGuildCreateRequest.Builder builder = GuildCreateRequest.builder();
    private final /*~~>*/List<RoleCreateRequest> roles = new ArrayList<>();

    /**
     * Sets the name for the created {@link Guild}.
     *
     * @param name The name of the guild.
     * @return This spec.
     */
    public LegacyGuildCreateSpec setName(String name) {
        builder.name(name);
        return this;
    }

    /**
     * Sets the voice region id for the created {@link Guild}.
     *
     * @param regionId The voice region id for the guild.
     * @return This spec.
     */
    public LegacyGuildCreateSpec setRegion(Region.Id regionId) {
        builder.region(regionId.getValue());
        return this;
    }

    /**
     * Sets the voice region id for the created {@link Guild}, automatic if null.
     *
     * @param regionId The voice region id for the guild, automatic if null.
     * @return This spec.
     */
    public LegacyGuildCreateSpec setRegion(@Nullable String regionId) {
        builder.region(regionId == null ? Possible.absent() : Possible.of(regionId));
        return this;
    }

    /**
     * Sets the image icon to display for the created {@link Guild}.
     *
     * @param icon The icon for the guild.
     * @return This spec.
     */
    public LegacyGuildCreateSpec setIcon(@Nullable Image icon) {
        builder.icon(icon == null ? Possible.absent() : Possible.of(icon.getDataUri()));
        return this;
    }

    /**
     * Sets the verification level required before a member can send messages in the created {@link Guild}.
     *
     * @param verificationLevel The verification level for the guild.
     * @return This spec.
     */
    public LegacyGuildCreateSpec setVerificationLevel(@Nullable Guild.VerificationLevel verificationLevel) {
        builder.verificationLevel(verificationLevel == null ?
                Possible.absent() : Possible.of(verificationLevel.getValue()));
        return this;
    }

    /**
     * Sets the default message notification level for the created {@link Guild}.
     *
     * @param notificationLevel The default notification level for the guild.
     * @return This spec.
     */
    public LegacyGuildCreateSpec setDefaultMessageNotificationLevel(@Nullable Guild.NotificationLevel notificationLevel) {
        builder.defaultMessageNotifications(notificationLevel == null ?
                Possible.absent() : Possible.of(notificationLevel.getValue()));
        return this;
    }

    /**
     * Sets the explicit content filter level for the created {@link Guild}.
     *
     * @param explicitContentFilter The explicit content filter level for the guild.
     * @return This spec.
     */
    public LegacyGuildCreateSpec setExplicitContentFilter(@Nullable Guild.ContentFilterLevel explicitContentFilter) {
        builder.explicitContentFilter(explicitContentFilter == null ?
                Possible.absent() : Possible.of(explicitContentFilter.getValue()));
        return this;
    }

    /**
     * Adds the role spec to the list of roles for the created {@link Guild}.
     *
     * @param legacyroleSpec The role spec to add to the list of roles.
     * @return This spec.
     */
    public LegacyGuildCreateSpec addRole(Consumer<? super LegacyRoleCreateSpec> legacyroleSpec) {
        final LegacyRoleCreateSpec mutatedSpec = new LegacyRoleCreateSpec();
        legacyroleSpec.accept(mutatedSpec);
        roles.add(mutatedSpec.asRequest());
        return this;
    }

    /**
     * Sets the default @everyone role for the created {@link Guild}. This shifts all other roles in the list, if
     * present, down by one. It does not replace other @everyone roles already set.
     * <p>
     * When creating a guild, Discord automatically takes the first role in the role array as the default @everyone
     * role. See this limitation and others at {@link LegacyGuildCreateSpec}.
     *
     * @param legacyroleSpec The default @everyone role spec to add to the list of roles.
     * @return This spec.
     */
    public LegacyGuildCreateSpec addEveryoneRole(Consumer<? super LegacyRoleCreateSpec> legacyroleSpec) {
        final LegacyRoleCreateSpec mutatedSpec = new LegacyRoleCreateSpec();
        legacyroleSpec.accept(mutatedSpec);
        roles.add(0, mutatedSpec.asRequest());
        return this;
    }

    /**
     * Adds the channel to the list of channels for the created {@link Guild}.
     *
     * @param name The name of the channel.
     * @param type The type of the channel.
     * @return This spec.
     */
    public LegacyGuildCreateSpec addChannel(String name, Channel.Type type) {
        builder.addChannel(PartialChannelCreateRequest.builder()
                .name(name)
                .type(type.getValue())
                .build());
        return this;
    }

    /**
     * Sets the ID of the AFK channel for the created {@link Guild}.
     *
     * @param id id for afk channel
     * @return This spec.
     */
    public LegacyGuildCreateSpec setAfkChannelId(@Nullable Snowflake id) {
        builder.afkChannelId(id == null ? Possible.absent() : Possible.of(Id.of(id.asLong())));
        return this;
    }

    /**
     * Sets the AFK timeout, in seconds, for the created {@link Guild}.
     *
     * @param afkTimeout The AFK timeout, in seconds.
     * @return This spec.
     */
    public LegacyGuildCreateSpec setAfkTimeout(@Nullable Integer afkTimeout) {
        builder.afkTimeout(afkTimeout == null ? Possible.absent() : Possible.of(afkTimeout));
        return this;
    }

    /**
     * Sets the id of the channel where guild notices such as welcome messages and boost events are posted for the
     * created {@link Guild}.
     *
     * @param id The id of the channel where guild notices such as welcome messages and boost events are posted.
     * @return This spec.
     */
    public LegacyGuildCreateSpec setSystemChannelId(@Nullable Snowflake id) {
        builder.systemChannelId(id == null ? Possible.absent() : Possible.of(Id.of(id.asLong())));
        return this;
    }

    /**
     * Sets the system channel flags for the created {@link Guild}.
     *
     * @param flags The system channel flags.
     * @return This spec.
     */
    public LegacyGuildCreateSpec setSystemChannelFlags(@Nullable Guild.SystemChannelFlag... flags) {
        if (flags != null) {
            builder.systemChannelFlags(Possible.of(Arrays.stream(flags)
                    .mapToInt(Guild.SystemChannelFlag::getValue)
                    .reduce(0, (left, right) -> left | right)));
        } else {
            builder.systemChannelFlags(Possible.absent());
        }
        return this;
    }

    @Override
    public GuildCreateRequest asRequest() {
        builder.roles(roles);
        return builder.build();
    }
}
