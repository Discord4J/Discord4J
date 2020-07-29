# Beginnings

Making your first bot is very simple. This tutorial will walk you through how to make a basic bot that responds to a ping command and listens to some other events. The final bot can be found at [the github.](https://github.com/xaanit/D4JExampleBot)

## Constructing the client

The first thing you need to do to actually create the bot is to **construct the client**. This is done using `DiscordClientBuilder.create` or directly through `DiscordClient.create`

```java
import discord4j.core.DiscordClientBuilder;

public class Bot {

  public static void main(String[] args) {
    DiscordClientBuilder builder = DiscordClientBuilder.create("TOKEN HERE");
  }

}
```

###### 🛈 Important Links 🛈
- [DiscordClientBuilder Javadocs](https://www.javadoc.io/doc/com.discord4j/discord4j-core/3.1.0/discord4j/core/DiscordClientBuilder.html)
- [DiscordClient Javadocs](https://www.javadoc.io/doc/com.discord4j/discord4j-core/3.1.0/discord4j/core/DiscordClient.html)
- [GatewayBootstrap Javadocs](https://www.javadoc.io/static/com.discord4j/discord4j-core/3.1.0/discord4j/core/shard/GatewayBootstrap.html)
- [GatewayDiscordClient Javadocs](https://www.javadoc.io/static/com.discord4j/discord4j-core/3.1.0/discord4j/core/GatewayDiscordClient.html)


```java
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;

public class Bot {

    public static void main(String[] args) {
        GatewayDiscordClient client = DiscordClientBuilder.create("TOKEN HERE")
                .build()
                .login()
                .block();
        client.onDisconnect().block();
    }

}
```

This is the very least that you need to be able to create a bot and login to show the bot as online.

###### ⚠️ Warning ⚠️
> You do not need to call block on `login` or `onDisconnect`, but if you do not have a non daemon thread running your program will instantly exit. Only call subscribe for login if you know what you're doing.

###### 🛈 Important Links 🛈
> [DiscordClient Javadocs](https://www.javadoc.io/doc/com.discord4j/discord4j-core/latest/discord4j/core/DiscordClient.html)

## Responding to events

Next let's listen to a couple of events. First let's listen to the ReadyEvent to figure out when we first originally connect to the gateway.

```java
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.object.entity.User;

public class Bot {

  public static void main(String[] args) {
    GatewayDiscordClient client = DiscordClientBuilder.create("TOKEN HERE").build().login().block();

    client.getEventDispatcher().on(ReadyEvent.class)
        .subscribe(event -> {
          User self = event.getSelf();
          System.out.println(String.format("Logged in as %s#%s", self.getUsername(), self.getDiscriminator()));
        });

    client.onDisconnect().block();
  }

}
```

This will listen to when we connect to the gateway and when `ReadyEvent` fires. This'll then print out the name and discriminator of the bot that is signing in. Next we'll figure out how to listen for a `!ping` command from only users and respond with a pong!

```java
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;

public class Bot {

  public static void main(String[] args) {
    GatewayDiscordClient client = DiscordClientBuilder.create("TOKEN HERE").build().login().block();

    client.getEventDispatcher().on(ReadyEvent.class)
        .subscribe(event -> {
          User self = event.getSelf();
          System.out.println(String.format("Logged in as %s#%s", self.getUsername(), self.getDiscriminator()));
        });

    client.getEventDispatcher().on(MessageCreateEvent.class)
        .map(MessageCreateEvent::getMessage)
        .filter(message -> message.getAuthor().map(user -> !user.isBot()).orElse(false))
        .filter(message -> message.getContent().equalsIgnoreCase("!ping"))
        .flatMap(Message::getChannel)
        .flatMap(channel -> channel.createMessage("Pong!"))
        .subscribe();

    client.onDisconnect().block();
  }

}
```

Now let's look back on this, there is a lot going on here so let's walk through it.

> `client.getEventDispatcher().on(MessageCreateEvent.class)` 

This listens for all `MessageCreateEvent`s that come in to the bot.

> `        .map(MessageCreateEvent::getMessage)
`

This turns all `MessageCreate` events into the messages that were sent. The `::` syntax is just shorthand for `map(event -> event.getMessage())`.

> `        .filter(message -> message.getAuthor().map(user -> !user.isBot()).orElse(false))
`

This filters out all members that are bots, so that we only get events from users.

> `        .filter(message -> message.getContent().equalsIgnoreCase("!ping"))
`

This filters out any messages that don't equal the content `!ping`.

> `        .flatMap(Message::getChannel)
`

This turns it into the channel the message came from.

> `        .flatMap(channel -> channel.createMessage("Pong!"))
`

This creates the message with the content `Pong!`

>`         .subscribe();
`

And this tells it to execute!

## Finale

Now that you have a basic bot up and running, you can continue to build off of it. Including making your own system so all events aren't in the same class where you construct the client.