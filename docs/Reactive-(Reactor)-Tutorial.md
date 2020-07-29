## Introduction
Discord4J uses [Reactor](https://projectreactor.io/) as the implementation of [reactive-streams](http://www.reactive-streams.org/) which follows [The Reactive Manifesto](https://www.reactivemanifesto.org/).

[Reactive programming](https://en.wikipedia.org/wiki/Reactive_programming) is a programming paradigm where data is expressed in "streams" and changes to these "streams" is propagated downwards (or "downstream"). This is achieved with a [declarative](https://en.wikipedia.org/wiki/Declarative_programming) style of programming, where the programmer builds the structure of the program that dictates logic, rather than handling its control flow directly. A very popular implementation of declarative programming is [SQL](https://en.wikipedia.org/wiki/SQL) and in Java declarative programs can be easily achieved using [lambdas](Lambda-Tutorial.md).

## Reactor Basics
You can view Reactor as simply an implementation of reactive programming in Java. While Java does provide mechanisms for better delegation of *work* (`ForkJoin`), this is unnecessarily complicated and does not provide nearly as much utility and robustness as Reactor.

### Publisher
A `Publisher` is actually a `reactive-streams` concept, and is even part of the [Flow API](https://docs.oracle.com/javase/9/docs/api/java/util/concurrent/Flow.Publisher.html). In short, a `Publisher` *publishes* data to a stream. In Discord, the "publisher" is Discord itself; they "publish" or "push" data to users and acts as a *source* of data. All data begins with a `Publisher`.

### Subscriber
A `Subscriber` is also a `reactive-streams` concept, and is also part of the [Flow API](https://docs.oracle.com/javase/9/docs/api/java/util/concurrent/Flow.Subscriber.html). A `Subscriber` "subscribes" or "consumes" data from a stream. In Discord, the "subscriber" is us, the users; we take the data *published* to us from Discord and process it in a way to monitor activity or respond in some fashion. All data ends at a `Subscriber`.

### Subscription
Also a `reactive-streams` concept and part of the [Flow API](https://docs.oracle.com/javase/9/docs/api/java/util/concurrent/Flow.Subscription.html) a `Subscription` describes a link between a `Publisher` and a `Subscriber`. A `Subscriber` *requests* data from a `Publisher` and the amount of data the `Publisher` *pushes* to the `Subscriber` is dependent on how much data the `Subscriber` requested. Additionally, the `Subscriber` can cancel the `Subscription` at any time.

While you as a programmer using Reactor will not see `Subscription` often, it is useful to know how data flows from a `Publisher` to a `Subscriber`. Without a `Subscription`, data is never *requested*, thus data never *flows* from a `Publisher` to a `Subscriber`.

### Mono
A [Mono](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Mono.html) represents a stream of data that either has an element, or not. It is the reactive equivalent of an [Optional](https://docs.oracle.com/javase/8/docs/api/java/util/Optional.html). Since `Mono` is a "provider" or a "source" of data, it is also an implementation of `Publisher`.

### Flux
A [Flux](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html) represents a stream of possibly unlimited data. It is the reactive equivalent of a [Stream](https://docs.oracle.com/javase/8/docs/api/java/util/stream/Stream.html). Since `Flux` is a "provider" or a "source" of data, it is also an implementation of `Publisher`.

### Basic Usage
Let's print a simple "Hello World" reactively.
```java
Mono.just("Hello World").subscribe(System.out::println);
// or
Flux.just('H', 'e', 'l', 'l', 'o', ' ', 'W', 'o', 'r', 'l', 'd', '\n').subscribe(System.out::print);
```
Let's breakdown the first line:
1. We create a `Mono` that will have a single element, a String `Hello World`.
2. We *subscribe* to this data, which creates a `Subscription` (that implicitly requests data from the `Publisher` (`Mono` in this case)), which allows the data to flow to our `System.out::println` method reference which consumes the data.

Let's breakdown the second line:
1. We create a `Flux` that will have multiple elements that eventually spell out the String "Hello World" with a newline character.
2. We *subscribe* to this data, which creates a `Subscription` (that implicitly requests data from the `Publisher` (`Flux` in this case), which allows the data to flow to our `System.out::print` method reference which consumes the data.

It is important to note two characteristics about these two examples:
1) `Flux.just("Hello World").subscribe(System.out::println);` is a just as valid example for `Flux`. `Flux` can represent a possibly infinite amount of data; this can be as small as no elements, one element, two elements, or a billion elements; it does not matter. Reactor provides `Mono` as a simple way to express "at most one element" similar to how `Optional` can be seen as a simple way to express "an element exists or not" compared to `Stream`. So even if you know you're only going to have at most one element, you *can* use `Flux`, but it is better to use `Mono` instead.

2) Both examples require a call to `subscribe`. Without any `subscribe`, no `Subscription` is created, thus data is never requested from a `Publisher`, thus data will never flow. The use of `subscribe` is critical and without it our program will simply do nothing.

### Basic Chaining (`map`, `flatMap`, `filter`, `filterWhen`)
Reactor has *many* methods (commonly referred to as "operations" or "ops") that allow programmers to manipulate the data to their content. The idea of combining multiple ops together is a form of "chaining". It should be an ultimate goal with Reactor to only express reactive operations as "chains".

#### `map`
`map` is a transformation of a data type `T` to some other data type `U`. For example:
```java
Mono.just("Hello World")
    .map(String::length)
    .subscribe(System.out::println); // prints 11
Flux.just("Hello", "World")
    .map(String::length)
    .subscribe(System.out::println) // prints 5 then 5
```
Both examples transform a data type of `String` to another data type of `Integer`, by using `String::length`.

#### `flatMap`
`flatMap` is a transformation of a data type `T` to some other data type `U` that is wrapped in some `Mono` or `Publisher` depending on the original source (so `Mono<U>` or `Publisher<U>`). For example:
```java
Mono.just("Hello World")
    .flatMap(aString -> Mono.just(aString.length()))
    .subscribe(System.out::println); // prints 11
Flux.just("Hello", "World")
    .flatMap(aString -> Flux.just(aString.length(), 42))
    .subscribe(System.out::println); // prints 5 then 42 then 5 then 42
```
Both examples transform a data type of `String` to another data type of `Integer`, but the source of the `Integer` is coming from some other reactive type. Every time data passes through a `flatMap` the inner `Publisher` is resubscribed, which means data is in essence "restarted" on each invocation.

`Mono`'s `flatMap` only supports a source coming from another `Mono`, while `Flux` supports any `Publisher`.

#### `filter`
`filter` prevents items from continuing to flow downstream if it fails a supplied [Predicate](https://docs.oracle.com/javase/8/docs/api/java/util/function/Predicate.html). For example:
```java
Mono.just("Hello World")
    .filter(aString -> aString.equals("Hello"))
    .subscribe(System.out::println); // prints nothing
Flux.just("Hello", "World")
    .filter(aString -> aString.equals("Hello"))
    .subscribe(System.out::println) // prints "Hello"
```
Both examples are *filtering* out items which do **NOT** equal "Hello".

For the `Mono` example, since the only element is "Hello World", which does **NOT** equal "Hello", then it is filtered out, thus nothing prints. It is important to note that data *did* flow, it just got filtered out in the end, resulting in an *empty* `Mono`. The concept of "emptiness" will be explained a bit later.

For the `Flux` example, since "World" does **NOT** equal "Hello", it was *filtered* out of the stream, thus, only "Hello" remained which resulted in the only thing being printed.

#### `filterWhen`
`filterWhen` is to `filter` as `flatMap` is to `map`. Rather than a `Predicate`, which is essentially a transformation from a type `T` to a `boolean`, `filterWhen` expects a transformation of type `T` to some `Publisher<Boolean>`, which will decide whether or not data should continue to flow downstream. For example:
```java
Mono.just("Hello World")
    .filterWhen(aString -> Mono.just(aString.equals("Hello")))
    .subscribe(System.out::println); // prints nothing
Flux.just("Hello", "World")
    .filterWhen(aString -> Mono.just(aString.equals("Hello")))
    .subscribe(System.out::println); // prints "Hello"
```
Similar to `flatMap`, as data passes through the `filterWhen`, the `Publisher` is resubscribed.