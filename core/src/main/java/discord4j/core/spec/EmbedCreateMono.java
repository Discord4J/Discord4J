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

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Message;
import discord4j.discordjson.json.*;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.entity.RestChannel;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * A mono used to configure and send an embed.
 *
 * @see <a href="https://i.stack.imgur.com/HRWHk.png">Embed Layout</a>
 */
public class EmbedCreateMono extends AuditableRequest<Message, ImmutableEmbedData.Builder, EmbedCreateMono> {

    private final GatewayDiscordClient gateway;
    private final RestChannel restChannel;

    public EmbedCreateMono(Supplier<ImmutableEmbedData.Builder> requestBuilder, @Nullable String reason,
                           GatewayDiscordClient gateway, RestChannel restChannel) {
        super(requestBuilder, reason);
        this.gateway = gateway;
        this.restChannel = restChannel;
    }

    public EmbedCreateMono(GatewayDiscordClient gateway, RestChannel restChannel) {
        this(ImmutableEmbedData::builder, null, gateway, restChannel);
    }

    @Override
    public EmbedCreateMono withReason(String reason) {
        return new EmbedCreateMono(requestBuilder, reason, gateway, restChannel);
    }

    @Override
    EmbedCreateMono withBuilder(UnaryOperator<ImmutableEmbedData.Builder> f) {
        return new EmbedCreateMono(apply(f), reason, gateway, restChannel);
    }

    /**
     * Sets the title of the embed.
     *
     * @param title The title of the embed.
     * @return This mono.
     */
    public EmbedCreateMono withTitle(String title) {
        return withBuilder(it -> it.title(title));
    }

    /**
     * Sets the description of the embed.
     *
     * @param description The description of the embed.
     * @return This mono.
     */
    public EmbedCreateMono withDescription(String description) {
        return withBuilder(it -> it.description(description));
    }

    /**
     * Sets the URL of the embed.
     *
     * @param url A URL which can be clicked on through the title of the embed.
     * @return This mono.
     */
    public EmbedCreateMono withUrl(String url) {
        return withBuilder(it -> it.url(url));
    }

    /**
     * Sets the timestamp to display in the embed. The timestamp is displayed locally for each user's timezone.
     *
     * @param timestamp A {@link Instant} to display in the embed footer.
     * @return This mono.
     */
    public EmbedCreateMono withTimestamp(Instant timestamp) {
        return withBuilder(it -> it.timestamp(DateTimeFormatter.ISO_INSTANT.format(timestamp)));
    }

    /**
     * Sets the color of the embed.
     *
     * @param color A {@link Color} to display on the embed.
     * @return This mono.
     */
    public EmbedCreateMono withColor(Color color) {
        return withBuilder(it -> it.color(color.getRGB()));
    }

    /**
     * Sets the footer of the embed.
     *
     * @param text    The footer text.
     * @param iconUrl An icon URL to display in the footer.
     * @return This mono.
     */
    public EmbedCreateMono withFooter(String text, @Nullable String iconUrl) {
        return withBuilder(it -> it.footer(
            EmbedFooterData.builder()
                .text(text)
                .iconUrl(iconUrl == null ? Possible.absent() : Possible.of(iconUrl))
                .build()
        ));
    }

    /**
     * Sets the image of the embed.
     *
     * @param url An image URL.
     * @return This mono.
     */
    public EmbedCreateMono withImage(String url) {
        return withBuilder(it -> it.image(EmbedImageData.builder().url(url).build()));
    }

    /**
     * Sets the thumbnail of the embed.
     *
     * @param url An image URL.
     * @return This mono.
     */
    public EmbedCreateMono withThumbnail(String url) {
        return withBuilder(it -> it.thumbnail(EmbedThumbnailData.builder().url(url).build()));
    }

    /**
     * Sets the author section of the embed.
     *
     * @param name    The author name to display.
     * @param url     A URL which can be clicked on through the author name.
     * @param iconUrl An icon URL displayed as the avatar next to the author name.
     * @return This mono.
     */
    public EmbedCreateMono withAuthor(String name, @Nullable String url, @Nullable String iconUrl) {
        return withBuilder(it -> it.author(
            EmbedAuthorData.builder()
                .name(name)
                .url(url == null ? Possible.absent() : Possible.of(url))
                .iconUrl(iconUrl == null ? Possible.absent() : Possible.of(iconUrl))
                .build()
        ));
    }

    /**
     * Adds a field to the embed.
     *
     * @param name   The name of the field.
     * @param value  The text inside of the field.
     * @param inline Whether to inline the field or not.
     * @return This mono.
     */
    public EmbedCreateMono addField(String name, String value, boolean inline) {
        return withBuilder(it -> it.addField(
            EmbedFieldData.builder()
                .name(name)
                .value(value)
                .inline(inline)
                .build()
        ));
    }

    @Override
    Mono<Message> getRequest() {
        return Mono.defer(
            () -> restChannel.createMessage(requestBuilder.get().build()))
            .map(data -> new Message(gateway, data));
    }
}
