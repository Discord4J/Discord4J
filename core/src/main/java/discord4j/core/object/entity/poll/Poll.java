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
package discord4j.core.object.entity.poll;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Entity;
import discord4j.core.spec.PollVotersRequestFlux;
import discord4j.discordjson.json.PollData;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A discord poll.
 *
 * @see <a href="https://discord.com/developers/docs/resources/poll#poll-object">Poll Object</a>
 */
public class Poll implements Entity {

    private final GatewayDiscordClient gateway;
    private final PollData data;
    private final Snowflake channelId;
    private final Snowflake messageId;
    private final PollQuestion question;
    private final List<PollAnswer> answers;
    private final Map<Integer, PollAnswer> pollAnswerById;

    /**
     * Constructs a Poll object.
     *
     * @param gateway the gateway client
     * @param data the poll data
     * @param messageId the message id of the poll
     */
    public Poll(final GatewayDiscordClient gateway, final PollData data, final long channelId, final long messageId) {
        this.gateway = gateway;
        this.data = data;
        this.channelId = Snowflake.of(channelId);
        this.messageId = Snowflake.of(messageId);

        this.question = new PollQuestion(data.question());
        this.answers = data.answers().stream().map(PollAnswer::new).collect(Collectors.toList());
        this.pollAnswerById = this.answers.stream().collect(Collectors.toMap(PollAnswer::getAnswerId, answer -> answer));
    }

    /**
     * Gets the question of the poll.
     *
     * @return the question of the poll
     */
    public PollQuestion getQuestion() {
        return this.question;
    }

    /**
     * Gets the answers of the poll.
     *
     * @return the answers of the poll
     */
    public List<PollAnswer> getAnswers() {
        return this.answers;
    }

    /**
     * Gets when the poll expires.
     *
     * @return An {@link Optional} containing the expiry of the poll, or {@link Optional#empty()} if not present
     */
    public Optional<Instant> getExpiry() {
        return this.data.expiry().map(date -> DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(date, Instant::from));
    }

    /**
     * Gets if the poll allows multiple selections.
     *
     * @return if the poll allows multiple selections
     */
    public boolean allowMultiselect() {
        return this.data.allowMultiselect();
    }

    /**
     * Gets the layout type of the poll.
     *
     * @return the layout type of the poll
     */
    public PollLayoutType getLayoutType() {
        return PollLayoutType.from(this.data.layoutType());
    }

    /**
     * Get the results of the poll.
     * Note that the results returned by this method are not updated in real-time, and correspond to the state of the
     * poll at the time of creation! Use {@link #getLatestResults()} to get the latest results.
     *
     * @return An {@link Optional} containing the results of the poll, or {@link Optional#empty()} if not present
     */
    public Optional<PollResult> getResults() {
        return this.data.results().toOptional().map(PollResult::new);
    }

    /**
     * Get the latest results of the poll.
     *
     * @return A {@link Mono} that, when subscribed to, will return the latest results of the poll, or empty if not
     * present. If an error occurs, it will be emitted through the {@link Mono}.
     */
    public Mono<PollResult> getLatestResults() {
        return this.gateway.getRestClient()
            .getMessageById(this.channelId, this.messageId)
            .getData()
            .map(messageData -> messageData.poll().toOptional().flatMap(pollData -> pollData.results().toOptional()))
            .flatMap(Mono::justOrEmpty)
            .map(PollResult::new);
    }

    /**
     * Request to immediately ends the poll.
     * This method will only work if the poll is not already ended, and it was created by the current bot.
     *
     * @return A {@link Mono} that, when subscribed to, will immediately end the poll and return the latest data that
     * represents the poll. If an error occurs, it will be emitted through the {@link Mono}.
     */
    public Mono<Poll> end() {
        return this.gateway.getRestClient()
            .getPollService()
            .endPoll(this.channelId.asLong(), this.messageId.asLong())
            .map(messageData -> new Poll(this.gateway, messageData.poll().get(), this.channelId.asLong(), this.messageId.asLong()));
    }

    /**
     * Request to get the voters of a specific answer.
     * The voters are returned in pages of 25 users each by default. Use {@link PollVotersRequestFlux#withLimit(Integer)} to
     * change the number of users per page. Use {@link PollVotersRequestFlux#withAfter(Snowflake)} to get the users after a
     * specific user.
     *
     * @param answerId the answer id
     * @return A {@link PollVotersRequestFlux} that, when subscribed to, will act as a
     * {@link reactor.core.publisher.Flux} of {@link discord4j.core.object.entity.User}s that will emit the voters
     * of the specified answer. If an error occurs, it will be emitted through the {@link PollVotersRequestFlux}.
     */
    public PollVotersRequestFlux getVoters(int answerId) {
        return PollVotersRequestFlux.of(this.gateway, this.gateway.rest(), this.channelId, this.messageId, answerId);
    }

    /**
     * Gets the raw data of the poll.
     *
     * @return the raw data of the poll
     */
    public PollData getData() {
        return this.data;
    }

    /**
     * Gets the message id of the poll.
     *
     * @return the message id of the poll
     */
    @Override
    public Snowflake getId() {
        return this.messageId;
    }

    /**
     * Gets the gateway client.
     *
     * @return the gateway client
     */
    @Override
    public GatewayDiscordClient getClient() {
        return this.gateway;
    }

    /**
     * Gets an answer by its id.
     *
     * @return An {@link Optional} containing the answer, or {@link Optional#empty()} if no answer with the given id exists
     */
    public Optional<PollAnswer> getAnswerById(int answerId) {
        return Optional.ofNullable(this.pollAnswerById.get(answerId));
    }

    @Override
    public String toString() {
        return "Poll{" +
            "data=" + data +
            ", messageId=" + messageId +
            ", channelId=" + channelId +
            '}';
    }

    /**
     * The layout type of poll.
     */
    public enum PollLayoutType {

        /** Unknown layout type. */
        UNKNOWN(-1),

        /** The default layout type. */
        DEFAULT(1);

        /**
         * The internal layout value as represented by discord.
         */
        private final int value;

        /**
         * Constructs a PollLayoutType object.
         *
         * @param value the internal discord value of the layout type
         */
        PollLayoutType(final int value) {
            this.value = value;
        }

        /**
         * Gets the internal discord value of the layout type.
         *
         * @return The underlying value as represented by Discord.
         */
        public int getValue() {
            return this.value;
        }

        /**
         * Gets the layout type from the internal value. It is guaranteed that invoking {@link #getValue()} from the
         * returned enum will equal ({@link #equals(Object)}) the supplied {@code value}.
         *
         * @param value The underlying value as represented by Discord.
         * @return The layout type for the given value.
         */
        public static PollLayoutType from(int value) {
            switch (value) {
                case 1:
                    return PollLayoutType.DEFAULT;
                default:
                    return PollLayoutType.UNKNOWN;
            }
        }

    }

}
