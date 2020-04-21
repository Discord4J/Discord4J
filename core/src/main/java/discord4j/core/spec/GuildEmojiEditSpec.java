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
package discord4j.core.spec;

import discord4j.core.object.entity.GuildEmoji;
import discord4j.discordjson.json.GuildEmojiModifyRequest;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.Snowflake;
import reactor.util.annotation.Nullable;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Spec used to modify an existing {@link GuildEmoji}.
 *
 * @see <a href="https://discordapp.com/developers/docs/resources/emoji#modify-guild-emoji">Modify Guild Emoji</a>
 */
public class GuildEmojiEditSpec implements AuditSpec<GuildEmojiModifyRequest> {

    private Possible<String> name = Possible.absent();
    private Possible<List<String>> roles = Possible.absent();
    private String reason;

    /**
     * Sets the name for the modified {@link GuildEmoji}.
     *
     * @param name The name for the emoji.
     * @return This spec.
     */
    public GuildEmojiEditSpec setName(String name) {
        this.name = Possible.of(name);
        return this;
    }

    /**
     * Sets the list of roles for which the modified {@link GuildEmoji} will be whitelisted.
     *
     * @param roles The set of role identifiers.
     * @return This spec.
     */
    public GuildEmojiEditSpec setRoles(Set<Snowflake> roles) {
        this.roles = Possible.of(roles.stream().map(Snowflake::asString).collect(Collectors.toList()));
        return this;
    }

    @Override
    public GuildEmojiEditSpec setReason(@Nullable final String reason) {
        this.reason = reason;
        return this;
    }

    @Override
    @Nullable
    public String getReason() {
        return reason;
    }

    @Override
    public GuildEmojiModifyRequest asRequest() {
        return GuildEmojiModifyRequest.builder()
                .name(name)
                .roles(roles)
                .build();
    }
}
