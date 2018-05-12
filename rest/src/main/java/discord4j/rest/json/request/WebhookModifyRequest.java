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

import javax.annotation.Nullable;

@PossibleJson
public class WebhookModifyRequest {

    private final Possible<String> name;
    @Nullable
    private final Possible<String> avatar;

    public WebhookModifyRequest(Possible<String> name, @Nullable Possible<String> avatar) {
        this.name = name;
        this.avatar = avatar;
    }

    @Override
    public String toString() {
        return "WebhookModifyRequest[" +
                "name=" + name +
                ", avatar=" + avatar +
                ']';
    }
}
