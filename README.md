# Discord4J  [![Build Status](https://drone.io/github.com/austinv11/Discord4J/status.png)](https://drone.io/github.com/austinv11/Discord4J/latest) [![Download](https://jitpack.io/v/austinv11/Discord4j.svg?style=flat-square)](https://jitpack.io/#austinv11/Discord4j)

Java interface for the unofficial [Discord](https://discordapp.com/) API, written in Java 8.
[The API is also available in a few other languages.](https://discordapi.com/unofficial/libs.html)

For the latest dev builds, [download it from my ci server.](https://drone.io/github.com/austinv11/Discord4J/files)

## Adding Discord4J as a dependency for a project
Given that `@VERSION@` = the a version of Discord4J (this can either be a release version, the short commit hash or `dev-SNAPSHOT`).
### With maven
In your `pom.xml` add:
```xml
...
<repositories>
  ...
  <repository> <!-- This repo fixes issues with transitive dependencies -->
    <id>jcenter</id>
    <url>http://jcenter.bintray.com</url>
  </repository>
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>
...
<dependencies>
  ...
  <dependency>
    <groupId>com.github.austinv11</groupId>
    <artifactId>Discord4j</artifactId>
    <version>@VERSION@</version>
   <!-- <classifier>shaded</classifier> <!-- Include this line if you want a shaded jar (all the Discord4J dependencies bundled into one jar)-->
  </dependency>
</dependencies>
...
```
### With Gradle
In your `build.gradle` add:
```groovy
...
repositories {
  ...
  jcenter() //This prevents issues with transitive dependencies
  maven {
    url  "https://jitpack.io"
  }
}
...
dependencies {
  ...
  compile "com.github.austinv11:Discord4j:@VERSION@"
  //compile "com.github.austinv11:Discord4j:@VERSION@:shaded" //Use this line instead of the one above it if you want a shaded jar (all the Discord4J dependencies bundled into one jar)
}
...
```
## So, how do I use this?
### Tutorials/Resources
* The [Discord4J Tutorial Series](http://blog.darichey.com/) maintained by @Panda
* The [Discord4J Read The Docs](https://discord4j.readthedocs.org/en/latest/index.html) maintained by @TheFjong.
* A Discord Bot [quick start example](https://gist.github.com/iabarca/a32fa8f3a57f98aee9dc9e935f851e72) maintined by @quantic
* A simple [Discord4J module example](https://github.com/Martacus/Simplecommands/tree/master) by @Martacus 

### Starting with the API
The very first thing you need to do is obtain a "DiscordClient" object. This can be done by using the `ClientBuilder`.
Example:
```java
public class Example {

  public static IDiscordClient getClient(String email, String password, boolean login) { //Returns an instance of the discord client
    ClientBuilder clientBuilder = new ClientBuilder(); //Creates the ClientBuilder instance
    clientBuilder.withLogin(email, password); //Adds the login info to the builder
    if (login) {
      return clientBuilder.login(); //Creates the client instance and logs the client in
    } else {
      return clientBuilder.build(); //Creates the client instance but it doesn't log the client in yet, you would have to call client.login() yourself
    }
  }
}
```
### Events
The Discord4J library is very event driven. Your bot can detect these events through the use of an event listener. There are two ways of creating an event listener:

1. Using `IListener`:
```java
public class InterfaceListener implements IListener<ReadyEvent> { //The event type in IListener<> can be any class which extends Event
  
  @Override
  public void handle(ReadyEvent event) { //This is called when the ReadyEvent is dispatched
    doCoolStuff();
  }
}
```

2. Using the `@EventSubscriber` annotation:
```java
public class AnnotationListener {
  
  @EventSubscriber
  public void onReadyEvent(ReadyEvent event) { //This method is called when the ReadyEvent is dispatched
    foo();
  }
  
  public void onMessageReceivedEvent(MessageReceivedEvent event) { //This method is NOT called because it doesn't have the @EventSubscriber annotation
    bar();
  }
}
```

Registering your listener:
```java
public class Main {
  
  public static void main(String[] args) {
    IDiscordClient client = Example.getClient(args[0], args[1], true); //Gets the client object (from the first example)
    EventDispatcher dispatcher = client.getDispatcher(); //Gets the EventDispatcher instance for this client instance
    dispatcher.registerListener(new InterfaceListener()); //Registers the IListener example class from above
    dispatcher.registerListener(new AnnotationListener()); //Registers the @EventSubscriber example class from above
  }
}
```

### Modules
Discord4J has an api for creating modular Discord Bots! See [Martacus's sample repo](https://github.com/Martacus/Simplecommands/tree/master) for an example as to how it works.

### More examples
See the [examples directory](https://github.com/austinv11/Discord4J/tree/master/src/test/java/sx/blah/discord/examples).

## Projects using Discord4J
* Instructability by @Kaioru: A simple command API (https://github.com/Kaioru/Instructability)

## Deprecation policy
Due to the nature of the discord api, any deprecations found in the api should not be expected to last past the current version. Meaning that if a method is deprecated on version 2.1.0, do not assume the method will be available in version 2.2.0.

## Development
The Discord API is still in development. Functions may break at any time.  
In such an event, please contact me or submit a pull request.

## Pull requests
No one is perfect at programming and I am no exception. If you see something that can be improved, please read the [contributing guildelines](https://github.com/austinv11/Discord4J/blob/master/.github/CONTRIBUTING.md) and feel free to submit a pull request! 

## Other info
More information can be found in the [docs](http://austinv11.github.io/Discord4J/docs.html). 
You can contact me on the [Discord API server](https://discord.gg/0SBTUU1wZTU7PCok) in the #java_discord4j channel.
