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

import discord4j.common.json.request.InviteCreateRequest;

public class InviteCreateSpec implements Spec<InviteCreateRequest>  {

    private int maxAge;
    private int maxUses;
    private boolean temporary;
    private boolean unique;

    public InviteCreateSpec setMaxAge(int maxAge) {
        this.maxAge = maxAge;
        return this;
    }

    public InviteCreateSpec setMaxUses(int maxUses) {
        this.maxUses = maxUses;
        return this;
    }

    public InviteCreateSpec setTemporary(boolean temporary) {
        this.temporary = temporary;
        return this;
    }

    public InviteCreateSpec setUnique(boolean unique) {
        this.unique = unique;
        return this;
    }

    @Override
    public InviteCreateRequest asRequest() {
        return new InviteCreateRequest(maxAge, maxUses, temporary, unique);
    }
}
