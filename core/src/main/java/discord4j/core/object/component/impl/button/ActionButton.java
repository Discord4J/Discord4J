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
 * Represents a button with a style of PRIMARY, SECONDARY, SUCCESS, or DANGER.
 * These buttons require a customId and will trigger an interaction when clicked.
 */
public class ActionButton extends Button {

    public static ActionButton primary(String customId) {
        return new ActionButton(Style.PRIMARY, customId);
    }

    public static ActionButton secondary(String customId) {
        return new ActionButton(Style.SECONDARY, customId);
    }

    public static ActionButton success(String customId) {
        return new ActionButton(Style.SUCCESS, customId);
    }

    public static ActionButton danger(String customId) {
        return new ActionButton(Style.DANGER, customId);
    }

    private ActionButton(Style style, String customId) {
        this(ButtonComponentData.builder()
                .style(style.getValue())
                .customId(customId)
                .build());
    }

    protected ActionButton(ButtonComponentData data) {
        super(data);
    }

    public ActionButton withStyle(Style style) {
        return this.create(builder -> builder.style(style.getValue()));
    }

    public ActionButton withComponentId(int componentId) {
        return this.create(builder -> builder.id(componentId));
    }

    public ActionButton withCustomId(String customId) {
        return this.create(builder -> builder.customId(customId));
    }

    public ActionButton withEmoji(Emoji emoji) {
        return this.create(builder -> builder.emoji(emoji.asEmojiData()));
    }

    public ActionButton withLabel(String label) {
        return this.create(builder -> builder.label(label));
    }

    private ActionButton create(Consumer<ImmutableButtonComponentData.Builder> builderConsumer) {
        ImmutableButtonComponentData.Builder dataBuilder = ButtonComponentData.builder().from(this.getData());
        builderConsumer.accept(dataBuilder);
        return new ActionButton(dataBuilder.build());
    }
}
