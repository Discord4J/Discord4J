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
 *     along with Discord4J. If not, see <http://www.gnu.org/licenses/>.
 */

package sx.blah.discord;

import org.eclipse.jetty.util.log.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MarkerIgnoringBase;
import org.slf4j.helpers.MessageFormatter;
import org.slf4j.helpers.NOPLoggerFactory;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.modules.Configuration;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.LogMarkers;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.time.Instant;
import java.time.LocalTime;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The main class of Discord4J. Static information about D4J is stored here as well as the entry point for module-only
 * execution.
 */
public class Discord4J {

	/**
	 * The name of the project.
	 */
	public static final String NAME;
	/**
	 * The version of the project.
	 */
	public static final String VERSION;

	/**
	 * The commit hash of the version of the project.
	 */
	public static final String COMMIT;

	/**
	 * The description of the project.
	 */
	public static final String DESCRIPTION;
	/**
	 * The github repo of the project.
	 */
	public static final String URL;
	/**
	 * SLF4J Instance.
	 */
	public static final Logger LOGGER = initLogger();
	/**
	 * When this class was loaded.
	 */
	protected static final Instant launchTime = Instant.now();
	/**
	 * Whether to allow for audio to be used.
	 */
	public static final AtomicBoolean audioDisabled = new AtomicBoolean(false);
	/**
	 * The alternate Discord base URL.
	 *
	 * @see sx.blah.discord.api.internal.DiscordEndpoints#BASE
	 */
	public static volatile String alternateUrl = null;
	/**
	 * Cached jetty logger instance.
	 */
	private static final org.eclipse.jetty.util.log.Logger jettyLogger;
	/**
	 * No-op jetty logger implementation.
	 */
	private static final org.eclipse.jetty.util.log.Logger ignoredJettyLogger = new org.eclipse.jetty.util.log.Logger() {
		@Override
		public String getName() {
			return "Jetty (Ignored)";
		}

		@Override
		public void warn(String msg, Object... args) {}

		@Override
		public void warn(Throwable thrown) {}

		@Override
		public void warn(String msg, Throwable thrown) {}

		@Override
		public void info(String msg, Object... args) {}

		@Override
		public void info(Throwable thrown) {}

		@Override
		public void info(String msg, Throwable thrown) {}

		@Override
		public boolean isDebugEnabled() {
			return false;
		}

		@Override
		public void setDebugEnabled(boolean enabled) {}

		@Override
		public void debug(String msg, Object... args) {}

		@Override
		public void debug(String msg, long value) {}

		@Override
		public void debug(Throwable thrown) {}

		@Override
		public void debug(String msg, Throwable thrown) {}

		@Override
		public org.eclipse.jetty.util.log.Logger getLogger(String name) {
			return this;
		}

		@Override
		public void ignore(Throwable ignored) {}
	};

	// Dynamically getting various information from maven
	static {
		InputStream stream = Discord4J.class.getClassLoader().getResourceAsStream("app.properties");
		Properties properties = new Properties();
		try {
			properties.load(stream);
			stream.close();
		} catch (IOException e) {
			Discord4J.LOGGER.error(LogMarkers.MAIN, "Discord4J Internal Exception", e);
		}

		NAME = properties.getProperty("application.name");
		VERSION = properties.getProperty("application.version");
		COMMIT = properties.getProperty("application.git.commit");
		DESCRIPTION = properties.getProperty("application.description");
		URL = properties.getProperty("application.url");

		jettyLogger = Log.getLog();
		Log.setLog(ignoredJettyLogger);

		LOGGER.info(LogMarkers.MAIN, "{} v{} {} ({})", NAME, VERSION, COMMIT, URL);
		LOGGER.info(LogMarkers.MAIN, "{}", DESCRIPTION);
	}

	/**
	 * Runs Discord4J using modules only.
	 *
	 * @param args The args should only include the bot token.
	 */
	public static void main(String[] args) {
		//This functionality is dependent on these options being true
		if (!Configuration.AUTOMATICALLY_ENABLE_MODULES || !Configuration.LOAD_EXTERNAL_MODULES)
			throw new RuntimeException("Invalid configuration! Must have auto-enabling of modules + loading of external modules enabled.");
		if (args.length == 0)
			throw new RuntimeException("Invalid configuration! No arguments passed in.");
		try {
			ClientBuilder builder = new ClientBuilder();
			IDiscordClient client = builder.withToken(args[0]).login();
			client.getDispatcher().registerListener((IListener<ReadyEvent>) (ReadyEvent e) -> {
				LOGGER.info(LogMarkers.MAIN, "Logged in as {}", e.getClient().getOurUser().getName());
			});
			//The modules should handle the rest
		} catch (DiscordException e) {
			LOGGER.error(LogMarkers.MAIN, "There was an error initializing the client", e);
		}
	}

