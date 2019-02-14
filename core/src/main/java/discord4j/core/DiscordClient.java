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
package discord4j.core;

import discord4j.core.object.Invite;
import discord4j.core.object.Region;
import discord4j.core.object.entity.*;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.util.Snowflake;
import discord4j.core.spec.GuildCreateSpec;
import discord4j.core.spec.UserEditSpec;
import discord4j.core.util.PaginationUtil;
import discord4j.rest.entity.*;
import discord4j.rest.entity.data.*;
import discord4j.rest.json.response.UserGuildResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A high-level abstraction of common Discord4J operations such as entity retrieval through Discord API or the creation
 * of real-time bot clients through Discord Gateway.
 */
public final class DiscordClient {

    private static final Logger log = Loggers.getLogger(DiscordClient.class);

    private final CoreResources coreResources;

    /**
     * Constructs a {@code DiscordClient} with an associated {@link CoreResources}.
     *
     * @param coreResources The {@link CoreResources} associated to this object.
     */
    DiscordClient(CoreResources coreResources) {
        this.coreResources = coreResources;
    }

    public static DiscordClient create(String token) {
        return new DiscordClientBuilder(token).build();
    }

    public static DiscordClientBuilder builder(String token) {
        return new DiscordClientBuilder(token);
    }

    /**
     * Obtain the {@link CoreResources} associated with this {@link DiscordClient}.
     *
     * @return the current {@link CoreResources} for this client
     */
    public CoreResources getCoreResources() {
        return coreResources;
    }

    /**
     * Requests to retrieve the channel represented by the supplied ID.
     *
     * @param channelId The ID of the channel.
     * @return A {@link Mono} where, upon successful completion, emits the {@link Channel} as represented by the
     * supplied ID. If an error is received, it is emitted through the {@code Mono}.
     */
    public RestChannel getChannelById(final Snowflake channelId) {
        return new RestChannel(coreResources.getRestClient(), channelId.asLong());
    }

    /**
     * Requests to retrieve the guild represented by the supplied ID.
     *
     * @param guildId The ID of the guild.
     * @return A {@link Mono} where, upon successful completion, emits the {@link Guild} as represented by the supplied
     * ID. If an error is received, it is emitted through the {@code Mono}.
     */
    public RestGuild getGuildById(final Snowflake guildId) {
        return new RestGuild(coreResources.getRestClient(), guildId.asLong());
    }

    /**
     * Requests to retrieve the guild emoji represented by the supplied IDs.
     *
     * @param guildId The ID of the guild.
     * @param emojiId The ID of the emoji.
     * @return A {@link Mono} where, upon successful completion, emits the {@link GuildEmoji} as represented by the
     * supplied IDs. If an error is received, it is emitted through the {@code Mono}.
     */
    public RestGuildEmoji getGuildEmojiById(final Snowflake guildId, final Snowflake emojiId) {
        return new RestGuildEmoji(coreResources.getRestClient(), guildId.asLong(), emojiId.asLong());
    }

    /**
     * Requests to retrieve the member represented by the supplied IDs.
     *
     * @param guildId The ID of the guild.
     * @param userId The ID of the user.
     * @return A {@link Mono} where, upon successful completion, emits the {@link Member} as represented by the supplied
     * IDs. If an error is received, it is emitted through the {@code Mono}.
     */
    public RestMember getMemberById(final Snowflake guildId, final Snowflake userId) {
        return new RestMember(coreResources.getRestClient(), guildId.asLong(), userId.asLong());
    }

    /**
     * Requests to retrieve the message represented by the supplied IDs.
     *
     * @param channelId The ID of the channel.
     * @param messageId The ID of the message.
     * @return A {@link Mono} where, upon successful completion, emits the {@link Message} as represented by the
     * supplied IDs. If an error is received, it is emitted through the {@code Mono}.
     */
    public RestMessage getMessageById(final Snowflake channelId, final Snowflake messageId) {
        return new RestMessage(coreResources.getRestClient(), channelId.asLong(), messageId.asLong());
    }

    /**
     * Requests to retrieve the role represented by the supplied IDs.
     *
     * @param guildId The ID of the guild.
     * @param roleId The ID of the role.
     * @return A {@link Mono} where, upon successful completion, emits the {@link Role} as represented by the supplied
     * IDs. If an error is received, it is emitted through the {@code Mono}.
     */
    public RestRole getRoleById(final Snowflake guildId, final Snowflake roleId) {
        return new RestRole(coreResources.getRestClient(), guildId.asLong(), roleId.asLong());
    }

