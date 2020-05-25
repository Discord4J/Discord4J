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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import discord4j.common.jackson.UnknownPropertyHandler;
import discord4j.discordjson.possible.PossibleFilter;
import discord4j.discordjson.possible.PossibleModule;

import java.util.function.Function;

/**
 * Provides a centralized Jackson 2.10 {@link ObjectMapper} allowing customization and
 * reuse across the application.
 */
public class JacksonResources {

    private static final Function<ObjectMapper, ObjectMapper> initializer = mapper -> mapper
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
            .registerModules(new PossibleModule(), new Jdk8Module())
            .setDefaultPropertyInclusion(JsonInclude.Value.construct(JsonInclude.Include.CUSTOM,
                    JsonInclude.Include.ALWAYS, PossibleFilter.class, null));

    private final ObjectMapper objectMapper;

    /**
     * Create a default {@link ObjectMapper} that allows any field visibility,
     * registers modules to handle Discord4J specific mappings and ignores unknown properties.
     */
    public JacksonResources() {
        this(mapper -> mapper.addHandler(new UnknownPropertyHandler(true)));
    }

    /**
     * Create a custom {@link com.fasterxml.jackson.databind.ObjectMapper}, based on the defaults given by
     * {@link #JacksonResources()}
     *
     * @param mapper a Function to customize the ObjectMapper to be created
     */
    public JacksonResources(Function<ObjectMapper, ObjectMapper> mapper) {
        this.objectMapper = initializer.andThen(mapper).apply(new ObjectMapper());
    }

    /**
     * Create with a pre-configured {@link ObjectMapper}. Using this will replace the
     * recommended default and can lead to unexpected behavior and errors.
     *
     * @param objectMapper a pre-configured ObjectMapper to use
     */
    public JacksonResources(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Create with a pre-configured {@link ObjectMapper} for all Discord4J related operations.
     */
    public static JacksonResources create() {
        return new JacksonResources(mapper -> mapper.addHandler(new UnknownPropertyHandler(true)));
    }

    /**
     * Get the {@link ObjectMapper} configured by this provider.
     *
     * @return a Jackson ObjectMapper used to map POJOs to and from JSON format
     */
    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}
