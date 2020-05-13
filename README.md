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

* 🚀 **Reactive** - Discord4J follows the [reactive-streams](http://www.reactive-streams.org/) protocol to ensure Discord bots run smoothly and efficiently regardless of size.

* 📜 **Official** - Automatic rate limiting, automatic reconnection strategies, and consistent naming conventions are among the many features Discord4J offers to ensure your Discord bots run up to Discord's specifications and to provide the least amount of surprises when interacting with our library.

* 🛠️ **Modular** - Discord4J breaks itself into modules to allow advanced users to interact with our API at lower levels to build minimal and fast runtimes or even add their own abstractions.

* ⚔️ **Powerful** - Discord4J can be used to develop any bot, big or small. We offer many tools for developing large-scale bots from [custom distribution frameworks](https://github.com/Discord4J/connect), [off-heap caching](https://github.com/Discord4J/Stores/tree/master/redis), and its interaction with Reactor allows complete integration with frameworks such as Spring and Micronaut.

* 🏫 **Community** - We pride ourselves on our inclusive community and are willing to help whenever challenges arise; or if you just want to chat! We offer help ranging from Discord4J specific problems, to general programming and web development help, and even Reactor-specific questions. Be sure to visit us on our [Discord server](https://discord.gg/NxGAeCY)!

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

## 🔀 Discord4J 3.0.x

Discord4J 3.1.x introduces performance and API enhancements, a plethora of new features, and dependency upgrades. A [Migration Guide](https://github.com/Discord4J/Discord4J/wiki/Migrating-from-v3.0-to-v3.1) is provided to aide users and ensure a smooth and readily available transition.

Discord4J 3.0.x installation instructions and examples can be found on the [3.0.x branch](https://github.com/Discord4J/Discord4J/tree/3.0.x).

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
public static final AudioPlayerManager PLAYER_MANAGER;

static {
  PLAYER_MANAGER = new DefaultAudioPlayerManager();
  // This is an optimization strategy that Discord4J can utilize to minimize allocations
  PLAYER_MANAGER.getConfiguration().setFrameBufferFactory(NonAllocatingAudioFrameBuffer::new);
  AudioSourceManagers.registerRemoteSources(PLAYER_MANAGER);
  AudioSourceManagers.registerLocalSource(PLAYER_MANAGER);
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

Typically, audio players will have queues or internal playlists for users to be able to automatically cycle through songs as they are finished or requested to be skipped over. We can manage this queue externally and pass it to other areas of our code to allow tracks to be viewed, queued, or skipped over by creating an `AudioTrackScheduler`.

```java
public final class AudioTrackScheduler extends AudioEventAdapter {

  private final List<AudioTrack> queue;
  private final AudioPlayer player;

  public AudioTrackScheduler(final AudioPlayer player) {
    // The queue may be modifed by different threads so guarantee memory safety
    // This does not, however, remove several race conditions currently present
    queue = Collections.synchronizedList(new LinkedList<>());
    this.player = player;
  }

  public List<AudioTrack> getQueue() {
    return queue;
  }

  public boolean play(final AudioTrack track) {
    return play(track, false);
  }

  public boolean play(final AudioTrack track, final boolean force) {
    final boolean playing = player.startTrack(track, !force);

    if (!playing) {
      queue.add(track);
    }

    return playing;
  }

  public boolean skip() {
    return !queue.isEmpty() && play(queue.remove(0), true);
  }

  @Override
  public void onTrackEnd(final AudioPlayer player, final AudioTrack track, final AudioTrackEndReason endReason) {
    // Advance the player if the track completed naturally (FINISHED) or if the track cannot play (LOAD_FAILED)
    if (endReason.mayStartNext) {
      skip();
    }
  }
}
```

Currently, Discord only allows 1 voice connection per server. Working within this limitation, it is logical to think of the 3 components we have worked with thus far (`AudioPlayer`, `LavaPlayerAudioProvider`, and `AudioTrackScheduler`) to be correlated to a specific `Guild`, naturally unique by some `Snowflake`. Logically, it makes sense to combine these objects into one, so that they can be put into a `Map` for easier retrieval when connecting to a voice channel or when working with commands.

```java
public final class GuildAudioManager {

  private static final Map<Snowflake, GuildAudioManager> MANAGERS = new ConcurrentHashMap<>();

  public static GuildAudioManager of(final Snowflake id) {
    return MANAGERS.computeIfAbsent(id, ignored -> new GuildAudioManager());
  }

  private final AudioPlayer player;
  private final AudioTrackScheduler scheduler;
  private final LavaPlayerAudioProvider provider;

  private GuildAudioManager() {
    player = PLAYER_MANAGER.createPlayer();
    scheduler = new AudioTrackScheduler(player);
    provider = new LavaPlayerAudioProvider(player);

    player.addListener(scheduler);
  }

  // getters
}
```

Finally, we need to connect to the voice channel. After connecting you are given a `VoiceConnection` object where you can utilize it later to disconnect from the voice channel by calling `VoiceConnection#disconnect`.

```java
final VoiceChannel channel = ...
final AudioProvider provider = GuildAudioManager.of(channel.getGuildId()).getProvider();
final VoiceConnection connection = channel.join(spec -> spec.setProvider(provider)).block();

// In the AudioLoadResultHandler, add AudioTrack instances to the AudioTrackScheduler (and send notifications to users)
PLAYER_MANAGER.loadItem("https://www.youtube.com/watch?v=dQw4w9WgXcQ", new AudioLoadResultHandler() { /* overrides */ })
```