    /**
     * Requests to retrieve the user represented by the supplied ID.
     *
     * @param userId The ID of the user.
     * @return A {@link Mono} where, upon successful completion, emits the {@link User} as represented by the supplied
     * ID. If an error is received, it is emitted through the {@code Mono}.
     */
    public RestUser getUserById(final Snowflake userId) {
        return new RestUser(coreResources.getRestClient(), userId.asLong());
    }

    /**
     * Requests to retrieve the webhook represented by the supplied ID.
     *
     * @param webhookId The ID of the webhook.
     * @return A {@link Mono} where, upon successful completion, emits the {@link Webhook} as represented by the
     * supplied ID. If an error is received, it is emitted through the {@code Mono}.
     */
    public RestWebhook getWebhookById(final Snowflake webhookId) {
        return new RestWebhook(coreResources.getRestClient(), webhookId.asLong());
    }

    /**
     * Requests to retrieve the application info.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link ApplicationInfo application info}. If
     * an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<ApplicationInfoData> getApplicationInfo() {
        return coreResources.getRestClient().getApplicationService()
                .getCurrentApplicationInfo()
                .map(ApplicationInfoData::new);
    }

    /**
     * Requests to retrieve the guilds the current client is in.
     *
     * @return A {@link Flux} that continually emits the {@link Guild guilds} that the current client is in. If an error
     * is received, it is emitted through the {@code Flux}.
     */
    public Flux<UserGuildData> getGuilds() {
        final Function<Map<String, Object>, Flux<UserGuildResponse>> makeRequest = params ->
                coreResources.getRestClient().getUserService()
                        .getCurrentUserGuilds(params);

        return PaginationUtil.paginateAfter(makeRequest, UserGuildResponse::getId, 0L, 100)
                .map(UserGuildData::new);
    }

    /**
     * Requests to retrieve the voice regions that are available.
     *
     * @return A {@link Flux} that continually emits the {@link Region regions} that are available. If an error is
     * received, it is emitted through the {@code Flux}.
     */
    public Flux<RegionData> getRegions() {
        return coreResources.getRestClient().getVoiceService().getVoiceRegions()
                .map(RegionData::new);
    }

    /**
     * Requests to retrieve the bot user.
     *
     * @return A {@link Mono} where, upon successful completion, emits the bot {@link User user}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public Mono<UserData> getSelf() {
        return coreResources.getRestClient().getUserService()
                .getCurrentUser()
                .map(UserData::new);
    }

    /**
     * Login the client to the gateway.
     *
     * @return A {@link Mono} that completes (either successfully or with an error) when the client disconnects from the
     * gateway without a reconnect attempt. It is recommended to call this from {@code main} and as a final statement
     * invoke {@link Mono#block()}.
     */
    public Mono<Void> login() {
        return gateway().login(gateway -> Mono.empty());
    }

    public GatewayBootstrap gateway() {
        return new GatewayBootstrap(this, GatewayResources.builder().build());
    }

    public GatewayBootstrap gateway(GatewayResources resources) {
        return new GatewayBootstrap(this, resources);
    }

    /**
     * Requests to create a guild.
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link GuildCreateSpec} to be operated on.
     * @return A {@link Mono} where, upon successful completion, emits the created {@link Guild}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public Mono<GuildData> createGuild(final Consumer<? super GuildCreateSpec> spec) {
        final GuildCreateSpec mutatedSpec = new GuildCreateSpec();
        spec.accept(mutatedSpec);

        return coreResources.getRestClient().getGuildService()
                .createGuild(mutatedSpec.asRequest())
                .map(GuildData::new);
    }

    /**
     * Requests to retrieve an invite.
     *
     * @param inviteCode The code for the invite (e.g. "xdYkpp").
     * @return A {@link Mono} where, upon successful completion, emits the {@link Invite} as represented by the
     * supplied invite code. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<InviteData> getInvite(final String inviteCode) {
        return coreResources.getRestClient().getInviteService()
                .getInvite(inviteCode)
                .map(InviteData::new);
    }

    /**
     * Requests to edit this client (i.e., modify the current bot user).
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link UserEditSpec} to be operated on.
     * @return A {@link Mono} where, upon successful completion, emits the edited {@link User}. If an error is received,
     * it is emitted through the {@code Mono}.
     */
    public Mono<UserData> edit(final Consumer<? super UserEditSpec> spec) {
        final UserEditSpec mutatedSpec = new UserEditSpec();
        spec.accept(mutatedSpec);

        return coreResources.getRestClient().getUserService()
                .modifyCurrentUser(mutatedSpec.asRequest())
                .map(UserData::new);
    }
}
