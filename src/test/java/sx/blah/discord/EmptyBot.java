package sx.blah.discord;

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.ReadyEvent;

public class EmptyBot {

	public static void main(String[] args) {
		((Discord4J.Discord4JLogger) Discord4J.LOGGER).setLevel(Discord4J.Discord4JLogger.Level.DEBUG);

		Log.setLog(new Logger() {
			@Override
			public String getName() {
				return "sdfsdsdfsf";
			}

			@Override
			public void warn(String s, Object... objects) {
				Discord4J.LOGGER.warn(s, objects);
			}

			@Override
			public void warn(Throwable throwable) {
				Discord4J.LOGGER.warn("", throwable);
			}

			@Override
			public void warn(String s, Throwable throwable) {
				Discord4J.LOGGER.warn(s, throwable);
			}

			@Override
			public void info(String s, Object... objects) {
				Discord4J.LOGGER.info(s, objects);
			}

			@Override
			public void info(Throwable throwable) {
				Discord4J.LOGGER.info("", throwable);
			}

			@Override
			public void info(String s, Throwable throwable) {
				Discord4J.LOGGER.info(s, throwable);
			}

			@Override
			public boolean isDebugEnabled() {
				return true;
			}

			@Override
			public void setDebugEnabled(boolean b) {

			}

			@Override
			public void debug(String s, Object... objects) {
				Discord4J.LOGGER.debug(s, objects);
			}

			@Override
			public void debug(String s, long l) {
				Discord4J.LOGGER.debug(s, l);
			}

			@Override
			public void debug(Throwable throwable) {
				Discord4J.LOGGER.debug("", throwable);
			}

			@Override
			public void debug(String s, Throwable throwable) {
				Discord4J.LOGGER.debug(s, throwable);
			}

			@Override
			public Logger getLogger(String s) {
				return this;
			}

			@Override
			public void ignore(Throwable throwable) {

			}
		});
		IDiscordClient client = new ClientBuilder().withToken(args[0]).registerListener(new IListener<ReadyEvent>() {
			@Override
			public void handle(ReadyEvent event) {
				System.out.println("Ohai");
			}
		}).login();
	}
}
