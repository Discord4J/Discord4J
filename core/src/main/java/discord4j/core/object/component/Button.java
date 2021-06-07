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
package discord4j.core.object.component;

import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.discordjson.json.ComponentData;
import discord4j.discordjson.json.ImmutableComponentData;
import reactor.util.annotation.Nullable;

import java.util.Optional;

public class Button implements MessageComponent {

    public static Button primary(String customId, String label) {
        return of(Button.Style.PRIMARY, customId, null, label, null);
    }

    public static Button primary(String customId, ReactionEmoji emoji) {
        return of(Button.Style.PRIMARY, customId, emoji, null, null);
    }

    public static Button primary(String customId, ReactionEmoji emoji, String label) {
        return of(Button.Style.PRIMARY, customId, emoji, label, null);
    }

    public static Button secondary(String customId, String label) {
        return of(Button.Style.SECONDARY, customId, null, label, null);
    }

    public static Button secondary(String customId, ReactionEmoji emoji) {
        return of(Button.Style.SECONDARY, customId, emoji, null, null);
    }

    public static Button secondary(String customId, ReactionEmoji emoji, String label) {
        return of(Button.Style.SECONDARY, customId, emoji, label, null);
    }

    public static Button success(String customId, String label) {
        return of(Button.Style.SUCCESS, customId, null, label, null);
    }

    public static Button success(String customId, ReactionEmoji emoji) {
        return of(Button.Style.SUCCESS, customId, emoji, null, null);
    }

    public static Button success(String customId, ReactionEmoji emoji, String label) {
        return of(Button.Style.SUCCESS, customId, emoji, label, null);
    }

    public static Button danger(String customId, String label) {
        return of(Button.Style.DANGER, customId, null, label, null);
    }

    public static Button danger(String customId, ReactionEmoji emoji) {
        return of(Button.Style.DANGER, customId, emoji, null, null);
    }

    public static Button danger(String customId, ReactionEmoji emoji, String label) {
        return of(Button.Style.DANGER, customId, emoji, label, null);
    }

    public static Button link(String url, String label) {
        return of(Button.Style.LINK, null, null, label, url);
    }

    public static Button link(String url, ReactionEmoji emoji) {
        return of(Button.Style.LINK, null, emoji, null, url);
    }

    public static Button link(String url, ReactionEmoji emoji, String label) {
        return of(Button.Style.LINK, null, emoji, label, url);
    }

    public static Button of(Style style, @Nullable String customId, @Nullable ReactionEmoji emoji, @Nullable String label, @Nullable String url) {
        ImmutableComponentData.Builder builder = ComponentData.builder()
                .type(MessageComponent.Type.BUTTON.getValue())
                .style(style.getValue());

        if (customId != null)
            builder.customId(customId);

        if (emoji != null)
            builder.emoji(emoji.getData());

        if (label != null)
            builder.label(label);

        if (url != null)
            builder.url(url);

        return new Button(builder.build());
    }

    private final ComponentData data;

    Button(ComponentData data) {
        this.data = data;
    }

    public Style getStyle() {
        return data.style().toOptional()
                .map(Style::of)
                .orElseThrow(IllegalStateException::new); // style should always be present on buttons
    }

    public Optional<String> getLabel() {
        return data.label().toOptional();
    }

    public Optional<ReactionEmoji> getEmoji() {
        return data.emoji().toOptional()
                .map(ReactionEmoji::of);
    }

    public Optional<String> getCustomId() {
        return data.customId().toOptional();
    }

    public Optional<String> getUrl() {
        return data.url().toOptional();
    }

    public boolean isDisabled() {
        return data.disabled().toOptional().orElse(false);
    }

    public Button disabled() {
        return new Button(ComponentData.builder().from(data).disabled(true).build());
    }

    @Override
    public Type getType() {
        return Type.BUTTON;
    }

    @Override
    public ComponentData getData() {
        return data;
    }

    enum Style {
        UNKNOWN(-1),
        PRIMARY(1),
        SECONDARY(2),
        SUCCESS(3),
        DANGER(4),
        LINK(5);

        private final int value;

        Style(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static Style of(int value) {
            switch (value) {
                case 1: return PRIMARY;
                case 2: return SECONDARY;
                case 3: return SUCCESS;
                case 4: return DANGER;
                case 5: return LINK;
                default: return UNKNOWN;
            }
        }
    }
}
