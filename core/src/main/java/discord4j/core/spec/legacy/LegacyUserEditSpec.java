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

import discord4j.discordjson.json.UserModifyRequest;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.Image;
import reactor.util.annotation.Nullable;

/**
 * LegacySpec used to modify the current user.
 *
 * @see <a href="https://discord.com/developers/docs/resources/user#modify-current-user">Modify Current User</a>
 */
public class LegacyUserEditSpec implements LegacySpec<UserModifyRequest> {

    private Possible<String> username = Possible.absent();
    private Possible<String> avatar = Possible.absent();

    /**
     * Sets the user's username. May cause the discriminator to be randomized.
     *
     * @param username The user's username.
     * @return This spec.
     */
    public LegacyUserEditSpec setUsername(String username) {
        this.username = Possible.of(username);
        return this;
    }

    /**
     * Sets the user's avatar.
     *
     * @param avatar The user's avatar.
     * @return This spec.
     */
    public LegacyUserEditSpec setAvatar(@Nullable Image avatar) {
        this.avatar = avatar == null ? Possible.absent() : Possible.of(avatar.getDataUri());
        return this;
    }

    @Override
    public UserModifyRequest asRequest() {
        return UserModifyRequest.builder()
                .username(username)
                .avatar(avatar)
                .build();
    }
}
