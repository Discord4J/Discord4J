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

import discord4j.core.object.Snowflake;
import discord4j.core.object.entity.*;
import discord4j.core.object.entity.bean.*;
import discord4j.store.util.LongLongTuple2;
import reactor.core.publisher.Mono;

public final class DiscordClient {

    private final ServiceMediator serviceMediator;
    private final StoreHolder storeHolder;

    DiscordClient(final ServiceMediator serviceMediator) {
        this.serviceMediator = serviceMediator;
        storeHolder = serviceMediator.getStoreHolder();
    }

    public Mono<Category> getCategoryById(final Snowflake categoryId) {
        return getChannelById(categoryId).ofType(Category.class);
    }

    public Mono<Guild> getGuildById(final Snowflake guildId) {
        return storeHolder.getGuildStore()
                .find(guildId.asLong())
                .cast(BaseGuildBean.class)
                .switchIfEmpty(serviceMediator.getRestClient().getGuildService()
                        .getGuild(guildId.asLong())
                        .map(BaseGuildBean::new))
                .map(baseGuildBean -> new Guild(serviceMediator, baseGuildBean));
    }

    public Mono<GuildChannel> getGuildChannelById(final Snowflake guildChannelId) {
        return getChannelById(guildChannelId).ofType(GuildChannel.class);
    }

    public Mono<GuildEmoji> getGuildEmojiById(final Snowflake guildId, final Snowflake emojiId) {
        return storeHolder.getGuildEmojiStore()
                .find(emojiId.asLong())
                .switchIfEmpty(serviceMediator.getRestClient().getEmojiService()
                        .getGuildEmoji(guildId.asLong(), emojiId.asLong())
                        .map(GuildEmojiBean::new))
                .map(guildEmojiBean -> new GuildEmoji(serviceMediator, guildEmojiBean, guildId.asLong()));
    }

    public Mono<Member> getMemberById(final Snowflake guildId, final Snowflake userId) {
        final Mono<MemberBean> member = storeHolder.getMemberStore()
                .find(LongLongTuple2.of(guildId.asLong(), userId.asLong()))
                .switchIfEmpty(serviceMediator.getRestClient().getGuildService()
                        .getGuildMember(guildId.asLong(), userId.asLong())
                        .map(MemberBean::new));

        final Mono<UserBean> user = storeHolder.getUserStore()
                .find(userId.asLong())
                .switchIfEmpty(serviceMediator.getRestClient().getUserService()
                        .getUser(userId.asLong())
                        .map(UserBean::new));

        return member.flatMap(memberBean -> user.map(userBean ->
                new Member(serviceMediator, memberBean, userBean, guildId.asLong())));
    }

    public Mono<Message> getMessageById(final Snowflake channelId, final Snowflake messageId) {
        return storeHolder.getMessageStore()
                .find(messageId.asLong())
                .switchIfEmpty(serviceMediator.getRestClient().getChannelService()
                        .getMessage(channelId.asLong(), messageId.asLong())
                        .map(MessageBean::new))
                .map(messageBean -> new Message(serviceMediator, messageBean));
    }

    public Mono<MessageChannel> getMessageChannelById(final Snowflake messageChannelId) {
        return getChannelById(messageChannelId).ofType(MessageChannel.class);
    }

    public Mono<Role> getRoleById(final Snowflake guildId, final Snowflake roleId) {
        return storeHolder.getRoleStore()
                .find(roleId.asLong())
                .switchIfEmpty(serviceMediator.getRestClient().getGuildService()
                        .getGuildRoles(guildId.asLong())
                        .filter(response -> response.getId() == roleId.asLong())
                        .map(RoleBean::new)
                        .singleOrEmpty())
                .map(roleBean -> new Role(serviceMediator, roleBean, guildId.asLong()));
    }

    public Mono<TextChannel> getTextChannelById(final Snowflake textChannelId) {
        return getChannelById(textChannelId).ofType(TextChannel.class);
    }

    public Mono<User> getUserById(final Snowflake userId) {
        return storeHolder.getUserStore()
                .find(userId.asLong())
                .switchIfEmpty(serviceMediator.getRestClient().getUserService()
                        .getUser(userId.asLong())
                        .map(UserBean::new))
                .map(userBean -> new User(serviceMediator, userBean));
    }

    public Mono<VoiceChannel> getVoiceChannelById(final Snowflake voiceChannelId) {
        return getChannelById(voiceChannelId).ofType(VoiceChannel.class);
    }

    public Mono<Webhook> getWebhookById(final Snowflake webhookId) {
        return serviceMediator.getRestClient().getWebhookService()
                .getWebhook(webhookId.asLong())
                .map(WebhookBean::new)
                .map(bean -> new Webhook(serviceMediator, bean));
    }

    private Mono<Channel> getChannelById(final Snowflake channelId) {
        final Mono<Category> category = storeHolder.getCategoryStore()
                .find(channelId.asLong())
                .map(categoryBean -> new Category(serviceMediator, categoryBean));

        final Mono<TextChannel> textChannel = storeHolder.getTextChannelStore()
                .find(channelId.asLong())
                .map(textChannelBean -> new TextChannel(serviceMediator, textChannelBean));

        final Mono<VoiceChannel> voiceChannel = storeHolder.getVoiceChannelStore()
                .find(channelId.asLong())
                .map(voiceChannelBean -> new VoiceChannel(serviceMediator, voiceChannelBean));

        final Mono<Channel> rest = serviceMediator.getRestClient().getChannelService()
                .getChannel(channelId.asLong())
                .map(channelResponse -> {
                    switch (Channel.Type.of(channelResponse.getType())) {
                        case GUILD_TEXT: return new TextChannel(serviceMediator, new TextChannelBean(channelResponse));
                        case DM: return new PrivateChannel(serviceMediator, new PrivateChannelBean(channelResponse));
                        case GUILD_VOICE: return new VoiceChannel(serviceMediator, new VoiceChannelBean(channelResponse));
                        case GUILD_CATEGORY: return new Category(serviceMediator, new CategoryBean(channelResponse));
                        default: throw new UnsupportedOperationException("Cannot initialize channel for " + channelId);
                    }
                });

        return category.cast(Channel.class)
                .switchIfEmpty(textChannel)
                .switchIfEmpty(voiceChannel)
                .switchIfEmpty(rest);
    }
}
