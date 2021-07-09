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

import discord4j.discordjson.json.TemplateModifyRequest;
import discord4j.discordjson.possible.Possible;

import java.util.Optional;

/**
 * LegacySpec to edit a guild template.
 *
 * @see discord4j.core.object.GuildTemplate#edit(java.util.function.Consumer)
 */
public class LegacyGuildTemplateEditSpec implements LegacySpec<TemplateModifyRequest> {

    private Possible<String> name = Possible.absent();
    private Possible<Optional<String>> description = Possible.absent();

    /**
     * Sets the name of the template.
     *
     * @param name The name for the template.
     * @return This spec.
     */
    public LegacyGuildTemplateEditSpec setName(String name) {
        this.name = Possible.of(name);
        return this;
    }

    /**
     * Sets the description of the template.
     *
     * @param description The description for the template.
     * @return This spec.
     */
    public LegacyGuildTemplateEditSpec setDescription(String description) {
        this.description = Possible.of(Optional.of(description));
        return this;
    }

    @Override
    public TemplateModifyRequest asRequest() {
        return TemplateModifyRequest.builder()
            .name(name)
            .description(description)
            .build();
    }
}
