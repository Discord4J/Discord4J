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

import discord4j.rest.json.request.WebhookCreateRequest;

import javax.annotation.Nullable;

public class WebhookCreateSpec implements AuditSpec<WebhookCreateRequest> {

    private String name;
    private String avatar;
    private String reason;

    public WebhookCreateSpec setName(String name) {
        this.name = name;
        return this;
    }

    public WebhookCreateSpec setAvatar(String avatar) {
        this.avatar = avatar;
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
        return new WebhookCreateRequest(name, avatar);
    }
}
