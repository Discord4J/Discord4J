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
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import discord4j.discordjson.json.ApplicationInfoData;
import discord4j.discordjson.possible.Possible;
import discord4j.voice.AudioProvider;
import discord4j.voice.VoiceConnection;
import discord4j.voice.retry.VoiceGatewayReconnectException;
import discord4j.voice.retry.VoiceGatewayResumeException;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;
import reactor.util.retry.Retry;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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
        Mono<Long> ownerId = client.rest().getApplicationInfo()
                .map(ApplicationInfoData::owner)
                .map(Possible::toOptional)
                .flatMap(Mono::justOrEmpty)
                .map(user -> Snowflake.asLong(user.id()))
                .cache();

        Map<Snowflake, GuildVoiceSupport> voiceGuildMap = new ConcurrentHashMap<>();

        List<EventHandler> eventHandlers = new ArrayList<>();
        eventHandlers.add(new Join(voiceGuildMap));
        eventHandlers.add(new Leave(client));
        eventHandlers.add(new Reconnect(client));
        eventHandlers.add(new Resume(client));
        eventHandlers.add(new Play(voiceGuildMap));
        eventHandlers.add(new Stop(voiceGuildMap));

        return client.on(MessageCreateEvent.class,
                        event -> ownerId.filter(
                                        owner -> {
                                            Long author = event.getMessage().getAuthor()
                                                    .map(User::getId)
                                                    .map(Snowflake::asLong)
                                                    .orElse(null);
                                            return owner.equals(author);
                                        })
                                .flatMap(id -> Mono.when(eventHandlers.stream()
                                        .map(handler -> handler.onMessageCreate(event))
                                        .collect(Collectors.toList()))
                                ))
                .then();
    }

    private static class GuildVoiceSupport {

        private final AudioPlayerManager playerManager;
        private final AudioProvider provider;
        private final AudioPlayer player;

        public GuildVoiceSupport() {
            this.playerManager = new DefaultAudioPlayerManager();
            playerManager.getConfiguration().setFrameBufferFactory(NonAllocatingAudioFrameBuffer::new);
            AudioSourceManagers.registerLocalSource(playerManager);
            AudioSourceManagers.registerRemoteSources(playerManager);
            this.player = playerManager.createPlayer();
            this.provider = new LavaplayerAudioProvider(player);
        }
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

    public static class Join extends EventHandler {

        private final Map<Snowflake, GuildVoiceSupport> voiceMap;

        public Join(Map<Snowflake, GuildVoiceSupport> voiceMap) {
            this.voiceMap = voiceMap;
        }

        @Override
        public Mono<Void> onMessageCreate(MessageCreateEvent event) {
            if (event.getMessage().getContent().equals("!vc join")) {
                return Mono.justOrEmpty(event.getMember())
                        .flatMap(Member::getVoiceState)
                        .flatMap(VoiceState::getChannel)
                        .flatMap(channel -> {
                            GuildVoiceSupport voice = voiceMap.computeIfAbsent(channel.getGuildId(),
                                    k -> new GuildVoiceSupport());
                            return channel.join().withProvider(voice.provider);
                        })
                        .retryWhen(Retry.backoff(2, Duration.ofSeconds(2)))
                        .doFinally(s -> log.info("Finalized join request after {}", s))
                        .onErrorResume(t -> {
                            log.error("Failed to join voice channel", t);
                            return Mono.empty();
                        })
                        .then();
            }
            return Mono.empty();
        }
    }

    public static class Leave extends EventHandler {

        private final GatewayDiscordClient client;

        public Leave(GatewayDiscordClient client) {
            this.client = client;
        }

        @Override
        public Mono<Void> onMessageCreate(MessageCreateEvent event) {
            if (event.getMessage().getContent().equals("!vc leave")) {
                return Mono.justOrEmpty(event.getMember())
                        .flatMap(Member::getVoiceState)
                        .flatMap(vs -> client.getVoiceConnectionRegistry()
                                .getVoiceConnection(vs.getGuildId())
                                .doOnSuccess(vc -> {
                                    if (vc == null) {
                                        log.info("No voice connection to leave!");
                                    }
                                }))
                        .flatMap(VoiceConnection::disconnect);
            }
            return Mono.empty();
        }
    }

    public static class Reconnect extends EventHandler {

        private final GatewayDiscordClient client;

        public Reconnect(GatewayDiscordClient client) {
            this.client = client;
        }

        @Override
        public Mono<Void> onMessageCreate(MessageCreateEvent event) {
            if (event.getMessage().getContent().equals("!vc retry")) {
                return Mono.justOrEmpty(event.getMember())
                        .flatMap(Member::getVoiceState)
                        .flatMap(vs -> client.getVoiceConnectionRegistry()
                                .getVoiceConnection(vs.getGuildId()))
                        .flatMap(vc -> vc.reconnect(VoiceGatewayReconnectException::new))
                        .doFinally(s -> log.info("Reconnect event handle complete"));
            }
            return Mono.empty();
        }
    }

    public static class Resume extends EventHandler {

        private final GatewayDiscordClient client;

        public Resume(GatewayDiscordClient client) {
            this.client = client;
        }

        @Override
        public Mono<Void> onMessageCreate(MessageCreateEvent event) {
            if (event.getMessage().getContent().equals("!vc resume")) {
                return Mono.justOrEmpty(event.getMember())
                        .flatMap(Member::getVoiceState)
                        .flatMap(vs -> client.getVoiceConnectionRegistry()
                                .getVoiceConnection(vs.getGuildId()))
                        .flatMap(vc -> vc.reconnect(VoiceGatewayResumeException::new))
                        .doFinally(s -> log.info("Reconnect event handle complete"));
            }
            return Mono.empty();
        }
    }

    public static class Play extends EventHandler {

        private final Map<Snowflake, GuildVoiceSupport> voiceMap;

        public Play(Map<Snowflake, GuildVoiceSupport> voiceMap) {
            this.voiceMap = voiceMap;
        }

        @Override
        public Mono<Void> onMessageCreate(MessageCreateEvent event) {
            if (event.getMessage().getContent().startsWith("!vc play ")) {
                return Mono.justOrEmpty(event.getMessage().getContent())
                        .map(content -> Arrays.asList(content.split(" ")))
                        .doOnNext(command -> event.getGuildId()
                                .map(id -> voiceMap.computeIfAbsent(id, k -> new GuildVoiceSupport()))
                                .ifPresent(voice -> voice.playerManager.loadItem(command.get(2),
                                        new MyAudioLoadResultHandler(voice.player))))
                        .then();
            }
            return Mono.empty();
        }
    }

    public static class Stop extends EventHandler {

        private final Map<Snowflake, GuildVoiceSupport> voiceMap;

        public Stop(Map<Snowflake, GuildVoiceSupport> voiceMap) {
            this.voiceMap = voiceMap;
        }

        @Override
        public Mono<Void> onMessageCreate(MessageCreateEvent event) {
            if (event.getMessage().getContent().equals("!vc stop")) {
                return event.getGuildId()
                        .map(id -> voiceMap.computeIfAbsent(id, k -> new GuildVoiceSupport()))
                        .map(voice -> Mono.<Void>fromRunnable(voice.player::stopTrack))
                        .orElseGet(Mono::empty);
            }
            return Mono.empty();
        }
    }
}
