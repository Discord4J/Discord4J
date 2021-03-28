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

import discord4j.discordjson.json.TemplateCreateRequest;
import discord4j.discordjson.possible.Possible;

import java.util.Optional;

/**
 * Spec to create a guild template.
 *
 * @see discord4j.core.object.entity.Guild#createTemplate(java.util.function.Consumer)
 */
public class GuildTemplateCreateSpec implements Spec<TemplateCreateRequest> {

    private String name = null;
    private Possible<Optional<String>> description = Possible.absent();

    /**
     * Sets the name of the template.
     *
     * @param name The name for the template.
     * @return This spec.
     */
    public GuildTemplateCreateSpec setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the description of the template.
     *
     * @param description The description for the template.
     * @return This spec.
     */
    public GuildTemplateCreateSpec setDescription(String description) {
        this.description = Possible.of(Optional.of(description));
        return this;
    }

    @Override
    public TemplateCreateRequest asRequest() {
        return TemplateCreateRequest.builder()
            .name(name)
            .description(description)
            .build();
    }
}
