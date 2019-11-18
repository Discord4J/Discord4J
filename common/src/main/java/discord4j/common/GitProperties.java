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

package discord4j.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Provide information about the Git repository version captured at build time.
 */
public class GitProperties {

    public static final String APPLICATION_NAME = "application.name";
    /**
     * Use it on the properties given by {@link #getProperties()} to get project version captured at build time.
     */
    public static final String APPLICATION_VERSION = "git.build.version";
    public static final String APPLICATION_URL = "application.url";
    /**
     * Use it on the properties given by {@link #getProperties()} to get repository version captured at build time.
     */
    public static final String GIT_COMMIT_ID_DESCRIBE = "git.commit.id.describe";

    /**
     * Load a {@link Properties} object with application version data.
     *
     * @return a property list with application version details
     * @see GitProperties#APPLICATION_VERSION
     * @see GitProperties#GIT_COMMIT_ID_DESCRIBE
     */
    public static Properties getProperties() {
        Properties properties = new Properties();
        try (InputStream inputStream = GitProperties.class.getResourceAsStream("git.properties")) {
            if (inputStream != null) {
                properties.load(inputStream);
            }
        } catch (IOException ignore) {
        }
        return properties;
    }
}
