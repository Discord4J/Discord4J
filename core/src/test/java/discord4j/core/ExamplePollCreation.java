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
package discord4j.core;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.object.entity.poll.Poll;
import discord4j.core.object.entity.poll.PollAnswer;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.discordjson.json.EmojiData;
import discord4j.discordjson.json.PollAnswerObject;
import discord4j.discordjson.json.PollMediaObject;
import reactor.core.publisher.Mono;

public class ExamplePollCreation {

    private static final String token = System.getenv("token");
    private static final String guildId = System.getenv("guildId");
    private static final String channelId = System.getenv("channelId");

    public static void main(String[] args) {
        DiscordClient.create(token)
            .withGateway(gw -> {
                Mono<Poll> createPoll = gw.on(GuildCreateEvent.class)
                    .filter(e -> e.getGuild().getId().asString().equals(guildId))
                    .next()
                    .flatMap(e -> e.getGuild().getChannelById(Snowflake.of(channelId)))
                    .ofType(TextChannel.class)
                    .flatMap(channel -> channel.createPoll()
                        .withQuestion("What is your favorite color?")
                        .withAnswers(
                            PollAnswer.of("Red", ReactionEmoji.unicode("\uD83D\uDD34")),
                            PollAnswer.of("Green", ReactionEmoji.unicode("\uD83D\uDFE2"))
                        )
                        .withAllowMultiselect(true)
                        .withDuration(3) // 3 hours
                        .withLayoutType(Poll.PollLayoutType.DEFAULT));

                return createPoll.then();
            })
            .block();
    }

}
