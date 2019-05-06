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
import discord4j.core.object.VoiceState;
import discord4j.core.object.data.ExtendedInviteBean;
import discord4j.core.object.data.stored.ChannelBean;
import discord4j.core.object.data.stored.VoiceStateBean;
import discord4j.core.object.trait.Categorizable;
import discord4j.core.object.trait.Invitable;
import discord4j.core.object.util.Snowflake;
import discord4j.core.spec.InviteCreateSpec;
import discord4j.core.spec.VoiceChannelEditSpec;
import discord4j.core.spec.VoiceChannelJoinSpec;
import discord4j.core.util.EntityUtil;
import discord4j.store.api.util.LongLongTuple2;
import discord4j.voice.VoiceConnection;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;
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
    public VoiceChannel(final ServiceMediator serviceMediator, final ChannelBean data) {
        super(serviceMediator, data);
    }

    @Override
    public Optional<Snowflake> getCategoryId() {
        return Optional.ofNullable(getData().getParentId()).map(Snowflake::of);
    }

    @Override
    public Mono<Category> getCategory() {
        return Mono.justOrEmpty(getCategoryId()).flatMap(getClient()::getChannelById).cast(Category.class);
    }

    @Override
    public Mono<ExtendedInvite> createInvite(final Consumer<? super InviteCreateSpec> spec) {
        final InviteCreateSpec mutatedSpec = new InviteCreateSpec();
        spec.accept(mutatedSpec);

        return getServiceMediator().getRestClient().getChannelService()
                .createChannelInvite(getId().asLong(), mutatedSpec.asRequest(), mutatedSpec.getReason())
                .map(ExtendedInviteBean::new)
                .map(bean -> new ExtendedInvite(getServiceMediator(), bean))
                .subscriberContext(ctx -> ctx.put("shard", getServiceMediator().getClientConfig().getShardIndex()));
    }

    @Override
    public Flux<ExtendedInvite> getInvites() {
        return getServiceMediator().getRestClient().getChannelService()
                .getChannelInvites(getId().asLong())
                .map(ExtendedInviteBean::new)
                .map(bean -> new ExtendedInvite(getServiceMediator(), bean))
                .subscriberContext(ctx -> ctx.put("shard", getServiceMediator().getClientConfig().getShardIndex()));
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
     * @param spec A {@link Consumer} that provides a "blank" {@link VoiceChannelEditSpec} to be operated on.
     * @return A {@link Mono} where, upon successful completion, emits the edited {@link VoiceChannel}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public Mono<VoiceChannel> edit(final Consumer<? super VoiceChannelEditSpec> spec) {
        final VoiceChannelEditSpec mutatedSpec = new VoiceChannelEditSpec();
        spec.accept(mutatedSpec);

        return getServiceMediator().getRestClient().getChannelService()
                .modifyChannel(getId().asLong(), mutatedSpec.asRequest(), mutatedSpec.getReason())
                .map(EntityUtil::getChannelBean)
                .map(bean -> EntityUtil.getChannel(getServiceMediator(), bean))
                .cast(VoiceChannel.class)
                .subscriberContext(ctx -> ctx.put("shard", getServiceMediator().getClientConfig().getShardIndex()));
    }

    /**
     * Requests to retrieve the voice states of this voice channel.
     *
     * @return A {@link Flux} that continually emits the {@link VoiceState voice states} of this voice channel. If an
     * error is received, it is emitted through the {@code Flux}.
     *
     * @implNote If the underlying {@link discord4j.core.DiscordClientBuilder#getStoreService() store} does not save
     * {@link VoiceStateBean} instances <b>OR</b> the bot is currently not logged in then the returned {@code Flux} will
     * always be empty.
     */
    public Flux<VoiceState> getVoiceStates() {
        return getServiceMediator().getStateHolder().getVoiceStateStore()
                .findInRange(LongLongTuple2.of(getGuildId().asLong(), Long.MIN_VALUE),
                             LongLongTuple2.of(getGuildId().asLong(), Long.MAX_VALUE))
                .filter(bean -> Objects.equals(bean.getChannelId(), getId().asLong()))
                .map(bean -> new VoiceState(getServiceMediator(), bean));
    }

    /**
     * Requests to the join this voice channel.
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link VoiceChannelJoinSpec} to be operated on.
     * @return A {@link Mono} where, upon successful completion, emits a {@link VoiceConnection}, indicating a
     * connection to the channel has been established. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<VoiceConnection> join(final Consumer<? super VoiceChannelJoinSpec> spec) {
        final VoiceChannelJoinSpec mutatedSpec = new VoiceChannelJoinSpec(getServiceMediator(), this);
        spec.accept(mutatedSpec);

        return mutatedSpec.asRequest();
    }

    @Override
    public String toString() {
        return "VoiceChannel{} " + super.toString();
    }
}
