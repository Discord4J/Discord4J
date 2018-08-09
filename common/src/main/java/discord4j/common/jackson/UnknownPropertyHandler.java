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
package discord4j.common.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.io.IOException;

public class UnknownPropertyHandler extends DeserializationProblemHandler {

    private static final Logger log = Loggers.getLogger(UnknownPropertyHandler.class);

    private final boolean ignoreUnknown;

    public UnknownPropertyHandler(boolean ignoreUnknown) {
        this.ignoreUnknown = ignoreUnknown;
    }

    @Override
    public boolean handleUnknownProperty(DeserializationContext ctx, JsonParser parser, JsonDeserializer<?> deser,
                                         Object beanOrClass, String propertyName) throws IOException {
        if (!ignoreUnknown) return false;

        log.debug("Skipping unknown json property of {}: \"{}\"", beanOrClass.getClass().getName(), propertyName);
        parser.skipChildren();
        return true;
    }
}
