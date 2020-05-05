# Discord4J

<a href="https://discord4j.com"><img align="right" src="https://raw.githubusercontent.com/Discord4J/discord4j-web/master/public/logo.svg?sanitize=true" width=27%></a>

[![Support Server Invite](https://img.shields.io/discord/208023865127862272.svg?color=7289da&label=Discord4J&logo=discord&style=flat-square)](https://discord.gg/NxGAeCY)
[![Maven Central](https://img.shields.io/maven-central/v/com.discord4j/discord4j-core/3.0.svg?style=flat-square)](https://search.maven.org/artifact/com.discord4j/discord4j-core)
[![Javadocs](https://www.javadoc.io/badge/com.discord4j/discord4j-core.svg?color=blue&style=flat-square)](https://www.javadoc.io/doc/com.discord4j/discord4j-core)
[![CircleCI branch](https://img.shields.io/circleci/project/github/Discord4J/Discord4J/master.svg?label=circleci&logo=circleci&style=flat-square)](https://circleci.com/gh/Discord4J/Discord4J/tree/master)

A reactive, enterprise-grade Java wrapper for the official [Discord Bot API](https://discordapp.com/developers/docs/intro).

## 🗒️ Goal

The Discord4J team believe in providing enterprise-grade software without the enterprise-grade hassle. Our goal is to provide a library that is **scalable** with little to no effort for its end users. From basic ping-pong bots running on Raspberry Pis to fully populated Kubernetes clusters integrated with other services, Discord4J provides the tools neccessary to deliver a seamless Discord bot development experience.

* 🚀 **Speed** - Nobody likes a slow unresponsive bot, and nobody likes needlessly spending money on upgrading hardware. Hence, Discord4J utilizes [Reactor](https://projectreactor.io/) as the solution for creating asynchronous and non-blocking applications. Unlike custom asynchronous solutions, Reactor is [proven](https://technology.amis.nl/2020/04/10/spring-blocking-vs-non-blocking-r2dbc-vs-jdbc-and-webflux-vs-web-mvc/) and backed by the same developers as Spring. Thanks to Reactor, you can squeeze every bit of performance out of your hardware to deliver a responsive experience to your users as well as minimizing costs for hosting.

* 🔑 **Standards** - Thanks to Reactor, Discord4J has perfect integration with [Spring](https://spring.io/reactive) to provide a standardized development experience for more advanced bots. Naming conventions in Reactor follow those in [`Optional`](https://docs.oracle.com/javase/8/docs/api/java/util/Optional.html), [`Stream`](https://docs.oracle.com/javase/8/docs/api/java/util/stream/Stream.html), and various other programming languages, unlike `CompletableFuture` and other custom asynchronous solutions. This allows experienced developers to spend less time learning new things and more time developing.

* 🧰 **Options** - Reactor [provides](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Mono.html#blockOptional--) [many](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html#toStream--) [ways](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Mono.html#toFuture--) for converting between reactive and standard Java APIs. Additionally, Kotlin provides [support for Reactor in coroutines](https://github.com/Kotlin/kotlinx.coroutines/tree/master/reactive/kotlinx-coroutines-reactor) for traditional imperative programming running in an asynchronous context. These options provide developers many ways to interact with other APIs and services without getting in the way while still offering *choice* to unlock its powerful capabilities.

* 🎛️ **Flexibility** - On top of providing the full feature set of Discord Bot API development such as REST, Gateway, Caching, and Voice integration, Discord4J provides many knobs and dials to tweak the implementation that best fits your needs. While our defaults are sensible for the majority of users, rest easy knowing no matter how large your bot gets, Discord4J can provide many solutions to problems that arise with scaling. For example, we provide a [distributed bot framework](https://github.com/Discord4J/connect) for when load balancing becomes critical.

* 🌈 **Community** - Sometimes you need help or have a question. Or maybe you just want to talk to some people. We offer a [Discord server](https://discord.gg/NxGAeCY) for all these things, as well as general announcements and the ability to test your bot on a large server.

## 📦 Installation

Discord4J recommends, and requires, developers to use a package manager like any other standard development environment. For instructions on **current stable releases** refer to our [`3.0.x`](https://github.com/Discord4J/Discord4J/tree/3.0.x) branch.

### 💡 IDE Tutorials
* [Creating a new Gradle project with IntelliJ](https://www.jetbrains.com/help/idea/getting-started-with-gradle.html) *(recommended)*
* [Creating a new Maven project with IntelliJ](https://www.jetbrains.com/help/idea/maven-support.html)
* [Creating a new Gradle project with Eclipse](https://www.vogella.com/tutorials/EclipseGradle/article.html#creating-gradle-projects)
* [Creating a new Maven project with Eclipse](https://www.vogella.com/tutorials/EclipseMaven/article.html#exercise-create-a-new-maven-enabled-project-via-eclipse)

#### Gradle / Kotlin Gradle DSL
```kotlin
repositories {
  mavenCentral()
}

dependencies {
  implementation("com.discord4j:discord4j-core:3.1.0.M2")
}
```

#### Maven
```xml
<dependencies>
  <dependency>
    <groupId>com.discord4j</groupId>
    <artifactId>discord4j-core</artifactId>
    <version>3.1.0.M2</version>
  </dependency>
</dependencies>
```

#### SBT
```scala
libraryDependencies ++= Seq(
  "com.discord4j" % "discord4j-core" % "3.1.0.M2"
)
```

## 🏃 Quick Example
Coming from `3.0.x`? Check our [Migration Guide](https://github.com/Discord4J/Discord4J/wiki/Migrating-from-v3.0-to-v3.1) (🏗️ WIP)!

The Discord4J team understands developers come from a wide range of experience backgrounds and some of those backgrounds may find Reactor overwhelming or daunting. As a pillar of our goal, **options**, we wish to provide you, the developer, tools to develop in a manner best suited for you. To achieve that, all of our examples will come with 2 seperate codebases. The first will be ***blocking*** (▶️) which uses the traditional Java programming model and should be familiar to any developer that has worked with Java in the past. The second will be ***reactive*** (⏩) to help showcase the paradigm and provide a learning experience for those wishing to learn reactive programming.

#### ▶️ - Blocking
```java
public final class QuickExample {

    public static void main(final String[] args) {
        final String token = args[0]; // [1]
        final DiscordClient client = DiscordClient.create(token); // [2]
        final GatewayDiscordClient gateway = client.login().block(); // [3]

        // [4]
        gateway.getEventDispatcher().on(MessageCreateEvent.class)
            .subscribe(event -> {
                final Message message = event.getMessage();
                if (message.getContent().equals("!ping")) {
                    final MessageChannel channel = message.getChannel().block();
                    channel.createMessage("Pong!").block();
                }
            });

        gateway.onDisconnect().block(); // [5]
    }
}
```

**[1]** - It is best practice not to hardcode your tokens or any sensitive information. Provide them through program arguments, environment variables, files, or other *external* sources.

**[2]** - `DiscordClient` provides an entry point to functionality available to a bot that is not logged into Discord, also known as, the Gateway.

**[3]** - `GatewayDiscordClient` represents a bot logged into Discord.

**[4]** - `EventDispatcher` allows us to be notified when the bot receives specified events from Discord. In this case, we just want to listen for created messages, hence, a `MessageCreateEvent`.

**[5]** - To prevent the JVM from exiting early, we must keep a non-daemon thread active. One way to achieve this is to halt, or block, the thread until the bot disconnects. Discord4J automatically handles reconnections, so this will only trigger for explicit calls to `logout`.

One may note an excessive amount of calls to `block`. Reactor is a *lazy* paradigm, similiar to [`Stream`](https://docs.oracle.com/javase/8/docs/api/java/util/stream/Stream.html), and, like `Stream`, Reactor needs a *terminal* operator to start processing data. In Reactor terms, this means *subscribing*. `block` is a special kind of subscribe where the calling thread is halted, *block*ed, until the request is finished, which then extracts the value for immediate use.

This is almost identical to how [`CompletableFuture`](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/CompletableFuture.html) works with `join` or `get`.

#### ⏩ - Reactive
```java
public final class QuickExample {

    public static void main(final String[] args) {
        final String token = args[0]; // [1]
        final DiscordClient client = DiscordClient.create(token); // [2]

        client.login().flatMap(gateway -> { // [3]
            final Flux<?> pingPong = gateway.getEventDispatcher().on(MessageCreateEvent.class) // [4]
                .map(MessageCreateEvent::getMessage) // [5]
                .filter(message -> message.getContent().equals("!ping")) // [6]
                .flatMap(Message::getChannel) // [7]
                .flatMap(channel -> channel.createMessage("Pong!")); // [8]

            final Mono<Void> onDisconnect = gateway.onDisconnect(); // [9]
            return Mono.when(pingPong, onDisconnect); // [10]
        }).block(); // [11]
    }
}
```

**[1]**, **[2]** - Identical to **[1]** and **[2]** for the **blocking** example!

**[3]** - Similiar to **[3]** in the **blocking** example we will be receiving a `GatewayDiscordClient`, except *asynchronously*. Since we want to act onto `gateway` with other reactive components, `flatMap` is utilized. `flatMap` in Reactor, along with `map` and `filter`, follow similiar functionality as used in `Optional` and `Stream`. If you do not understand, we encourage you to do some research or even drop by our Discord and ask in `#reactor-help` (but please do some research first!).

**[4]** - Idential to **[4]** for the blocking example!

**[5]** - To asynchronously and reactively handle the event, we will be assembling a *chain*. The first step in this chain, is extracting a `Message` out of the `MessageCreateEvent`.

**[6]** - Next, we want to filter messages where the content equals `!ping`. In other words, we want to *discard* `Message` objects which ***do not*** equal `!ping`.

**[7]** - Next, we want to extract, asynchronously, a `MessageChannel` from the `Message` object. Remmeber, at this point, only `Message` objects where the content equals `!ping` are being processed.

**[8]** - Finally, asynchronously, send a message: `Pong!`. As noted in our **blocking** example, Reactor is *lazy*, this chain will not do anything until it is *subscribed*. Currently, we are just holding onto the chain as a variable named `pingPong`. It won't do anything yet, it simply represents a "chain" of actions!

**[9]** - In **[5]** in our **blocking** example, we discussed how `onDisconnect` will "block" until the bot is disconnected intentionally. In this example, you will note `block` is not called. Instead, we are merely holding onto a representation of something that will "finish" after the bot is disconnected intentionally. This will be useful in a later footnote.

**[10]** - Combine `pingPong` and `onDisconnect` to form a new chain! As noted in the Javadocs of [`when`](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Mono.html#when-org.reactivestreams.Publisher...-), it will only "complete", or "finish", until all the provided chains are complete. This means the `flatMap` from **[3]** will not complete until both the `pingPong` event is finished and after the bot disconnects intentionally.

**[11]** - Blocks for the same reason as noted in **[5]** in our **blocking** example. As noted in the step above, the `flatMap` won't be "done" until 2 conditions are met, which can practically mean indefinitely.

### 🤔 Takeway

As you can see, reactive programming is quite different and, depending on your familiararity with streaming paradigms, seemingly more difficult! However, reactive programming provides a *lot* of performance benefits over traditional programming and encourages better programming practices as a consequence of many factors. It can be a rewarding experience for those who wish to learn it and the code can be just as readable if not more so than "normal" programming paradigms. If you wish to learn more about Reactor, we encourage you to do some research! We have many resources pinned in our `#reactor-help` channel on Discord.

But we cannot stress enough how much everything here is *optional*. Reactor can be just as easy as traditional programming practices as we have shown in our **blocking** example. 🧰 **Options!**
