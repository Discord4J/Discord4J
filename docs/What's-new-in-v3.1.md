# Introduction

Welcome to this next major release in Discord4J, a reactive wrapper library for the Discord REST API and real-time Bot Gateway with voice capabilities.

After a year of feedback from users since releasing v3.0, we collected a series of changes to provide and support more use cases and bot configurations. While some of these changes could make their way into following v3.0 minor releases, some involve deeper design work that would have an impact on present releases in terms of binary and behavior changes.

In addition, since Discord is an evolving platform, some of its changes also have impact in the type structure that makes it difficult to implement every change in minor releases. As such, a new major release is the opportunity to include all of these changes and prepare the library structure for future additions more rapidly.

## Maintenance mode for v3.0

After v3.1.0 releases, only bugfixes will be accepted in the 3.0.x branch.

# Architectural changes

## DiscordClient is split from Gateway actions

In order to support more use cases, such as bots that don't use the Gateway to perform some actions, components related to REST API operations are now structurally separated from the Gateway operations.

This means a `DiscordClient` no longer gives you access to the Store abstraction for cached entities from the Gateway and is now used to spawn Gateway connections and operating the REST API directly.

Moving forward, the new `GatewayDiscordClient` represents the place to fetch and work with Gateway-based entities. You now acquire this instance every time to subscribe to `DiscordClient::login` or `GatewayBootstrap::login` after creating it with `DiscordClient::gateway`.

## Shard group creates a GatewayDiscordClient

One of the main goals for this release is to lay the groundwork to build distributed bot architectures, a model where bot components are split across multiple processes.

Since an array of configurations exist, we decided to orient the creation of `GatewayDiscordClient` towards "shard groups" that encapsulate caches and events around an arbitrary number of shards. A brand new `GatewayBootstrap` API provides creating simple, sharded, monolithic or distributed bots from a single place.

## One EventDispatcher publishes for a shard group

Building a shard group creates a context that aggregates payloads from joining Gateway connections, publishing `Event` instances to a single `EventDispatcher`, simplifying managing a sharded bot within a process.

For this reason, `Event` instances are now aware of the shard they were created by.

## Caches are shared across a shard group

Since Gateway updates are processed within a shard group, Stores, our entity cache abstraction, are created once per group. This eases management for single-process sharded bots.

# Important API changes

For a more exhaustive list, please look at <<API changes>>.

## Promote `Snowflake` class to common module

We moved the `discord4j.core.object.util.Snowflake` type to `discord4j.common.util.Snowflake` in order to support all modules across the Discord4J project using it. We feel it is a great abstraction to handle IDs and the primary token of addressing particular Discord entities across the lifetime of your application.

Other types affected by module promotion: `Image`, `Permission`, `PermissionSet`. See <<Moved types>>.

# Behavior changes

## Login completes on connect instead of disconnect

An important change in this major update is the change of the `login()` method existing in `DiscordClient`. We feel that the method makes most sense if it returns a `Mono` that *completes as login succeeds*, instead of logout, and to return a `GatewayDiscordClient` handle for the underlying gateway connections.

## Spec code is run on subscription time

Previously in v3.0, `Spec` consumers were called on assembly time, meaning when a reactive pipeline was declared. This could lead to unexpected code execution so moving forward this was changed in alignment to the expectation of reactive programming where no significant work should happen until you subscribe.

# New features

## Gateway intents support

Add support to establish Gateway connections using the Intent system, reducing the inbound event load on your bot. See `GatewayBootstrap::setEnabledIntents` or `GatewayBootstrap::setDisabledIntents`

## Wait until Gateway connections are ready

Allow customizing the behavior whether a `GatewayDiscordClient` should be obtained immediately (similar to v3.0 behavior) or to wait until all shards in the group have connected (Discord "READY" status). Check `GatewayBootstrap::setAwaitConnections`

## Custom EventDispatcher

Allow changing the `EventDispatcher` implementation for event handling. Check the class for builders and factories.

## Entity retrieval strategy

Allow modifying the behavior of the `getXById` methods in `GatewayDiscordClient`. By default, like in v3.0, if a requested entity is not present in the `Store`, a REST API request is made. Since this might not be desired, you can globally change the behavior at `GatewayBootstrap::setEntityRetrievalStrategy` or per-call through `GatewayDiscordClient::withRetrievalStrategy`.

## Custom Reactor resources used by REST, Gateway and Voice

All Reactor resources used by Discord4J can be configured, across REST, Gateway and Voice operations. You can now control almost every aspect of Discord4J threading model.

## Guild member request behavior

Allow providing a custom `MemberRequestFilter` to more explicitly control member requests should be automatically issued. Check `GatewayBootstrap::setMemberRequestFilter`.

## On-demand guild member request (lazy loading)

Add methods to directly request for guild members, such as `GatewayDiscordClient::requestMembers`.

## REST-only entities

Allow working with Discord4J without an active Gateway connection. Just create a `DiscordClient` and perform calls through the `getXById` methods to use REST API directly.

You can also obtain a "REST entity" which wraps a particular entity with its ID (no actual data) and can be used to perform actions without unneeded API requests. These are available directly through one of the entity types supported: `RestChannel`, `RestEmoji`, `RestGuild`, `RestInvite`, `RestMember`, `RestMessage`, `RestRole`, `RestUser`, `RestWebhook`. 

