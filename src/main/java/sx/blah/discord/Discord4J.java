/*
 * Discord4J - Unofficial wrapper for Discord API
 * Copyright (c) 2015
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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author qt
 * @since 7:56 PM 16 Aug, 2015
 * Project: DiscordAPI
 * <p>
 * <p>
 * Main class. :D
 */
public class Discord4J {
	
	/**
     * The name of the project
     */
    public static String NAME;
	/**
     * The version of the api
     */
    public static String VERSION;
	/**
     * The api's description
     */
    public static String DESCRIPTION;
	/**
     * The github repo for the api
     */
    public static String URL;

    /**
     * SLF4J Instance
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(Discord4J.class);
    
    //Dynamically getting various information from maven
    static {
        InputStream stream = Discord4J.class.getClassLoader().getResourceAsStream("app.properties");
        Properties properties = new Properties();
        try {
            properties.load(stream);
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        NAME = properties.getProperty("application.name");
        VERSION = properties.getProperty("application.version");
        DESCRIPTION = properties.getProperty("application.description");
        URL = properties.getProperty("application.url");
        
        LOGGER.info("{} v{}", NAME, VERSION);
        LOGGER.info("{}", DESCRIPTION);
    }
}
