package discord4j.core.object.entity.channel;

import discord4j.core.object.ThreadListPart;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.StartThreadFromMessageMono;
import discord4j.core.spec.StartThreadFromMessageSpec;
import discord4j.core.spec.StartThreadWithoutMessageMono;
import discord4j.core.spec.StartThreadWithoutMessageSpec;
import discord4j.discordjson.json.ListThreadsData;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * A Discord message channel in a guild that isn't a thread and can have threads.
 */
public interface TopLevelGuildMessageWithThreadsChannel extends TopLevelGuildMessageChannel {

    /**
     * Request to retrieve all threads in this channel.
     *
     * @return A {@link Flux} that continually emits the {@link ThreadChannel threads} of the channel. If an error is
     * received, it is emitted through the {@code Flux}.
     */
    default Flux<ThreadChannel> getAllThreads() {
        return Flux.merge(getActiveThreads(), getPublicArchivedThreads());
    }

    /**
     * Request to retrieve all active threads in this forum channel.
     *
     * @return A {@link Flux} that continually emits the {@link ThreadChannel threads} of the channel. If an error is
     * received, it is emitted through the {@code Flux}.
     */
    default Flux<ThreadChannel> getActiveThreads() {
        return getClient().getGuildById(getGuildId())
            .flatMapMany(Guild::getActiveThreads)
            .map(ThreadListPart::getThreads)
            .flatMap(Flux::fromIterable)
            .filter(thread -> thread.getParentId().map(id -> id.equals(getId())).orElse(false));
    }

    /**
     * Requests to retrieve the public archived threads for this channel.
     *
     * @return A {@link Flux} that continually emits the public archived {@link ThreadChannel threads} of the channel.
     * If an error is received, it is emitted through the {@code Flux}.
     */
    default Flux<ThreadChannel> getPublicArchivedThreads() {
        return getRestChannel()
            .getPublicArchivedThreads()
            .map(ListThreadsData::threads)
            .flatMap(Flux::fromIterable)
            .map(channelData -> new ThreadChannel(getClient(), channelData));
    }

    /**
     * Start a new public thread that is not connected to an existing message. Properties specifying how to create the thread
     * can be set via the {@link StartThreadWithoutMessageSpec} specifier.
     *
     * @param spec the properties to create the thread with
     * @return A {@link Mono} where, upon successful completion, emits the created {@link ThreadChannel}.
     * If an error is received, it is emitted through the {@link Mono}.
     */
    default Mono<ThreadChannel> startPublicThreadWithoutMessage(StartThreadWithoutMessageSpec spec) {
        return getRestChannel().startThreadWithoutMessage(spec.asRequest())
            .map(data -> new ThreadChannel(getClient(), data));
    }

    /**
     * Start a new public thread that is connected to an existing message. Properties specifying how to create the thread
     * can be set via the {@link StartThreadFromMessageSpec} specifier.
     *
     * @param message the message to start the thread with
     * @param spec the properties to create the thread with
     * @return A {@link Mono} where, upon successful completion, emits the created {@link ThreadChannel}.
     * If an error is received, it is emitted through the {@link Mono}.
     */
    default Mono<ThreadChannel> startPublicThreadWithMessage(Message message, StartThreadFromMessageSpec spec) {
        return getRestChannel().startThreadFromMessage(message.getId().asLong(), spec.asRequest())
            .map(data -> new ThreadChannel(getClient(), data));
    }

    /**
     * Start a new public thread that is not connected to an existing message. Properties specifying how to create the thread
     * can be set via the {@code withXxx} methods of the returned {@link StartThreadWithoutMessageMono}.
     *
     * @param threadName the name of the thread
     * @return A {@link StartThreadWithoutMessageMono} where, upon successful completion, emits the created {@link ThreadChannel}.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    StartThreadWithoutMessageMono startPublicThreadWithoutMessage(String threadName);

    /**
     * Start a new public thread that is not connected to an existing message. Properties specifying how to create the thread
     * can be set via the {@code withXxx} methods of the returned {@link StartThreadWithoutMessageMono}.
     *
     * @param threadName the name of the thread
     * @param message the message to start the thread with
     * @return A {@link StartThreadWithoutMessageMono} where, upon successful completion, emits the created {@link ThreadChannel}.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    default StartThreadFromMessageMono startPublicThreadWithMessage(String threadName, Message message) {
        return StartThreadFromMessageMono.of(threadName, message);
    }

}
