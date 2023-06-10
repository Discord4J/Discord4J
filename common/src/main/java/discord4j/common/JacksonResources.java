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
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import discord4j.common.jackson.UnknownPropertyHandler;
import discord4j.discordjson.possible.PossibleFilter;
import discord4j.discordjson.possible.PossibleModule;

import java.util.function.Function;

/**
 * Provides a centralized Jackson 2.10 {@link ObjectMapper} allowing customization and
 * reuse across the application.
 */
public class JacksonResources {

    /**
     * A mapper of {@link ObjectMapper} with all the required options for Discord4J operations.
     */
    public static final Function<ObjectMapper, ObjectMapper> INITIALIZER = mapper -> mapper
            .registerModule(new PossibleModule())
            .registerModule(new JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .registerModule(new Jdk8Module())
            .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
            .setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.PUBLIC_ONLY)
            .setVisibility(PropertyAccessor.CREATOR, JsonAutoDetect.Visibility.ANY)
            .setDefaultPropertyInclusion(JsonInclude.Value.construct(JsonInclude.Include.CUSTOM,
                    JsonInclude.Include.ALWAYS, PossibleFilter.class, null));

    /**
     * A mapper of {@link ObjectMapper} to handle unknown properties without throwing errors.
     */
    public static final Function<ObjectMapper, ObjectMapper> HANDLE_UNKNOWN_PROPERTIES =
            mapper -> mapper.addHandler(new UnknownPropertyHandler(true));

    private final ObjectMapper objectMapper;

    /**
     * Create a default {@link ObjectMapper} that allows any field visibility,
     * registers modules to handle Discord4J specific mappings and ignores unknown properties.
     *
     * @deprecated use {@link #create()}
     */
    @Deprecated
    public JacksonResources() {
        this(HANDLE_UNKNOWN_PROPERTIES);
    }

    /**
     * Create a custom {@link ObjectMapper}, based on the defaults given by {@link #JacksonResources()}.
     *
     * @param mapper a Function to customize the ObjectMapper to be created
     * @deprecated use one of the static factories and then call {@link #withMapperFunction(Function)}
     */
    @Deprecated
    public JacksonResources(Function<ObjectMapper, ObjectMapper> mapper) {
        this.objectMapper = INITIALIZER.andThen(mapper).apply(new ObjectMapper());
    }

    /**
     * Create with a pre-configured {@link ObjectMapper}. Using this will replace the
     * recommended default and can lead to unexpected behavior and errors.
     *
     * @param objectMapper a pre-configured ObjectMapper to use
     * @deprecated use {@link #createFromObjectMapper(ObjectMapper)} instead, but consider all Discord4J-related
     * transformations are applied on the given {@code ObjectMapper}
     */
    @Deprecated
    public JacksonResources(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Create with a pre-configured {@link ObjectMapper} for all Discord4J related operations.
     */
    public static JacksonResources create() {
        return new JacksonResources(HANDLE_UNKNOWN_PROPERTIES);
    }

    /**
     * Create based on {@link ObjectMapper} applying on it all changes required for Discord4J related operations.
     */
    public static JacksonResources createFromObjectMapper(ObjectMapper objectMapper) {
        return new JacksonResources(INITIALIZER.andThen(HANDLE_UNKNOWN_PROPERTIES).apply(objectMapper));
    }

    /**
     * Return a new {@link JacksonResources} based on this current {@link ObjectMapper} but applying the given function.
     *
     * @param transformer a mapper to enrich the current {@link ObjectMapper}
     * @return a new instance with the {@code transformer} applied
     */
    public JacksonResources withMapperFunction(Function<ObjectMapper, ObjectMapper> transformer) {
        return new JacksonResources(transformer.apply(objectMapper));
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