Obtaining a `RestClient` can be done through `GatewayDiscordClient::rest` or any `DiscordClient` instance, capable of directly querying the API or creating such REST entities.

## Normalized internal data representation

A large effort is made into converging our many internal data structures into a more maintainable approach. We decide to use Immutables library to model the Discord domain through `Data` classes. All are created with a `Builder` that can simplify their usage when interacting with some API, particularly through REST-only entities.

## ShardCoordinator abstraction

A new abstraction is created to support coordination of Gateway IDENTIFY requests across multiple processes.

## More sharding configurations supported

A builder to configure how a shard group creates shards is available through `ShardingStrategy` and can be set at `GatewayBootstrap`. The total shard count can be fixed or used as recommended by Discord, connect specific shards by index and also support a sharding concurrency value or "very large bot sharding system".

## Improved rate limiting implementations

Provide a new approach to rate limit which increases throughput without locking structures. This is used across all of Discord4J: REST request queue, REST global rate limiter, Gateway identify limit and Gateway outbound limit.

## Custom max missed heartbeat ACK

Bring back a setting from v2 to help dealing with connection instability by avoiding detecting zombie connections too early.

## Custom Voice Gateway connection factory

Provide new API to customize Voice operations and tasks, in an effort to help integrating it with other tools and frameworks.

## Custom Gateway invalidation strategy

Allow customizing the actions that should occur on the entity cache on shard invalidation (non-resumable disconnections)

## Custom Gateway destroy handler

Allow customizing the actions that should occur when logging out a shard group from the Gateway.

## Custom Gateway to Event mapping

Allow customizing how are `Event` instances created from the Gateway updates, potentially enriching or disabling some of the original handling.

## Custom or extend Gateway configuration

Provide access to the low-level `GatewayClient` configuration, allowing extension for custom `GatewayClient` factories. This is particularly useful to create distributed bot architectures.

# API changes

## Signature changes

### DiscordClient
* Login returns `Mono<GatewayDiscordClient>` and emits on Gateway connection instead of disconnection.
* Can only be created from factory methods.
* Methods that retrieved entities like `getGuildById` now return REST-only entities. If you want to keep the old behavior, create a `GatewayDiscordClient` through one of the login methods.

### DiscordClientBuilder
* Methods that configured Gateway-specific options were moved to `GatewayBootstrap`.

### PayloadTransformer
It is now a `Flux<ByteBuf>` operator.

### IdentifyOptions
Constructor made private. This class was also made immutable with a rich set of methods to customize more options.

### GlobalRateLimiter
Migrate to reactive API.

### CloseException
Method `getReason` now returns Optional. In addition, improved `toString()` method and added `getContext()`. A related class `CloseStatus` also had its `getReason` method changed in the same way.

### ReadyEvent
getTrace now returns a List<String>.

### MessageBulkDeleteEvent
getChannel now returns a Mono<TextChannel> instead of Mono<MessageChannel>.

## New types

### Color
Our own color class to avoid a java.desktop module dependency.

### ReactorResources
A place to customize many Reactor related options.

### StateView
A read-only view for the entity cache (store) contents.

### ShardCoordinator
New class to group shard operations across JVM boundaries.

### ShardInfo
New class to group shard index and count.

### SessionInfo
New class to group sessionId and sequence.

### REST-only entities
Introduce types related to rest module operations.

### GatewayOptions
Refactored all parameters used by DefaultGatewayClient and extracted them to this new class.

### DispatchEventMapper
Allow customizing the process in which Gateway payloads are processed and converted into Events.

### Events
* New event: ReactionRemoveEmojiEvent

## Moved types

### Snowflake
Moved from `discord4j.core.object.util` to `discord4j.common.util`

### Permission and PermissionSet
Moved from `discord4j.core.object.util` to `discord4j.rest.util`

### Image
Moved from `discord4j.core.object.util` to `discord4j.rest.util`

### Possible
Moved to the discord-json project which is now a dependency of discord4j-common.

## Renamed types

### JacksonResources
Formerly JacksonResourceProvider.

### ReconnectOptions
Formerly `RetryOptions`, it is now immutable, split from the stateful `ReconnectContext`

### ReconnectContext
Formerly `RetryContext`.

## Removed types

### ClientConfig
Shard information can now be obtained from each `Event`.

### ServiceMediator
State and resources held by this class were relocated to appropriate components:

* GatewayClient are now located in GatewayClientGroup which is accessible from a GatewayDiscordClient
* RestClient is now the parent class of DiscordClient so methods can be called directly from it
* StateHolder is now available at GatewayResources, accessible from GatewayDiscordClient
* EventDispatcher is accessible from GatewayDiscordClient
* VoiceClient was replaced by a VoiceConnectionRegistry, a far more powerful alternative, accessible from GatewayDiscordClient

### GatewayClientFactory
`GatewayClientFactory` was removed as it can be now be used in a functional way within `GatewayBootstrap::login(Function)`.

### RateLimiterTransformer

### RateLimiter
Removed interface, along with SimpleBucket, as part of the rate limiting implementations rework.

## Additions

### Event
Add getShardInfo.

### GatewayStateChange
Add resume states to `GatewayStateChange`.

### MemberChunkEvent
Add new methods related to chunk index, count, etc.

## Renames

## Removals

### DiscordClientBuilder
* No longer possible to set debug mode. Use the reactor-debug-agent or `Hooks.onOperatorDebug()` at the start of your application if you wish to retain this capability.
