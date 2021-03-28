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

import discord4j.discordjson.json.TemplateCreateGuildRequest;
import discord4j.discordjson.possible.Possible;

public class TemplateCreateGuildSpec implements Spec<TemplateCreateGuildRequest> {

    private String name = null;
    private Possible<String> icon = Possible.absent();

    /**
     * Sets the name for the modified {@link discord4j.core.object.GuildTemplate}.
     *
     * @param name The name for the template.
     * @return This spec.
     */
    public TemplateCreateGuildSpec setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the icon for the modified {@link discord4j.core.object.GuildTemplate}.
     *
     * @param icon The icon for the template.
     * @return This spec.
     */
    public TemplateCreateGuildSpec setIcon(String icon) {
        this.icon = Possible.of(icon);
        return this;
    }

    @Override
    public TemplateCreateGuildRequest asRequest() {
        return TemplateCreateGuildRequest.builder()
            .name(name)
            .icon(icon)
            .build();
    }
}
