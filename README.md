# Discord4J

<a href="https://discord4j.com"><img align="right" src="https://raw.githubusercontent.com/Discord4J/discord4j-web/master/public/logo.svg?sanitize=true" width=27%></a>

[![Support Server Invite](https://img.shields.io/discord/208023865127862272.svg?color=7289da&label=Discord4J&logo=discord&style=flat-square)](https://discord.gg/NxGAeCY)
[![Maven Central](https://img.shields.io/maven-central/v/com.discord4j/discord4j-core/3.0.svg?style=flat-square)](https://search.maven.org/artifact/com.discord4j/discord4j-core)
[![Javadocs](https://www.javadoc.io/badge/com.discord4j/discord4j-core.svg?color=blue&style=flat-square)](https://www.javadoc.io/doc/com.discord4j/discord4j-core)
[![CircleCI branch](https://img.shields.io/circleci/project/github/Discord4J/Discord4J/master.svg?label=circleci&logo=circleci&style=flat-square)](https://circleci.com/gh/Discord4J/Discord4J/tree/master)


A fast, reactive Java wrapper for the official [Discord Bot API](https://discord.com/developers/docs/intro).

Built with [Reactor](https://projectreactor.io/), [Netty](https://netty.io/), and a focus on flexibility, Discord4J provides an effective, non-blocking interface for creating Discord bots. The [reactive](https://www.reactivemanifesto.org/) and asynchronous nature of the library allows for scalability through backpressure handling and the efficient use of resources. Its [modularized](#modules) structure gives the user the ability to tailor their experience to different levels of abstraction and pick the right tools for the job.

## Installation

**Current stable releases** are from [`3.0.x`](https://github.com/Discord4J/Discord4J/tree/3.0.x). Check the [branch]((https://github.com/Discord4J/Discord4J/tree/3.0.x)) for instructions.

Instructions to use our latest pre-releases from `master` branch (3.1.x):

##### Gradle

```groovy
repositories {
  mavenCentral()
}

dependencies {
  implementation 'com.discord4j:discord4j-core:3.1.0.RC1'
}
```

##### Maven

```xml
<dependencies>
  <dependency>
    <groupId>com.discord4j</groupId>
    <artifactId>discord4j-core</artifactId>
    <version>3.1.0.RC1</version>
  </dependency>
</dependencies>
```

##### SBT

```scala
libraryDependencies ++= Seq(
  "com.discord4j" % "discord4j-core" % "3.1.0.RC1"
)
```

### Dependencies

- [Reactor Core](https://github.com/reactor/reactor-core) 3.3
- [Reactor Netty](https://github.com/reactor/reactor-netty) 0.9
- [Jackson Databind](https://github.com/FasterXML/jackson-databind) 2.11

## Quick Example

Coming from v3.0.x? Check our [Migration Guide](https://github.com/Discord4J/Discord4J/wiki/Migrating-from-v3.0-to-v3.1) (work in progress)

##### Reactive

```java
DiscordClient.create(System.getenv("token"))
        .withGateway(client -> {
            client.getEventDispatcher().on(ReadyEvent.class)
                    .subscribe(ready -> System.out.println("Logged in as " + ready.getSelf().getUsername()));

            client.getEventDispatcher().on(MessageCreateEvent.class)
                    .map(MessageCreateEvent::getMessage)
                    .filter(msg -> msg.getContent().equals("!ping"))
                    .flatMap(Message::getChannel)
                    .flatMap(channel -> channel.createMessage("Pong!"))
                    .subscribe();

            return client.onDisconnect();
        })
        .block();
```

##### Blocking

```java
DiscordClient.create(System.getenv("token"))
        .withGateway(client -> {
            client.on(ReadyEvent.class)
                    .subscribe(ready -> System.out.println("Logged in as " + ready.getSelf().getUsername()));

            client.on(MessageCreateEvent.class)
                    .subscribe(event -> {
                        Message message = event.getMessage();
                        if (message.getContent().equals("!ping")) {
                            message.getChannel().block().createMessage("Pong!").block();
                        }
                    });

            return client.onDisconnect();
        })
        .block();
```

Check out more examples [here](https://github.com/Discord4J/Discord4J/tree/master/core/src/test/java/discord4j/core)

## Modules
Discord4J is highly oriented towards customizability. To achieve this, the project is divided into several "modules" which can be used separately depending on your use case.

### [Core](./core/README.md)
The `core` module combines the other modules to form high-level abstractions for the entire Discord Bot API. This is the module most users will want when making bots.

### [Rest](./rest/README.md)
The `rest` module provides a low-level HTTP client specifically for Discord which properly handles Discord's [ratelimiting system](https://discord.com/developers/docs/topics/rate-limits).

### [Gateway](./gateway/README.md)
The `gateway` module provides a low-level WebSocket client for interacting with the [Discord Gateway](https://discord.com/developers/docs/topics/gateway).

### [Voice](./voice/README.md)
The `voice` module provides a client to manipulate audio through [Voice Connections](https://discord.com/developers/docs/topics/voice-connections).

### [Common](./common/README.md)
The `common` module contains base utilities and models useful for other modules.

## Stores
Discord4J's mechanism for storing information received on the gateway is completely pluggable. This allows both the ability to customize what is stored and how. The [Stores](https://github.com/Discord4J/Stores) repository contains some pre-made implementations as well as the API for making your own.

## Useful Links
* [Discord4J Site](https://discord4j.com)
* [Discord4J Wiki](https://github.com/Discord4J/Discord4J/wiki)
* [Javadoc](http://javadoc.io/doc/com.discord4j/discord4j-core/)
* [Reactor 3 Reference Guide](http://projectreactor.io/docs/core/release/reference/)

## Snapshots

Development builds can be obtained from Sonatype Snapshots repository or [Jitpack](https://github.com/Discord4J/Discord4J/wiki/Using-Jitpack).

Make sure you have the appropriate repositories, using Gradle:

##### Gradle

```groovy
repositories {
  maven { url 'https://oss.sonatype.org/content/repositories/snapshots' }
  mavenCentral()
}

dependencies {
  implementation 'com.discord4j:discord4j-core:3.1.0-SNAPSHOT'
}
```

##### Maven

```xml
<repositories>
    <repository>
        <id>oss.sonatype.org-snapshot</id>
        <url>http://oss.sonatype.org/content/repositories/snapshots</url>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.discord4j</groupId>
        <artifactId>discord4j-core</artifactId>
        <version>3.1.0-SNAPSHOT</version>
    </dependency>
</dependencies>
```
