# Discord4J Core
The `core` module combines the other modules to form high-level abstractions for the entire Discord Bot API. This is the module most users will want when making bots.

The main features of this module include the high-level `DiscordClient` and representations of Discord's entities. These wrap the behavior of the [rest](../rest/README.md) and [gateway](../gateway/README.md) to receive data and interact with Discord as well as the [store](../store/README.md) module to optionally and efficiently store this data.

## Installation
Just replace `@VERSION@` with the latest given by ![](https://img.shields.io/maven-central/v/com.discord4j/discord4j-core.svg?style=flat-square)
### Gradle
```groovy
repositories {
  mavenCentral()
}

dependencies {
  implementation 'com.discord4j:discord4j-core:@VERSION@'
}
```
### Maven
```xml
<dependencies>
  <dependency>
    <groupId>com.discord4j.discord4j</groupId>
    <artifactId>discord4j-core</artifactId>
    <version>@VERSION@</version>
  </dependency>
</dependencies>
```

### SBT
```scala
libraryDependencies ++= Seq(
  "com.discord4j" % "discord4j-core" % "@VERSION@"
)
```

## Development builds
Please follow our instructions at [Using Jitpack](https://github.com/Discord4J/Discord4J/wiki/Using-Jitpack)

## Example Usage
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
