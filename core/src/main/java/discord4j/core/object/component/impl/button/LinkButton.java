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
package discord4j.core.object.component.impl.button;

import discord4j.core.object.component.impl.Button;
import discord4j.core.object.emoji.Emoji;
import discord4j.discordjson.json.component.ButtonComponentData;
import discord4j.discordjson.json.component.ImmutableButtonComponentData;

import java.util.function.Consumer;

/**
 * Represents a button with a style of LINK.
 */
public class LinkButton extends Button {

    public static LinkButton of(String url) {
        return new LinkButton(url);
    }

    private LinkButton(String url) {
        this(ButtonComponentData.builder()
                .style(Style.LINK.getValue())
                .url(url)
                .build());
    }

    protected LinkButton(ButtonComponentData data) {
        super(data);
    }

    public LinkButton withComponentId(Integer componentId) {
        return this.create(builder -> builder.id(componentId));
    }

    public LinkButton withUrl(String url) {
        return this.create(builder -> builder.url(url));
    }

    public LinkButton withEmoji(Emoji emoji) {
        return this.create(builder -> builder.emoji(emoji.asEmojiData()));
    }

    public LinkButton withLabel(String label) {
        return this.create(builder -> builder.label(label));
    }

    private LinkButton create(Consumer<ImmutableButtonComponentData.Builder> builderConsumer) {
        ImmutableButtonComponentData.Builder dataBuilder = ButtonComponentData.builder().from(this.getData());
        builderConsumer.accept(dataBuilder);
        return new LinkButton(dataBuilder.build());
    }
}
