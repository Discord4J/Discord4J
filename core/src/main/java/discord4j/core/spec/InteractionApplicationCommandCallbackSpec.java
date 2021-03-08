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

import discord4j.core.object.entity.Message;
import discord4j.discordjson.json.AllowedMentionsData;
import discord4j.discordjson.json.EmbedData;
import discord4j.discordjson.json.InteractionApplicationCommandCallbackData;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.AllowedMentions;
import reactor.util.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class InteractionApplicationCommandCallbackSpec implements Spec<InteractionApplicationCommandCallbackData> {

    @Nullable
    private String content;
    private boolean tts;
    private List<EmbedData> embeds;
    private AllowedMentionsData allowedMentionsData;
    private int flags;

    public InteractionApplicationCommandCallbackSpec setContent(String content) {
        this.content = content;
        return this;
    }

    public InteractionApplicationCommandCallbackSpec setTts(boolean tts) {
        this.tts = tts;
        return this;
    }

    public InteractionApplicationCommandCallbackSpec setEphemeral(boolean ephemeral) {
        flags = ephemeral ? Message.Flag.EPHEMERAL.getFlag() : 0;
        return this;
    }

    public InteractionApplicationCommandCallbackSpec addEmbed(Consumer<? super EmbedCreateSpec> spec) {
        if (embeds == null) {
            embeds = new ArrayList<>(1); // most common case is only 1 embed per message
        }
        final EmbedCreateSpec mutatedSpec = new EmbedCreateSpec();
        spec.accept(mutatedSpec);
        embeds.add(mutatedSpec.asRequest());
        return this;
    }

    public InteractionApplicationCommandCallbackSpec setAllowedMentions(@Nullable AllowedMentions allowedMentions) {
        allowedMentionsData = allowedMentions != null ? allowedMentions.toData() : null;
        return this;
    }

    @Override
    public InteractionApplicationCommandCallbackData asRequest() {
        return InteractionApplicationCommandCallbackData.builder()
                .content(content == null ? Possible.absent() : Possible.of(content))
                .tts(tts)
                .flags(flags == 0 ? Possible.absent() : Possible.of(flags))
                .embeds(embeds == null ? Possible.absent() : Possible.of(embeds))
                .allowedMentions(allowedMentionsData == null ? Possible.absent() : Possible.of(allowedMentionsData))
                .build();
    }
}
