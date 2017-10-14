## Sharding

### What is sharding?

When a bot becomes part of a lot of servers it becomes too clunky to handle all the events on only a handful of threads, this is why sharding it introduced, to allow multi threaded delegation of events posted from discord to the API more efficiently.

Note: Sharding is required for bots that are members in over 2500 guilds, otherwise the bot will not work

### How do I shard?

In D4J you have the option of choosing how many shards you want to connect with yourself or letting discord choose the optimal number for you. All configuration is done by calling the methods `#withShards(shardCount)` or `#withRecommendedShardCount(useRecommended)` on the ClientBuilder object when it's being created, like so:

As usual the below code is based on the existing [[Basic bot]] example.
```java
    // Handles the creation and getting of a IDiscordClient object for a token
    static IDiscordClient getBuiltDiscordClient(String token){

        // The ClientBuilder object is where you will attach your params for configuring the instance of your bot.
        // Such as withToken, setDaemon etc
        return new ClientBuilder()
                .withToken(token)
                .withRecommendedShardCount()
                .build();

    }
```