package discord4j.commands.example;

import discord4j.commands.BaseCommand;
import discord4j.commands.CommandProvider;
import discord4j.core.event.domain.message.MessageCreateEvent;

import java.util.Optional;

public class SimpleCommandProvider implements CommandProvider {

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
                              return new LogoutCommand();
                          else
                              return null;
                      });
    }
}
