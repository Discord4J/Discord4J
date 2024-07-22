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

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.spec.ApplicationEmojiEditMono;
import discord4j.core.spec.ApplicationEmojiEditSpec;
import discord4j.core.util.EntityUtil;
import discord4j.discordjson.json.EmojiData;
import discord4j.discordjson.json.UserData;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.Objects;

/**
 * A Discord application emoji.
 * <br>
 * <a href="https://discord.com/developers/docs/resources/emoji#emoji-resource">Emoji Resource</a>
 */
public final class ApplicationEmoji extends Emoji {

    /** The ID of the application this emoji is associated to. */
    private final long applicationId;

    /**
     * Constructs a {@code ApplicationEmoji} with an associated {@link GatewayDiscordClient} and Discord data.
     *
     * @param gateway The {@link GatewayDiscordClient} associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     * @param applicationId The ID of the application this emoji is associated to.
     */
    public ApplicationEmoji(final GatewayDiscordClient gateway, final EmojiData data, final long applicationId) {
        super(gateway, data);
        this.applicationId = applicationId;
    }

    /**
     * Gets the ID of the application this emoji is associated to.
     *
     * @return The ID of the application this emoji is associated to.
     */
    public Snowflake getApplicationId() {
        return Snowflake.of(applicationId);
    }

    /**
     * Requests to retrieve the application this emoji is associated to.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link ApplicationInfo application} this emoji is associated
     * to. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<ApplicationInfo> getApplication() {
        return this.getClient().getApplicationInfo();
    }

    /**
     * Requests to edit this application emoji. Properties specifying how to edit this emoji can be set via the {@code
     * withXxx} methods of the returned {@link ApplicationEmojiEditMono}.
     *
     * @return A {@link ApplicationEmojiEditMono} where, upon successful completion, emits the edited {@link ApplicationEmoji}. If
     * an error is received, it is emitted through the {@code ApplicationEmojiEditMono}.
     */
    public ApplicationEmojiEditMono edit() {
        return ApplicationEmojiEditMono.of(this);
    }

    /**
     * Requests to edit this application emoji.
     *
     * @param spec an immutable object that specifies how to edit this emoji
     * @return A {@link Mono} where, upon successful completion, emits the edited {@link ApplicationEmoji}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public Mono<ApplicationEmoji> edit(ApplicationEmojiEditSpec spec) {
        Objects.requireNonNull(spec);
        return Mono.defer(
                () -> this.getClient().getRestClient().getEmojiService()
                        .modifyApplicationEmoji(this.applicationId, getId().asLong(), spec.asRequest()))
                .map(data -> new ApplicationEmoji(this.getClient(), data, getApplicationId().asLong()));
    }

    /**
     * Requests to delete this emoji.
     *
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the emoji has been deleted.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> delete() {
        return this.getClient().getRestClient().getEmojiService()
            .deleteApplicationEmoji(this.applicationId, getId().asLong());
    }

    /**
     * Requests to retrieve the user that created this emoji. This method will always hit the REST API.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link User user} that created this emoji. If
     * an error is received, it is emitted through the {@code Mono}.
     */
    @Override
    public Mono<User> getUser() {
        UserData user = data.user().toOptional()
            .orElseThrow(IllegalStateException::new); // this should be safe for application emojis

        return gateway.getRestClient().getEmojiService()
            .getApplicationEmoji(this.applicationId, getId().asLong())
            .map(data -> new User(gateway, user));
    }

    @Override
    public boolean equals(@Nullable final Object obj) {
        return EntityUtil.equals(this, obj);
    }

    @Override
    public int hashCode() {
        return EntityUtil.hashCode(this);
    }

    @Override
    public String toString() {
        return "ApplicationEmoji{" +
                "data=" + data +
                ", applicationId=" + applicationId +
                '}';
    }
}
