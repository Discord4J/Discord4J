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

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import sx.blah.discord.Discord4J.Discord4JLogger;
import sx.blah.discord.Discord4J.Discord4JLogger.Level;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.util.RequestBuffer.IRequest;
import sx.blah.discord.util.RequestBuffer.IVoidRequest;

import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * General testing bot / suite. Running this test will run all the tests in the suite. A <i>discordToken</i>
 * argument must be present in the VM (i.e. launch with a <i>-DdiscordToken="mytoken"</i> flag).
 * <p>
 * <b>WARNING:</b> Only run this bot where its <i>only</i> server is a server dedicated for testing.
 */
@RunWith(Suite.class)
@SuiteClasses(VoiceChannelTest.class)
public class TestBot {
	private static final ScheduledExecutorService SERVICE = new ScheduledThreadPoolExecutor(1);
	private static final String TOKEN = System.getProperty("discordToken");

	public static IDiscordClient CLIENT;
	public static IGuild GUILD;

	@BeforeClass
	public static void loginTest() throws InterruptedException {
		((Discord4JLogger) Discord4J.LOGGER).setLevel(Level.TRACE);
		CLIENT = new ClientBuilder().withToken(TOKEN).build();

		executeLater(() -> CLIENT.login());
		CLIENT.getDispatcher().waitFor(ReadyEvent.class);

		List<IGuild> guilds = CLIENT.getGuilds();
		assertThat(guilds.size(), is(1));

		GUILD = guilds.get(0);
		assertThat(GUILD, notNullValue());
	}

	@AfterClass
	public static void logoutTest() {
		CLIENT.logout();
	}

	@Before
	public void rateLimitBuffer() throws InterruptedException {
		Thread.sleep(1000); // Prevents RLEs between all tests
	}

	public static <T> Future<T> executeLater(IRequest<T> execution) {
		return SERVICE.schedule(execution::request, 1L, TimeUnit.SECONDS);
	}

	public static Future<Void> executeLater(IVoidRequest execution) {
		return SERVICE.schedule(execution::request, 1L, TimeUnit.SECONDS);
	}
}
