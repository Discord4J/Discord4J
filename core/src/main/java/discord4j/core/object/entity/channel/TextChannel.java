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
package discord4j.core.object.entity.channel;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.spec.TextChannelEditMono;
import discord4j.core.spec.TextChannelEditSpec;
import discord4j.core.spec.legacy.LegacyTextChannelEditSpec;
import discord4j.core.util.EntityUtil;
import discord4j.discordjson.json.ChannelData;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * A Discord text channel.
 */
public final class TextChannel extends BaseGuildMessageChannel {

    /**
     * Constructs an {@code TextChannel} with an associated {@link GatewayDiscordClient} and Discord data.
     *
     * @param gateway The {@link GatewayDiscordClient} associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    public TextChannel(GatewayDiscordClient gateway, ChannelData data) {
        super(gateway, data);
    }

    /**
     * Gets the amount of seconds an user has to wait before sending another message (0-120).
     * <p>
     * Bots, as well as users with the permission {@code manage_messages} or {@code manage_channel}, are unaffected.
     *
     * @return The amount of seconds an user has to wait before sending another message (0-120).
     */
    public int getRateLimitPerUser() {
        return getData().rateLimitPerUser().toOptional()
                .orElseThrow(IllegalStateException::new); // this should be safe for all TextChannels
    }

    /**
     * Gets whether this channel is considered NSFW (Not Safe For Work).
     *
     * @return {@code true} if this channel is considered NSFW (Not Safe For Work), {@code false} otherwise.
     */
    public boolean isNsfw() {
        return getData().nsfw().toOptional().orElse(false);
    }

    /**
     * Requests to edit this text channel.
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link LegacyTextChannelEditSpec} to be operated on.
     * @return A {@link Mono} where, upon successful completion, emits the edited {@link TextChannel}. If an error is
     * received, it is emitted through the {@code Mono}.
     * @deprecated use {@link #edit(TextChannelEditSpec)} or {@link #edit()} which offer an immutable approach to build
     * specs
     */
    @Deprecated
    public Mono<TextChannel> edit(final Consumer<? super LegacyTextChannelEditSpec> spec) {
        return Mono.defer(
                () -> {
                    LegacyTextChannelEditSpec mutatedSpec = new LegacyTextChannelEditSpec();
                    spec.accept(mutatedSpec);
                    return getClient().getRestClient().getChannelService()
                            .modifyChannel(getId().asLong(), mutatedSpec.asRequest(), mutatedSpec.getReason());
                })
                .map(bean -> EntityUtil.getChannel(getClient(), bean))
                .cast(TextChannel.class);
    }

    /**
     * Requests to edit this text channel. Properties specifying how to edit this channel can be set via the {@code
     * withXxx} methods of the returned {@link TextChannelEditMono}.
     *
     * @return A {@link TextChannelEditMono} where, upon successful completion, emits the edited {@link TextChannel}. If
     * an error is received, it is emitted through the {@code TextChannelEditMono}.
     */
    public TextChannelEditMono edit() {
        return TextChannelEditMono.of(this);
    }

    /**
     * Requests to edit this text channel.
     *
     * @param spec an immutable object that specifies how to edit this text channel
     * @return A {@link Mono} where, upon successful completion, emits the edited {@link TextChannel}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public Mono<TextChannel> edit(TextChannelEditSpec spec) {
        Objects.requireNonNull(spec);
        return Mono.defer(
                () -> getClient().getRestClient().getChannelService()
                        .modifyChannel(getId().asLong(), spec.asRequest(), spec.reason()))
                .map(bean -> EntityUtil.getChannel(getClient(), bean))
                .cast(TextChannel.class);
    }

    @Override
    public String toString() {
        return "TextChannel{} " + super.toString();
    }
}
