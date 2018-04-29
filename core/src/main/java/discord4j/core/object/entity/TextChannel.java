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

import discord4j.core.ServiceMediator;
import discord4j.core.object.ExtendedInvite;
import discord4j.core.object.PermissionOverwrite;
import discord4j.core.object.bean.ExtendedInviteBean;
import discord4j.core.object.entity.bean.MessageBean;
import discord4j.core.object.entity.bean.TextChannelBean;
import discord4j.core.object.util.Snowflake;
import discord4j.core.spec.InviteCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.core.spec.TextChannelEditSpec;
import discord4j.core.util.EntityUtil;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

/** A Discord text channel. */
public final class TextChannel extends BaseChannel implements GuildChannel, MessageChannel {

    /** Delegates {@link GuildChannel} operations. */
    private final BaseGuildChannel guildChannel;

    /** Delegates {@link MessageChannel} operations. */
    private final BaseMessageChannel messageChannel;

    /**
     * Constructs an {@code TextChannel} with an associated ServiceMediator and Discord data.
     *
     * @param serviceMediator The ServiceMediator associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    public TextChannel(final ServiceMediator serviceMediator, final TextChannelBean data) {
        super(serviceMediator, data);
        guildChannel = new BaseGuildChannel(serviceMediator, data.getGuildChannel());
        messageChannel = new BaseMessageChannel(serviceMediator, data.getMessageChannel());
    }

    @Override
    public Snowflake getGuildId() {
        return guildChannel.getGuildId();
    }

    @Override
    public Mono<Guild> getGuild() {
        return guildChannel.getGuild();
    }

    @Override
    public Set<PermissionOverwrite> getPermissionOverwrites() {
        return guildChannel.getPermissionOverwrites();
    }

    @Override
    public String getName() {
        return guildChannel.getName();
    }

    @Override
    public Optional<Snowflake> getCategoryId() {
        return guildChannel.getCategoryId();
    }

    @Override
    public Mono<Category> getCategory() {
        return guildChannel.getCategory();
    }

    @Override
    public int getRawPosition() {
        return guildChannel.getRawPosition();
    }

    @Override
    public Mono<Integer> getPosition() {
        return guildChannel.getPosition();
    }

    @Override
    public Optional<Snowflake> getLastMessageId() {
        return messageChannel.getLastMessageId();
    }

    @Override
    public Mono<Message> getLastMessage() {
        return messageChannel.getLastMessage();
    }

    @Override
    public Optional<Instant> getLastPinTimestamp() {
        return messageChannel.getLastPinTimestamp();
    }

    @Override
    public Mono<Message> createMessage(final Consumer<MessageCreateSpec> spec) {
        return messageChannel.createMessage(spec);
    }

    @Override
    public Mono<Message> createMessage(final MessageCreateSpec spec) {
        return messageChannel.createMessage(spec);
    }

    /**
     * Gets the channel topic.
     *
     * @return The channel topic.
     */
    public String getTopic() {
        return Optional.ofNullable(getData().getTopic()).orElse("");
    }

    @Override
    protected TextChannelBean getData() {
        return (TextChannelBean) super.getData();
    }

    /**
     * Gets whether this channel is considered NSFW (Not Safe For Work).
     *
     * @return {@code true} if this channel is considered NSFW (Not Safe For Work), {@code false} otherwise.
     */
    public boolean isNsfw() {
        return getData().isNsfw();
    }

    /**
     * Gets the <i>raw</i> mention. This is the format utilized to directly mention another text channel (assuming the
     * text channel exists in context of the mention).
     *
     * @return The <i>raw</i> mention.
     */
    public String getMention() {
        return "<#" + getId().asString() + ">";
    }

    /**
     * Requests to edit this text channel.
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link TextChannelEditSpec} to be operated on. If some
     * properties need to be retrieved via blocking operations (such as retrieval from a database), then it is
     * recommended to build the spec externally and call {@link #edit(TextChannelEditSpec)}.
     *
     * @return A {@link Mono} where, upon successful completion, emits the edited {@link TextChannel}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public Mono<TextChannel> edit(final Consumer<TextChannelEditSpec> spec) {
        final TextChannelEditSpec mutatedSpec = new TextChannelEditSpec();
        spec.accept(mutatedSpec);
        return edit(mutatedSpec);
    }

    /**
     * Requests to edit this text channel.
     *
     * @param spec A configured {@link TextChannelEditSpec} to perform the request on.
     * @return A {@link Mono} where, upon successful completion, emits the edited {@link TextChannel}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public Mono<TextChannel> edit(final TextChannelEditSpec spec) {
        return getServiceMediator().getRestClient().getChannelService()
                .modifyChannel(getId().asLong(), spec.asRequest())
                .map(EntityUtil::getChannelBean)
                .map(bean -> EntityUtil.getChannel(getServiceMediator(), bean))
                .cast(TextChannel.class);
    }

    /**
     * Requests to create an invite.
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link InviteCreateSpec} to be operated on. If some
     * properties need to be retrieved via blocking operations (such as retrieval from a database), then it is
     * recommended to build the spec externally and call {@link #createInvite(InviteCreateSpec)}.
     *
     * @return A {@link Mono} where, upon successful completion, emits the created {@link ExtendedInvite}. If an error
     * is received, it is emitted through the {@code Mono}.
     */
    public Mono<ExtendedInvite> createInvite(final Consumer<InviteCreateSpec> spec) {
        final InviteCreateSpec mutatedSpec = new InviteCreateSpec();
        spec.accept(mutatedSpec);
        return createInvite(mutatedSpec);
    }

    /**
     * Requests to create an invite.
     *
     * @param spec A configured {@link InviteCreateSpec} to perform the request on.
     * @return A {@link Mono} where, upon successful completion, emits the created {@link ExtendedInvite}. If an error
     * is received, it is emitted through the {@code Mono}.
     */
    public Mono<ExtendedInvite> createInvite(final InviteCreateSpec spec) {
        return getServiceMediator().getRestClient().getChannelService()
                .createChannelInvite(getId().asLong(), spec.asRequest())
                .map(ExtendedInviteBean::new)
                .map(bean -> new ExtendedInvite(getServiceMediator(), bean));
    }
}
