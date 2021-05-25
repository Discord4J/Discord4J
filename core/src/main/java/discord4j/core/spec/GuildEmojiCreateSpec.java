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
import discord4j.discordjson.json.GuildEmojiCreateRequest;
import discord4j.rest.util.Image;
import discord4j.common.util.Snowflake;
import reactor.util.annotation.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Spec used to create {@link GuildEmoji} objects. Emojis and animated emojis have a maximum file size of 256kb.
 *
 * @see <a href="https://discord.com/developers/docs/resources/emoji#create-guild-emoji">Create Guild Emoji</a>
 */
public class GuildEmojiCreateSpec implements Spec<GuildEmojiCreateRequest> {

    private String name;
    private String image;
    private final Set<Snowflake> roles = new HashSet<>();
    @Nullable
    private String reason;

    /**
     * Sets the name for the created {@link GuildEmoji}.
     *
     * @param name The name for the emoji.
     * @return This spec.
     */
    public GuildEmojiCreateSpec setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the image for the created {@link GuildEmoji}.
     *
     * @param image The image used for the emoji.
     * @return This spec.
     */
    public GuildEmojiCreateSpec setImage(Image image) {
        this.image = image.getDataUri();
        return this;
    }

    /**
     * Adds a role for which the created {@link GuildEmoji} will be whitelisted.
     *
     * @param roleId The role identifier.
     * @return This spec.
     */
    public GuildEmojiCreateSpec addRole(Snowflake roleId) {
        roles.add(roleId);
        return this;
    }

    public GuildEmojiCreateSpec setReason(@Nullable final String reason) {
        this.reason = reason;
        return this;
    }

    @Nullable
    public String getReason() {
        return reason;
    }

    @Override
    public GuildEmojiCreateRequest asRequest() {
        return GuildEmojiCreateRequest.builder()
                .name(name)
                .image(image)
                .roles(roles.stream().map(Snowflake::asString).collect(Collectors.toList()))
                .build();
    }
}
