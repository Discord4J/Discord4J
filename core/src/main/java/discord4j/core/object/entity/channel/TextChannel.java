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
import discord4j.core.object.ThreadListPart;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.StartThreadFromMessageMono;
import discord4j.core.spec.StartThreadWithoutMessageMono;
import discord4j.core.spec.TextChannelEditMono;
import discord4j.core.spec.TextChannelEditSpec;
import discord4j.core.spec.legacy.LegacyTextChannelEditSpec;
import discord4j.core.util.EntityUtil;
import discord4j.discordjson.json.ChannelData;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * A Discord text channel.
 */
public final class TextChannel extends BaseTopLevelGuildChannel implements TopLevelGuildMessageChannel {

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

    /**
     * Request to retrieve all threads in this channel.
     *
     * @return A {@link Flux} that continually emits the {@link ThreadChannel threads} of the channel. If an error is
     * received, it is emitted through the {@code Flux}.
     */
    public Flux<ThreadChannel> getAllThreads() {
        return getClient().getGuildChannels(getGuildId())
            .ofType(ThreadChannel.class)
            .filter(thread -> thread.getParentId().map(id -> id.equals(getId())).orElse(false));
    }

    /**
     * Requests to retrieve the public archived threads for this channel.
     * <p>
     * The audit log parts can be {@link ThreadListPart#combine(ThreadListPart) combined} for easier querying. For example,
     * <pre>
     * {@code
     * channel.getPublicArchivedThreads()
     *     .take(10)
     *     .reduce(ThreadListPart::combine)
     * }
     * </pre>
     *
     * @return A {@link Flux} that continually parts of this channel's thread list. If an error is received, it is emitted
     * through the {@code Flux}.
     */
    public Flux<ThreadListPart> getPublicArchivedThreads() {
        return getRestChannel().getPublicArchivedThreads()
            .map(data -> new ThreadListPart(getClient(), data));
    }

    /**
     * Requests to retrieve the private archived threads for this channel.
     * <p>
     * The thread list parts can be {@link ThreadListPart#combine(ThreadListPart) combined} for easier querying. For example,
     * <pre>
     * {@code
     * channel.getPrivateArchivedThreads()
     *     .take(10)
     *     .reduce(ThreadListPart::combine)
     * }
     * </pre>
     *
     * @return A {@link Flux} that continually parts of this channel's thread list. If an error is received, it is emitted
     * through the {@code Flux}.
     */
    public Flux<ThreadListPart> getPrivateArchivedThreads() {
        return getRestChannel().getPrivateArchivedThreads()
            .map(data -> new ThreadListPart(getClient(), data));
    }

    /**
     * Requests to retrieve the joined private archived threads for this channel.
     * <p>
     * The thread list parts can be {@link ThreadListPart#combine(ThreadListPart) combined} for easier querying. For example,
     * <pre>
     * {@code
     * channel.getJoinedPrivateArchivedThreads()
     *     .take(10)
     *     .reduce(ThreadListPart::combine)
     * }
     * </pre>
     *
     * @return A {@link Flux} that continually parts of this channel's thread list. If an error is received, it is emitted
     * through the {@code Flux}.
     */
    public Flux<ThreadListPart> getJoinedPrivateArchivedThreads() {
        return getRestChannel().getJoinedPrivateArchivedThreads()
            .map(data -> new ThreadListPart(getClient(), data));
    }

    /**
     * Start a new public thread that is not connected to an existing message. Properties specifying how to create the thread
     * can be set via the {@code withXxx} methods of the returned {@link StartThreadWithoutMessageMono}.
     *
     * @param name the name of the thread
     * @return A {@link StartThreadWithoutMessageMono} where, upon successful completion, emits the created {@link ThreadChannel}.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public StartThreadWithoutMessageMono startPublicThreadWithoutMessage(String name) {
        return StartThreadWithoutMessageMono.of(name, ThreadChannel.Type.GUILD_PUBLIC_THREAD, this);
    }

    /**
     * Start a new public thread that is not connected to an existing message. Properties specifying how to create the thread
     * can be set via the {@code withXxx} methods of the returned {@link StartThreadWithoutMessageMono}.
     *
     * @param name the name of the thread
     * @param message the message to start the thread with
     * @return A {@link StartThreadWithoutMessageMono} where, upon successful completion, emits the created {@link ThreadChannel}.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public StartThreadFromMessageMono startPublicThreadWithMessage(String name, Message message) {
        return StartThreadFromMessageMono.of(name, message);
    }

    /**
     * Start a new private thread. Properties specifying how to create the thread can be set via the {@code withXxx}
     * methods of the returned {@link StartThreadWithoutMessageMono}.
     *
     * @param name the name of the thread
     * @return A {@link StartThreadWithoutMessageMono} where, upon successful completion, emits the created {@link ThreadChannel}.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public StartThreadWithoutMessageMono startPrivateThread(String name) {
        return StartThreadWithoutMessageMono.of(name, ThreadChannel.Type.GUILD_PRIVATE_THREAD, this);
    }

}
