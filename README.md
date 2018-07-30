![Discord4J Logo](../images/d4j_l.png?raw=true)

# Discord4J [![Maven Central](https://img.shields.io/maven-central/v/com.discord4j/Discord4J/2.svg?style=flat-square)](https://mvnrepository.com/artifact/com.discord4j/Discord4J)  [![JCenter](https://img.shields.io/bintray/v/austinv11/maven/Discord4J.svg?style=flat-square)](https://bintray.com/austinv11/maven/Discord4J/_latestVersion)  [![Support Server Invite](https://img.shields.io/badge/Join-Discord4J-7289DA.svg?style=flat-square&logo=discord)](https://discord.gg/NxGAeCY)

A fast, reactive Java wrapper for the REST and Gateway components of the official [Discord Bot API](https://discordapp.com/developers/docs/intro).

Built with [Reactor](https://projectreactor.io/), [Netty](https://netty.io/), and a focus on flexibility, Discord4J provides an efficient, non-blocking interface for creating Discord bots.

## Installation
### Gradle
```groovy
repositories {
  maven { url  "https://jitpack.io" }
}

dependencies {
  implementation "com.discord4j.discord4j:discord4j-core:@VERSION@"
}
```
### Maven
```xml
<repositories>
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>

<dependencies>
  <dependency>
    <groupId>com.discord4j.discord4j</groupId>
    <artifactId>discord4j-core</artifactId>
    <version>@VERSION@</version>
  </dependency>
</dependencies>
```

## Quick Example
```java
final DiscordClient client = new DiscordClientBuilder("token").build();

client.getEventDispatcher().on(ReadyEvent.class)
        .subscribe(ready -> System.out.println("Logged in as " + ready.getSelf().getUsername()));

client.getEventDispatcher().on(MessageCreateEvent.class)
        .map(MessageCreateEvent::getMessage)
        .filter(msg -> msg.getContent().map(content -> content.equals("!ping")).orElse(false))
        .flatMap(Message::getChannel)
        .flatMap(channel -> channel.createMessage("Pong!"))
        .subscribe();

client.login().block();
```

## Modules
Discord4J is highly oriented towards customizability and fine-grained control. To achieve this, the project is divided into several "modules" which can be used separately depending on your use case.

### [Core](./core/README.md)
The `core` module combines the other modules to form high-level abstractions for the entire Discord Bot API. This is the module most users will want when making bots.

### [Rest](./rest/README.md)
The `rest` module provides a low-level HTTP client specifically for Discord which properly handles Discord's [ratelimiting system](https://discordapp.com/developers/docs/topics/rate-limits).

### [Gateway](./gateway/README.md)
The `gateway` module provides a low-level WebSocket client for interacting with the [Discord Gateway](https://discordapp.com/developers/docs/topics/gateway).

### [Store](./store/README.md)
The `store` module provides a platform for the efficient caching of Discord gateway data.

### [Common](./common/README.md)
The `common` module contains base utilities and models useful for other modules.

## Useful Links
* [Discord4J Site](https://discord4j.com)
* [Discord4J Wiki](https://github.com/Discord4J/Discord4J/wiki)
* [Javadoc](https://jitpack.io/com/discord4j/discord4j/discord4j-core/v3-SNAPSHOT/javadoc/index.html)
* [Reactor 3 Reference Guide](http://projectreactor.io/docs/core/release/reference/)