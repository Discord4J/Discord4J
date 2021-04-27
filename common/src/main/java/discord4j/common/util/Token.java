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

package discord4j.common.util;

import discord4j.discordjson.json.AccessTokenData;
import discord4j.discordjson.possible.Possible;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * A token associated with a particular user that is used to authenticate requests to the Discord API and access
 * resources on the user's behalf.
 *
 * @see <a href="https://discord.com/developers/docs/reference#authentication">Authentication</a>
 */
public class Token {

    protected final AccessTokenData data;

    /**
     * Constructs a {@code Token} that is used to make requests to the Discord API on behalf of the bot account
     * associated with the given bot token.
     *
     * @param token A bot token obtained by registering a bot in the Discord Developer Portal.
     * @return A new {@code Token} associated with the bot account whose token has been provided.
     */
    public static Token of(final String token) {
        return new Token(AccessTokenData.builder()
                .accessToken(token)
                .tokenType("Bot")
                .expiresIn(Long.MAX_VALUE)
                .guild(Possible.absent())
                .refreshToken(Possible.absent())
                .scope("bot")
                .webhook(Possible.absent())
                .build());
    }

    protected Token(final AccessTokenData data) {
        this.data = Objects.requireNonNull(data);
    }

    /**
     * Gets this {@code Token} as a string as it appears when presented to the Discord API via the HTTP authorization
     * header.
     *
     * @return This {@code Token} as a string.
     */
    public String asString() {
        return data.accessToken();
    }

    /**
     * Gets whether this {@code Token} has expired.
     *
     * @return Whether this {@code Token} has expired.
     */
    public boolean hasExpired() {
        return false;
    }

    /**
     * Gets a new {@code Token} that is exchanged using this {@code Token}.
     *
     * @return A {@link Mono} that emits the refreshed {@code Token} upon successful completion. If an error is received,
     * it is emitted through the {@code Mono}.
     */
    public Mono<? extends Token> refresh() {
        return Mono.empty();
    }

    /**
     * Gets a string representation of this {@code Token}.
     *
     * @return A string representation of this {@code Token}.
     * @see #asString()
     */
    @Override
    public String toString() {
        return "Token{" +
                "data=" + data +
                '}';
    }
}
