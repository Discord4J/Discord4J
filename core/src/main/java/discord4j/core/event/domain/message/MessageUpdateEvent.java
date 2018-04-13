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
package discord4j.core.event.domain.message;

import discord4j.core.DiscordClient;
import discord4j.core.event.Update;
import discord4j.core.object.entity.User;
import discord4j.core.object.util.Snowflake;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Set;

public class MessageUpdateEvent extends MessageEvent {

    private final Update<String> content;
    // TODO private final Update<Embed> embed;
    private final Update<Boolean> pinned;
    private final Update<Boolean> mentionsEveryone;
    private final Update<Set<User>> userMentions;
    private final Update<Set<Snowflake>> roleMentions;

    public MessageUpdateEvent(DiscordClient client, @Nullable Update<String> content, @Nullable Update<Boolean> pinned,
                              @Nullable Update<Boolean> mentionsEveryone, @Nullable Update<Set<User>> userMentions,
                              @Nullable Update<Set<Snowflake>> roleMentions) {
        super(client);
        this.content = content;
        this.pinned = pinned;
        this.mentionsEveryone = mentionsEveryone;
        this.userMentions = userMentions;
        this.roleMentions = roleMentions;
    }

    public Optional<Update<String>> getContent() {
        return Optional.ofNullable(content);
    }

    public Optional<Update<Boolean>> isPinned() {
        return Optional.ofNullable(pinned);
    }

    public Optional<Update<Boolean>> mentionsEveryone() {
        return Optional.ofNullable(mentionsEveryone);
    }

    public Optional<Update<Set<User>>> getUserMentions() {
        return Optional.ofNullable(userMentions);
    }

    public Optional<Update<Set<Snowflake>>> getRoleMentions() {
        return Optional.ofNullable(roleMentions);
    }
}
