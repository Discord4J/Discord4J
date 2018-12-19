package discord4j.core;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEvent;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventListener;
import com.sedmelluq.discord.lavaplayer.player.event.TrackEndEvent;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.VoiceChannel;
import discord4j.core.object.util.Snowflake;
import discord4j.voice.AudioProvider;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyTest {

    private static final byte[] SILENCE = {(byte) 0xF8, (byte) 0xFF, (byte) 0xFE};

    public static void main(String[] args) {
        Hooks.onOperatorDebug();

        AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioPlayer audioPlayer = playerManager.createPlayer();

        AtomicBoolean playing = new AtomicBoolean(false);
        AudioLoadResultHandler resHandler = new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                audioPlayer.playTrack(track);
                playing.set(true);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {

            }

            @Override
            public void noMatches() {

            }

            @Override
            public void loadFailed(FriendlyException exception) {

            }
        };

        Queue<String> queue = new LinkedList<>();
        Pattern playPattern = Pattern.compile("^!play <?([^<>]+)>?$");
        Pattern skipPattern = Pattern.compile("!skip");

        DiscordClient client = new DiscordClientBuilder("MjI5MDY0MzgyOTA1MDU3Mjgx.DtEVgw.5y7JXsEktr6xaBss_hGtsTskCM8").build();

        client.getEventDispatcher().on(GuildCreateEvent.class)
                .filter(g -> g.getGuild().getId().asLong() == 208023865127862272L)
                .flatMap(g -> client.getChannelById(Snowflake.of(208298176782794754L)).ofType(VoiceChannel.class))
                .flatMap(vc -> vc.join(false, false, new LavaplayerAudioProvider(audioPlayer)))
                .subscribe();

        client.getEventDispatcher().on(MessageCreateEvent.class)
                .filter(m -> m.getGuildId().map(it -> it.asLong() == 208023865127862272L).orElse(false))
                .map(MessageCreateEvent::getMessage)
                .flatMap(it -> Mono.justOrEmpty(it.getContent()))
                .doOnNext(c -> {
                    Matcher m = playPattern.matcher(c);
                    if (m.matches()) {
                        String url = m.group(1);
                        if (queue.isEmpty() && !playing.get()) {
                            playerManager.loadItem(url, resHandler);
                        } else {
                            queue.add(url);
                        }
                    } else {
                        m = skipPattern.matcher(c);
                        if (m.matches()) {
                            audioPlayer.stopTrack();
                            if (!queue.isEmpty()) {
                                String next = queue.remove();
                                playerManager.loadItem(next, resHandler);
                            }
                        }
                    }
                })
                .subscribe();

        audioPlayer.addListener(new AudioEventListener() {
            @Override
            public void onEvent(AudioEvent event) {
                if (event instanceof TrackEndEvent) {
                    if (!queue.isEmpty()) {
                        String next = queue.remove();
                        playerManager.loadItem(next, resHandler);
                    } else {
                        playing.set(false);
                    }
                }
            }
        });

        client.login().block();
    }

    static final class LavaplayerAudioProvider implements AudioProvider {

        private final AudioPlayer audioPlayer;
        private AudioFrame lastFrame;

        public LavaplayerAudioProvider(AudioPlayer audioPlayer) {
            this.audioPlayer = audioPlayer;
        }

        @Override
        public boolean provide(ByteBuffer buf) {
            if (lastFrame == null && (lastFrame = audioPlayer.provide()) == null) {
                return false;
            }

            byte[] data = lastFrame.getData();
            lastFrame = null;
            buf.put(data);
            buf.flip();
            return true;
        }
    }
}
