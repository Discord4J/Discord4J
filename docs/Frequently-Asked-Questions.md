A non-comprehensive collection of common pitfalls/questions we see a lot when using Discord4J.

# Snowflakes
"What is a snowflake? How do I create one from an ID I got from Discord?"

Snowflakes are the format Discord [uses](https://discord.com/developers/docs/reference#snowflakes) for their IDs. Discord4J encodes this in the type located at `discord4j.common.util.Snowflake` in v3.1 or `discord4j.core.object.util.Snowflake` in v3.0 which you will see all over the library. This provides a way to convert back and forth between different representations of snowflakes and some utilities around them. 

To obtain a `Snowflake` from an ID you got from the Discord client, you can use one of its several static factory (`of()`) methods. The most convenient for this case is either `of(long)` or `of(String)`. There is no difference between these methods. For example, if my ID is `84766711735136256` I can do: `Snowflake.of("84766711735136256")` or `Snowflake.of(84766711735136256L)`. *Take note of the `L` at the end of the latter example.* This is a [long literal](https://docs.oracle.com/javase/tutorial/java/nutsandbolts/datatypes.html) and could be required to tell Java that the number is a `long`. 

# Getting entities
"I want to get a channel, role, guild, etc..."
## In response to an event
"...when handling an event."

If you have an `Event` and want to get an entity related to that event, more than likely there is a path to get to that info from the event. For example, `MessageCreateEvent` directly has `getGuild()` and the channel can be obtained through the related message like `getMessage().getChannel()`.
## Without an event
"...outside of an event handler."

If you don't have an `Event` or there isn't a way to get the entity you want from the event, you can use the fact that all entities have a unique ID associated to them called a `Snowflake`. You can [obtain](https://support.discordapp.com/hc/en-us/articles/206346498-Where-can-I-find-my-User-Server-Message-ID-) this ID in the Discord client and pass it to one of the many `getXById(Snowflake)` methods of `DiscordClient`.

# Channel types
"I can't send a message to a `Channel`, what gives?"

Discord4J uses a hierarchy of types to separate the behavior of all of the different types of channels in Discord. Consider the fact that a method for sending messages doesn't make much sense for a `VoiceChannel`. Here is an overview of this hierarchy (with some of the internal details removed): ![Discord4J Channel Type Hierarchy](https://cdn.discordapp.com/attachments/451125724766535710/583071759155068928/68747470733a2f2f63646e2e646973636f72646170702e636f6d2f6174746163686d656e74732f3231303933383535323536.png)

In general, Discord4J uses the least specific type of channel it can. For example, `Message#getChannel()` gets you a `MessageChannel` which could be any of `TextChannel`, `NewsChannel`, or `PrivateChannel`. So, what should you do if you *know* you have one of the more specific types? Cast! This can be done in Reactor with either the [`ofType(Class)`](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Mono.html#ofType-java.lang.Class-) or [`cast(Class)`](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Mono.html#cast-java.lang.Class-) operator. So, to get a channel which you *know* is a `TextChannel` you could do `client.getChannelById(id).cast(TextChannel.class)`.

# Reactor
"What the heck is a Mono?"

Discord4J uses a library called Reactor to facilitate reactive programming in Java. Both the concepts and the library itself can be intimidating at first. As this is a very broad topic, we have split off this documentation into other pages and defer to Reactor docs. Please see our [Reactive (Reactor) Tutorial](Reactive-(Reactor)-Tutorial.md) and the awesome [Reactor Reference Guide](https://projectreactor.io/docs/core/release/reference/).

## Do I use map or flatMap?

![Do I use map or flatMap](https://cdn.discordapp.com/attachments/582222617163989052/679491828671709225/hanvZpa.png)

# Building a plugin

## I'm getting java.lang.NoSuchMethodError: io.netty...

If you're trying to build a Minecraft plugin with Discord4J, you'll have a conflict with the netty version both provide. Discord4J uses newer netty methods that don't exist in Minecraft's version, therefore you must use a technique called "Shading" to make your plugin ship the latest netty.

This can be achieved through Maven or Gradle

## Maven

Use [maven-shade-plugin](https://rmannibucau.metawerx.net/post/mavens-shade-plugin-source-relocation).

```xml
<!-- pom.xml -->
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-shade-plugin</artifactId>
  <version>3.2.3</version>
  <executions>
    <execution>
      <phase>package</phase>
      <goals>
        <goal>shade</goal>
      </goals>
      <configuration>
        <relocations>
          <relocation>
            <pattern>io.netty</pattern>
            <shadedPattern>shaded.io.netty</shadedPattern>
          </relocation>
        </relocations>
      </configuration>
    </execution>
  </executions>
</plugin>
```

## Gradle

Use [gradle-shadow-plugin](https://imperceptiblethoughts.com/shadow/getting-started/).

```groovy
// build.gradle
plugins {
  id 'com.github.johnrengelman.shadow' version '5.2.0'
  id 'java'
}

// ...

// Relocating a Package
shadowJar {
   relocate 'io.netty', 'com.discord4j.shaded.io.netty'
}
```

If you have any issues regarding this process, feel free to get in touch with us in our [![Support Server Invite](https://img.shields.io/discord/208023865127862272.svg?color=7289da&label=Discord4J&logo=discord&style=flat-square)](https://discord.gg/NxGAeCY)