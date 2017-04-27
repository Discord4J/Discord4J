## Basic bot command structure

This page is somewhat of a follow on from the [[Basic bot]] page. Full code will can be seen below but will demonstrate a number of basic command systems that could be used. Note that this is a pretty diverse topic and many more implementations exist using other concepts such as annotations etc, this page is just a few examples to get you started. Listed from basic to most complex.

---

## Handling input

There are a variety of ways to effectively handle input, the simplest is to "tokenize" the message a user inputs, check it for a prefix, check the "command" associated with it and then pass the args, if any, to a function to handle that input. Note that the rest of the bot is the same as the code examples in [[Basic bot]]

### With string splitting and switching

CommandHandler.java (Drop in replacement for the basic one)
```java
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

import java.util.*;

public class CommandHandler {

    @EventSubscriber
    public void onMessageReceived(MessageReceivedEvent event){

        // Note for error handling, you'll probably want to log failed commands with a logger or sout
        // In most cases it's not advised to annoy the user with a reply incase they didn't intend to trigger a
        // command anyway, such as a user typing ?notacommand, the bot should not say "notacommand" doesn't exist in
        // most situations. It's partially good practise and partially developer preference

        // Given a message "/test arg1 arg2", argArray will contain ["/test", "arg1", "arg"]
        String[] argArray = event.getMessage().getContent().split(" ");

        // First ensure at least the command and prefix is present, the arg length can be handled by your command func
        if(argArray.length == 0)
            return;

        // Check if the first arg (the command) starts with the prefix defined in the utils class
        if(!argArray[0].startsWith(BotUtils.BOT_PREFIX))
            return;

        // Extract the "command" part of the first arg out by just ditching the first character
        String commandStr = argArray[0].substring(1);

        // Load the rest of the args in the array into a List for safer access
        List<String> argsList = new ArrayList<>(Arrays.asList(argArray));
        argsList.remove(0); // Remove the command

        // Begin the switch to handle the string to command mappings. It's likely wise to pass the whole event or
        // some part (IChannel) to the command handling it
        switch (commandStr) {

            case "test":
                testCommand(event, argsList);
                break;

        }
    }


    private void testCommand(MessageReceivedEvent event, List<String> args){

        BotUtils.sendMessage(event.getChannel(), "You ran the test command with args: " + args);

    }

}
```

The above code will produce the following output:
![Example of output](http://i.imgur.com/SIjDDEm.png)

More commands can be added by adding more switch cases

---

## Handling commands

This section will use string splitting to get the prefix, command and args as it's the simplest.

### Switch or if statement

This is the most basic command structure and whilst it's fast and easy to get going, it's not easily extendable or maintainable. So if you intend to use a system of many (>10) commands it's advised to see another route

See above for a switch example.


### Map of commands using an arbitrary command interface

Using a map allows for a far more modular approach, severely reducing the amount of manual boilerplate a developer has to write for various commands.

Command.java
```java
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

import java.util.List;

public interface Command {

    // Interface for a command to be implemented in the command map
    void runCommand(MessageReceivedEvent event, List<String> args);

}
```

CommandHandler.java
```java
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

import java.util.*;

public class CommandHandler {

    // A static map of commands mapping from command string to the functional impl
    private static Map<String, Command> commandMap = new HashMap<>();

    // Statically populate the commandMap with the intended functionality
    // Might be better practise to do this from an instantiated objects constructor
    static {

        commandMap.put("testcommand", (event, args) -> {
            BotUtils.sendMessage(event.getChannel(), "You ran the test command with args: " + args);
        });

        commandMap.put("ping", (event, args) -> {
            BotUtils.sendMessage(event.getChannel(), "pong");
        });

    }

    @EventSubscriber
    public void onMessageReceived(MessageReceivedEvent event){

        // Note for error handling, you'll probably want to log failed commands with a logger or sout
        // In most cases it's not advised to annoy the user with a reply incase they didn't intend to trigger a
        // command anyway, such as a user typing ?notacommand, the bot should not say "notacommand" doesn't exist in
        // most situations. It's partially good practise and partially developer preference

        // Given a message "/test arg1 arg2", argArray will contain ["/test", "arg1", "arg"]
        String[] argArray = event.getMessage().getContent().split(" ");

        // First ensure at least the command and prefix is present, the arg length can be handled by your command func
        if(argArray.length == 0)
            return;

        // Check if the first arg (the command) starts with the prefix defined in the utils class
        if(!argArray[0].startsWith(BotUtils.BOT_PREFIX))
            return;

        // Extract the "command" part of the first arg out by just ditching the first character
        String commandStr = argArray[0].substring(1);

        // Load the rest of the args in the array into a List for safer access
        List<String> argsList = new ArrayList<>(Arrays.asList(argArray));
        argsList.remove(0); // Remove the command

        // Instead of delegating the work to a switch, automatically do it via calling the mapping if it exists

        if(commandMap.containsKey(commandStr))
            commandMap.get(commandStr).runCommand(event, argsList);

    }

}
```
(The above example takes advantage of lambdas from Java 8)

The above code will produce the following output:
![Example of output](http://i.imgur.com/vNIh4rj.png)

As usual, small disclaimer, this is not "the best code" this is purely intended to show some decent practises in handling commands without too much overhead, other solutions may be better for your use case.

---

