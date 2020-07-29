## Introduction
Discord4J v3 is a completely different programming paradigm compared to v2. Rather than it being focused around synchronous, blocking invocations; everything is handled in an asynchronous, reactive context. The API has also been completely refactored, allowing D4J to provide a much cleaner, richer, consistent, and flexible approach to bot development.

## Blocking
The core of Discord4J's design is centered around [reactive programming](Reactive-(Reactor)-Tutorial.md), using Reactor as its implementation. This is primarily focused around 2 classes, `Mono` and `Flux`. While `Mono` and `Flux` are designed for asynchronous computations, they do offer synchronous conversions for more traditional imperative programming that will be familiar to v2 developers.

###### ⚠️ Warning ⚠️
> Blocking completely eliminates any and all benefits of reactive programming. We ***highly*** recommend that you learn more about reactive programming and eventually convert your code to be more reactive after the initial migration for better performance and scalability.

Any method that returns a `Mono` or `Flux` must be "subscribed" to in order for an action to be performed. This is vastly different compared to v2, where simply invoking the method *instantly* caused the method to execute. To mimic that behavior, we can simply call `Mono#block`:

```java
TextChannel channel = (TextChannel) discordClient.getChannelById(Snowflake.of(1234567890L)).block();
channel.createMessage("Hello World").block();
```

`Flux` can be converted to a `Mono` using `Flux#collectList`.

```java
List<Role> roles = guild.getRoles().collectList().block();
```

### JDA Developers
`Mono` is a significantly more powerful version of `RestAction`. It provides both an analog to `queue` and `complete` (`subscribe` and `block`) while additionally providing more operations for easier and more generic handling of data and actions both synchronously and asynchronously.

### Javacord Developers
`Mono` is a significantly more powerful version of `CompletableFuture`. It provides a more concise, standard, and easier manipulation of data and actions asynchronously compared to `CompletableFuture`'s copious amounts of `apply` and `handle` methods and still provides an analog to `get` (or `await`) via `Mono#block`.  In fact, a `Mono` can be converted [to](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Mono.html#toFuture--) and [from](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Mono.html#fromFuture-java.util.concurrent.CompletableFuture-) a `CompletableFuture` natively.

## EventDispatcher and IListener
`EventDispatcher` has been reworked and `IListener` (and consequently `@EventSubscriber`) has been completely removed in v3. To "listen" for an event, simply call `EventDispatcher#on` and *subscribe* for its contents:

```java
eventDispatcher.on(MessageCreateEvent.class).subscribe(event -> /* do stuff */);
```

To replicate `IListener`'s functionality you may use the following example:

```java
interface EventListener<T extends Event> {
    Class<T> getEventType();
    void execute(T event);
}

...

EventListener<MessageCreateEvent> listener = ...
eventDispatcher.on(listener.getEventType()).subscribe(listener::execute);
```

## ReadyEvent
`ReadyEvent` in v3 now represents Discord's [ReadyEvent](https://discordapp.com/developers/docs/topics/gateway#ready), which is sent before any `GuildCreateEvent`. This is different compared to v2 where `ReadyEvent` was defined when the bot was "ready", meaning all guilds have been received. In exchange, however, v3 does not require the bot to be "ready" to execute any actions (such as sending a message). This, consequently, also means v3 does not require the bot to be *logged in* to perform actions to Discord.

To mimic v2's `ReadyEvent`, i.e. know when all guilds have been received, you may use the following example:

```java
discordClient.getEventDispatcher().on(ReadyEvent.class) // Listen for ReadyEvent(s)
    .map(event -> event.getGuilds().size()) // Get how many guilds the bot is in
    .flatMap(size -> client.getEventDispatcher()
        .on(GuildCreateEvent.class) // Listen for GuildCreateEvent(s)
        .take(size) // Take only the first `size` GuildCreateEvent(s) to be received
        .collectList()) // Take all received GuildCreateEvents and make it a List
    .subscribe(events -> /* All guilds have been received, client is fully connected */);
```

