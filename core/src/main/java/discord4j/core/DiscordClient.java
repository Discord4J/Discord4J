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

import discord4j.common.json.payload.StatusUpdate;
import discord4j.core.event.EventDispatcher;
import discord4j.core.object.Snowflake;
import discord4j.core.object.entity.*;
import discord4j.core.object.entity.bean.*;
import discord4j.core.util.EntityUtil;
import discord4j.rest.util.RouteUtils;
import discord4j.store.util.LongLongTuple2;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/** A high-level abstraction of common Discord operations such as entity retrieval and Discord shard manipulation. */
public final class DiscordClient {

    /** The ServiceMediator associated to this object. */
    private final ServiceMediator serviceMediator;

    /**
     * Constructs a {@code DiscordClient} with an associated ServiceMediator.
     *
     * @param serviceMediator The ServiceMediator associated to this object.
     */
    DiscordClient(final ServiceMediator serviceMediator) {
        this.serviceMediator = serviceMediator;
    }

    /**
     * Requests to retrieve the category represented by the supplied ID.
     *
     * @param categoryId The ID of the category.
     * @return A {@link Mono} where, upon successful completion, emits the {@link Category} as represented by the
     * supplied ID. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Category> getCategoryById(final Snowflake categoryId) {
        return getChannelById(categoryId).ofType(Category.class);
    }

    /**
     * Requests to retrieve the guild channel represented by the supplied ID.
     *
     * @param guildChannelId The ID of the guild channel.
     * @return A {@link Mono} where, upon successful completion, emits the {@link GuildChannel} as represented by the
     * supplied ID. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<GuildChannel> getGuildChannelById(final Snowflake guildChannelId) {
        return getChannelById(guildChannelId).ofType(GuildChannel.class);
    }

    /**
     * Requests to retrieve the message channel represented by the supplied ID.
     *
     * @param messageChannelId The ID of the message channel.
     * @return A {@link Mono} where, upon successful completion, emits the {@link MessageChannel} as represented by the
     * supplied ID. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<MessageChannel> getMessageChannelById(final Snowflake messageChannelId) {
        return getChannelById(messageChannelId).ofType(MessageChannel.class);
    }

    /**
     * Requests to retrieve the text channel represented by the supplied ID.
     *
     * @param textChannelId The ID of the text channel.
     * @return A {@link Mono} where, upon successful completion, emits the {@link TextChannel} as represented by the
     * supplied ID. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<TextChannel> getTextChannelById(final Snowflake textChannelId) {
        return getChannelById(textChannelId).ofType(TextChannel.class);
    }

    /**
     * Requests to retrieve the voice channel represented by the supplied ID.
     *
     * @param voiceChannelId The ID of the voice channel.
     * @return A {@link Mono} where, upon successful completion, emits the {@link VoiceChannel} as represented by the
     * supplied ID. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<VoiceChannel> getVoiceChannelById(final Snowflake voiceChannelId) {
        return getChannelById(voiceChannelId).ofType(VoiceChannel.class);
    }

    /**
     * Requests to retrieve the channel represented by the supplied ID.
     *
     * @param channelId The ID of the channel.
     * @return A {@link Mono} where, upon successful completion, emits the {@link Channel} as represented by the
     * supplied ID. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Channel> getChannelById(final Snowflake channelId) {
        final Mono<CategoryBean> category = serviceMediator.getStoreHolder().getCategoryStore().find(channelId.asLong());
        final Mono<TextChannelBean> textChannel = serviceMediator.getStoreHolder().getTextChannelStore().find(channelId.asLong());
        final Mono<VoiceChannelBean> voiceChannel = serviceMediator.getStoreHolder().getVoiceChannelStore().find(channelId.asLong());

        final Mono<ChannelBean> rest = serviceMediator.getRestClient().getChannelService()
                .getChannel(channelId.asLong())
                .map(EntityUtil::getChannelBean);

        return category.cast(ChannelBean.class)
                .switchIfEmpty(textChannel)
                .switchIfEmpty(voiceChannel)
                .switchIfEmpty(rest)
                .map(channelBean -> EntityUtil.getChannel(serviceMediator, channelBean));
    }

    /**
     * Requests to retrieve the guild represented by the supplied ID.
     *
     * @param guildId The ID of the guild.
     * @return A {@link Mono} where, upon successful completion, emits the {@link Guild} as represented by the supplied
     * ID. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Guild> getGuildById(final Snowflake guildId) {
        return serviceMediator.getStoreHolder().getGuildStore()
                .find(guildId.asLong())
                .cast(BaseGuildBean.class)
                .switchIfEmpty(serviceMediator.getRestClient().getGuildService()
                        .getGuild(guildId.asLong())
                        .map(BaseGuildBean::new))
                .map(baseGuildBean -> new Guild(serviceMediator, baseGuildBean));
    }

    /**
     * Requests to retrieve the guild emoji represented by the supplied IDs.
     *
     * @param guildId The ID of the guild.
     * @param emojiId The ID of the emoji.
     * @return A {@link Mono} where, upon successful completion, emits the {@link GuildEmoji} as represented by the
     * supplied IDs. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<GuildEmoji> getGuildEmojiById(final Snowflake guildId, final Snowflake emojiId) {
        return serviceMediator.getStoreHolder().getGuildEmojiStore()
                .find(emojiId.asLong())
                .switchIfEmpty(serviceMediator.getRestClient().getEmojiService()
                        .getGuildEmoji(guildId.asLong(), emojiId.asLong())
                        .map(GuildEmojiBean::new))
                .map(guildEmojiBean -> new GuildEmoji(serviceMediator, guildEmojiBean, guildId.asLong()));
    }

    /**
     * Requests to retrieve the member represented by the supplied IDs.
     *
     * @param guildId The ID of the guild.
     * @param userId The ID of the user.
     * @return A {@link Mono} where, upon successful completion, emits the {@link Member} as represented by the supplied
     * IDs. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Member> getMemberById(final Snowflake guildId, final Snowflake userId) {
        final Mono<MemberBean> member = serviceMediator.getStoreHolder().getMemberStore()
                .find(LongLongTuple2.of(guildId.asLong(), userId.asLong()))
                .switchIfEmpty(serviceMediator.getRestClient().getGuildService()
                        .getGuildMember(guildId.asLong(), userId.asLong())
                        .map(MemberBean::new));

        return member.flatMap(memberBean -> getUserBean(userId).map(userBean ->
                new Member(serviceMediator, memberBean, userBean, guildId.asLong())));
    }

    /**
     * Requests to retrieve the message represented by the supplied IDs.
     *
     * @param channelId The ID of the channel.
     * @param messageId The ID of the message.
     * @return A {@link Mono} where, upon successful completion, emits the {@link Message} as represented by the
     * supplied IDs. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Message> getMessageById(final Snowflake channelId, final Snowflake messageId) {
        return serviceMediator.getStoreHolder().getMessageStore()
                .find(messageId.asLong())
                .switchIfEmpty(serviceMediator.getRestClient().getChannelService()
                        .getMessage(channelId.asLong(), messageId.asLong())
                        .map(MessageBean::new))
                .map(messageBean -> new Message(serviceMediator, messageBean));
    }

    /**
     * Requests to retrieve the role represented by the supplied IDs.
     *
     * @param guildId The ID of the guild.
     * @param roleId The ID of the role.
     * @return A {@link Mono} where, upon successful completion, emits the {@link Role} as represented by the supplied
     * IDs. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Role> getRoleById(final Snowflake guildId, final Snowflake roleId) {
        return serviceMediator.getStoreHolder().getRoleStore()
                .find(roleId.asLong())
                .switchIfEmpty(serviceMediator.getRestClient().getGuildService()
                        .getGuildRoles(guildId.asLong())
                        .filter(response -> response.getId() == roleId.asLong())
                        .map(RoleBean::new)
                        .singleOrEmpty())
                .map(roleBean -> new Role(serviceMediator, roleBean, guildId.asLong()));
    }

    /**
     * Requests to retrieve the user represented by the supplied ID.
     *
     * @param userId The ID of the user.
     * @return A {@link Mono} where, upon successful completion, emits the {@link User} as represented by the supplied
     * ID. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<User> getUserById(final Snowflake userId) {
        return getUserBean(userId).map(userBean -> new User(serviceMediator, userBean));
    }

    /**
     * Requests to retrieve the webhook represented by the supplied ID.
     *
     * @param webhookId The ID of the webhook.
     * @return A {@link Mono} where, upon successful completion, emits the {@link Webhook} as represented by the
     * supplied ID. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Webhook> getWebhookById(final Snowflake webhookId) {
        return serviceMediator.getRestClient().getWebhookService()
                .getWebhook(webhookId.asLong())
                .map(WebhookBean::new)
                .map(webhookBean -> new Webhook(serviceMediator, webhookBean));
    }

    /**
     * Requests to retrieve the application user.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link ApplicationUser application user}. If
     * an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<ApplicationUser> getApplicationUser() {
        return serviceMediator.getRestClient().getApplicationService()
                .getCurrentApplicationInfo()
                .map(ApplicationBean::new)
                .flatMap(applicationBean -> getUserBean(Snowflake.of(applicationBean.getId()))
                        .map(userBean -> new ApplicationUser(serviceMediator, userBean, applicationBean)));
    }

    /**
     * Logs in the client to the gateway.
     *
     * @return A {@link Mono} that completes (either successfully or with an error) when the client disconnects from the
     * gateway without a reconnect attempt. It is recommended to call this from {@code main} and as a final statement
     * invoke {@link Mono#block()}.
     */
    public Mono<Void> login() {
        // TODO Add configuration -- refactor GatewayClient#execute parameters into a config object
        final int[] shards = {0, 1};
        final StatusUpdate statusUpdate = null;

        final Map<String, Object> parameters = new HashMap<>(3);
        parameters.put("compress", "zlib-stream");
        parameters.put("encoding", "json");
        parameters.put("v", 6);

        return serviceMediator.getRestClient().getGatewayService().getGateway()
                .flatMap(response -> serviceMediator.getGatewayClient()
                        .execute(RouteUtils.expandQuery(response.getUrl(), parameters), shards, statusUpdate));
    }

    /** Logs out the client from the gateway. */
    public void logout() {
        serviceMediator.getGatewayClient().close(false);
    }

    /**
     * Gets the event dispatcher, allowing reactive subscription of client events.
     *
     * @return an EventDispatcher associated with this client.
     */
    public EventDispatcher getEventDispatcher() {
        return serviceMediator.getEventDispatcher();
    }

    /**
     * Gets the configuration for this client.
     *
     * @return The configuration for this client.
     */
    public ClientConfig getConfig() {
        return serviceMediator.getClientConfig();
    }

    /**
     * Requests to retrieve the user bean represented by the supplied ID.
     *
     * @param userId The ID of the user.
     * @return A {@link Mono} where, upon successful completion, emits the {@link UserBean} as represented by the
     * supplied ID. If an error is received, it is emitted through the {@code Mono}.
     */
    private Mono<UserBean> getUserBean(final Snowflake userId) {
        return serviceMediator.getStoreHolder().getUserStore()
                .find(userId.asLong())
                .switchIfEmpty(serviceMediator.getRestClient().getUserService()
                        .getUser(userId.asLong())
                        .map(UserBean::new));
    }
}
