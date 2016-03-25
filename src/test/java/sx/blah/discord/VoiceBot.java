package sx.blah.discord;

import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.IListener;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.VoiceUserSpeakingEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IVoiceChannel;

public class VoiceBot {
    public static void main(String... args) {
        try {
            IDiscordClient client = new ClientBuilder().withLogin(args[0] /* username */, args[1] /* password */).login();
            client.getAudioChannel().queueFile(args[3]);

            client.getDispatcher().registerListener(new IListener<ReadyEvent>() {


				@Override
                public void handle(ReadyEvent event) {
                    IGuild guild = client.getGuilds().get(1);
                    IVoiceChannel voiceChannel = guild.getVoiceChannels().get(0);
                    voiceChannel.join();
                }
            });

            client.getDispatcher().registerListener(new IListener<VoiceUserSpeakingEvent>() {
                @Override
                public void handle(VoiceUserSpeakingEvent event) {
                    try {
                        if (event.isSpeaking())
                            client.getOrCreatePMChannel(event.getUser()).sendMessage("You are speaking");
                        else
                            client.getOrCreatePMChannel(event.getUser()).sendMessage("You stopped speaking");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (DiscordException e) {
            e.printStackTrace();
        }
    }
}
