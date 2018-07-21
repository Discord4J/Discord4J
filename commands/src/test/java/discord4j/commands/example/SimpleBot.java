package discord4j.commands.example;

import discord4j.commands.CommandBootstrapper;
import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;

public class SimpleBot {

    public static void main(String[] args) throws InterruptedException {
        DiscordClient client = new DiscordClientBuilder(args[0]).build();
        CommandBootstrapper bootstrapper = new CommandBootstrapper();
        bootstrapper.addCommandProvider(new SimpleCommandProvider(client));
        bootstrapper.attach(client).subscribe();
        client.login().block();
        Thread.sleep(10000);
    }
}
