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

import discord4j.core.object.entity.Webhook;
import discord4j.discordjson.json.WebhookCreateRequest;
import discord4j.rest.util.Image;
import reactor.util.annotation.Nullable;

/**
 * Spec used to create a {@link Webhook} entity.
 *
 * @see <a href="https://discordapp.com/developers/docs/resources/webhook#create-webhook">Create Webhook</a>
 */
public class WebhookCreateSpec implements AuditSpec<WebhookCreateRequest> {

    private String name;
    private String avatar;
    private String reason;

    /**
     * Sets the name of the created {@link Webhook}.
     *
     * @param name The webhook name.
     * @return This spec.
     */
    public WebhookCreateSpec setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the image of the created {@link Webhook}.
     *
     * @param avatar The webhook image.
     * @return This spec.
     */
    public WebhookCreateSpec setAvatar(@Nullable Image avatar) {
        this.avatar = avatar == null ? null : avatar.getDataUri();
        return this;
    }

    @Override
    public WebhookCreateSpec setReason(@Nullable final String reason) {
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
        return WebhookCreateRequest.builder()
                .name(name)
                .avatar(avatar)
                .build();
    }
}
