## RateLimitExceptions and you

### What are rate limits and RLEs?

Rate limits are Discord's way of preventing one user from using up too many resources, it's done to provide a fair and equal service to all users. In terms of technical details, for every rate-limited request made to the API, the response includes a header which tells the API when it can make the next call. 

### How do I fix them?

In D4J any method that throws an unchecked `RateLimitException` is ratelimited. Thankfully D4J provides a helpful class, `RequestBuffer` and looks like so:
```java
RequestBuffer.request(() -> {
    // Ratelimited methods go here
});
``` 
It works by catching any `RateLimitException`s that are thrown by the methods inside and then retrying the request when it can. It's generally not advised to put multiple methods that talk to the same endpoint and are ratelimited in the same buffer. For example, this is potentially bad practise:
```java
RequestBuffer.request(() -> {
    // This is bad, if the second message send fails it will retry both!
    channel.sendMessage(message);
    channel.sendMessage(message2);
});
``` 

Please note that if you do intend to catch the `RateLimitException` inside the buffer (say, for logging purposes) make sure to throw it back up (otherwise the RequestBuffer can't catch it!), eg:

```java
RequestBuffer.request(() -> {
    try{
        channel.sendMessage(message);
     } catch (RateLimitException e){
        System.out.println("Do some logging");
        throw e; // This makes sure that RequestBuffer will do the retry for you
    }
});
``` 

Last of all, if you want to actually use the result of the RequestBuffer by blocking until it returns you can do so by just returning the type you wish to return and the lambda will infer it, as usual this is just a more-easily-readable version of instantiating a IRequest implementation from the RequestBuffer class:

```java
IMessage returnedMessage = RequestBuffer.request(() -> {
    return IDiscordClient.getMessageByID(someID);
}).get();
```
#### RequestBuilder

Another alternative to solving RLEs is to use the `RequestBuilder` class. The main advantage to `RequestBuilder` over `RequestBuffer` is `RequestBuilder` will automatically log any exceptions thrown for you versus either letting exceptions be thrown or you having to do so manually.

```java
RequestBuilder builder = new RequestBuilder(client);
builder.shouldBufferRequests(true);
// You may optionally setup management to particular exceptions using methods such as onDiscordError(), onMissingPermissionsError(), etc.
builder.doAction(() -> {
    // Some code here
    // Return true or false depending if the action "succeeded"
}).execute();
// Optionally, you can even chain more actions by using the andThen() method before the execute()!
```
