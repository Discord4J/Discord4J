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

import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.ReadyEvent;

public class EmptyBot {

	public static void main(String[] args) {
		((Discord4J.Discord4JLogger) Discord4J.LOGGER).setLevel(Discord4J.Discord4JLogger.Level.DEBUG);

//		Log.setLog(new Logger() {
//			@Override
//			public String getName() {
//				return "sdfsdsdfsf";
//			}
//
//			@Override
//			public void warn(String s, Object... objects) {
//				Discord4J.LOGGER.warn(s, objects);
//			}
//
//			@Override
//			public void warn(Throwable throwable) {
//				Discord4J.LOGGER.warn("", throwable);
//			}
//
//			@Override
//			public void warn(String s, Throwable throwable) {
//				Discord4J.LOGGER.warn(s, throwable);
//			}
//
//			@Override
//			public void info(String s, Object... objects) {
//				Discord4J.LOGGER.info(s, objects);
//			}
//
//			@Override
//			public void info(Throwable throwable) {
//				Discord4J.LOGGER.info("", throwable);
//			}
//
//			@Override
//			public void info(String s, Throwable throwable) {
//				Discord4J.LOGGER.info(s, throwable);
//			}
//
//			@Override
//			public boolean isDebugEnabled() {
//				return true;
//			}
//
//			@Override
//			public void setDebugEnabled(boolean b) {
//
//			}
//
//			@Override
//			public void debug(String s, Object... objects) {
//				Discord4J.LOGGER.debug(s, objects);
//			}
//
//			@Override
//			public void debug(String s, long l) {
//				Discord4J.LOGGER.debug(s, l);
//			}
//
//			@Override
//			public void debug(Throwable throwable) {
//				Discord4J.LOGGER.debug("", throwable);
//			}
//
//			@Override
//			public void debug(String s, Throwable throwable) {
//				Discord4J.LOGGER.debug(s, throwable);
//			}
//
//			@Override
//			public Logger getLogger(String s) {
//				return this;
//			}
//
//			@Override
//			public void ignore(Throwable throwable) {
//
//			}
//		});
		IDiscordClient client = new ClientBuilder().withToken(args[0]).registerListener(new IListener<ReadyEvent>() {
			@Override
			public void handle(ReadyEvent event) {
				System.out.println("Ohai");
			}
		}).login();
	}
}
