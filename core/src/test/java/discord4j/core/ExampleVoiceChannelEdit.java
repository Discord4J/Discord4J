package discord4j.core;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.VoiceChannel;

public class ExampleVoiceChannelEdit {

    public static void main(String[] args) {
        Snowflake toEdit = Snowflake.of(System.getenv("toEdit"));

        DiscordClient.create(System.getenv("token"))
                .withGateway(client -> client.getEventDispatcher().on(MessageCreateEvent.class)
                        .map(MessageCreateEvent::getMessage)
                        .filter(msg -> msg.getContent().equals("!voiceChannelEdit"))
                        .flatMap(Message::getGuild)
                        .flatMap(guild -> guild.getChannelById(toEdit).cast(VoiceChannel.class))
                        .flatMap(channel -> channel.edit().withName("edited voice channel")))
                .block();
    }
}
