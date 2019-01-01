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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
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
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.VoiceChannel;
import discord4j.core.object.util.Snowflake;
import discord4j.voice.AudioProvider;
import discord4j.voice.AudioReceiver;
import discord4j.voice.VoiceConnection;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;

public class ExampleVoiceBot {

    private static String token;
    private static String voiceChannel;
    private static String audioSource;
    private static String guild;
    private static String owner;

    @BeforeClass
    public static void initialize() {
        token = System.getenv("token");
        voiceChannel = System.getenv("voiceChannel");
        audioSource = System.getenv("audioSource");
        guild = System.getenv("guild");
        owner = System.getenv("owner");
    }

    @Test
    @Ignore("Example code excluded from CI")
    public void testVoiceBot() {
        // Set up LavaPlayer
        AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
        playerManager.getConfiguration().setFrameBufferFactory(NonAllocatingAudioFrameBuffer::new);
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioPlayer player = playerManager.createPlayer();
        AudioProvider provider = new LavaplayerAudioProvider(player);
        playerManager.loadItem(audioSource, new MyAudioLoadResultHandler(player));

        // Bind events and log in
        DiscordClient client = new DiscordClientBuilder(token).build();

        Mono<Void> leaveMessage = client.getEventDispatcher().on(MessageCreateEvent.class)
                .filter(e -> e.getMember().map(Member::getId).map(it -> it.asString().equals(owner)).orElse(false))
                .filter(e -> e.getMessage().getContent().map(it -> it.equals("!leave")).orElse(false))
                .next()
                .then();

        client.getEventDispatcher().on(GuildCreateEvent.class)
                .filter(e -> e.getGuild().getId().asString().equals(guild))
                .flatMap(g -> client.getChannelById(Snowflake.of(voiceChannel)).ofType(VoiceChannel.class))
                .flatMap(channel -> channel.join(provider, new LoggingAudioReceiver()))
                .flatMap(leaveMessage::thenReturn)
                .subscribe(VoiceConnection::disconnect);

        client.login().block();
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
            if (didProvide) getBuffer().flip();
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

    private static class LoggingAudioReceiver extends AudioReceiver {

        @Override
        public void receive(char sequence, int timestamp, int ssrc, byte[] audio) {
            System.out.println("packet from: " + ssrc);
        }
    }
}
