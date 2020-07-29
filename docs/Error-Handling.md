ℹ️ _For a more detailed source of information, please refer to [this section](https://projectreactor.io/docs/core/release/reference/#error.handling) of the Reactor reference guide._

According to reactive streams specification, errors are terminal signals. This typically means any running sequence will be **terminated** and the error propagated to all operators down the chain.

## Handling Errors
The following are some valid strategies for dealing with errors:

### Accept the error
Error will propagate downstream until the end of the chain and then run the `onError` callback in your subscriber.

You can optionally log and react on the side using `doOnError` operator.

As good practice, we encourage you to **implement the `onError` callback** when subscribing to be properly notified. If you don't override the `onError` callback, you will receive a Reactor `ErrorCallbackNotImplemented` exception wrapping your original exception.

```java
client.getEventDispatcher().on(MessageCreateEvent.class)
        .map(MessageCreateEvent::getMessage)
        .filter(message -> message.getAuthor().map(user -> !user.isBot()).orElse(false))
        .filter(message -> message.getContent().equalsIgnoreCase("!ping"))
        .flatMap(Message::getChannel)
        .flatMap(channel -> channel.createMessage("Pong!"))
        .doOnError(error -> { /* You can be notified here as well! */ })
        .subscribe(null, error -> { 
            // the error signal will stop here and terminate the sequence
            System.out.println(e);
        });
```

⚠️ Taking this approach under `EventDispatcher` sequences will terminate your subscription, missing all further events for that subscriber. In general, this is a poor solution if you wish to perform extra behavior like starting another chain.

### [Catch and return a static default value](https://projectreactor.io/docs/core/release/reference/#_static_fallback_value): `onErrorReturn`

✔️ This approach is good for individual API requests when you want to return a value in case of an error.

⚠️ Taking this approach with `EventDispatcher` will terminate the sequence, and no further events will be received by that subscriber, unless you apply this operator to an inner sequence, without affecting the outer one containing all events, like this example:

```java
client.getEventDispatcher().on(MessageCreateEvent.class)
        .map(MessageCreateEvent::getMessage)
        .filter(message -> message.getAuthor().map(user -> !user.isBot()).orElse(false))
        .filter(message -> message.getContent().startsWith("!user "))
        .flatMap(message -> Mono.just(message.getContent())
                .map(content -> content.split(" ", 2))
                .flatMap(tokens -> message.getClient().getUserById(Snowflake.of(tokens[1])))
                .map(user -> user.getUsername() + "#" + user.getDiscriminator())
                .onErrorReturn("Could not find that user") // Replace any error with this message
                .flatMap(name -> message.getChannel()
                        .flatMap(channel -> channel.createMessage(name))))
        .subscribe(null, System.out::println);
```

onErrorReturn has overloads to include a condition so you could for example use the following to only recover from `404` status errors (Not found):

```java
.onErrorReturn(ClientException.isStatusCode(404), "Could not find that user")
```

### [Catch and execute an alternative path with a fallback method](https://projectreactor.io/docs/core/release/reference/#_fallback_method): `onErrorResume`

✔️ This approach is good for individual API requests when you want to provide alternative behavior.

✔️ This approach is great when working inside a `flatMap` with `EventDispatcher`, as you will replace the sequence with, for example, `Mono.empty()` effectively suppresing the error while maintaining the original sequence.

```java
Flux.just("😀", "😬", "😂", "😄")
    .flatMap(emoji -> message.addReaction(ReactionEmoji.unicode(emoji))
            .onErrorResume(e -> Mono.empty()) // error is discarded
    )
    .subscribe(); // so it won't get here
```
If you were to place `onErrorResume` outside a `flatMap`, you'll replace the sequence, potentially missing some elements being processed:
```java
Flux.just("😀", "😬", "😂", "😄")
    .flatMap(emoji -> message.addReaction(ReactionEmoji.unicode(emoji))) // if this fails on the 3rd emoji
    .onErrorResume(e -> Mono.empty()) // you'll replace the sequence with an empty one, and miss the last one
    .subscribe(); // but the error still won't reach here!
```

### [Catch and Rethrow](https://projectreactor.io/docs/core/release/reference/#_catch_and_rethrow): `onErrorMap`

✔️ This approach is good for individual API requests when you want to translate the error, typically `ClientException`, to a type you control for additional behavior downstream.

✔️ This approach is good when working with `EventDispatcher` for the same reason as above. Be aware that the sequence is still on error and can be handled by a different strategy on following operators.

### [Retrying](https://projectreactor.io/docs/core/release/reference/#_retrying): `retry`, `retryWhen`
Error will terminate the original sequence, but `retry()` (and [variants](https://projectreactor.io/docs/core/release/reference/#_retrying)) will **re-subscribe** to the upstream Flux. Be aware that this ultimately means that a new sequence is created.
```java
client.getEventDispatcher().on(MessageCreateEvent.class)
        .map(MessageCreateEvent::getMessage)
        .filter(message -> message.getAuthor().map(user -> !user.isBot()).orElse(false))
        .filter(message -> message.getContent().equalsIgnoreCase("!ping"))
        .flatMap(Message::getChannel)
        .flatMap(channel -> channel.createMessage("Pong!"))
        .retry()
        .subscribe();
```

⚠️ This approach is generally appropriate for API requests, but there are certain errors you should not retry. By default, Discord4J retries some errors for you, using an exponential backoff with jitter strategy.

✔️ This approach is compatible with `EventDispatcher` sources when using the [default event processor](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/EmitterProcessor.html), due to it only relaying events since the time of subscription. As retrying creates a new subscription, the erroring event will be discarded and the sequence will continue from the next event.

### Catch and continue mode, and reverting to stop mode

#### `onErrorContinue`

⚠️ This only works **on supporting operators**: `flatMap`, `map` and `filter`, among others according to their javadocs. This operator goes beyond the Reactive Streams spec and uses the Reactor Context to work, therefore it is prone to issues when combining it with unsupporting operators. Only use this operator if you understand the consequences or you're familiar with how Reactor Context works.

Applying `onErrorContinue` on a `Flux` will change the default behavior of treating errors as terminal events to discarding erroring elements and keeping the same sequence active.

If an error occurs, _supporting operators_ will look for the continue strategy flag before reaching other operators.

Around `Mono` sequences, **we generally recommend sticking with resuming if you already use it**, unless you want a catch-all behavior that can override other `onError*` calls.

👷 🏗 TODO: Include examples

#### `onErrorStop`

Using `onErrorStop` will revert the behavior to treating errors as terminal events. This can be used to accurately scope the continue strategy and avoid surprises, specially when combining it with `onErrorResume`.

## Error Sources
Typical Reactor operators will throw errors if you:
- Throw any `RuntimeException` inside a lambda within an operator (see [4.6.2](https://projectreactor.io/docs/core/release/reference/#_handling_exceptions_in_operators_or_functions) for an in-depth explanation)
```java
Flux.just(1, 2, 0)
        .map(i -> "100 / " + i + " = " + (100 / i)) // this triggers an error with 0
        .subscribe();
```
- Transform a signal into an error one
```java
Flux.just("Mega", "Micro", "Nano")
        .flatMap(s -> {
            if (s.startsWith("M")) {
                return Mono.just(s);
            } else {
                return Mono.error(new RuntimeException());
            }
        })
        .subscribe();
```
- Receive an HTTP error code (400s or 500s)
```java
client.getEventDispatcher().on(MessageCreateEvent.class)
        .map(MessageCreateEvent::getMessage)
        .filter(message -> message.getAuthor().map(user -> !user.isBot()).orElse(false))
        .filter(message -> message.getContent().equalsIgnoreCase("!ping"))
        .flatMap(Message::getChannel)
        .flatMap(channel -> channel.createMessage("Pong!")) // this can fail with 403, 500, etc...
        .subscribe();
```
- Return `null` (except some documented cases)
```java
Flux.just(1, 2, 3)
        .map(n -> null) // illegal operation
        .subscribe();
```
- Overflow due to not generating enough demand
```java
// Generate a tick every 10 ms
Flux.interval(Duration.ofMillis(10))
        //.onBackpressureDrop() // uncommenting this avoids the error: drop tick if consumer is choked
        .flatMap(tick -> Mono.never()) // would "never" consume the upstream ticks, overflows
        .subscribe();
```

## Handling errors across multiple requests using Discord4J

Until now we have seen examples that deal with error handling on particular sequences, and while you should continue to use these patterns for most use cases, you might find yourself applying the same operator to a lot of requests. For those cases, Discord4J provides a way to install an error handler across many or all requests made by a `DiscordClient`.

When you build a Discord4J client through `DiscordClientBuilder` or `ShardingClientBuilder` you'll notice that there are many setters for a variety of customization. You can handle errors in multiple requests by providing a custom [`RouterOptions`](https://static.javadoc.io/com.discord4j/discord4j-rest/3.0.4/discord4j/rest/request/RouterOptions.html) object through `setRouterOptions` method.

You could, for example, build your clients this way:

```java
DiscordClient client = new DiscordClientBuilder(token)
        .setRouterOptions(RouterOptions.builder()
                // globally suppress any not found (404) error
                .onClientResponse(ResponseFunction.emptyIfNotFound())
                // bad requests (400) while adding reactions will be suppressed
                .onClientResponse(ResponseFunction.emptyOnErrorStatus(RouteMatcher.route(Routes.REACTION_CREATE), 400))
                // server error (500) while creating a message will be retried, with backoff, until it succeeds
                .onClientResponse(ResponseFunction.retryWhen(RouteMatcher.route(Routes.MESSAGE_CREATE),
                        Retry.onlyIf(ClientException.isRetryContextStatusCode(500))
                                .exponentialBackoffWithJitter(Duration.ofSeconds(2), Duration.ofSeconds(10))))
                // wait 1 second and retry any server error (500)
                .onClientResponse(ResponseFunction.retryOnceOnErrorStatus(500))
                .build())
        .build();
```

Each time `onClientResponse` is called, you're adding a strategy to transform each response made by the `DiscordClient`. If an error occurs, Discord4J processes the error through the following handlers:
1. Handle rate limiting errors (429), these cannot be modified.
2. Handle the errors using the ones installed by `onClientResponse`.
3. Handle server errors (500s) and retry them using exponential backoff.

The first handler that matches will consume the error and apply its strategy, meaning that the order of declaration is important.

You can look at the [`ResponseFunction`](https://static.javadoc.io/com.discord4j/discord4j-rest/3.0.15/discord4j/rest/response/ResponseFunction.html) class for commonly used error handlers. A version covering all requests is available, but also a version allowing you to apply the handler to only some API Routes, with the support of [`RouteMatcher`](https://static.javadoc.io/com.discord4j/discord4j-rest/3.0.4/discord4j/rest/request/RouteMatcher.html). Explore the [Javadocs](javadoc.io/doc/com.discord4j/discord4j-rest) for the rest module to understand more.