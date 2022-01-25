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

package discord4j.core.spec;

import discord4j.discordjson.json.GuildStickerModifyRequest;
import discord4j.discordjson.possible.Possible;
import org.immutables.value.Value;

@Value.Immutable(singleton = true)
interface GuildStickerEditSpecGenerator extends AuditSpec<GuildStickerModifyRequest> {

    String name();

    Possible<String> description();

    String tags();

    @Override
    default GuildStickerModifyRequest asRequest() {
        return GuildStickerModifyRequest.builder()
                .name(name())
                .description(description())
                .tags(tags())
                .build();
    }

}

