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

import discord4j.common.jackson.Possible;
import discord4j.common.jackson.PossibleJson;

@PossibleJson
public class RoleModifyRequest {

    private final Possible<String> name;
    private final Possible<Long> permissions;
    private final Possible<Integer> color;
    private final Possible<Boolean> hoist;
    private final Possible<Boolean> mentionable;

    public RoleModifyRequest(Possible<String> name, Possible<Long> permissions,
                             Possible<Integer> color, Possible<Boolean> hoist,
                             Possible<Boolean> mentionable) {
        this.name = name;
        this.permissions = permissions;
        this.color = color;
        this.hoist = hoist;
        this.mentionable = mentionable;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Possible<String> name = Possible.absent();
        private Possible<Long> permissions = Possible.absent();
        private Possible<Integer> color = Possible.absent();
        private Possible<Boolean> hoist = Possible.absent();
        private Possible<Boolean> mentionable = Possible.absent();

        public Builder name(String name) {
            this.name = Possible.of(name);
            return this;
        }

        public Builder permissions(long permissions) {
            this.permissions = Possible.of(permissions);
            return this;
        }

        public Builder color(int color) {
            this.color = Possible.of(color);
            return this;
        }

        public Builder hoist(boolean hoist) {
            this.hoist = Possible.of(hoist);
            return this;
        }

        public Builder mentionable(boolean mentionable) {
            this.mentionable = Possible.of(mentionable);
            return this;
        }

        public RoleModifyRequest build() {
            return new RoleModifyRequest(name, permissions, color, hoist, mentionable);
        }
    }

    @Override
    public String toString() {
        return "RoleModifyRequest{" +
                "name=" + name +
                ", permissions=" + permissions +
                ", color=" + color +
                ", hoist=" + hoist +
                ", mentionable=" + mentionable +
                '}';
    }
}
