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
import reactor.util.annotation.Nullable;

import java.util.Optional;

public class GuildTemplateCreateSpec implements AuditSpec<TemplateCreateRequest> {

    private String name = null;
    private Possible<Optional<String>> description = Possible.absent();
    @Nullable
    private String reason;

    /**
     * Sets the name for the modified {@link discord4j.core.object.GuildTemplate}.
     *
     * @param name The name for the template.
     * @return This spec.
     */
    public GuildTemplateCreateSpec setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the description for the modified {@link discord4j.core.object.GuildTemplate}.
     *
     * @param description The description for the template.
     * @return This spec.
     */
    public GuildTemplateCreateSpec setDescription(String description) {
        this.description = Possible.of(Optional.of(description));
        return this;
    }

    @Override
    public GuildTemplateCreateSpec setReason(@Nullable final String reason) {
        this.reason = reason;
        return this;
    }

    @Override
    @Nullable
    public String getReason() {
        return reason;
    }

    @Override
    public TemplateCreateRequest asRequest() {
        return TemplateCreateRequest.builder()
            .name(name)
            .description(description)
            .build();
    }
}