## FAQ
#### What is wrong with v2?
Everything. Its problems stem from being the oldest of the 3 major libraries (written before a time the bot API existed) and its developer(s) having inadequate knowledge of Java conventions/practices and lacking OOP concepts.

1. v2 is a completely blocking API. This means threads must "wait" for actions to be completed before continuing. This wastes a tremendous amount of resources at scale as threads could be accomplishing various other tasks as they "wait" for previous tasks to finish. As noted by [this chart](https://i.imgur.com/dioE0Fh.png), even wasting a millisecond, in relative terms, is a huge waste of time for a computer and a typical Discord request is about 50 milliseconds. v3, thanks to Reactor, can maximize the usage of this wasted computing power to utilize less resources to accomplish more tasks.

   Of course, you do not have to utilize Reactor's asynchronous features. As previously discussed, blocking with Reactor can be easily accomplished to achieve the previous paradigm.

2. Channel hierarchy is both wrong and inconsistent. `IChannel` can be represented either by a guild's text channel or a privately messaged channel; meaning methods like `IChannel#getGuild` make no fundamental sense if the type of the channel is private. Should the method return null, or throw an exception? This ambiguity is made worse by the fact `IVoiceChannel` *extends* `IChannel`; meaning `IVoiceChannel` conceptually represents a guild's text channel, a privately messaged channel, *and* a guild voice channel! Most methods in `IVoiceChannel` throw an exception as they make no sense in the context of an actual voice channel (you cannot send a message in a voice channel, for instance). Additionally, categories *are* channels, but in v2 they are not represented this way via `ICategory`. v3 fixes all these issues with a much better entity hierarchy structure.

3. `RequestBuffer`/`RequestBuilder` were workarounds for rate limiting when it was introduced (yes, v2 was built before rate limiting was a concept for Discord). This makes their usage cumbersome as they *should* be applied to *every* possible request to Discord. This makes knowing when to use them entirely unclear (*which* methods, exactly, should either be applied to?), and its existence not balancing its requirement for bot development as a lot of users do not realize these two features exist. v3 fixes this as rate limits will be automatically handled and requests will be executed in order.

4. The MessageHistory API is confusing as its construction is spread out across *15* different methods with unexpected, unorthodox, and/or confusing naming and behaviors. Additionally, since `MessageHistory` is a `List`, *all* messages must be obtained before manipulating them; meaning for very large message histories, it is very likely to reach an OutOfMemoryError when attempting to obtain a history of these channels. v3, in contrast, only has 2 methods to obtain a "message history", with all of the functionality of v2's message history being applicable and more. Additionally, because of Reactor, messages can be retrieved on-demand so not all messages have to be loaded in memory before utilizing them for some purpose (like bulk-delete).

5. v2 is riddled with inconsistencies across its API. Some methods return `null`, others throw an exception, while others return `Optional`. v3 has been heavily focused on staying consistent across its entire API to prevent any unexpected behaviors or lopsided functionality. If something can be "absent", it'll return `Optional`. If it can make a request to Discord, it returns either a `Mono` or `Flux`. There are no surprises on what a method may attempt to accomplish or inconsistencies with handling specific cases.

6. Manipulating entities in v2 is both inefficient and cumbersome. Most entities when being created or edited can set multiple properties at once. For instance, when you create a channel you can set the name, type, position, permission overwrites, etc. all in one request, however, in v2 this is impossible. In order to create/edit with multiple properties you must call individual methods one at a time which makes an entire request to Discord on each and every single invocation. This is tremendously wasteful and quickly makes your bot approach a rate limit. v3 fixes this by utilizing [Specs](Specs.md).

7. While it is "possible" to disable the cache in v2, it instantly causes a crash on startup when attempted. v3 was designed with caches being disabled in mind, allowing very lightweight configurations if desired. We have tested v3 running on some of [Tatsumaki](https://tatsumaki.xyz/)'s shards, and v3 was able to stay under 10MB of RAM usage. v3's Store API is also far more flexible, allowing other configurations such as off-heap caching to be possible.

8. v2 is completely mutable which is susceptible to many race conditions that are incredibly hard to replicate, track down, and/or fix. v3 attempts to be as immutable as possible which has [numerous benefits](https://www.ibm.com/developerworks/library/j-ft4/index.html) for us as a library and you as a user.

9. v2 follows some unusual conventions. `I` prefixes for interfaces (which is a C# convention, but not a Java one), as well as a questionable package hierarchy structure (most events are under `impl`, for example). v3's follows proper Java industry conventions and a package hierarchy that makes intuitive sense.

#### Where is `RequestBuffer`/`RequestBuilder`?
`RequestBuffer` and `RequestBuilder` have been completely removed in v3. By default, v3 will execute requests in order and handle rate limits.

#### Where is `MessageHistory`?
`MessageHistory` has been completely removed in v3. A "message history" can be obtained by calling either [MessageChannel#getMessagesBefore](https://jitpack.io/com/discord4j/discord4j/discord4j-core/v3-SNAPSHOT/javadoc/discord4j/core/object/entity/MessageChannel.html#getMessagesBefore-discord4j.core.object.util.Snowflake-) or [MessageChannel#getMessagesAfter](https://jitpack.io/com/discord4j/discord4j/discord4j-core/v3-SNAPSHOT/javadoc/discord4j/core/object/entity/MessageChannel.html#getMessagesAfter-discord4j.core.object.util.Snowflake-).

## Migration steps

Important: This section focuses on Discord4J v3.1 rather than v3.0.

### Client building

1. The main utility class `Discord4J` was removed. If you used `Discord4J::enableJettyLogging` you should read up on [Logging](Logging.md).
1. `DiscordException` is not used anymore, and you should attempt to migrate towards reactive [Error handling](Error-Handling.md) or if you decide to block, catch `RuntimeException` or `ClientException`.
1. If your login flow consisted in obtaining an `IDiscordClient` instance, you should now either expect `GatewayDiscordClient` or `DiscordClient`
1. `ClientBuilder` is now replaced with `DiscordClientBuilder`. Several options don't exist anymore or have replacements elsewhere. It comes with default options to allow a monolithic multi-shard bot to function. You can start with `DiscordClient::create` or `DiscordClient::builder`.
1. `ClientBuilder::setDaemon` does not have an equivalent as it uses Reactor threading. Check the [Threading](Threading.md) page for more information.
1. `ClientBuilder::withPingTimeout` is now called `setMaxMissedHeartbeatAck` and can be set at `DiscordClient::gateway`.
1. `ClientBuilder::setMaxReconnectAttempts` now live under `ReconnectOptions` which can be set at `DiscordClient::gateway`. See [v3.1 Migration Guide](Migrating-from-v3.0-to-v3.1)
1. Shard options like `ClientBuilder::withShards`, `::setShard` and `::withRecommendedShardCount` are present under `DiscordClient::gateway`. See [v3.1 Migration Guide](Migrating-from-v3.0-to-v3.1.md)
1. `ClientBuilder` cache-related options like `setMaxMessageCacheCount` and `setCacheProvider` now live under the Stores abstraction and can be replaced using a combination of `MappingStoreService` and `CaffeineStoreService`. These options are set under `DiscordClient::gateway`. See also [Stores-Caffeine](https://github.com/Discord4J/Stores/tree/master/caffeine)
1. `ClientBuilder::registerListener` variants are moved to `GatewayDiscordClient::on` method, or `GatewayDiscordClient::getEventDispatcher`.
1. `ClientBuilder::set5xxRetryCount` is now abstracted to `DiscordClientBuilder::onClientResponse` where you can set a custom retrying policy. Discord4J retries most 5xx errors by default.
1. Event processing options now live under `EventDispatcher` abstraction and can be replaced at `DiscordClient::gateway`, then `GatewayBootstrap::setEventDispatcher`. See `EventDispatcher` class for some built-in factories.
1. Setting an initial presence/status is done at `DiscordClient::gateway` then `GatewayBootstrap::setInitialPresence`.
1. Calling `DiscordClient::login` directly uses all defaults for Gateway. To customize go through `DiscordClient::gateway`, customize the given `GatewayBootstrap` and then call `GatewayBootstrap::login`.

```java
// v2.10.x
IDiscordClient client = new ClientBuilder()
    .withToken(bot.getToken())
    .setDaemon(bot.isDaemon())
    .withPingTimeout(bot.getMaxMissedPings())
    .setMaxReconnectAttempts(bot.getMaxReconnectAttempts())
    .login();
// v3.1.x
GatewayDiscordClient client = DiscordClient.create(bot.getToken())
    .login()
    .block();
```

### Event dispatching
1. Use `GatewayDiscordClient::on` to attach a subscriber to receive all events for that type.
1. If you relied heavily upon `@EventSubscriber` from v2 you can create an [`EventSubscriberAdapter`](https://gist.github.com/quanticc/57ca056ace9c557dc2303ef1e6549253) class yourself and use it in the following way:
```java
GatewayDiscordClient client = DiscordClient.create(token).login().block();
EventSubscriberAdapter adapter = new EventSubscriberAdapter() {

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        log.info("> {}", event.getMessage().getContent().orElse(""));
    }
};
client.on(Event.class)
    .as(adapter::listener)
    .subscribe();
    
gateway.onDisconnect().block(); // we should block until it disconnects
```
Since `@EventSubscriber` was removed you can use that pattern, overriding the methods you wish to get notified. There is also a reactive alternative [`ReactiveEventAdapter`](https://gist.github.com/quanticc/b9c04f16ffdd925b845e3d4851fb10bd) you can use in a similar fashion.
1. If you previously used `IListener<E>` you can also migrate it to `EventSubscriberAdapter`
1. Events now live under the `discord4j.core.event` package.
1. `ReconnectSuccessEvent` -> `ReconnectEvent`
1. `ReconnectFailureEvent` -> `ReconnectFailEvent`
1. `DisconnectedEvent` has split into `ReconnectStartEvent` and `DisconnectEvent`
1. `UserBanEvent` -> `BanEvent`
1. `UserPardonEvent` -> `UnbanEvent`
1. `MessageReceivedEvent` -> `MessageCreateEvent`

### Event processing
1. In general, if an `Event` returns a `Mono` or `Flux`, it means it involves a request that might incur latency and therefore we use Reactor to properly route and schedule that action asynchronously. Such methods are safe to call `Mono::block` or `Flux::blockLast` upon to use the contained type in a blocking way.
1. All entities have different names now but the correlation should be easy to follow, for example: `IMessage` -> `Message`
1. Checking if a `MessageChannel` is private can be done through `instanceof PrivateChannel` or using `getType` after blocking, or `.ofType(PrivateChannel.class)` before blocking.
1. `DiscordClient::getOurUser` -> `GatewayDiscordClient::getSelf` and optionally block

### Working with permissions
Consider the following call:
```java
boolean hasPermission = message.getChannel().getModifiedPermissions(message.getAuthor())
                    .containsAll(EnumSet.of(Permission.MANAGE_MESSAGES));
```
To migrate this you should know:
1. To work with `PermissionSet` rather than `EnumSet<Permission>`. Build one using `PermissionSet::of`.
1. Since `Message::getChannel` returns a `Mono<MessageChannel>` and calls like this only make sense under Guild channels, you must ensure first that the actual type is `GuildChannel`: `message.getChannel().ofType(GuildChannel.class)`
1. If you block such a `Mono` and the underlying channel is not a guild one, it will return `null`. There is also `Mono::blockOptional` to get an `Optional`
1. After blocking you can then call `GuildChannel::getEffectivePermissions` using the `Snowflake` for a member or role
The resulting code should look like:
```java
Snowflake authorId = message.getAuthor()
        .map(User::getId)
        .orElseThrow(IllegalArgumentException::new);
boolean hasPermission = message.getChannel().ofType(GuildChannel.class)
        .flatMap(channel -> channel.getEffectivePermissions(authorId))
        .map(set -> set.containsAll(PermissionSet.of(Permission.MANAGE_MESSAGES)))
        .blockOptional()
        .orElse(false);
```
