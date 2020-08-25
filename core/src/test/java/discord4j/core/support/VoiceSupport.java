package discord4j.core.support;

import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame;
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
import discord4j.discordjson.json.MessageCreateRequest;
import discord4j.voice.AudioProvider;
import discord4j.voice.VoiceConnection;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class VoiceSupport {

    private static final Logger log = Loggers.getLogger(VoiceSupport.class);

    private final GatewayDiscordClient client;

    public static VoiceSupport create(GatewayDiscordClient client) {
        return new VoiceSupport(client);
    }

    VoiceSupport(GatewayDiscordClient client) {
        this.client = client;
    }

    public Mono<Void> eventHandlers() {
        return voiceHandler(client);
    }

    public static Mono<Void> voiceHandler(GatewayDiscordClient client) {
        AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
        playerManager.getConfiguration().setFrameBufferFactory(NonAllocatingAudioFrameBuffer::new);
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioPlayer player = playerManager.createPlayer();
        AudioProvider provider = new LavaplayerAudioProvider(player);

        Mono<Void> join = client.getEventDispatcher().on(MessageCreateEvent.class)
                .filter(e -> e.getMessage().getContent().equals("!join"))
                .doOnNext(e -> log.info("Received voice join request"))
                .flatMap(e -> Mono.justOrEmpty(e.getMember())
                        .flatMap(Member::getVoiceState)
                        .flatMap(VoiceState::getChannel)
                        .flatMap(channel -> channel.join().withProvider(provider))
                        .doFinally(s -> log.info("Finalized join request after {}", s))
                        .onErrorResume(t -> {
                            log.error("Failed to join voice channel", t);
                            return Mono.empty();
                        }))
                .then();

        Mono<Void> leave = client.getEventDispatcher().on(MessageCreateEvent.class)
                .filter(e -> e.getMessage().getContent().equals("!leave"))
                .doOnNext(e -> log.info("Received voice leave request"))
                .flatMap(e -> Mono.justOrEmpty(e.getMember())
                        .flatMap(Member::getVoiceState)
                        .flatMap(vs -> client.getVoiceConnectionRegistry()
                                .getVoiceConnection(vs.getGuildId())
                                .doOnSuccess(vc -> {
                                    if (vc == null) {
                                        log.info("No voice connection to leave!");
                                    }
                                }))
                        .flatMap(VoiceConnection::disconnect))
                .then();

        Mono<Void> reconnect = client.getEventDispatcher().on(MessageCreateEvent.class)
                .filter(e -> e.getMessage().getContent().equals("!vcretry"))
                .flatMap(e -> Mono.justOrEmpty(e.getMember())
                        .flatMap(Member::getVoiceState)
                        .flatMap(vs -> client.getVoiceConnectionRegistry()
                                .getVoiceConnection(vs.getGuildId()))
                        .flatMap(VoiceConnection::reconnect)
                        .doFinally(s -> log.info("Reconnect event handle complete")))
                .then();

        Mono<Void> play = client.getEventDispatcher().on(MessageCreateEvent.class)
                .filter(e -> e.getMessage().getContent().startsWith("!play "))
                .flatMap(e -> Mono.justOrEmpty(e.getMessage().getContent())
                        .map(content -> Arrays.asList(content.split(" ")))
                        .doOnNext(command -> playerManager.loadItem(command.get(1),
                                new MyAudioLoadResultHandler(player))))
                .then();

        Mono<Void> stop = client.getEventDispatcher().on(MessageCreateEvent.class)
                .filter(e -> e.getMessage().getContent().equals("!stop"))
                .doOnNext(e -> player.stopTrack())
                .then();

        Mono<Void> currentGuild = client.getEventDispatcher().on(MessageCreateEvent.class)
                .filter(e -> e.getMessage().getContent().equals("!vcguild"))
                .flatMap(e -> Mono.justOrEmpty(e.getMember())
                        .flatMap(Member::getVoiceState)
                        .flatMap(vs -> e.getMessage().getRestChannel().createMessage(
                                MessageCreateRequest.builder()
                                        .content(vs.getGuildId().asString())
                                        .build())))
                .then();

        return Mono.zip(join, leave, reconnect, play, stop, currentGuild, client.onDisconnect()).then();
    }

    private static class LavaplayerAudioProvider extends AudioProvider {

        private final AudioPlayer player;
        private final MutableAudioFrame frame = new MutableAudioFrame();

        private LavaplayerAudioProvider(AudioPlayer player) {
            super(ByteBuffer.allocate(StandardAudioDataFormats.DISCORD_OPUS.maximumChunkSize()));
            this.player = player;
            this.frame.setBuffer(getBuffer());
        }

        @Override
        public boolean provide() {
            boolean didProvide = player.provide(frame);
            if (didProvide) {
                getBuffer().flip();
            }
            return didProvide;
        }
    }

    private static class MyAudioLoadResultHandler implements AudioLoadResultHandler {

        private final AudioPlayer player;

        private MyAudioLoadResultHandler(AudioPlayer player) {
            this.player = player;
        }

        @Override
        public void trackLoaded(AudioTrack track) {
            player.playTrack(track);
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
    }
}
