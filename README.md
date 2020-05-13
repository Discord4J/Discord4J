# Discord4J

<a href="https://discord4j.com"><img align="right" src="https://raw.githubusercontent.com/Discord4J/discord4j-web/master/public/logo.svg?sanitize=true" width=27%></a>

[![Support Server Invite](https://img.shields.io/discord/208023865127862272.svg?color=7289da&label=Discord4J&logo=discord&style=flat-square)](https://discord.gg/NxGAeCY)
[![Maven Central](https://img.shields.io/maven-central/v/com.discord4j/discord4j-core/3.0.svg?style=flat-square)](https://search.maven.org/artifact/com.discord4j/discord4j-core)
[![Javadocs](https://www.javadoc.io/badge/com.discord4j/discord4j-core.svg?color=blue&style=flat-square)](https://www.javadoc.io/doc/com.discord4j/discord4j-core)
[![CircleCI branch](https://img.shields.io/circleci/project/github/Discord4J/Discord4J/master.svg?label=circleci&logo=circleci&style=flat-square)](https://circleci.com/gh/Discord4J/Discord4J/tree/master)

Discord4J is a fast, powerful, unopinionated, reactive library to enable quick and easy development of Discord bots for Java, Kotlin, and other JVM languages using the official [Discord Bot API](https://discord.com/developers/docs/intro).

## 🏃 Quick Example

In this example, whenever an user sends a `!ping` message the bot will immediately respond with `Pong!`.

```java
public final class ExampleBot {

  public static void main(final String[] args) {
    final String token = args[0];
    final DiscordClient client = DiscordClient.create(token);
    final GatewayDiscordClient gateway = client.login().block();

    gateway.on(MessageCreateEvent.class).subscribe(event -> {
      final Message message = event.getMessage();
      if ("!ping".equals(message.getContent())) {
        final MessageChannel channel = message.getChannel().block();
        channel.createMessage("Pong!").block();
      }
    });

    gateway.onDisconnect().block();
  }
}
```

## 💎 Benefits

when im not tired

## 📦 Installation

* [Creating a new Gradle project with IntelliJ](https://www.jetbrains.com/help/idea/getting-started-with-gradle.html) *(recommended)*
* [Creating a new Maven project with IntelliJ](https://www.jetbrains.com/help/idea/maven-support.html)
* [Creating a new Gradle project with Eclipse](https://www.vogella.com/tutorials/EclipseGradle/article.html#creating-gradle-projects)
* [Creating a new Maven project with Eclipse](https://www.vogella.com/tutorials/EclipseMaven/article.html#exercise-create-a-new-maven-enabled-project-via-eclipse)

### Gradle
```groovy
repositories {
  mavenCentral()
}

dependencies {
  implementation 'com.discord4j:discord4j-core:3.1.0.M2'
}
```

### Gradle Kotlin DSL
```kotlin
repositories {
  mavenCentral()
}

dependencies {
  implementation("com.discord4j:discord4j-core:3.1.0.M2")
}
```

### Maven
```xml
<dependencies>
  <dependency>
    <groupId>com.discord4j</groupId>
    <artifactId>discord4j-core</artifactId>
    <version>3.1.0.M2</version>
  </dependency>
</dependencies>
```

### SBT
```scala
libraryDependencies ++= Seq(
  "com.discord4j" % "discord4j-core" % "3.1.0.M2"
)
```

## ⚛️ Reactive

Discord4J uses [Project Reactor](https://projectreactor.io/) as the foundation for our asynchronous framework. Reactor provides a simple yet extremely powerful API that enables users to reduce resources and increase performance.

```java
public final class ExampleBot {

  public static void main(final String[] args) {
    final String token = args[0];
    final DiscordClient client = DiscordClient.create(token);

    client.login().flatMapMany(gateway -> gateway.on(MessageCreateEvent.class))
      .map(MessageCreateEvent::getMessage)
      .filter(message -> "!ping".equals(message.getContent()))
      .flatMap(Message::getChannel)
      .flatMap(channel -> channel.createMessage("Pong!"))
      .blockLast();
  }
}
```

Discord4J also provides several methods to aide in better reactive chain compositions, such as `GatewayDiscordClient#withGateway` and `EventDispatcher#on` with an [error handling](https://github.com/Discord4J/Discord4J/wiki/Error-Handling) overload.

```java
final String token = args[0];
final DiscordClient client = DiscordClient.create(token);

client.withGateway(gateway -> {
  final Publisher<?> pingPong = gateway.on(MessageCreateEvent.class, event ->
    Mono.just(event.getMessage())
      .filter(message -> "!ping".equals(message.getContent()))
      .flatMap(Message::getChannel)
      .flatMap(channel -> channel.createMessage("Pong!")));
            
    final Publisher<?> onDisconnect = gateway.onDisconnect()
      .doOnTerminate(() -> System.out.println("Disconnected!"));

    return Mono.when(pingPong, onDisconnect);
  }).block();
```

## 🧵 Kotlin

By utilizing Reactor, Discord4J has native integration with [Kotlin coroutines](https://kotlinlang.org/docs/reference/coroutines-overview.html) when paired with the [kotlinx-coroutines-reactor](https://github.com/Kotlin/kotlinx.coroutines/tree/master/reactive/kotlinx-coroutines-reactor) library.

```kotlin
val token = args[0]
val client = DiscordClient.create(token)

client.withGateway {
  mono {
    it.on(MessageCreateEvent::class.java)
      .asFlow()
      .collect {
        val message = it.message
        if (message.content == "!ping") {
          val channel = message.channel.awaitSingle()
          channel.createMessage("Pong!").awaitSingle()
        }
      }
  }
}
```

## 🎵 Voice and Music

Discord4J provides full support for voice connections and the ability to send audio to other users connected to the same channel. Discord4J can accept any [Opus](https://opus-codec.org/) audio source with [LavaPlayer](https://github.com/sedmelluq/lavaplayer) being the preferred solution for downloading and encoding audio from YouTube, SoundCloud, and other providers.

To get started, you will first need to instantiate and configure an, conventionally global, `AudioPlayerManager`.

```java
public final static AudioPlayerManager playerManager;

static {
  playerManager = new DefaultAudioPlayerManager();
  // This is an optimization strategy that Discord4J can utilize to minimize allocations
  playerManager.getConfiguration().setFrameBufferFactory(NonAllocatingAudioFrameBuffer::new);
  AudioSourceManagers.registerRemoteSources(playerManager);
}
```

Next, we need to allow Discord4J to read from an `AudioPlayer` to an `AudioProvider`.

```java
public final class LavaPlayerAudioProvider extends AudioProvider {

  private final AudioPlayer player;
  private final MutableAudioFrame frame;

  public LavaPlayerAudioProvider(final AudioPlayer player) {
    // Allocate a ByteBuffer for Discord4J's AudioProvider to hold audio data for Discord
    super(ByteBuffer.allocate(StandardAudioDataFormats.DISCORD_OPUS.maximumChunkSize()));
    // Set LavaPlayer's AudioFrame to use the same buffer as Discord4J's
    frame = new MutableAudioFrame();
    frame.setBuffer(getBuffer());
    this.player = player;
  }

  @Override
  public boolean provide() {
    // AudioPlayer writes audio data to the AudioFrame
    final boolean didProvide = player.provide(frame);

    if (didProvide) {
      getBuffer().flip();
    }

    return didProvide;
  }
}
```

too tired will finish later

## 🔀 Discord4J 3.0.x

Discord4J 3.1.x introduces performance and API enhancements, a plethora of new features, and dependency upgrades. A [Migration Guide](https://github.com/Discord4J/Discord4J/wiki/Migrating-from-v3.0-to-v3.1) is provided to aide users and ensure a smooth and readily available transition.

Discord4J 3.0.x installation instructions and examples can be found on the [3.0.x branch](https://github.com/Discord4J/Discord4J/tree/3.0.x).
