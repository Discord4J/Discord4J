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

import discord4j.core.object.entity.Webhook;
import discord4j.discordjson.json.WebhookCreateRequest;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.Image;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

/**
 * LegacySpec used to create a {@link Webhook} entity.
 *
 * @see <a href="https://discord.com/developers/docs/resources/webhook#create-webhook">Create Webhook</a>
 */
public class LegacyWebhookCreateSpec implements LegacyAuditSpec<WebhookCreateRequest> {

    private @Nullable String name;
    private @Nullable String avatar;
    private @Nullable String reason;

    /**
     * Sets the name of the created {@link Webhook}.
     *
     * @param name The webhook name.
     * @return This spec.
     */
    public LegacyWebhookCreateSpec setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the image of the created {@link Webhook}.
     *
     * @param avatar The webhook image.
     * @return This spec.
     */
    public LegacyWebhookCreateSpec setAvatar(@Nullable Image avatar) {
        this.avatar = avatar == null ? null : avatar.getDataUri();
        return this;
    }

    @Override
    public LegacyWebhookCreateSpec setReason(@Nullable final String reason) {
        this.reason = reason;
        return this;
    }

    @Nullable
    @Override
    public String getReason() {
        return reason;
    }

    @Override
    public WebhookCreateRequest asRequest() {
        if (name == null) {
            throw new IllegalStateException("Name must be set.");
        }
        return WebhookCreateRequest.builder()
                .name(name)
                .avatar(Possible.of(Optional.ofNullable(avatar)))
                .build();
    }
}
