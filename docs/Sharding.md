# What is sharding

Sharding is the process a bot guilds are split into multiple connections to the Discord Gateway. Discord requires you to have a max of 2,500 guilds per shard, but the recommended is 1 shard per thousand guilds, so as your bot scales you'll eventually need to switch to a sharding scheme.

Since v3.1, Discord4J takes a different approach to Sharding: a single `GatewayDiscordClient`, created from a `DiscordClient`, represents a **shard group** where 1 to N Gateway connections are established, each connection represents a shard and will receive updates from a subset of the bot guilds, however, they will be published to a single `EventDispatcher` and `StoreService`. This capability, combined with the modular nature of all Gateway-related components, allows for multiple bot architecture configurations.

# Enabling the feature

By default, a `GatewayDiscordClient` is configured for automatic sharding capabilities, meaning it will attempt to start the amount of shards Discord recommends given your bot size. When bootstrapping a connection to the Gateway, you can configure this using `.setSharding(ShardingStrategy)`.

```java
DiscordClient.create(System.getenv("token"))
        .gateway()
        .setSharding(ShardingStrategy.fixed(2))
        .withGateway(client -> client.on(ReadyEvent.class)
                .doOnNext(ready -> log.info("Logged in as {}", ready.getSelf().getUsername()))
                .then())
        .block();
```

This example will create a group splitting your bot into 2 shards and connecting to both. `ShardingStrategy` provides some static factories to quickly get started:

- `ShardingStrategy.recommended()` will provide the recommended amount of shards and include all of them in the group
- `ShardingStrategy.fixed(shardCount)` will use the given `shardCount` and include all shards `0..shardCount-1` in the group
- `ShardingStrategy.single()` will use a simple [0,1] configuration

You can also customize it entirely using `ShardingStrategy.builder()` for features such as:

- Set the total number of shards: `count(int)`
- Define the shard ID source (defaults from ID 0 to `shardCount - 1`): statically by `indices(int...)` or based on a `Publisher` using `indices(Function<Integer, Publisher<Integer>>)`
- Include only a subset of shard IDs using `filter(Predicate<ShardInfo>)`
- [If your bot supports it](https://discord.com/developers/docs/topics/gateway#sharding-for-very-large-bots) allow concurrent Gateway handshakes using `maxConcurrency(int)`

# Supported architectures

## Local configurations

If your bot infrastructure is running under the same JVM, your bot runs in a local configuration. This means you can simply use our `GatewayDiscordClient`, appropriately configure `ShardingStrategy` and have a functional sharded bot.

### Monolith with in-memory entity store

- Defaults to an in-memory (JDK) entity `Store` or can be customized through `MappingStoreService` and libraries like [stores-caffeine](https://github.com/Discord4J/Stores/tree/master/caffeine).

```java
DiscordClient.create(System.getenv("token"))
        .gateway()
        .setSharding(shardingStrategy)
        .withGateway(client -> { ... })
        .block();
```

### Monolith with custom entity store

- Switch to an external `StoreService` like [stores-redis](https://github.com/Discord4J/Stores/tree/master/redis)
- Can be combined with previously mentioned `MappingStoreService` for a customized layout seeking performance and efficiency.

```java
DiscordClient.create(System.getenv("token"))
        .gateway()
        .setStoreService(...)
        .setSharding(shardingStrategy)
        .withGateway(client -> { ... })
        .block();
```

### Multiple shard groups under the same JVM

- If you want to logically split shard groups, you can spawn multiple `GatewayDiscordClient` instances
- To make use of this pattern, you need to share the same parent `DiscordClient` to get automatically rate limit handling
- You need to share the `ShardCoordinator` across groups to get Gateway authentication rate limits automatically handled
- You can **optionally** share the `StoreService`, just supply the same instance to both Gateway bootstrap calls
- Can also apply this pattern for the *same* shards to acquire two independent groups for testing, redundancy/failover, etc

```java
// the first group will get even shard IDs
ShardingStrategy first = ShardingStrategy.builder()
        .count(10)
        .filter(s -> s.getIndex() % 2 == 0)
        .build();
// the second group will get odd shard IDs
ShardingStrategy second = ShardingStrategy.builder()
        .count(10)
        .filter(s -> s.getIndex() % 2 != 0)
        .build();

DiscordClient sharedClient = DiscordClient.create(System.getenv("token"));

ShardCoordinator coordinator = LocalShardCoordinator.create();

GatewayDiscordClient firstGroup = sharedClient.gateway()
        .setSharding(first)
        .setEnabledIntents(...)
        .setShardCoordinator(coordinator)
        .login()
        .block();

GatewayDiscordClient secondGroup = sharedClient.gateway()
        .setSharding(second)
        .setEnabledIntents(...)
        .setShardCoordinator(coordinator)
        .login()
        .block();
```

## Distributed configurations

If you plan to run your bot infrastructure across multiple machines, your bot runs in a distributed configuration. This poses some additional challenges as communication must travel across JVM boundaries. Fortunately there is an ongoing effort from Discord4J collaborators and contributors to support many distributed architectures.

### Introducing [Discord4J Connect](https://github.com/Discord4J/connect)

**Connect** is a project within the Discord4J organization attempting to provide distributed bot architectures over Discord4J. It goes beyond the JVM and *connects* your bot event flow across multiple middlewares to unlock your bot scaling potential.

Connect uses the concept of **Leader** and **Worker** to express a Discord4J bot topology:

- Leaders will establish a websocket connection to Discord Gateway, so they are in charge of handling its lifecycle and delivering payloads to a middleware
- Workers are not connecting to Discord Gateway directly and instead receive payloads from the middleware, connecting to it on startup
- Middleware is the set of components that need to work in a distributed fashion to communicate to an arbitrary number of Leaders and Workers

A typical Middleware is required to provide the following distributed functionality:

- `ShardCoordinator` to properly throttle and schedule Gateway authentication attempts (IDENTIFY)
- `GlobalRateLimiter` to handle global throttling of requests correctly, even across boundaries
- `Router` to coordinate REST API requests, taking rate limit buckets into account, even across boundaries
- A broker or equivalent middleware to route Gateway payloads between Leaders and Workers
- `StoreService` that can offer an entity cache to the whole topology

Connect project is attempting to provide implementations for each of the previous elements. Technologies such as RSocket, RabbitMQ and redis are used, and more are planned to offer a multitude of options to choose from.

Please check the [examples folder](https://github.com/Discord4J/connect/tree/master/examples) for information about how to set up a distributed bot using connect.

### Writing leaders, reading stateless workers

- Leader is in charge of writing to the entity cache and routing Gateway payloads
- Workers read from the distributed entity cache and process commands
- Workers are not tied to any particular shard, but the payload broker/middleware can route

![](https://github.com/Discord4J/connect/blob/master/distributed-discord4j-bot-writer-leaders.svg)

### Thin leaders, stateless workers

- Transfer the Gateway payload processing load to the workers and free resources on the leader side
- Workers read Gateway payloads, write to the entity cache and process commands
- Workers are not tied to any particular shard, but the payload broker/middleware can route

![](https://github.com/Discord4J/connect/blob/master/distributed-discord4j-bot-thin-leaders.svg)

### Dedicated voice workers

It is planned that Discord4J Connect supports distributed audio processing. The interfaces in the voice module exist, but they lack a distributed-capable implementation.

