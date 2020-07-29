## What is sharding

Sharding is multiple connections to discord. It's used when you have a lot of guilds. Discord requires you to have a max of 2,500 guilds per shard, but the recommended is 1 shard per thousand guilds. Discord4J's system is that each `DiscordClient` you have is one shard, the default when creating a client is a total shard count of `1` and the current shard index is `0` (shards are zero based index), if you wanted to make another shard you'd have to make another DiscordClient.

## Coordinate multiple shards

### Within the same JVM instance

The following example will allow you to initialize multiple shards from the same place. They will be pre-configured with the necessary components to coordinate their Stores, Rate limits and [Gateway connection restrictions](https://discordapp.com/developers/docs/topics/gateway#identifying).

```java
new ShardingClientBuilder(token)
		.build()
		.map(builder -> builder.setInitialPresence(Presence.invisible()))
		.map(DiscordClientBuilder::build)
		.flatMap(DiscordClient::login)
		.blockLast();
```

Notice that no shard count is specified. This is because calling `ShardingClientBuilder` this way will use the [recommended amount of shards](https://discordapp.com/developers/docs/topics/gateway#sharding). If you're looking to set an specific number of shards to connect, look at this example:

```java
new ShardingClientBuilder(token)
		.setShardCount(shardCount)
		.build()
		.map(DiscordClientBuilder::build)
		.flatMap(DiscordClient::login)
		.blockLast();
```

Notice the line `.setShardCount(shardCount)`. In both cases you can further configure each individual client within a `map` operator below `.build()`.

To summarize:

```java
new ShardingClientBuilder(token)
		.build()
		// you have access to each individual DiscordClientBuilder for every shard
		.map(builder -> builder.setInitialPresence(Presence.invisible()))
		.map(DiscordClientBuilder::build)           // build each shard client
		.flatMap(DiscordClient::login)              // login each shard
		.blockLast();                               // wait until they disconnect
```

### Across multiple processes

This section is a work in progress! 👷 🏗 

This capability will target Discord4J v3.1 to consolidate all coordination components require to operate an architecture like the following:

![](https://github.com/Discord4J/meta/blob/master/distributed-discord4j-bot.svg)
