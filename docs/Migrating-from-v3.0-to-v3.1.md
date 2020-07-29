# Before you start

NOTE: Feel free to suggest changes or to point out things missing while you migrate. Thanks!

This document is meant to help you migrate your application to Discord4J v3.1 if you're already on v3.0. If not, please check our [guild](Migrating-from-v2.x-to-v3.1.md) first.

Discord4J is transitioning to a new architecture where users are able to work with the library REST capabilities separately from real-time Gateway operations in an easier way than before.

In addition, spawning Gateway connections is treated separately from `DiscordClient`, allowing users to have all joining shards publish events and entities to a single location, removing the need of using separate classes for handling multiple shards in a single process.

While this means that a single `EventDispatcher` and `StateHolder` are shared across all shards controlled by a single `GatewayDiscordClient`, you are in control of what shards are connected to it (`DiscordClient` + Gateway capabilities = `GatewayDiscordClient`). This enables you to more easily setup distributed architectures where certain actions are coordinated even across multiple JVMs.

For a more in-depth explanation of the new features and changes introduced check [What's new in v3.1](What's-new-in-v3.1.md) and [glossary](Glossary.md).

## Updating dependencies
Discord4J v3.1 depends on Reactor Dysprosium release train ([Reactor Core](https://github.com/reactor/reactor-core) 3.3.x and [Reactor Netty](https://github.com/reactor/reactor-netty) 0.9.x).

One important change in Reactor is the addition of the `Schedulers.boundedElastic()` scheduler that caps the number of threads. It should help for cases when you do blocking operations whilst keeping a limit on the amount of threads created.

### Gradle

```groovy
repositories {
  mavenCentral()
}

dependencies {
  implementation 'com.discord4j:discord4j-core:3.1.0'
}
```

### Maven

```xml
<dependencies>
    <dependency>
        <groupId>com.discord4j</groupId>
        <artifactId>discord4j-core</artifactId>
        <version>3.1.0</version>
    </dependency>
</dependencies>
```

# Quick migration

Most notable change from v3.0 to v3.1 is the behavior of the `login()` method. We feel that the method makes most sense if it returns a `Mono` that completes as login is completed, instead of logout, and to return a handle for the underlying gateway connections.

This change is needed to allow separation between a `DiscordClient` and the real-time Gateway, represented by a `GatewayDiscordClient`. To acquire one and perform actions with a bot, you need to login to the Gateway first.

Following is the quickstart example from v3.0 migrated to v3.1 as a starting point. Just move the code you had before `login()` into the `.withGateway(client -> ...)` block and add `return client.onDisconnect()`.

```java
DiscordClientBuilder.create(System.getenv("token"))
    .build()
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

As you could have noticed, another important change is the return type of the `Message::getContent()` method. Starting from v3.1 this method will return `String` instead of `Optional<String>`. This means that to directly migrate you now have to wrap `message.getContent()` like this: `Optional.of(message.getContent())`

If the IDE warns you when calling `subscribe`, it's because the sequences could be wired together instead of calling `.subscribe()` within a reactive pipeline. To remove them you can transform the code into:

```java
DiscordClientBuilder.create(System.getenv("token"))
        .build()
        .withGateway(client -> {
            Mono<Void> onReady = client.getEventDispatcher().on(ReadyEvent.class)
                    .doOnNext(ready -> System.out.println("Logged in as " + ready.getSelf().getUsername()))
                    .then();

            Mono<Void> ping = client.getEventDispatcher().on(MessageCreateEvent.class)
                    .map(MessageCreateEvent::getMessage)
                    .filter(msg -> msg.getContent().equals("!ping"))
                    .flatMap(Message::getChannel)
                    .flatMap(channel -> channel.createMessage("Pong!"))
                    .then();

            return Mono.when(onReady, ping);
        })
        .block();
```

Converting each event listener into a `Mono<Void>` allows us to wire both together and avoid calling `client.onDisconnect()`.

# Discord4J features

== Connecting to the Gateway
We added convenience methods to `DiscordClient`, namely static `DiscordClient.builder(token)` and `DiscordClient.create(token)` to quickly get started.

To connect using the default options:

```java
DiscordClient client = DiscordClient.create(System.getenv("token")); // <1>
GatewayDiscordClient gateway = client.login().block(); // <2>

gateway.on(ReadyEvent.class) // <3>
        .subscribe(ready -> System.out.println("Logged in as " + ready.getSelf().getUsername()));

gateway.onDisconnect().block(); // <4>
```

- <1> Shortcut to `new DiscordClientBuilder(token).build()`
- <2> Acquire a synchronous handle on gateway connections, can be used to register events, get cached entities, etc.
- <3> Shortcut to `gateway.getEventDispatcher().on(ReadyEvent.class)`
- <4> To keep the `main` thread alive, await until bot disconnects through `logout()`


You can also connect using a traditional `flatMap` style, but if you're going this route we recommend the new `withGateway` API:

```java
DiscordClient client = DiscordClient.create(System.getenv("token"));
client.withGateway(gateway -> {
    Flux<ReadyEvent> hello = gateway.on(ReadyEvent.class)
            .doOnNext(ready -> System.out.println("Logged in as " + ready.getSelf().getUsername()));

    Flux<MessageCreateEvent> messages = gateway.on(MessageCreateEvent.class)
            .doOnNext(event -> System.out.println("> " + event.getMessage().getContent()));

    return Mono.when(hello, messages); // <1>
}).block(); // <2>
```

- <1> Can use `Mono.when` to await for the completion of multiple sequences
- <2> Blocking here establishes the connections and waits until the bot logs out.

## Adding event listeners

Access to the `EventDispatcher` is through `GatewayDiscordClient`. Calling `#getEventDispatcher()` is optional as shortcut `.on(...)` methods have been added to it.

```java
// Alternative 1
gateway.on(MessageCreateEvent.class)
        .map(MessageCreateEvent::getMessage)
        .filter(msg -> Optional.of(msg.getContent()).map("!ping"::equals).orElse(false))
        .flatMap(Message::getChannel)
        .flatMap(channel -> channel.createMessage("Pong!")
                .onErrorResume(t -> Mono.empty())) // <1>
        .subscribe();

// Alternative 2
gateway.on(MessageCreateEvent.class,
        event -> Mono.just(event.getMessage())
                .filter(message -> Optional.of(message.getContent()).map("!ping"::equals).orElse(false))
                .flatMap(Message::getChannel)
                .flatMap(channel -> channel.createMessage("Pong!")))
        .subscribe(); // <2>
```

- <1> Needs error handling code along the chain
- <2> Error handling provided for you within the `event -> { ... }` block

## Customizing Gateway features

Discord4J v3.1 introduces a new architecture where a `GatewayBootstrap` creates a **shard group** that will share events and caches.

Most of the options you would previously set at the `DiscordClientBuilder` or `ShardingClientBuilder` level in v3.0, are now located at `GatewayBootstrap`. This is a builder to establish gateway connections from a `DiscordClient` by calling `gateway()`.

### Migrating from `setShardCount(n)` or `setShardIndex(i)`

Get a builder with `client.gateway()` and then call `setSharding(ShardingStrategy)`. Creating a `ShardingStrategy` can be done using the following factories:

* `ShardingStrategy.recommended()` will provide the recommended amount of shards and include all of them in the group
* `ShardingStrategy.fixed(n)` will use the given `shardCount` and include all shards `0..N` in the group
* `ShardingStrategy.single()` will use a simple `[0,1]` configuration, for small bots and also [distributed bot architectures](https://github.com/Discord4J/connect)

You can also customize the strategy using `ShardingStrategy.builder()` allowing you to configure:

* Total count of shards parameter through `count`
* Shards identified to the Gateway using `indices`
* Can be also combined with `filter` to connect a subset of shards

### Migrating from `setInitialPresence`
Similar to above, call `setInitialStatus` which now takes a `Function<ShardInfo, StatusUpdate>`. If you used `setInitialPresence(Presence.invisible())` you should now use `setInitialStatus(shard -> Presence.invisible())`

### Migrating from `setIdentifyOptions`
`IdentifyOptions` cannot be set directly now and you'll have to use a mix of `setSharding` and `setResumeOptions` depending on your use case.

### Migrating from `setStoreService(service)`
Get a builder with `client.gateway()` and then call `setStoreService(service)`. If you used `ShardingClientBuilder` before, Discord4J can automatically prepare your `StoreService` with shard invalidation capabilities.

### Migrating from `setEventProcessor` and `setEventScheduler`
A new interface `EventDispatcher` is added to more easily customize both options. There are a few built-in factories:

* `EventDispatcher.buffering()` that stores all events **until the first subscription**, then events are published to all subscribers as they are received. This is identical to the one used in v3.0.
* `EventDispatcher.withEarliestEvents(int)` keeps only the earliest events and the rest are dropped **until the first subscription**, then events are published to all subscribers as they are received.
* `EventDispatcher.withLatestEvents(int)` keeps only the latest events **until the first subscription**, then events are published to all subscribers as they are received. Initial events like `ReadyEvent` might be dropped.
* `EventDispatcher.replayingWithTimeout(Duration)` that buffers and replays all events up to `Duration` maximum age.
* `EventDispatcher.replayingWithSize(int)` that buffers and replays the latest events.

To customize the above options, get a builder with `client.gateway()` and then call `setEventDispatcher(...)`.

The current default is a `ReplayingEventDispatcher`, created using `ReplayingEventDispatcher.create()`. It can be customized through `ReplayingEventDispatcher.builder()` and it works in the following way:

* Buffers all events before a subscription exists, as long as they match a filter. By default, `GatewayLifecycleEvent` and `GuildCreateEvent` types. This can be customized through `replayEventFilter`.
* Early subscribers get all previously buffered events, until a given timeout. By default, 5 seconds after the first subscriber arrives and can be changed in `stopReplayingTrigger`.
* Late subscribers only get events as they are published, no replay capabilities until all subscribers are disposed.

### Migrating from `setGatewayClientFactory`
Use `login(Function)` overload after calling `client.gateway()`. Used to build distributed bot architectures.

### Migrating from `setRetryOptions`
Use `setReconnectOptions` after calling `client.gateway()`. By default Discord4J will always attempt to reconnect using an exponential backoff with jitter strategy.

### Migrating from `setGatewayObserver`
Use `setGatewayObserver` after calling `client.gateway()`.

### Migrating from `setIdentifyLimiter`
Discord4J v3.1 introduced a new API called `ShardCoordinator` which groups all options related to coordinating multiple shard identification. We supply `LocalShardCoordinator` by default and can be replaced by one that is capable of working with a distributed bot architecture.

### Migrating from `setVoiceConnectionScheduler`
Use `setVoiceReactorResources` after calling `client.gateway()`. It takes a `ReactorResource` object that will replace the one set at the `DiscordClientBuilder` level only for voice. A similar override exists for gateway in `setGatewayReactorResources`.

### Gateway options

Gateway options are set in `GatewayBootstrap`. You can obtain one by calling `gateway()` from `DiscordClient`.

```java
DiscordClient client = DiscordClient.create(token);
GatewayDiscordClient gateway = client.gateway()
        .setInitialStatus(shard -> Presence.online()) // <1>
        .setSharding(ShardingStrategy.recommended()) // <2>
        .setShardCoordinator(LocalShardCoordinator.create()) // <3>
        .setAwaitConnections(true) // <4>
        .setStoreService(new JdkStoreService()) // <5>
        .setEventDispatcher(EventDispatcher.buffering()) // <6>
        .login()
        .block();
```

- <1> Set the initial presence depending on the shard.
- <2> Sharding policy used by this shard group builder.
- <3> Allows coordinating shard login across multiple instances.
- <4> Configures how to obtain a `GatewayDiscordClient`: if `false`, once the connection process begins (at least 1 shard connects, this is the default) or  if `true`, await until all shards have connected.
- <5> Configure the backing store.
- <6> Configure the event dispatcher model.

## Customizing REST features

Core options for REST operations are set at `DiscordClientBuilder` before building a `DiscordClient`, similar to v3.0. These core resources can later by retrieved through `DiscordClient::getCoreResources()` or `GatewayDiscordClient::getCoreResources()` methods.


```java
JacksonResources jackson = new JacksonResources();
DiscordClient.builder(System.getenv("token")) // <1>
    .setJacksonResources(jackson) // <2>
    .setGlobalRateLimiter(BucketGlobalRateLimiter.create()) // <3>
    .setExchangeStrategies(ExchangeStrategies.jackson(jackson)) // <4>
    .setReactorResources(ReactorResources.create()) // <5>
    .onClientResponse(ResponseFunction.emptyIfNotFound()) // <6>
    .build()
    .gateway()
    .login()
    .onDisconnect()
    .block();
```

- <1> Make sure you begin with obtaining a builder.
- <2> Can customize how the Jackson object mapper works.
- <3> Can customize how the global rate limiter works.
- <4> Can customize how are REST requests written and REST responses read.
- <5> Can customize the underlying HttpClient used and the threading model
- <6> Can customize how the REST client handles response codes under given scenarios.

## Requesting Discord entities

Starting from v3.1, Discord4J allows you to access **REST entities**, which identifies a given Discord entity in terms of their key parameters, without querying the REST API until you require access to the data they represent. This is expressed across two kinds of classes:

* `RestEntity` classes provide a way to query the REST API for a specific entity. They are located in the `discord4j.rest.entity` package.
* `EntityData` classes represent a JSON response encapsulated in an immutable object. They are located in the `discord4j.discordjson.json` package.

#### Creating REST entities

Here are the multiple locations you can get a REST entity from:

* Replace "Entity" in the following examples with the one you're looking for: Channel, Emoji, Guild, Invite, Member, Message, Role, User, Webhook.
* Any of the `getEntityById()` methods in `DiscordClient` or `GatewayDiscordClient::rest()` and you'll get a `RestEntity` class
* Create them directly by ID: `RestEntity.create(123456789012345L)`

The classes available are: `RestChannel`, `RestEmoji`, `RestGuild`, `RestInvite`, `RestMember`, `RestMessage`, `RestRole`, `RestUser` and `RestWebhook`

To retrieve these objects you should call methods in `DiscordClient`, obtained from calling `getClient().rest()` from most library objects, like `Event` and `Entity` instances.

Once you acquire one of these objects, you can perform API methods without querying the contents. Alternative, you can perform an API request to get its content through `getData()` and will return a `Mono<EntityData>`.

### Fetching entities

To obtain cached entities you can use the same methods as in v3.0: `getEntityById(Snowflake)` from within `GatewayDiscordClient`. These methods will fallback to a REST API request if they are not found in cache.

Starting from v3.1 you can customize how these entities are fetched, using the **EntityRetrievalStrategy**. This is configured when bootstrapping a Gateway group:

```java
GatewayDiscordClient gateway = discordClient.gateway()
        .setEntityRetrievalStrategy(EntityRetrievalStrategy.STORE) // <1>
        .login()
        .block();
```

- <1> Only retrieve entities from store by default


The following strategies are available:

* `EntityRetrievalStrategy.STORE` to only fetch from the Store (cache) and therefore return empty if a request entity is missing.
* `EntityRetrievalStrategy.REST` to fetch from REST directly, without attempting to hit the Store.
* `EntityRetrievalStrategy.STORE_FALLBACK_REST` to use the default setting from v3.0, which is attempting to hit the Store and if it's missed, fall back to a REST API call.

## Logging

Logger structure has changed for v3.1, adding contextual information regarding gateway ID, shard ID, request bucket and request ID. For more details about the available loggers in this version, check our [Logging](Logging.md) page.


## Advanced features

For more detailed list of changes and migration notes, please check [What's new in v3.1](What's-new-in-v3.1.md).
