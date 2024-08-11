package discord4j.core.object.emoji;

import discord4j.discordjson.json.EmojiData;
import reactor.util.annotation.Nullable;

import java.util.Objects;

public class EmojiUnicode extends Emoji {

    private final String raw;

    EmojiUnicode(String raw) {
        this.raw = raw;
    }

    public String getRaw() {
        return raw;
    }

    @Override
    public EmojiData asEmojiData() {
        return EmojiData.builder()
            .name(raw)
            .build();
    }

    @Override
    public String asFormat() {
        return this.getRaw();
    }

    @Override
    public String toString() {
        return "ReactionEmoji.Unicode{" +
            "raw='" + raw + '\'' +
            "} " + super.toString();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EmojiUnicode unicode = (EmojiUnicode) o;
        return Objects.equals(raw, unicode.getRaw());
    }

    @Override
    public int hashCode() {
        return raw.hashCode();
    }

}
