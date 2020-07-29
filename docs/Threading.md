# Reactor and threading

⚠️ _This is an advanced topic and expects you have read the **Threading and Schedulers** section of the Reactor reference. [Check it out](https://projectreactor.io/docs/core/release/reference/#schedulers) to familiarize yourself with its API and usage._

Reactor is a **concurrency agnostic runtime**, there is no set thread affinity for each operator, leaving users in control on the thread in which operations happen. The framework provides many `Scheduler` implementations and helper methods that materialize that control and are used by Discord4J throughout its modules.

# Threading model

Discord4J sets reasonable defaults on the threading aspect, to cover the most possible use cases without compromising performance. First, since our network runtime is the [Reactor Netty](https://github.com/reactor/reactor-netty) project, every HTTP (API and websocket) and UDP interaction occurs on their default scheduling approach which is providing an **event loop** using an amount of threads given by the available processors, with a minimum of 4.

Given that each of the Reactor Netty event loop threads do not allow blocking operations, we use a [ForkJoinPoolScheduler](https://github.com/reactor/reactor-addons/blob/master/reactor-extra/src/main/java/reactor/scheduler/forkjoin/ForkJoinPoolScheduler.java) to dispatch every event (through `EventDispatcher` + `publishOn`).

Every HTTP API response is given back to you through the **Bounded Elastic Scheduler**, that is capable of supporting blocking operations downstream.

Some operations create exclusive Schedulers for serializing calls like emitting permits for rate-limited operations like the GlobalRateLimiter, Gateway IDENTIFY limiter and the per-session outbound limiter.

# Customization

Since Reactor gives the control to its users regarding threading, it is our goal to extend that flexibility to you as well. While you're still under control using operators such as [`publishOn`](https://projectreactor.io/docs/core/release/reference/#_publishon) and [`subscribeOn`](https://projectreactor.io/docs/core/release/reference/#_subscribeon) you can also override some of the defaults we set above.

## ReactorResources

A new class dedicated to group application-wide resources used by Discord4J was created starting from v3.1.0. [ReactorResources](https://www.javadoc.io/doc/com.discord4j/discord4j-common/latest/discord4j/common/ReactorResources.html) is the place to customize a set of `Scheduler` instances and other Reactor-related resources, including the schedulers used by HTTP/Websocket and UDP clients.

An instance is created per `DiscordClient` and they can be shared if created externally:

```java
ReactorResources reactorResources = ReactorResources.builder()
        .timerTaskScheduler(Schedulers.newParallel("my-scheduler"))
        .blockingTaskScheduler(Schedulers.boundedElastic())
        .build();
DiscordClient discordClient = DiscordClientBuilder.create("TOKEN")
        .setReactorResources(reactorResources)
        .build();
```

### Replacing Schedulers

```java
ReactorResources reactorResources = ReactorResources.builder()
        .timerTaskScheduler(Schedulers.newParallel("my-scheduler"))
        .blockingTaskScheduler(Schedulers.boundedElastic())
        .build();
```

#### `timerTaskScheduler`

Dedicated to all tasks that involve delays, or other time-sensitive operations. This means **blocking** is forbidden under these schedulers. Defaults to a parallel scheduler named `d4j-parallel-N` and will throw an exception if running blocking code on it.

- Delaying Gateway identify attempts
- Delaying outbound Gateway payloads under per-session rate limit
- Scheduling Gateway periodic heartbeat
- Delaying REST API requests under Discord global rate limit
- Delaying REST API requests under Discord per-bucket rate limit
- Scheduling Voice Gateway periodic heartbeat
- Delaying connection attempts for Gateway and Voice Gateway

#### `blockingTaskScheduler`

Dedicated to all tasks where **blocking** is possible. Defaults to Reactor global `boundedElastic` scheduler.

- Scheduling early event listeners registered through `GatewayBootstrap::withEventDispatcher`
- Publishing responses from the REST API operations

### Specific resources for Gateway and Voice

A `ReactorResources` instance will be used for REST API operations, and reused for Gateway and Voice operations, **unless** overridden by the user.

You can customize the resources used exclusively for Gateway through the bootstrap:

```java
GatewayDiscordClient client = DiscordClient.create("TOKEN")
        .gateway()
        .setGatewayReactorResources(reactorResources -> new GatewayReactorResources(...))
        .login()
        .block();
```

A `GatewayReactorResources` has an extra scheduler for dedicated Gateway payload sending. If not overridden, it will use a dedicated single scheduler called `d4j-gateway`. Can be created through `GatewayReactorResources::DEFAULT_PAYLOAD_SENDER_SCHEDULER`.

And the same can be done for voice:

```java
GatewayDiscordClient client = DiscordClient.create("TOKEN")
        .gateway()
        .setVoiceReactorResources(reactorResources -> new VoiceReactorResources(...))
        .login()
        .block();
```

A `VoiceGatewayResources` requires extra parameters due to all concurrent tasks it need to keep. Among them:

- Scheduling Voice send task (defaults to `timerTaskScheduler` if not replaced)
- Scheduling Voice receive task (defaults to `timerTaskScheduler` if not replaced)

## EventDispatcher

The `EventDispatcher` is a key element in the Discord4J Core architecture. Since v3.1.0 a wide range of customization is possible on this component, in particular: the backing Processor, its back-pressure (flow control) strategy and the Scheduler used for publishing events.

### Replacing thread model

Acquire a builder using `EventDispatcher::builder` and customize its scheduler using `eventScheduler`:

```java
EventDispatcher customDispatcher = EventDispatcher.builder()
        .eventScheduler(Schedulers.boundedElastic())
        .build();
```

The current default is creating a `ForkJoinPool`-based scheduler called `d4j-events`. If you decide to change it, these are our recommendations:
- If you perform a lot of blocking and slow operations, switch to `Schedulers.boundedElastic()`.
- If you **never** call block, perform operations that park threads like blocking HTTP libraries nor blocking I/O in general, try using `Schedulers.immediate()` to minimize thread switching.
