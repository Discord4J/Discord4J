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

package discord4j.core.spec.legacy;

import discord4j.discordjson.json.WebhookModifyWithTokenRequest;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.Image;
import org.jspecify.annotations.Nullable;

public class LegacyWebhookEditWithTokenSpec implements LegacySpec<WebhookModifyWithTokenRequest> {

    private Possible<String> name = Possible.absent();
    private Possible<String> avatar = Possible.absent();

    /**
     * Sets the name of the modified {@link discord4j.core.object.entity.Webhook}.
     *
     * @param name The webhook name.
     * @return This spec.
     */
    public LegacyWebhookEditWithTokenSpec setName(String name) {
        this.name = Possible.of(name);
        return this;
    }

    /**
     * Sets the image of the modified {@link discord4j.core.object.entity.Webhook}.
     *
     * @param avatar The webhook image.
     * @return This spec.
     */
    public LegacyWebhookEditWithTokenSpec setAvatar(@Nullable Image avatar) {
        this.avatar = avatar == null ? Possible.absent() : Possible.of(avatar.getDataUri());
        return this;
    }

    @Override
    public WebhookModifyWithTokenRequest asRequest() {
        return WebhookModifyWithTokenRequest.builder()
                .name(name)
                .avatar(avatar)
                .build();
    }
}
