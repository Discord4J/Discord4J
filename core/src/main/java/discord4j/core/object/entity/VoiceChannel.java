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
import discord4j.core.object.data.ExtendedInviteBean;
import discord4j.core.object.data.stored.VoiceChannelBean;
import discord4j.core.object.trait.Categorizable;
import discord4j.core.object.trait.Invitable;
import discord4j.core.object.util.Snowflake;
import discord4j.core.spec.InviteCreateSpec;
import discord4j.core.spec.VoiceChannelEditSpec;
import discord4j.core.util.EntityUtil;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.function.Consumer;

/** A Discord voice channel. */
public final class VoiceChannel extends BaseGuildChannel implements Categorizable, Invitable {

    /**
     * Constructs an {@code VoiceChannel} with an associated ServiceMediator and Discord data.
     *
     * @param serviceMediator The ServiceMediator associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    public VoiceChannel(final ServiceMediator serviceMediator, final VoiceChannelBean data) {
        super(serviceMediator, data);
    }

    @Override
    VoiceChannelBean getData() {
        return (VoiceChannelBean) super.getData();
    }

    @Override
    public final Optional<Snowflake> getCategoryId() {
        return Optional.ofNullable(getData().getParentId()).map(Snowflake::of);
    }

    @Override
    public final Mono<Category> getCategory() {
        return Mono.justOrEmpty(getCategoryId()).flatMap(getClient()::getCategoryById);
    }

    @Override
    public Mono<ExtendedInvite> createInvite(final InviteCreateSpec spec) {
        return getServiceMediator().getRestClient().getChannelService()
                .createChannelInvite(getId().asLong(), spec.asRequest())
                .map(ExtendedInviteBean::new)
                .map(bean -> new ExtendedInvite(getServiceMediator(), bean));
    }

    @Override
    public Flux<ExtendedInvite> getInvites() {
        return getServiceMediator().getRestClient().getChannelService()
                .getChannelInvites(getId().asLong())
                .map(ExtendedInviteBean::new)
                .map(bean -> new ExtendedInvite(getServiceMediator(), bean));
    }

    /**
     * Gets the bitrate (in bits) for this voice channel.
     *
     * @return Gets the bitrate (in bits) for this voice channel.
     */
    public int getBitrate() {
        return getData().getBitrate();
    }

    /**
     * Gets the user limit of this voice channel.
     *
     * @return The user limit of this voice channel.
     */
    public int getUserLimit() {
        return getData().getUserLimit();
    }

    /**
     * Requests to edit a voice channel.
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link VoiceChannelEditSpec} to be operated on. If some
     * properties need to be retrieved via blocking operations (such as retrieval from a database), then it is
     * recommended to build the spec externally and call {@link #edit(VoiceChannelEditSpec)}.
     *
     * @return A {@link Mono} where, upon successful completion, emits the edited {@link VoiceChannel}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public Mono<VoiceChannel> edit(final Consumer<VoiceChannelEditSpec> spec) {
        final VoiceChannelEditSpec mutatedSpec = new VoiceChannelEditSpec();
        spec.accept(mutatedSpec);
        return edit(mutatedSpec);
    }

    /**
     * Requests to edit a voice channel.
     *
     * @param spec A configured {@link VoiceChannelEditSpec} to perform the request on.
     * @return A {@link Mono} where, upon successful completion, emits the edited {@link VoiceChannel}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public Mono<VoiceChannel> edit(final VoiceChannelEditSpec spec) {
        return getServiceMediator().getRestClient().getChannelService()
                .modifyChannel(getId().asLong(), spec.asRequest())
                .map(EntityUtil::getChannelBean)
                .map(bean -> EntityUtil.getChannel(getServiceMediator(), bean))
                .cast(VoiceChannel.class);
    }
}
