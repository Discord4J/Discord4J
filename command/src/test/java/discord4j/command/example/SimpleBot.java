package discord4j.command.example;

import discord4j.command.CommandBootstrapper;
import discord4j.command.NaiveCommandDispatcher;
import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;

public class SimpleBot {

    public static void main(String[] args) throws InterruptedException {
        DiscordClient client = new DiscordClientBuilder(args[0]).build();
        CommandBootstrapper bootstrapper = new CommandBootstrapper(new NaiveCommandDispatcher("!"));
        bootstrapper.addProvider(new SimpleCommandProvider(client));
        bootstrapper.attach(client).log().subscribe();
        client.login().block();
    }
}
