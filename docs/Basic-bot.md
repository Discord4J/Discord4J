## Basic bot

This section covers the concepts surrounding the "core" of your bot, namely the `IDiscordClient` interface which represents your way to interact with your bot.

Most `Event` objects (such as the `MessageReceivedEvent`, for example) will expose a way to access the `IDiscordClient` instance it was dispatched from, it's highly advised to access it this way rather than using a singleton. The client can be accessed by calling `Event#getClient()`.

The code below demonstrates a basic bot implementation registering one listener for `MessageReceivedEvent` using both the `IListener` interface and the `EventSubscriber` notation. This is written assuming that the D4J version mentioned on the main page is included on the classpath via Maven, Gradle or however else you included the library. Refer to the README document on the main page of the GitHub repo for more details about that. 

When `/test` is typed it'll send a message in that channel.

`MainRunner.java`:
```java
import sx.blah.discord.api.IDiscordClient;

public class MainRunner {

    public static void main(String[] args){

        if(args.length != 1){
            System.out.println("Please enter the bots token as the first argument e.g java -jar thisjar.jar tokenhere");
            return;
        }

        IDiscordClient cli = BotUtils.getBuiltDiscordClient(args[0]);

        /*
        // Commented out as you don't really want duplicate listeners unless you're intentionally writing your code 
        // like that.
        // Register a listener via the IListener interface
        cli.getDispatcher().registerListener(new IListener<MessageReceivedEvent>() {
            public void handle(MessageReceivedEvent event) {
                if(event.getMessage().getContent().startsWith(BotUtils.BOT_PREFIX + "test"))
                    BotUtils.sendMessage(event.getChannel(), "I am sending a message from an IListener listener");
            }
        });
        */

        // Register a listener via the EventSubscriber annotation which allows for organisation and delegation of events
        cli.getDispatcher().registerListener(new MyEvents());

        // Only login after all events are registered otherwise some may be missed.
        cli.login();

    }

}
```

BotUtils.java:
```java
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RateLimitException;
import sx.blah.discord.util.RequestBuffer;

class BotUtils {

    // Constants for use throughout the bot
    static String BOT_PREFIX = "/";

    // Handles the creation and getting of a IDiscordClient object for a token
    static IDiscordClient getBuiltDiscordClient(String token){

        // The ClientBuilder object is where you will attach your params for configuring the instance of your bot.
        // Such as withToken, setDaemon etc
        return new ClientBuilder()
                .withToken(token)
                .build();

    }

    // Helper functions to make certain aspects of the bot easier to use.
    static void sendMessage(IChannel channel, String message){

        // This might look weird but it'll be explained in another page.
        RequestBuffer.request(() -> {
            try{
                channel.sendMessage(message);
            } catch (DiscordException e){
                System.err.println("Message could not be sent with error: ");
                e.printStackTrace();
            }
        });

		/*
		// The below example is written to demonstrate sending a message if you want to catch the RLE for logging purposes
        RequestBuffer.request(() -> {
            try{
                channel.sendMessage(message);
            } catch (RateLimitException e){
                System.out.println("Do some logging");
                throw e;
            }
        });
        */

    }
}

```

MyEvents.java:
```java
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

public class MyEvents {

    @EventSubscriber
    public void onMessageReceived(MessageReceivedEvent event){
        if(event.getMessage().getContent().startsWith(BotUtils.BOT_PREFIX + "test"))
            BotUtils.sendMessage(event.getChannel(), "I am sending a message from an EventSubscriber listener");
    }

}
```
Please note that in the traditional example the method that exposes the built `IDiscordClient` object contains a boolean option for logging in or building the client object, this is omitted due to the assumption that the correct flow is: Build the `IDiscordClient`, register events with the `EventDispatcher`, log in. This correct flow can also be achieved by passing in the listeners to register to the `ClientBuilder` object via `#registerListener/s` and then immediately calling `#login()` or `#build()`

Additionally, since exceptions are unchecked now, they do not need to be explicitly handled unless that's a desired behaviour.

This bot isn't designed to treat you to a three course dinner at a five star hotel or anything, it's purely to demonstrate the "correct" way of logging in and registering listeners.
