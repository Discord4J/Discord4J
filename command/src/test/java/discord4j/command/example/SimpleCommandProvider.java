package discord4j.command.example;

import discord4j.command.CommandProvider;
import discord4j.command.ProviderContext;
import discord4j.core.DiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.ApplicationInfo;
import discord4j.core.object.util.Snowflake;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class SimpleCommandProvider implements CommandProvider {

    private final DiscordClient client;
    private final Snowflake ownerId;

    public SimpleCommandProvider(DiscordClient client) {
        this.client = client;
        this.ownerId = client.getApplicationInfo().map(ApplicationInfo::getOwnerId).block();
    }

    @Override
    public Flux<ProviderContext> provide(MessageCreateEvent context, String cmdName, int startIndex, int endIndex) {
        return Mono.just(cmdName)
                .map(cmd -> {
                    if (cmd.equalsIgnoreCase("echo")) {
                        return ProviderContext.of(new EchoCommand(startIndex, endIndex));
                    } else if (cmd.equalsIgnoreCase("logout")) {
                        return ProviderContext.of(new LogoutCommand(ownerId));
                    } else {
                        return null;
                    }
                }).flux();
    }
}
