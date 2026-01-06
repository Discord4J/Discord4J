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
package discord4j.core.spec.legacy;

import discord4j.discordjson.json.TemplateCreateGuildRequest;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.Image;
import org.jspecify.annotations.Nullable;

/**
 * LegacySpec used to create a guild from a template.
 *
 * @see discord4j.core.object.GuildTemplate#createGuild(java.util.function.Consumer)
 */
public class LegacyGuildCreateFromTemplateSpec implements LegacySpec<TemplateCreateGuildRequest> {

    private @Nullable String name = null;
    private Possible<String> icon = Possible.absent();

    /**
     * Sets the name for the created guild.
     *
     * @param name The name of the guild.
     * @return This spec.
     */
    public LegacyGuildCreateFromTemplateSpec setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the icon for the created guild.
     *
     * @param icon The icon of the guild.
     * @return This spec.
     */
    public LegacyGuildCreateFromTemplateSpec setIcon(Image icon) {
        this.icon = Possible.of(icon.getDataUri());
        return this;
    }

    @Override
    public TemplateCreateGuildRequest asRequest() {
        if (this.name == null) {
            throw new IllegalStateException("Name must be set.");
        }
        return TemplateCreateGuildRequest.builder()
            .name(name)
            .icon(icon)
            .build();
    }
}
