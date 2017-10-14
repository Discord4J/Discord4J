Events in Discord terms represent "things that happen", registering and listening to these events is formally known as the Observer/Observable pattern and is used extensively throughout D4J. In brief, the Observer/Observable pattern operates off of the principle of "subscribing" to when something happens. In an analogous example imagine subscribing to a YouTube channel, every time a new video is released by that creator, you're notified. The same thing happens here, you can "subscribe" to get updates about certain events via the `EventDispatcher`, when a new event of that type occurs, the method is called.

Most, if not all, events sent by Discord have an interface encoding it for reasonable usage in the D4J API, for example `MessageReceivedEvent` is an event that's dispatched when a message is received by the bot from any other user, this includes DMs. A full list of events that potentially have encoded interfaces can be found here https://discordapp.com/developers/docs/topics/gateway

Some specifics:
- GuildCreateEvent is dispatched when the data about that guild is sent to the bot, contrary to popular belief this event can multiple times, on startup and on initial joining of the guild.
- ReadyEvent is dispatched only after all shards/guilds are ready and all data is received.

The syntax for using both the `EventSubscriber` and `IListener` methods of registering events can be found on the main D4J README. A practical example of both can also be found under the [[Basic bot]] page.
