/*
 *     This file is part of Discord4J.
 *
 *     Discord4J is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Discord4J is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */

package sx.blah.discord;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import sx.blah.discord.handle.impl.events.guild.voice.VoiceChannelCreateEvent;
import sx.blah.discord.handle.impl.events.guild.voice.VoiceChannelDeleteEvent;
import sx.blah.discord.handle.impl.events.guild.voice.VoiceDisconnectedEvent;
import sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelJoinEvent;
import sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelMoveEvent;
import sx.blah.discord.handle.obj.IVoiceChannel;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class VoiceChannelTest {
	private static final String CHANNEL_NAME = ":musical_note: Music ♬ \uD83C\uDFB5";

	@Test(timeout = 5000)
	public void test1Create() throws InterruptedException, ExecutionException {
		List<IVoiceChannel> voiceChannels = TestBot.GUILD.getVoiceChannelsByName(CHANNEL_NAME);
		assertThat(voiceChannels.isEmpty(), is(true));

		Future<IVoiceChannel> voiceChannel = TestBot.executeLater(() ->
				(IVoiceChannel) TestBot.GUILD.createVoiceChannel(CHANNEL_NAME));
		VoiceChannelCreateEvent event = TestBot.CLIENT.getDispatcher().waitFor(VoiceChannelCreateEvent.class);
		assertThat(event.getVoiceChannel(), is(voiceChannel.get()));

		voiceChannels = TestBot.GUILD.getVoiceChannelsByName(CHANNEL_NAME);
		assertThat(voiceChannels.size(), is(1));
	}

	@Test(timeout = 5000)
	public void test2Join() throws InterruptedException {
		IVoiceChannel voiceChannel = TestBot.GUILD.getVoiceChannelsByName(CHANNEL_NAME).get(0);

		assertThat(TestBot.GUILD.getConnectedVoiceChannel(), nullValue());

		TestBot.executeLater(voiceChannel::join);
		UserVoiceChannelJoinEvent event = TestBot.CLIENT.getDispatcher().waitFor(UserVoiceChannelJoinEvent.class);
		assertThat(event.getVoiceChannel(), is(voiceChannel));

		assertThat(TestBot.GUILD.getConnectedVoiceChannel(), is(voiceChannel));
	}

	@Test(timeout = 5000)
	public void test3Move() throws InterruptedException {
		Optional<IVoiceChannel> possibleVoiceChannel = TestBot.GUILD.getVoiceChannels()
				.stream().filter(channel -> !channel.getName().equals(CHANNEL_NAME)).findAny();
		assertThat(possibleVoiceChannel.isPresent(), is(true));

		IVoiceChannel oldVoiceChannel = TestBot.GUILD.getVoiceChannelsByName(CHANNEL_NAME).get(0);
		IVoiceChannel newVoiceChannel = possibleVoiceChannel.get();

		TestBot.executeLater(() -> TestBot.CLIENT.getOurUser().moveToVoiceChannel(newVoiceChannel));
		UserVoiceChannelMoveEvent event = TestBot.CLIENT.getDispatcher().waitFor(UserVoiceChannelMoveEvent.class);
		assertThat(event.getOldChannel(), is(oldVoiceChannel));
		assertThat(event.getNewChannel(), is(newVoiceChannel));

		assertThat(TestBot.GUILD.getConnectedVoiceChannel(), is(newVoiceChannel));
	}

	@Test(timeout = 5000)
	public void test4Leave() throws InterruptedException {
		IVoiceChannel voiceChannel = TestBot.GUILD.getConnectedVoiceChannel();

		TestBot.executeLater(() -> voiceChannel.leave());
		VoiceDisconnectedEvent event = TestBot.CLIENT.getDispatcher().waitFor(VoiceDisconnectedEvent.class);
		assertThat(event.getVoiceChannel(), is(voiceChannel));

		assertThat(TestBot.GUILD.getConnectedVoiceChannel(), nullValue());
	}

	@Test(timeout = 5000)
	public void test5Delete() throws InterruptedException {
		IVoiceChannel voiceChannel = TestBot.GUILD.getVoiceChannelsByName(CHANNEL_NAME).get(0);

		TestBot.executeLater(() -> voiceChannel.delete());
		VoiceChannelDeleteEvent event = TestBot.CLIENT.getDispatcher().waitFor(VoiceChannelDeleteEvent.class);
		assertThat(event.getVoiceChannel(), is(voiceChannel));
	}
}
