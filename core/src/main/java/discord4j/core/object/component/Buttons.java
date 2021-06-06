package discord4j.core.object.component;

import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.discordjson.json.ComponentData;
import discord4j.discordjson.json.EmojiData;
import discord4j.discordjson.json.ImmutableComponentData;
import reactor.util.annotation.Nullable;

// TODO idk about this one chief
public abstract class Buttons {

    public static Button primary(String customId, String label) {
        return of(Button.Style.PRIMARY, label, null, customId, null);
    }

    public static Button primary(String customId, ReactionEmoji emoji) {
        return of(Button.Style.PRIMARY, null, emoji, customId, null);
    }

    public static Button primary(String customId, ReactionEmoji emoji, String label) {
        return of(Button.Style.PRIMARY, label, emoji, customId, null);
    }

    public static Button secondary(String customId, String label) {
        return of(Button.Style.SECONDARY, label, null, customId, null);
    }

    public static Button secondary(String customId, ReactionEmoji emoji) {
        return of(Button.Style.SECONDARY, null, emoji, customId, null);
    }

    public static Button secondary(String customId, ReactionEmoji emoji, String label) {
        return of(Button.Style.SECONDARY, label, emoji, customId, null);
    }

    public static Button success(String customId, String label) {
        return of(Button.Style.SUCCESS, label, null, customId, null);
    }

    public static Button success(String customId, ReactionEmoji emoji) {
        return of(Button.Style.SUCCESS, null, emoji, customId, null);
    }

    public static Button success(String customId, ReactionEmoji emoji, String label) {
        return of(Button.Style.SUCCESS, label, emoji, customId, null);
    }

    public static Button danger(String customId, String label) {
        return of(Button.Style.DANGER, label, null, customId, null);
    }

    public static Button danger(String customId, ReactionEmoji emoji) {
        return of(Button.Style.DANGER, null, emoji, customId, null);
    }

    public static Button danger(String customId, ReactionEmoji emoji, String label) {
        return of(Button.Style.DANGER, label, emoji, customId, null);
    }

    public static Button link(String url, String label) {
        return of(Button.Style.LINK, label, null, null, url);
    }

    public static Button link(String url, ReactionEmoji emoji) {
        return of(Button.Style.LINK, null, emoji, null, url);
    }

    public static Button link(String url, ReactionEmoji emoji, String label) {
        return of(Button.Style.LINK, label, emoji, null, url);
    }

    // TODO ugly
    public static Button of(Button.Style style, @Nullable String label, @Nullable ReactionEmoji emoji, @Nullable String customId, @Nullable String url) {
        ImmutableComponentData.Builder builder = ComponentData.builder()
                .type(MessageComponent.Type.BUTTON.getValue())
                .style(style.getValue());

        if (label != null) builder.label(label);

        if (emoji != null) builder.emoji(emoji.getData());

        if (customId != null) builder.customId(customId);

        if (url != null) builder.url(url);

        return new Button(builder.build());
    }
}
