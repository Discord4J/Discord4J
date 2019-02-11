# Discord4J

<img align="right" src="https://raw.githubusercontent.com/Discord4J/discord4j-web/master/public/logo.svg?sanitize=true" width=27%>

[![Maven Central](https://img.shields.io/maven-central/v/com.discord4j/discord4j-core.svg?style=flat-square)](https://mvnrepository.com/artifact/com.discord4j/discord4j-core) [![Support Server Invite](https://img.shields.io/badge/Join-Discord4J-7289DA.svg?style=flat-square&logo=discord)](https://discord.gg/NxGAeCY)

A fast, reactive Java wrapper for the official [Discord Bot API](https://discordapp.com/developers/docs/intro).

Built with [Reactor](https://projectreactor.io/), [Netty](https://netty.io/), and a focus on flexibility, Discord4J provides an effective, non-blocking interface for creating Discord bots. The [reactive](https://www.reactivemanifesto.org/) and asynchronous nature of the library allows for scalability through backpressure handling and the efficient use of resources. Its [modularized](#modules) structure gives the user the ability to tailor their experience to different levels of abstraction and pick the right tools for the job.

## Installation
### Gradle
```groovy
repositories {
  jcenter()
}

dependencies {
  implementation 'com.discord4j:discord4j-core:3.0.0.M2'
}
```
### Maven
```xml
<repositories>
  <repository>
    <id>jcenter</id>
    <url>https://jcenter.bintray.com/</url>
  </repository>
</repositories>

<dependencies>
  <dependency>
    <groupId>com.discord4j</groupId>
    <artifactId>discord4j-core</artifactId>
    <version>3.0.0.M2</version>
  </dependency>
</dependencies>
```

### SBT
```scala
libraryDependencies ++= Seq(
  "com.discord4j" % "discord4j-core" % "3.0.0.M2"
)

resolvers += "jcenter" at "https://jcenter.bintray.com/"
```

## Quick Example (Reactive and Blocking)
```java
final DiscordClient client = new DiscordClientBuilder("token").build();

client.getEventDispatcher().on(ReadyEvent.class)
        .subscribe(ready -> System.out.println("Logged in as " + ready.getSelf().getUsername()));

client.getEventDispatcher().on(MessageCreateEvent.class)
        .map(MessageCreateEvent::getMessage)
        .filter(msg -> msg.getContent().map("!ping"::equals).orElse(false))
        .flatMap(Message::getChannel)
        .flatMap(channel -> channel.createMessage("Pong!"))
        .subscribe();

client.login().block();
```
```java
final DiscordClient client = new DiscordClientBuilder("token").build();

client.getEventDispatcher().on(ReadyEvent.class)
        .subscribe(ready -> System.out.println("Logged in as " + ready.getSelf().getUsername()));

client.getEventDispatcher().on(MessageCreateEvent.class)
        .subscribe(event -> {
            Message message = event.getMessage();
            if (message.getContent().map("!ping"::equals).orElse(false)) {
                message.getChannel().block().createMessage("Pong!").block();
            }
         });

client.login().block();
```

## Modules
Discord4J is highly oriented towards customizability. To achieve this, the project is divided into several "modules" which can be used separately depending on your use case.

### [Core](./core/README.md)
The `core` module combines the other modules to form high-level abstractions for the entire Discord Bot API. This is the module most users will want when making bots.

### [Rest](./rest/README.md)
The `rest` module provides a low-level HTTP client specifically for Discord which properly handles Discord's [ratelimiting system](https://discordapp.com/developers/docs/topics/rate-limits).

### [Gateway](./gateway/README.md)
The `gateway` module provides a low-level WebSocket client for interacting with the [Discord Gateway](https://discordapp.com/developers/docs/topics/gateway).

### [Voice](./voice/README.md)
The `voice` module provides a client to manipulate audio through [Voice Connections](https://discordapp.com/developers/docs/topics/voice-connections).

### [Command](./command/README.md)
The `command` module provides a set of low-level tools for dealing with bot commands. It can be used on its own or easily serve as a basis of interoperability for higher-level command libraries. 

### [Common](./common/README.md)
The `common` module contains base utilities and models useful for other modules.

## Stores
Discord4J's mechanism for storing information received on the gateway is completely pluggable. This allows both the ability to customize what is stored and how. The [Stores](https://github.com/Discord4J/Stores) repository contains some pre-made implementations as well as the API for making your own.

## Development builds
Please follow our instructions at [Using Jitpack](https://github.com/Discord4J/Discord4J/wiki/Using-Jitpack)

## Useful Links
* [Discord4J Site](https://new.discord4j.com)
* [Discord4J Wiki](https://github.com/Discord4J/Discord4J/wiki)
* [Javadoc](http://javadoc.io/doc/com.discord4j/discord4j-core/)
* [Reactor 3 Reference Guide](http://projectreactor.io/docs/core/release/reference/)
