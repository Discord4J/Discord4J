package discord4j.commands.example;

import discord4j.commands.BaseCommand;
import discord4j.commands.CommandProvider;
import discord4j.core.event.domain.message.MessageCreateEvent;

import java.util.Optional;

public class SimpleCommandProvider implements CommandProvider {

    @Override
    public Optional<? extends BaseCommand> provide(MessageCreateEvent context) {
        if (context.getMessage().getContent().isPresent()) {
            String content = context.getMessage().getContent().get();
            if (content.startsWith("!")) {
                String cmd = content.replaceFirst("!", "").split(" ")[0];
                switch (cmd) {
                    case "echo":
                        return Optional.of(new EchoCommand());
                    case "logout":
                        return Optional.of(new LogoutCommand());
                }
            }
        }
        return Optional.empty();
    }
}
