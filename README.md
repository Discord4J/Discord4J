<img align="right" src="http://i.imgur.com/quyShBK.png" height="320" width="320">
# Discord4J [![CircleCI](https://circleci.com/gh/austinv11/Discord4J/tree/dev.svg?style=svg)](https://circleci.com/gh/austinv11/Discord4J/tree/dev) [![Download](https://jitpack.io/v/austinv11/Discord4j.svg?style=flat-square)](https://jitpack.io/#austinv11/Discord4j) [![Coverage Status](https://coveralls.io/repos/github/austinv11/Discord4J/badge.svg?branch=dev)](https://coveralls.io/github/austinv11/Discord4J?branch=dev) [![Dependency Status](https://www.versioneye.com/user/projects/57b27840b9c824005436e491/badge.svg?style=flat-square)](https://www.versioneye.com/user/projects/57b27840b9c824005436e491) [![Support Server Invite](https://img.shields.io/badge/Join-Discord4J-7289DA.svg?style=flat-square)](https://discord.gg/NxGAeCY)

Java interface for the official [Discord](https://discordapp.com/) API, written in Java 8.
[The API is also available in a few other languages.](https://discordapi.com/unofficial/libs.html)

For the latest dev builds, use the short commit hash or `dev-SNAPSHOT` as your version.

## Adding Discord4J as a dependency for a project
Given that `@VERSION@` = the version of Discord4J (this can either be a release version, the short commit hash or `dev-SNAPSHOT`).
### With Maven
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
* A [quick overview of the AudioPlayer](https://github.com/oopsjpeg/d4j-audioplayer) by @oopsjpeg
* A Discord Bot [quick start example](https://gist.github.com/quanticc/a32fa8f3a57f98aee9dc9e935f851e72) maintined by @quantic
* A simple [Discord4J module example](https://github.com/Martacus/Simplecommands/tree/master) by @Martacus 

### Starting with the API
The very first thing you need to do is obtain a "DiscordClient" object. This can be done by using the `ClientBuilder`.
Example:
```java
public class Example {

  public static IDiscordClient createClient(String token, boolean login) { // Returns a new instance of the Discord client
    ClientBuilder clientBuilder = new ClientBuilder(); // Creates the ClientBuilder instance
    clientBuilder.withToken(token); // Adds the login info to the builder
    try {
		if (login) {
      		return clientBuilder.login(); // Creates the client instance and logs the client in
    	} else {
      		return clientBuilder.build(); // Creates the client instance but it doesn't log the client in yet, you would have to call client.login() yourself
    	}
	} catch (DiscordException e) { // This is thrown if there was a problem building the client
		e.printStackTrace()
	}
  }
}
```
### Events
The Discord4J library is very event driven. Your bot can detect these events through the use of an event listener. There are two ways of creating an event listener:

1. Using `IListener`:
```java
public class InterfaceListener implements IListener<ReadyEvent> { // The event type in IListener<> can be any class which extends Event
  
  @Override
  public void handle(ReadyEvent event) { // This is called when the ReadyEvent is dispatched
    doCoolStuff();
  }
}
```

2. Using the `@EventSubscriber` annotation:
```java
public class AnnotationListener {
  
  @EventSubscriber
  public void onReadyEvent(ReadyEvent event) { // This method is called when the ReadyEvent is dispatched
    foo();
  }
  
  public void onMessageReceivedEvent(MessageReceivedEvent event) { // This method is NOT called because it doesn't have the @EventSubscriber annotation
    bar();
  }
}
```

Registering your listener:
```java
public class Main {
  
  public static void main(String[] args) {
    IDiscordClient client = Example.createClient(args[0], true); // Gets the client object (from the first example)
    EventDispatcher dispatcher = client.getDispatcher(); // Gets the EventDispatcher instance for this client instance
    dispatcher.registerListener(new InterfaceListener()); // Registers the IListener example class from above
    dispatcher.registerListener(new AnnotationListener()); // Registers the @EventSubscriber example class from above
  }
}
```

### Modules
Discord4J has an API for creating modular Discord Bots! See [Martacus's sample repo](https://github.com/Martacus/Simplecommands/tree/master) for an example as to how it works.

### More examples
See the [examples directory](https://github.com/austinv11/Discord4J/tree/master/src/test/java/sx/blah/discord/examples).

## Projects using Discord4J
* Official Discord4J Addons: A collection of official addons and modules for Discord4J (https://github.com/Discord4J-Addons)
* D4J/JDA Player by @DV8FromTheWorld: A full-featured audio player for Discord4J and JDA (https://github.com/DV8FromTheWorld/JDA-Player)
* Discordinator by @alpha;helix: A modularized command API (https://github.com/alphahelix00/Discordinator)
* Instructability by @Kaioru: A simple command API (https://github.com/Kaioru/Instructability)

## Deprecation policy
Due to the nature of the Discord API, any deprecations found in the API should not be expected to last past the current
 version. Meaning that if a method is deprecated on version 2.1.0, do not assume the method will be available in version 2.2.0.

## Development
The Discord API is still in development. Functions may break at any time.  
In such an event, please contact me or submit a pull request.

## Pull requests
No one is perfect at programming and I am no exception. If you see something that can be improved, please read the [contributing guildelines](https://github.com/austinv11/Discord4J/blob/master/.github/CONTRIBUTING.md) and feel free to submit a pull request! 

## Other info
More information can be found in the [javadocs](http://austinv11.github.io/Discord4J/docs.html). 

You can contact me on the [Official Discord4J Server (recommended)](https://discord.gg/NxGAeCY) or the [Discord API server](https://discord.gg/0SBTUU1wZTU7PCok) in the #java_discord4j channel.
