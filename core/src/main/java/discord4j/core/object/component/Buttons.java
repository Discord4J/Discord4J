package discord4j.core.object.component;

import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.discordjson.json.ComponentData;
import discord4j.discordjson.json.EmojiData;
import discord4j.discordjson.json.ImmutableComponentData;
import reactor.util.annotation.Nullable;

// TODO idk about this one chief
public abstract class Buttons {

    public static Button primary(String customId, String label) {
        return of(Button.Style.PRIMARY, label, null, customId, null, false);
    }

    public static Button primary(String customId, ReactionEmoji emoji) {
        return of(Button.Style.PRIMARY, null, emoji, customId, null, false);
    }

    public static Button primary(String customId, ReactionEmoji emoji, String label) {
        return of(Button.Style.PRIMARY, label, emoji, customId, null, false);
    }

    public static Button primaryDisabled(String customId, String label) {
        return of(Button.Style.PRIMARY, label, null, customId, null, true);
    }

    public static Button primaryDisabled(String customId, ReactionEmoji emoji) {
        return of(Button.Style.PRIMARY, null, emoji, customId, null, true);
    }

    public static Button primaryDisabled(String customId, ReactionEmoji emoji, String label) {
        return of(Button.Style.PRIMARY, label, emoji, customId, null, true);
    }

    public static Button secondary(String customId, String label) {
        return of(Button.Style.SECONDARY, label, null, customId, null, false);
    }

    public static Button secondary(String customId, ReactionEmoji emoji) {
        return of(Button.Style.SECONDARY, null, emoji, customId, null, false);
    }

    public static Button secondary(String customId, ReactionEmoji emoji, String label) {
        return of(Button.Style.SECONDARY, label, emoji, customId, null, false);
    }

    public static Button secondaryDisabled(String customId, String label) {
        return of(Button.Style.SECONDARY, label, null, customId, null, true);
    }

    public static Button secondaryDisabled(String customId, ReactionEmoji emoji) {
        return of(Button.Style.SECONDARY, null, emoji, customId, null, true);
    }

    public static Button secondaryDisabled(String customId, ReactionEmoji emoji, String label) {
        return of(Button.Style.SECONDARY, label, emoji, customId, null, true);
    }

    public static Button success(String customId, String label) {
        return of(Button.Style.SUCCESS, label, null, customId, null, false);
    }

    public static Button success(String customId, ReactionEmoji emoji) {
        return of(Button.Style.SUCCESS, null, emoji, customId, null, false);
    }

    public static Button success(String customId, ReactionEmoji emoji, String label) {
        return of(Button.Style.SUCCESS, label, emoji, customId, null, false);
    }

    public static Button successDisabled(String customId, String label) {
        return of(Button.Style.SUCCESS, label, null, customId, null, true);
    }

    public static Button successDisabled(String customId, ReactionEmoji emoji) {
        return of(Button.Style.SUCCESS, null, emoji, customId, null, true);
    }

    public static Button successDisabled(String customId, ReactionEmoji emoji, String label) {
        return of(Button.Style.SUCCESS, label, emoji, customId, null, true);
    }

    public static Button danger(String customId, String label) {
        return of(Button.Style.DANGER, label, null, customId, null, false);
    }

    public static Button danger(String customId, ReactionEmoji emoji) {
        return of(Button.Style.DANGER, null, emoji, customId, null, false);
    }

    public static Button danger(String customId, ReactionEmoji emoji, String label) {
        return of(Button.Style.DANGER, label, emoji, customId, null, false);
    }

    public static Button dangerDisabled(String customId, String label) {
        return of(Button.Style.DANGER, label, null, customId, null, true);
    }

    public static Button dangerDisabled(String customId, ReactionEmoji emoji) {
        return of(Button.Style.DANGER, null, emoji, customId, null, true);
    }

    public static Button dangerDisabled(String customId, ReactionEmoji emoji, String label) {
        return of(Button.Style.DANGER, label, emoji, customId, null, true);
    }

    public static Button link(String url, String label) {
        return of(Button.Style.LINK, label, null, null, url, false);
    }

    public static Button link(String url, ReactionEmoji emoji) {
        return of(Button.Style.LINK, null, emoji, null, url, false);
    }

    public static Button link(String url, ReactionEmoji emoji, String label) {
        return of(Button.Style.LINK, label, emoji, null, url, false);
    }

    public static Button linkDisabled(String url, String label) {
        return of(Button.Style.LINK, label, null, null, url, true);
    }

    public static Button linkDisabled(String url, ReactionEmoji emoji) {
        return of(Button.Style.LINK, null, emoji, null, url, true);
    }

    public static Button linkDisabled(String url, ReactionEmoji emoji, String label) {
        return of(Button.Style.LINK, label, emoji, null, url, true);
    }

    // TODO ugly
    public static Button of(Button.Style style, @Nullable String label, @Nullable ReactionEmoji emoji, @Nullable String customId, @Nullable String url, boolean disabled) {
        ImmutableComponentData.Builder builder = ComponentData.builder()
                .type(MessageComponent.Type.BUTTON.getValue())
                .style(style.getValue());

        if (label != null) builder.label(label);

        if (emoji instanceof ReactionEmoji.Custom) {
            ReactionEmoji.Custom custom = (ReactionEmoji.Custom) emoji;
            builder.emoji(EmojiData.builder()
                    .id(custom.getId().asLong())
                    .name(custom.getName())
                    .animated(custom.isAnimated())
                    .build());
        } else if (emoji instanceof ReactionEmoji.Unicode) {
            ReactionEmoji.Unicode unicode = (ReactionEmoji.Unicode) emoji;
            builder.emoji(EmojiData.builder().name(unicode.getRaw()).build());
        }

        if (customId != null) builder.customId(customId);

        if (url != null) builder.url(url);

        builder.disabled(disabled);

        return new Button(builder.build());
    }
}
