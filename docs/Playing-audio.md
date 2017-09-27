## Playing audio in D4J

This is a short and sweet guide on how to play a simple file using the built in D4J utilities, which tends to work for most people. If you want a more complex solution but can support a wider range of input sources (YouTube, etc.) and can result in better playback on poorer connections, check out https://github.com/sedmelluq/lavaplayer written by sedmelluq.

Small disclaimer: As of the current version the built in D4J audio system has an issue where it stutters very slightly as the song is playing. This is not the case at all using Lavaplayer, so for flawless playback it's advised to use that.

### Code and explanation

As usual, the majority of the other code required for running this code is from the [[Basic bot]] example. (Songs used are kept in a folder called "music" at the root of the project, the songs used in the in the example image are by Kill Evan, https://killevan.bandcamp.com/)

```java
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.util.audio.AudioPlayer;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class CommandHandler {

    // A static map of commands mapping from command string to the functional impl
    private static Map<String, Command> commandMap = new HashMap<>();

    // Statically populate the commandMap with the intended functionality
    // Might be better practise to do this from an instantiated objects constructor
    static {

        // If the IUser that called this is in a voice channel, join them
        commandMap.put("joinvoice", (event, args) -> {

            IVoiceChannel userVoiceChannel = event.getAuthor().getVoiceStateForGuild(event.getGuild()).getChannel();

            if(userVoiceChannel == null)
                return;

            userVoiceChannel.join();

        });

        commandMap.put("leavevoice", (event, args) -> {

            IVoiceChannel botVoiceChannel = event.getClient().getOurUser().getVoiceStateForGuild(event.getGuild()).getChannel();

            if(botVoiceChannel == null)
                return;

            botVoiceChannel.leave();

        });

        // Plays the first song found containing the first arg
        commandMap.put("playsong", (event, args) -> {

            IVoiceChannel botVoiceChannel = event.getClient().getOurUser().getVoiceStateForGuild(event.getGuild()).getChannel();

            if(botVoiceChannel == null) {
                BotUtils.sendMessage(event.getChannel(), "Not in a voice channel, join one and then use joinvoice");
                return;
            }

            // Turn the args back into a string separated by space
            String searchStr = String.join(" ", args);

            // Get the AudioPlayer object for the guild
            AudioPlayer audioP = AudioPlayer.getAudioPlayerForGuild(event.getGuild());

            // Find a song given the search term
            File[] songDir = new File("music")
                    .listFiles(file -> file.getName().contains(searchStr));

            if(songDir == null || songDir.length == 0)
                return;

            // Stop the playing track
            audioP.clear();

            // Play the found song
            try {
                audioP.queue(songDir[0]);
            } catch (IOException | UnsupportedAudioFileException e) {
                BotUtils.sendMessage(event.getChannel(), "There was an issue playing that song.");
                e.printStackTrace();
            }

            BotUtils.sendMessage(event.getChannel(), "Now playing: " + songDir[0].getName());

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

The above code produces the following output and plays the songs correctly: ![Example output](http://i.imgur.com/6SSvtMR.png)
