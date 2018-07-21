package discord4j.commands.example;

import discord4j.commands.BaseCommand;
import discord4j.commands.CommandProvider;
import discord4j.core.DiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.ApplicationInfo;
import discord4j.core.object.util.Snowflake;

import java.util.Optional;

public class SimpleCommandProvider implements CommandProvider {

    private final DiscordClient client;
    private final Snowflake ownerId;

    public SimpleCommandProvider(DiscordClient client) {
        this.client = client;
        this.ownerId = client.getApplicationInfo().map(ApplicationInfo::getOwnerId).block();
    }

    @Override
    public Optional<? extends BaseCommand> provide(MessageCreateEvent context) {
        return context.getMessage()
                      .getContent()
                      .filter(s -> s.startsWith("!"))
                      .map(s -> s.replaceFirst("!", "").split(" ")[0])
                      .map(cmd -> {
                          if (cmd.equalsIgnoreCase("echo"))
                              return new EchoCommand();
                          else if (cmd.equalsIgnoreCase("logout"))
                              return new LogoutCommand(ownerId);
                          else
                              return null;
                      });
    }
}
