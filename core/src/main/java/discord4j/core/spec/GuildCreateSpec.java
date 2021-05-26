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
package discord4j.core.spec;

import discord4j.core.object.Region;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.Channel;
import discord4j.discordjson.json.GuildCreateRequest;
import discord4j.discordjson.json.PartialChannelCreateRequest;
import discord4j.discordjson.json.RoleCreateRequest;
import discord4j.rest.util.Image;
import reactor.util.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A spec used to configure and create a {@link Guild}. <b>This can only be used for bots in less than 10 guilds.</b>
 * <p>
 * This spec also has some limitations to it. The name, region, verification level, and default message notification
 * level are all required to be able to properly build the spec. The first role added, either from
 * {@link #addEveryoneRole} or {@link #addRole}, will automatically be set as the default @everyone role. Each
 * subsequent call to {@link #addEveryoneRole} will not override the first role but shift all other roles down.
 *
 * @see <a href="https://discord.com/developers/docs/resources/guild#create-guild">Create Guild</a>
 */
public class GuildCreateSpec implements Spec<GuildCreateRequest> {

    private String name;
    private String region;
    @Nullable
    private String icon;
    private int verificationLevel;
    private int defaultMessageNotificationLevel;
    private int explicitContentFilter;
    private final List<RoleCreateRequest> roles = new ArrayList<>();
    private final List<PartialChannelCreateRequest> channels = new ArrayList<>();

    /**
     * Sets the name for the created {@link Guild}.
     *
     * @param name The name of the guild.
     * @return This spec.
     */
    public GuildCreateSpec setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the voice region for the created {@link Guild}.
     *
     * @param regionId The voice region for the guild.
     * @return This spec.
     */
    public GuildCreateSpec setRegion(Region.Id regionId) {
        this.region = regionId.getValue();
        return this;
    }

    /**
     * Sets the image icon to display for the created {@link Guild}.
     *
     * @param icon The icon for the guild.
     * @return This spec.
     */
    public GuildCreateSpec setIcon(@Nullable Image icon) {
        this.icon = (icon == null) ? null : icon.getDataUri();
        return this;
    }

    /**
     * Sets the verification level required before a member can send messages in the created {@link Guild}.
     *
     * @param verificationLevel The verification level for the guild.
     * @return This spec.
     */
    public GuildCreateSpec setVerificationLevel(Guild.VerificationLevel verificationLevel) {
        this.verificationLevel = verificationLevel.getValue();
        return this;
    }

    /**
     * Sets the default message notification level for the created {@link Guild}.
     *
     * @param notificationLevel The default notification level for the guild.
     * @return This spec.
     */
    public GuildCreateSpec setDefaultMessageNotificationLevel(Guild.NotificationLevel notificationLevel) {
        this.defaultMessageNotificationLevel = notificationLevel.getValue();
        return this;
    }

    /**
     * Sets the explicit content filter level for the created {@link Guild}.
     *
     * @param explicitContentFilter The explicit content filter level for the guild.
     * @return This spec.
     */
    public GuildCreateSpec setExplicitContentFilter(Guild.ContentFilterLevel explicitContentFilter) {
        this.explicitContentFilter = explicitContentFilter.getValue();
        return this;
    }

    /**
     * Adds the role spec to the list of roles for the created {@link Guild}.
     *
     * @param roleSpec The role spec to add to the list of roles.
     * @return This spec.
     */
    public GuildCreateSpec addRole(Consumer<? super RoleCreateSpec> roleSpec) {
        final RoleCreateSpec mutatedSpec = new RoleCreateSpec();
        roleSpec.accept(mutatedSpec);
        roles.add(mutatedSpec.asRequest());
        return this;
    }

    /**
     * Sets the default @everyone role for the created {@link Guild}. This shifts all other roles in the list, if
     * present, down by one. It does not replace other @everyone roles already set.
     * <p>
     * When creating a guild, Discord automatically takes the first role in the role array as the default @everyone
     * role. See this limitation and others at {@link GuildCreateSpec}.
     *
     * @param roleSpec The default @everyone role spec to add to the list of roles.
     * @return This spec.
     */
    public GuildCreateSpec addEveryoneRole(Consumer<? super RoleCreateSpec> roleSpec) {
        final RoleCreateSpec mutatedSpec = new RoleCreateSpec();
        roleSpec.accept(mutatedSpec);
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
    public GuildCreateSpec addChannel(String name, Channel.Type type) {
        channels.add(PartialChannelCreateRequest.builder()
                .name(name)
                .type(type.getValue())
                .build());
        return this;
    }

    @Override
    public GuildCreateRequest asRequest() {
        return GuildCreateRequest.builder()
                .name(name)
                .region(region)
                .icon(icon)
                .verificationLevel(verificationLevel)
                .defaultMessageNotifications(defaultMessageNotificationLevel)
                .explicitContentFilter(explicitContentFilter)
                .roles(roles)
                .channels(channels)
                .build();
    }
}
