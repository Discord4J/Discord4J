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
package discord4j.rest.json.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import discord4j.common.jackson.Possible;

public class InviteCreateRequest {

    @JsonProperty("max_age")
    private final int maxAge;
    @JsonProperty("max_uses")
    private final int maxUses;
    private final boolean temporary;
    private final boolean unique;
    @JsonProperty("target_user")
    private final Possible<String> targetUser;
    @JsonProperty("target_user_type")
    private final Possible<Integer> targetUserType;

    public InviteCreateRequest(int maxAge, int maxUses, boolean temporary, boolean unique, Possible<String> targetUser,
                               Possible<Integer> targetUserType) {
        this.maxAge = maxAge;
        this.maxUses = maxUses;
        this.temporary = temporary;
        this.unique = unique;
        this.targetUser = targetUser;
        this.targetUserType = targetUserType;
    }

    @Override
    public String toString() {
        return "InviteCreateRequest{" +
                "maxAge=" + maxAge +
                ", maxUses=" + maxUses +
                ", temporary=" + temporary +
                ", unique=" + unique +
                ", targetUser=" + targetUser +
                ", targetUserType=" + targetUserType +
                '}';
    }
}
