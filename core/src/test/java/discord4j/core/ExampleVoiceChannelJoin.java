package discord4j.core;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.VoiceChannel;

import java.util.Objects;

public class ExampleVoiceChannelJoin {

    public static void main(String[] args) {
        Snowflake toJoin = Snowflake.of(Objects.requireNonNull(System.getenv("toJoin")));

        DiscordClient.create(System.getenv("token"))
            .withGateway(client -> client.getEventDispatcher().on(MessageCreateEvent.class)
                .map(MessageCreateEvent::getMessage)
                .filter(msg -> msg.getContent().equals("!voiceChannelJoin"))
                .flatMap(Message::getGuild)
                .flatMap(guild -> guild.getChannelById(toJoin).cast(VoiceChannel.class))
                .flatMap(channel -> channel.join()
                    .withSelfDeaf(true)
                    .withSelfMute(true)))
            .block();
    }
}
