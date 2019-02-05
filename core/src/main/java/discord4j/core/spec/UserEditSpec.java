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
import discord4j.rest.json.request.UserModifyRequest;

public class UserEditSpec implements Spec<UserModifyRequest> {

    private Possible<String> username = Possible.absent();
    private Possible<String> avatar = Possible.absent();

    public UserEditSpec setUsername(String username) {
        this.username = Possible.of(username);
        return this;
    }

    public UserEditSpec setAvatar(String avatar) {
        this.avatar = Possible.of(avatar);
        return this;
    }

    @Override
    public UserModifyRequest asRequest() {
        return new UserModifyRequest(username, avatar);
    }
}
