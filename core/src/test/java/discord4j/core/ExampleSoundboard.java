/*
 * This file is part of Discord4J.
 *
 * Discord4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Discord4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J. If not, see <http://www.gnu.org/licenses/>.
 */

package discord4j.core;

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
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.component.LayoutComponent;
import discord4j.core.object.entity.Member;
import discord4j.core.support.GuildCommandRegistrar;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.voice.AudioProvider;
import discord4j.voice.VoiceConnection;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ExampleSoundboard {

    private static final Logger log = Loggers.getLogger(ExampleSoundboard.class);

    private static final String token = System.getenv("token");
    private static final long guildId = Long.parseLong(System.getenv("guildId"));

    // Additional env vars for audio sources
    // sourceN : the path or URL
    // labelN : the button label (optional)
    // N from 1 up to 25

    public static void main(String[] args) {
        DiscordClient.create(token)
                .withGateway(ExampleSoundboard::setup)
                .block();
    }

    static Publisher<?> setup(GatewayDiscordClient client) {
        Map<Snowflake, GuildVoiceSupport> voiceGuildMap = new ConcurrentHashMap<>();

        // preemptively check for sources
        /*~~>*/List<LayoutComponent> buttons = getButtons();
        if (buttons.isEmpty()) {
            return Mono.error(new RuntimeException("Please define at least 1 'sourceN' env variable (N up to 25)"));
        }

        // a listener that captures our slash command interactions
        Publisher<?> onChatInputInteraction = client.on(ChatInputInteractionEvent.class, event -> {
            if ("join".equals(event.getCommandName())) {
                return event.deferReply().withEphemeral(true)
                        .then(Mono.justOrEmpty(event.getInteraction().getMember()))
                        .flatMap(Member::getVoiceState)
                        .flatMap(VoiceState::getChannel)
                        .flatMap(channel -> {
                            Snowflake key = event.getInteraction().getGuildId().orElse(null);
                            GuildVoiceSupport voice = voiceGuildMap.computeIfAbsent(key, k -> new GuildVoiceSupport());

                            return channel.join()
                                    .withProvider(voice.provider)
                                    .then(event.editReply("Done!"));
                        })
                        .doFinally(s -> log.info("Finalized join request after {}", s))
                        .onErrorResume(t -> {
                            log.error("Failed to join voice channel", t);
                            return event.editReply("Something happened...");
                        })
                        .then();
            } else if ("leave".equals(event.getCommandName())) {
                return event.deferReply().withEphemeral(true)
                        .then(Mono.justOrEmpty(event.getInteraction().getMember()))
                        .flatMap(Member::getVoiceState)
                        .flatMap(vs -> client.getVoiceConnectionRegistry()
                                .getVoiceConnection(vs.getGuildId())
                                .doOnSuccess(vc -> {
                                    if (vc == null) {
                                        log.info("No voice connection to leave!");
                                    }
                                }))
                        .flatMap(VoiceConnection::disconnect)
                        .then(event.editReply("Bye bye!"))
                        .onErrorResume(t -> {
                            log.error("Failed to leave voice channel", t);
                            return event.editReply("Something happened...");
                        })
                        .then();
            } else if ("sounds".equals(event.getCommandName())) {
                // TODO: once it's easier to patch an initial response with LayoutComponents we can auto-join here
                return Mono.justOrEmpty(event.getInteraction().getMember())
                        .flatMap(Member::getVoiceState)
                        .flatMap(vs -> client.getVoiceConnectionRegistry()
                                .getVoiceConnection(vs.getGuildId())
                                .hasElement())
                        .flatMap(connected -> {
                            if (connected) {
                                return event.reply()
                                        .withEphemeral(true)
                                        .withContent("Hit it!")
                                        .withComponents(buttons);
                            } else {
                                return event.reply()
                                        .withEphemeral(true)
                                        .withContent("Not in a voice channel! type `/join` first");
                            }
                        });
            }
            return Mono.empty();
        });

        // a listener that captures button presses
        Publisher<?> onButtonInteraction = client.on(ButtonInteractionEvent.class, press -> {
            if (press.getCustomId().startsWith("source")) {
                return press.deferEdit().withEphemeral(true)
                        .then(Mono.justOrEmpty(press.getInteraction().getGuildId()))
                        .flatMap(id -> Mono.justOrEmpty(voiceGuildMap.get(id)))
                        .map(it -> it.playerManager.loadItem(System.getenv(press.getCustomId()),
                                new MyAudioLoadResultHandler(it.player)))
                        .then();
            }
            return Mono.empty();
        });

        // register the command and then subscribe to multiple listeners, using Mono.when
        return GuildCommandRegistrar.create(client.getRestClient(), guildId, getCommandSources())
                .registerCommands()
                .thenMany(Mono.when(onChatInputInteraction, onButtonInteraction));
    }

    private static /*~~>*/List<LayoutComponent> getButtons() {
        /*~~>*/List<Button> buttons = new ArrayList<>();
        for (int i = 1; i <= 25; i++) {
            String source = System.getenv("source" + i);
            if (source != null) {
                String label = System.getenv("label" + i);
                buttons.add(Button.primary("source" + i, label != null ? label : String.valueOf(i)));
                log.info("Source #{}: {}", i, source);
            }
        }
        /*~~>*/List<LayoutComponent> components = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            int from = i * 5;
            int to = Math.min(buttons.size(), (i + 1) * 5);
            if (from < buttons.size()) {
                components.add(ActionRow.of(buttons.subList(from, to)));
            }
        }
        return components;
    }

    private static /*~~>*/List<ApplicationCommandRequest> getCommandSources() {
        return Arrays.asList(
                ApplicationCommandRequest.builder()
                        .name("join")
                        .description("Join my current voice channel")
                        .build(),
                ApplicationCommandRequest.builder()
                        .name("leave")
                        .description("Leave my current voice channel")
                        .build(),
                ApplicationCommandRequest.builder()
                        .name("sounds")
                        .description("Display the soundboard")
                        .build()
        );
    }

    static class GuildVoiceSupport {

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

    static class LavaplayerAudioProvider extends AudioProvider {

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

    static class MyAudioLoadResultHandler implements AudioLoadResultHandler {

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
