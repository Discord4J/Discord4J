## Reactor and threading
⚠️ _This is an advanced topic and expects you have read the **Threading and Schedulers** section of the Reactor reference. [Check it out](https://projectreactor.io/docs/core/release/reference/#schedulers) to familiarize yourself with its API and usage._

Reactor is a **concurrency agnostic runtime**, there is no set thread affinity for each operator, leaving users in control on the thread in which operations happen. The framework provides many `Scheduler` implementations and helper methods that materialize that control and are used by Discord4J throughout its modules.

### Discord4J threading model
Discord4J sets reasonable defaults on the threading aspect, to cover the most possible use cases without compromising performance. First, since our network runtime is the [Reactor Netty](https://github.com/reactor/reactor-netty) project, every HTTP (API and websocket) and UDP interaction occurs on their default scheduling approach which is providing an **event loop** using an amount of threads given by the available processors, with a minimum of 4.

Second, and given that each of the Reactor Netty event loop threads do not allow blocking operations, we use a [ForkJoinPoolScheduler](https://github.com/reactor/reactor-addons/blob/master/reactor-extra/src/main/java/reactor/scheduler/forkjoin/ForkJoinPoolScheduler.java) to dispatch every event (through `EventDispatcher` + `publishOn`).

Third, and due to the same reason, every HTTP API response is given back to you through the **elastic Scheduler**, that provides an unbounded amount of threads that can perform work-stealing, has a small reuse window, and are evicted after 60 seconds of inactivity. This also applies to scheduling every delayed request to the API caused by rate limits.

Finally, we use a `Schedulers.single()` factory to send each message through the Websocket-based Gateway, to properly serialize potentially concurrent operations especially during the early stages of a Gateway connection. This is not modifiable to ensure Gateway rate-limits are properly respected.

### Customization
Since Reactor gives the control to its users regarding threading, it is our goal to extend that flexibility to you as well. While you're still under control using operators such as [`publishOn`](https://projectreactor.io/docs/core/release/reference/#_publishon) and [`subscribeOn`](https://projectreactor.io/docs/core/release/reference/#_subscribeon) you can also override some of the defaults we set above.

#### Overriding `EventDispatcher` thread model

Pass a `Scheduler` instance to `DiscordClientBuilder#setEventScheduler(Scheduler)`, these are our recommendations:

- If you perform a lot of blocking and slow operations, switch to `Schedulers.elastic()`.
- If you **never** call block, perform operations that park threads like blocking HTTP libraries nor blocking I/O in general, try using `Schedulers.immediate()` to minimize thread switching.
- The current default is: `ForkJoinPoolScheduler.create("events")` providing a fair balance between the two options.

#### Overriding API requests thread model

Pass a `RouterFactory` instance to `DiscordClientBuilder#setRouterFactory(RouterFactory)`. You can build one this way:

```java
RouterFactory routerFactory = new DefaultRouterFactory(Schedulers.elastic(), Schedulers.elastic());
DiscordClient client = builder.setRouterFactory(routerFactory)
       .build();
```

The first parameter is to schedule each API response, and the second one is to set Scheduler for rate limits:

- Use `Schedulers.elastic()` or `ForkJoinPoolScheduler` if you do blocking operations.
- Same as the previous tip, try `Schedulers.immediate()` if you **never** block, as you would get an error if a blocking operation is detected on a network thread.
- Use `Schedulers.parallel()` as the **second** parameter to go back to the Reactor default for delayed operations.
