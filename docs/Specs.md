## Introduction
Specs, short for Specification(s), are a unique design philosophy adopted by Discord4J to handle requests that contain multiple *optional* properties. They are very similar to the common [builder pattern](https://en.wikipedia.org/wiki/Builder_pattern), but with two very important differentiating characteristics:

1) The end-user does **not** construct the builder.
2) The end-user does **not** construct the finalized object.

These two characteristics provide Discord4J with tremendous flexibility when it comes to constructing requests without breaking the API at a future date. Different requests to Discord may require different procedures and Specs allows Discord4J to "construct" these requests in an implementation-dependent manner while still providing the end-user control in "building" the request parameters using a singular syntax that is consistent across the API.

### Example
All Specs that an end user interacts with will be provided via a `Consumer`. For example, for `MessageChannel#createMessage`:
```java
messageChannel.createMessage(spec -> /* manipulate the spec */)
```

One may note that all `Spec` instances have an `asRequest` method. This method is an internal behaviorally implementation-specific method and should never be called by the end-user. Once the Spec has been "built", simply leave it alone.
```java
Mono<Message> message = messageChannel.createMessage(messageSpec -> {
    messageSpec.setContent("Content not in an embed!");
    // You can see in this example even with simple singular property defining specs the syntax is concise
    messageSpec.setEmbed(embedSpec -> embedSpec.setDescription("Description is in an embed!"));
});
```

### Templates
It is a very common pattern, especially when dealing with embedded messages, to provide a "template" that can later be edited to fit a specific use-case. Using `Consumer#andThen` allows this pattern to be implemented easily:
```java
Consumer<EmbedCreateSpec> template = spec -> {
    // Edit the spec as you normally would
};
...
// embedSpec can be edited as you normally would, but the edits from template will already be applied
Mono<Message> message = messageChannel.createMessage(messageSpec -> messageSpec.setEmbed(template.andThen(embedSpec -> {})));
```
This pattern additionally helps protect the end-user from accidentally sharing specs across multiple invocations, as the state is never "reset" and mutating `Spec` instances is **not** thread-safe.