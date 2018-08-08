![Discord4J Logo](/images/d4j_l.png?raw=true)

# Discord4J [![Maven Central](https://img.shields.io/maven-central/v/com.discord4j/Discord4J/2.svg?style=flat-square)](https://mvnrepository.com/artifact/com.discord4j/Discord4J)  [![JCenter](https://img.shields.io/bintray/v/austinv11/maven/Discord4J.svg?style=flat-square)](https://bintray.com/austinv11/maven/Discord4J/_latestVersion)  [![Support Server Invite](https://img.shields.io/badge/Join-Discord4J-7289DA.svg?style=flat-square&logo=discord)](https://discord.gg/NxGAeCY) [![Documentation Status](https://img.shields.io/readthedocs/discord4j.svg?style=flat-square)](http://discord4j.readthedocs.io/en/latest/) [![Build Status](https://img.shields.io/circleci/project/github/Discord4J/Discord4J/master.svg?style=flat-square)](https://circleci.com/gh/Discord4J/Discord4J/tree/master)

Java interface for the official [Discord](https://discordapp.com/) API, written in Java 8.
[The API is also available in a few other languages.](https://discordapi.com/unofficial/libs.html)

For the latest dev builds, use [Jitpack](https://jitpack.io/#Discord4J/Discord4J).

## Adding Discord4J as a dependency for a project
Given that `@VERSION@` = the version of Discord4J.
### With Maven
In your `pom.xml` add:
```xml
<repositories>
  <repository>
    <id>jcenter</id>
    <url>http://jcenter.bintray.com</url>
  </repository>
</repositories>

<dependencies>
  <dependency>
    <groupId>com.discord4j</groupId> <!-- However our packages are all under sx.blah in version 2.x! -->
    <artifactId>Discord4J</artifactId>
    <version>@VERSION@</version>
  </dependency>
</dependencies>
```
### With Gradle
In your `build.gradle` add: 
```groovy
repositories {
  jcenter()
}

dependencies {
  compile "com.discord4j:Discord4J:@VERSION@"
}
```
### With SBT
In your `build.sbt` add:
```sbt
libraryDependencies ++= Seq(
  "com.discord4j" % "Discord4J" % "@VERSION@"
)

resolvers += "jcenter" at "http://jcenter.bintray.com"
```
### Manually with the shaded jar
If you don't use Maven nor Gradle (which you really should, because it's a lot more flexible and allows you to update easily), you can always [grab the shaded jar file](https://discord4j.com/downloads.html) (which has all the D4J dependencies inside), and link it in your IntelliJ or Eclipse project.
#### IntelliJ
Module Settings > Dependencies > click the + > JARs or directories > Select your JAR file
#### Eclipse
Project Properties > Java Build Path > Add the jar file

## So, how do I use this?
### Tutorials/Resources
* A [quick overview of the AudioPlayer](https://github.com/oopsjpeg/d4j-audioplayer) by [@oopsjpeg](https://github.com/oopsjpeg)
* A Discord Bot [quick start example](https://gist.github.com/quanticc/a32fa8f3a57f98aee9dc9e935f851e72) maintined by [@quantic](https://github.com/quanticc)
* A simple [Discord4J module example](https://github.com/Martacus/Simplecommands/tree/master) by [@Martacus](https://github.com/Martacus)
* The [Official Javadocs](https://discord4j.com/docs.html) (or the [Dash](https://kapeli.com/dash)/[Velocity](https://velocity.silverlakesoftware.com/)/[Zeal](https://zealdocs.org/) mirror maintained by [@jammehcow](https://github.com/jammehcow))

### Starting with the API
The very first thing you need to do is obtain an `IDiscordClient` object. This can be done by using the `ClientBuilder`.
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
            e.printStackTrace();
            return null;
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
        foo(); // Will be called!
    }
  
    public void onMessageReceivedEvent(MessageReceivedEvent event) { // This method is NOT called because it doesn't have the @EventSubscriber annotation
        bar(); // Never called!
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

## Deprecation policy
Due to the nature of the Discord API, any deprecations found in the API should not be expected to last past the current
version. Meaning that if a method is deprecated on version 2.1.0, do not assume the method will be available in version 2.2.0.

## Development
The Discord API is still in development. Functions may break at any time.  
In such an event, please contact me or submit a pull request.

## Pull requests
No one is perfect at programming and I am no exception. If you see something that can be improved, please read the [contributing guildelines](https://github.com/austinv11/Discord4J/blob/master/.github/CONTRIBUTING.md) and feel free to submit a pull request! 

## Other info
More information can be found in the official [javadocs](https://discord4j.com/docs.html).
Alternatively you can view the docs through [Dash](https://kapeli.com/dash), [Velocity](https://velocity.silverlakesoftware.com/), or [Zeal](https://zealdocs.org/) (maintained by [@jammehcow](https://github.com/jammehcow)) under the *User Contributed* tab.

You can contact me on the [Official Discord4J Server (recommended)](https://discord.gg/NxGAeCY) or the [Discord API server](https://discord.gg/discord-api) in the #java_discord4j channel.
