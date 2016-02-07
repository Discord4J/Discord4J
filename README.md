# Discord4J  [![Build Status](https://drone.io/github.com/austinv11/Discord4J/status.png)](https://drone.io/github.com/austinv11/Discord4J/latest) [ ![Download](https://api.bintray.com/packages/austinv11/maven/Discord4J/images/download.svg) ](https://bintray.com/austinv11/maven/Discord4J/_latestVersion)

Java interface for the unofficial [Discord](https://discordapp.com/) API, written in Java 8.
[The API is also available in a few other languages.](https://discordapi.com/unofficial/libs.html)

For the latest dev builds, [download it from my ci server.](https://drone.io/github.com/austinv11/Discord4J/files)

## Adding Discord4J as a dependency for a project
Given that `@VERSION@` = the latest version of Discord4J.
### With maven
In your `pom.xml` add:
```xml
...
<repositories>
  ...
  <repository>
    <snapshots>
      <enabled>false</enabled>
    </snapshots>
    <id>bintray-austinv11-maven</id>
    <name>bintray</name>
    <url>http://dl.bintray.com/austinv11/maven</url>
  </repository>
</repositories>
...
<dependencies>
  ...
  <dependency>
    <groupId>sx.blah</groupId>
    <artifactId>Discord4J</artifactId>
    <version>@VERSION@</version>
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
  maven {
    url  "http://dl.bintray.com/austinv11/maven" 
  }
}
...
dependencies {
  ...
  compile "sx.blah:Discord4J:@VERSION@"
}
...
```
## So, how do I use this?
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
.2 Using the `@EventSubscriber` annotation:
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

## Deprecation policy
Due to the nature of the discord api, any deprecations found in the api should not be expected to last past the current version. Meaning that if a method is deprecated on version 2.1.0, do not assume the method will be available in version 2.2.0.

## Development
The Discord API is still in development. Functions may break at any time.  
In such an event, please contact me or submit a pull request.

## Pull requests
No one is perfect at programming and I am no exception. If you see something that can be improved, please feel free to submit a pull request! 

## Other info
More information can be found in the [docs](http://austinv11.github.io/Discord4J/docs.html). 
You can contact me on the [Discord API server](https://discord.gg/0SBTUU1wZTU7PCok) in the #java_discord4j channel.
