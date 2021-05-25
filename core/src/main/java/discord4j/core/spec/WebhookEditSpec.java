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
import discord4j.discordjson.json.WebhookModifyRequest;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.Image;
import discord4j.common.util.Snowflake;
import reactor.util.annotation.Nullable;

/**
 * Spec to modify a {@link Webhook} entity.
 *
 * @see <a href="https://discord.com/developers/docs/resources/webhook#modify-webhook">Modify Webhook</a>
 */
public class WebhookEditSpec implements Spec<WebhookModifyRequest> {

    private Possible<String> name = Possible.absent();
    private Possible<String> avatar = Possible.absent();
    @Nullable
    private String reason;
    private Possible<String> channelId = Possible.absent();

    /**
     * Sets the name of the modified {@link Webhook}.
     *
     * @param name The webhook name.
     * @return This spec.
     */
    public WebhookEditSpec setName(String name) {
        this.name = Possible.of(name);
        return this;
    }

    /**
     * Sets the image of the modified {@link Webhook}.
     *
     * @param avatar The webhook image.
     * @return This spec.
     */
    public WebhookEditSpec setAvatar(@Nullable Image avatar) {
        this.avatar = avatar == null ? Possible.absent() : Possible.of(avatar.getDataUri());
        return this;
    }

    /**
     * Sets the channel ID of the modified {@link Webhook}.
     *
     * @param id the new channel id this webhook should be moved to
     * @return This spec.
     */
    public WebhookEditSpec setChannel(@Nullable Snowflake id) {
        this.channelId = id == null ? Possible.absent() : Possible.of(id.asString());
        return this;
    }

    public WebhookEditSpec setReason(final String reason) {
        this.reason = reason;
        return this;
    }

    @Nullable
    public String getReason() {
        return reason;
    }

    @Override
    public WebhookModifyRequest asRequest() {
        return WebhookModifyRequest.builder()
                .name(name)
                .avatar(avatar)
                .channelId(channelId)
                .build();
    }
}
