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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J. If not, see <http://www.gnu.org/licenses/>.
 */
package discord4j.core.object;

import discord4j.core.GatewayDiscordClient;
import discord4j.discordjson.json.InviteTargetUsersJobStatusData;
import discord4j.discordjson.possible.Possible;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class InviteTargetUsersJobStatus implements DiscordObject {

    /** The gateway associated to this object. */
    private final GatewayDiscordClient gateway;

    /** The raw data as represented by Discord. */
    private final InviteTargetUsersJobStatusData data;

    public InviteTargetUsersJobStatus(final GatewayDiscordClient gateway, final InviteTargetUsersJobStatusData data) {
        this.gateway = gateway;
        this.data = data;
    }

    @Override
    public GatewayDiscordClient getClient() {
        return this.gateway;
    }

    public InviteTargetUsersJobStatusData getData() {
        return this.data;
    }

    /**
     * Get the current status of the invite target users job.
     *
     * @return the current status of the job, as represented by {@link Status}
     */
    public Status getStatus() {
        return Status.of(this.getData().status());
    }

    /**
     * Get the creation timestamp of the invite target users job.
     *
     * @return the timestamp when the job was created, represented as an {@link Instant}.
     */
    public Instant getCreatedAt() {
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(this.getData().createdAt(), Instant::from);
    }

    /**
     * Get the timestamp when the invite target users job was completed.
     *
     * @return an {@link Optional} containing the completion timestamp as an {@link Instant},
     *         or an empty {@link Optional} if the job has not been completed or the completion timestamp is unavailable.
     */
    public Optional<Instant> getCompletedAt() {
        return this.getData().completedAt().map(completedAtStr -> DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(completedAtStr, Instant::from));
    }

    /**
     * Get the total number of users associated with the invite target users job.
     *
     * @return the total number of users as an integer
     */
    public int getTotalUsers() {
        return this.getData().totalUsers();
    }

    /**
     * Get the number of users that have been processed in the invite target users job.
     *
     * @return the number of processed users as an integer
     */
    public int getProcessedUsers() {
        return this.getData().processedUsers();
    }

    /**
     * Get the error message associated with the invite target users job.
     *
     * @return the error message as an {@link Optional}, or an empty {@link Optional} if no error occurred
     */
    public Optional<String> getErrorMessage() {
        return Possible.flatOpt(this.getData().errorMessage());
    }

    @Override
    public String toString() {
        return "InviteTargetUsersJobStatus{" +
            "data=" + data +
            '}';
    }

    public enum Status {
        /**
         * The default value.
         */
        UNSPECIFIED(0),
        /**
         * The job is still being processed.
         */
        PROCESSING(1),
        /**
         * The job has been completed successfully.
         */
        COMPLETED(2),
        /**
         * The job has failed, see {@link InviteTargetUsersJobStatus#getErrorMessage()} for more details.
         */
        FAILED(3);


        /** The underlying value as represented by Discord. */
        private final int value;

        /**
         * Constructs a {@code InviteTargetUsersJobStatus.Status}.
         *
         * @param value The underlying value as represented by Discord.
         */
        Status(int value) {
            this.value = value;
        }

        /**
         * Gets the underlying value as represented by Discord.
         *
         * @return The underlying value as represented by Discord.
         */
        public int getValue() {
            return value;
        }

        /**
         * Gets the type of target user. It is guaranteed that invoking {@link #getValue()} from the returned enum
         * will be equal ({@code ==}) to the supplied {@code value}.
         *
         * @param value The underlying value as represented by Discord.
         * @return The type of target user.
         */
        public static InviteTargetUsersJobStatus.Status of(final int value) {
            switch (value) {
                case 1: return PROCESSING;
                case 2: return COMPLETED;
                case 3: return FAILED;
                default: return UNSPECIFIED;
            }
        }
    }
}
