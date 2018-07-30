# Discord4J Core
The `core` module combines the other modules to form high-level abstractions for the entire Discord Bot API. This is the module most users will want when making bots.

The main features of this module include the high-level `DiscordClient` and representations of Discord's entities. These wrap the behavior of the [rest](../rest/README.md) and [gateway](../gateway/README.md) to receive data and interact with Discord as well as the [store](../store/README.md) module to optionally and efficiently store this data.

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