	/**
	 * Gets the time when this class was loaded.
	 *
	 * @return The time when this class was loaded.
	 */
	public static Instant getLaunchTime() {
		return launchTime;
	}

	/**
	 * Enables Jetty Websocket logging. WARNING: This spams the console a ton.
	 */
	public static void enableJettyLogging() {
		Log.setLog(jettyLogger);
	}

	/**
	 * Sets the base Discord URL. This defaults to <code>https://discordapp.com/</code>
	 *
	 * <p>NOTE: This must be called before the initialization of {@link sx.blah.discord.api.internal.DiscordEndpoints}.
	 *
	 * @param url The base URL.
	 */
	public static void setBaseDiscordUrl(String url) {
		LOGGER.info("Base url changed to {}", url);
		alternateUrl = url;
	}

	/**
	 * Disables audio loading.
	 */
	public static void disableAudio() {
		LOGGER.info("Disabled audio.");
		audioDisabled.set(true);
	}

	/**
	 * Initializes the logger as either the {@link Discord4JLogger default implementation} or a found SLF4J implementation.
	 *
	 * @return The initialized logger.
	 */
	private static Logger initLogger() {
		if (!isSLF4JImplementationPresent()) {
			System.err.println("Discord4J: ERROR INITIALIZING LOGGER!");
			System.err.println("Discord4J: No SLF4J implementation found, reverting to the internal implementation ("+Discord4JLogger.class.getName()+")");
			System.err.println("Discord4J: It is *highly* recommended to use a fully featured implementation like logback!");
			return new Discord4JLogger(Discord4J.class.getName());
		} else {
			return LoggerFactory.getLogger(Discord4J.class);
		}
	}

	/**
	 * Gets whether an SLF4J implementation is present on the classpath.
	 *
	 * @return Whether an SLF4J implementation is present on the classpath.
	 */
	private static boolean isSLF4JImplementationPresent() {
		try {
			Class.forName("org.slf4j.impl.StaticLoggerBinder"); //First try to find the implementation
			return !(LoggerFactory.getILoggerFactory() instanceof NOPLoggerFactory); //Implementation found! Double check the logger factory
		} catch (ClassNotFoundException e) {
			return false; //No implementation found
		}
	}

	/**
	 * A logger implementation used by Discord4J if no valid SLF4J implementation is found.
	 */
	public static class Discord4JLogger extends MarkerIgnoringBase {

		private final String name;
		private volatile int level = Level.INFO.ordinal();
		private volatile PrintStream standard, error;

		public Discord4JLogger(String name) {
			this.name = name;
			standard = System.out;
			error = System.err;
		}

		/**
		 * Sets the level for the logger.
		 *
		 * @param level The level for the logger.
		 */
		public void setLevel(Level level) {
			this.level = level.ordinal();
		}

		/**
		 * Sets the stream for "standard" (any level below {@link Level#WARN}) messages to be printed to.
		 *
		 * @param stream The stream to use.
		 */
		public void setStandardStream(PrintStream stream) {
			this.standard = stream;
		}

		/**
		 * Sets the stream for "error" (any level above {@link Level#INFO}) messages to be printed to.
		 *
		 * @param stream The stream to use.
		 */
		public void setErrorStream(PrintStream stream) {
			this.error = stream;
		}

		/**
		 * Logs a message on the given level.
		 *
		 * @param level The level to log the message on.
		 * @param message The message to log.
		 * @param error The error to log.
		 */
		private void log(Level level, String message, Throwable error) {
			if (level.ordinal() >= this.level) {
				PrintStream stream = level.ordinal() >= Level.WARN.ordinal() ? this.error : standard;

				stream.format("%s: [%s][%s][%s] - %s\n", LocalTime.now(), level, Thread.currentThread().getName(), name, message);

				if (error != null)
					error.printStackTrace(stream);
			}
		}

		@Override
		public boolean isTraceEnabled() {
			return level == Level.TRACE.ordinal();
		}

		@Override
		public void trace(String msg) {
			log(Level.TRACE, msg, null);
		}

		@Override
		public void trace(String format, Object arg) {
			FormattingTuple tuple = MessageFormatter.format(format, arg);
			log(Level.TRACE, tuple.getMessage(), tuple.getThrowable());
		}

