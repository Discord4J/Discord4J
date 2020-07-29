# Glossary 

## Core classes

### DiscordClient
Main REST API wrapper and an entry point to spawn Gateway connections. Can be obtained by calling `getClient().rest()` from most objects.

### CoreResources
A mediator for resources essential to operate on `DiscordClient`, like the `RestClient`, `JacksonResources` and `ReactorResources`.

### GatewayDiscordClient
Discord Gateway is the websocket connection receiving real-time updates. In v3.1 we call `GatewayDiscordClient` to the manager of all resources and state regarding all participating shards. Connections are maintained by it and is capable of giving access to cached entities from updates. Can be obtained by calling `getClient()` from most objects.

### GatewayBootstrap
A builder to create a `GatewayDiscordClient`. The place where resources and many configurations are set before connecting to Discord Gateway. Defaults are used to enable built-in sharding and can be customized in many ways.

### GatewayClient
The same component as in v3.0, it keeps all state regarding a single shard lifecycle, providing automatic reconnections and forwarding updates to the parent `GatewayDiscordClient`.

### GatewayResources
A mediator for most resources required by a `GatewayClient` to operate. The rest are provided during `GatewayBootstrap` setup and are used to build `GatewayOptions`.

### GatewayOptions
The set of configuration passed to a `GatewayClient` in order to be created.

### ShardCoordinator
A manager in charge of relaying lifecycle events for sharding. It is used to notify clients that shards are identifying, connecting or disconnecting to allow for distributed application scenarios where the notification may be relayed across boundaries.

### StateHolder
The same component as in v3.0, it stores all entity updates from the Gateway. In v3.1 though, it will receive updates from all joining shards.

### EventDispatcher
The same component as in v3.0, it publishes Gateway events to the user. In v3.1 the events are coming from all shards so a single instance is enough for a `GatewayDiscordClient`.

### ReactorResources
Encapsulates Reactor Netty HttpClient and Scheduler for blocking and timed tasks, can be customized.

### JacksonResources
Jackson ObjectMapper configured specifically for Discord4J, can be customized.

### RouterFactory
Factory to create a `Router` which performs REST API requests. Can be customized to coordinate multiple instances.
