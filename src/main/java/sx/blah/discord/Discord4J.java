/*
 * Discord4J - Unofficial wrapper for Discord API
 * Copyright (c) 2016
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package sx.blah.discord;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.IListener;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.modules.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Properties;

/**
 * Main class. :D
 */
public class Discord4J {

	/**
	 * The name of the project
	 */
	public static final String NAME;
	/**
	 * The version of the api
	 */
	public static final String VERSION;
	/**
	 * The api's description
	 */
	public static final String DESCRIPTION;
	/**
	 * The github repo for the api
	 */
	public static final String URL;

	/**
	 * SLF4J Instance
	 */
	public static final Logger LOGGER = LoggerFactory.getLogger(Discord4J.class);

	/**
	 * When this class was loaded.
	 */
	protected static final LocalDateTime launchTime = LocalDateTime.now();

	//Dynamically getting various information from maven
	static {
		InputStream stream = Discord4J.class.getClassLoader().getResourceAsStream("app.properties");
		Properties properties = new Properties();
		try {
			properties.load(stream);
			stream.close();
		} catch (IOException e) {
			Discord4J.LOGGER.error("Discord4J Internal Exception", e);
		}
		NAME = properties.getProperty("application.name");
		VERSION = properties.getProperty("application.version");
		DESCRIPTION = properties.getProperty("application.description");
		URL = properties.getProperty("application.url");

		LOGGER.info("{} v{}", NAME, VERSION);
		LOGGER.info("{}", DESCRIPTION);
	}

	/**
	 * This is used to run Discord4J independent of any bot, making it module dependent.
	 *
	 * @param args The args should be either email/password or just the bot token
	 */
	public static void main(String[] args) {
		//This functionality is dependent on these options being true
		if (!Configuration.AUTOMATICALLY_ENABLE_MODULES || !Configuration.LOAD_EXTERNAL_MODULES)
			throw new RuntimeException("Invalid configuration!");

		// There needs to be at least 1 arg
		if (args.length < 1)
			throw new IllegalArgumentException("At least 1 argument required!");

		try {
			ClientBuilder builder = new ClientBuilder();
			IDiscordClient client = (args.length == 1 ? builder.withToken(args[0]) : builder.withLogin(args[0], args[1])).login();
			client.getDispatcher().registerListener((IListener<ReadyEvent>) (ReadyEvent e) -> {
				LOGGER.info("Logged in as {}", e.getClient().getOurUser().getName());
			});
			//The modules should handle the rest
		} catch (DiscordException e) {
			LOGGER.error("There was an error initializing the client", e);
		}
	}

	/**
	 * Gets the time when this class was loaded. Useful for keeping track of application uptime.
	 * Note: See {@link IDiscordClient#getLaunchTime()} for uptime of a specific client instance.
	 *
	 * @return The launch time.
	 */
	public static LocalDateTime getLaunchTime() {
		return launchTime;
	}
}