		@Override
		public void trace(String format, Object arg1, Object arg2) {
			FormattingTuple tuple = MessageFormatter.format(format, arg1, arg2);
			log(Level.TRACE, tuple.getMessage(), tuple.getThrowable());
		}

		@Override
		public void trace(String format, Object... arguments) {
			FormattingTuple tuple = MessageFormatter.arrayFormat(format, arguments);
			log(Level.TRACE, tuple.getMessage(), tuple.getThrowable());
		}

		@Override
		public void trace(String msg, Throwable t) {
			log(Level.TRACE, msg, t);
		}

		@Override
		public boolean isDebugEnabled() {
			return level <= Level.DEBUG.ordinal();
		}

		@Override
		public void debug(String msg) {
			log(Level.DEBUG, msg, null);
		}

		@Override
		public void debug(String format, Object arg) {
			FormattingTuple tuple = MessageFormatter.format(format, arg);
			log(Level.DEBUG, tuple.getMessage(), tuple.getThrowable());
		}

		@Override
		public void debug(String format, Object arg1, Object arg2) {
			FormattingTuple tuple = MessageFormatter.format(format, arg1, arg2);
			log(Level.DEBUG, tuple.getMessage(), tuple.getThrowable());
		}

		@Override
		public void debug(String format, Object... arguments) {
			FormattingTuple tuple = MessageFormatter.arrayFormat(format, arguments);
			log(Level.DEBUG, tuple.getMessage(), tuple.getThrowable());
		}

		@Override
		public void debug(String msg, Throwable t) {
			log(Level.DEBUG, msg, t);
		}

		@Override
		public boolean isInfoEnabled() {
			return level <= Level.INFO.ordinal();
		}

		@Override
		public void info(String msg) {
			log(Level.INFO, msg, null);
		}

		@Override
		public void info(String format, Object arg) {
			FormattingTuple tuple = MessageFormatter.format(format, arg);
			log(Level.INFO, tuple.getMessage(), tuple.getThrowable());
		}

		@Override
		public void info(String format, Object arg1, Object arg2) {
			FormattingTuple tuple = MessageFormatter.format(format, arg1, arg2);
			log(Level.INFO, tuple.getMessage(), tuple.getThrowable());
		}

		@Override
		public void info(String format, Object... arguments) {
			FormattingTuple tuple = MessageFormatter.arrayFormat(format, arguments);
			log(Level.INFO, tuple.getMessage(), tuple.getThrowable());
		}

		@Override
		public void info(String msg, Throwable t) {
			log(Level.INFO, msg, t);
		}

		@Override
		public boolean isWarnEnabled() {
			return level <= Level.WARN.ordinal();
		}

		@Override
		public void warn(String msg) {
			log(Level.WARN, msg, null);
		}

		@Override
		public void warn(String format, Object arg) {
			FormattingTuple tuple = MessageFormatter.format(format, arg);
			log(Level.WARN, tuple.getMessage(), tuple.getThrowable());
		}

		@Override
		public void warn(String format, Object arg1, Object arg2) {
			FormattingTuple tuple = MessageFormatter.format(format, arg1, arg2);
			log(Level.WARN, tuple.getMessage(), tuple.getThrowable());
		}

		@Override
		public void warn(String format, Object... arguments) {
			FormattingTuple tuple = MessageFormatter.arrayFormat(format, arguments);
			log(Level.WARN, tuple.getMessage(), tuple.getThrowable());
		}

		@Override
		public void warn(String msg, Throwable t) {
			log(Level.WARN, msg, t);
		}

		@Override
		public boolean isErrorEnabled() {
			return level <= Level.ERROR.ordinal();
		}

		@Override
		public void error(String msg) {
			log(Level.ERROR, msg, null);
		}

		@Override
		public void error(String format, Object arg) {
			FormattingTuple tuple = MessageFormatter.format(format, arg);
			log(Level.ERROR, tuple.getMessage(), tuple.getThrowable());
		}

		@Override
		public void error(String format, Object arg1, Object arg2) {
			FormattingTuple tuple = MessageFormatter.format(format, arg1, arg2);
			log(Level.ERROR, tuple.getMessage(), tuple.getThrowable());
		}

		@Override
		public void error(String format, Object... arguments) {
			FormattingTuple tuple = MessageFormatter.arrayFormat(format, arguments);
			log(Level.ERROR, tuple.getMessage(), tuple.getThrowable());
		}

		@Override
		public void error(String msg, Throwable t) {
			log(Level.ERROR, msg, t);
		}

		/**
		 * Log levels.
		 */
		public enum Level {
			TRACE, DEBUG, INFO, WARN, ERROR, NONE
		}
	}
}
