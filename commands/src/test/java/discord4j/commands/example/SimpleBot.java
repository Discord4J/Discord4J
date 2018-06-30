package discord4j.commands.example;

import discord4j.commands.CommandBootstrapper;
import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;

public class SimpleBot {

    public static void main(String[] args) throws InterruptedException {
        DiscordClient client = new DiscordClientBuilder(args[0]).build();
        CommandBootstrapper bootstrapper = new CommandBootstrapper.Config().setInterceptor(flux -> {
            flux.subscribe(System.out::println);
            return flux;
        }).strapTo(client);
        bootstrapper.getCommandDispatcher().addCommandProvider(new SimpleCommandProvider());
        client.login().block();
        Thread.sleep(10000);
    }
}
