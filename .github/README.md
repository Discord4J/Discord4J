![Discord4J Logo](../images/d4j_l.png?raw=true)

# Discord4J [![Maven Central](https://img.shields.io/maven-central/v/com.discord4j/Discord4J/2.svg?style=flat-square)](https://mvnrepository.com/artifact/com.discord4j/Discord4J)  [![JCenter](https://img.shields.io/bintray/v/austinv11/maven/Discord4J.svg?style=flat-square)](https://bintray.com/austinv11/maven/Discord4J/_latestVersion)  [![Support Server Invite](https://img.shields.io/badge/Join-Discord4J-7289DA.svg?style=flat-square&logo=discord)](https://discord.gg/NxGAeCY)

A reactive Java wrapper for the REST and Gateway components of the official [Discord Bot API](https://discordapp.com/developers/docs/intro).

Built with [Reactor](https://projectreactor.io/), [Netty](https://netty.io/), and a focus on flexibility, Discord4J provides an efficient, non-blocking interface for creating Discord bots.

## Installation
```groovy
repositories {
  maven {
    url  "https://jitpack.io"
  }
}

dependencies {
  compile "com.github.Discord4J.Discord4J:Discord4J-core:@VERSION@"
}
```

## Quick Example
```java
DiscordClient client = new ClientBuilder("token").build();

client.getEventDispatcher().on(ReadyEvent.class)
        .subscribe(ready -> System.out.println("Logged in as " + ready.getSelf().getUsername()));

client.getEventDispatcher().on(MessageCreateEvent.class)
        .map(MessageCreateEvent::getMessage)
        .filter(msg -> msg.getContent().map(content -> content.equals("!ping")).orElse(false))
        .flatMap(Message::getChannel)
        .flatMap(channel -> channel.createMessage(spec -> spec.setContent("Pong!")))
        .subscribe();

client.login().block();
```

## Useful Links
* [Discord4J Site](discord4j.com)
* [Discord4J Wiki](http://discord4j.readthedocs.io/en/latest/)
* [Reactor 3 Reference Guide](http://projectreactor.io/docs/core/release/reference/)