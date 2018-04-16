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

import discord4j.common.jackson.Possible;
import discord4j.common.json.request.GuildEmojiModifyRequest;
import discord4j.core.object.util.Snowflake;

import java.util.Set;

public class GuildEmojiEditSpec implements Spec<GuildEmojiModifyRequest> {

    private Possible<String> name = Possible.absent();
    private Possible<long[]> roles = Possible.absent();

    public GuildEmojiEditSpec setName(String name) {
        this.name = Possible.of(name);
        return this;
    }

    public GuildEmojiEditSpec setRoles(Set<Snowflake> roles) {
        this.roles = Possible.of(roles.stream().mapToLong(Snowflake::asLong).toArray());
        return this;
    }

    @Override
    public GuildEmojiModifyRequest asRequest() {
        return new GuildEmojiModifyRequest(name, roles);
    }
}
