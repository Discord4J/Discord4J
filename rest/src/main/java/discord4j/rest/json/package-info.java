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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * Contains all of the Plain Old Java Objects (POJOs) used to represent requests and responses to and from Discord.
 * <p>
 * The following rules should be followed when creating a new POJO:
 * <h1>Placement</h1>
 * <ul>
 * <li>Objects which are to be <b>serialized and sent</b> to Discord should be placed in
 * {@link discord4j.common.json.request}.
 * <li>Objects which are to be <b>received and deserialized</b> from Discord should be placed in
 * {@link discord4j.rest.json.response}.
 * <li>Objects whose receiving and sending models are identical should be placed in {@link discord4j.common.json}.
 * </ul>
 * <h1>Field Types</h1>
 * <p>
 * Discord4J, through Jackson, provides many mappings between Discord JSON "types" and Java types. See
 * <a href="https://discordapp.com/developers/docs/reference#snowflake-ids">Snowflake IDs</a> and
 * <a href="https://discordapp.com/developers/docs/reference#nullable-and-optional-resource-fields">Nullable and
 * Optional Resource Fields</a>.
 * <p>
 * Snowflake IDs are <b>not</b> represented as {@code String}s. Instead, they are {@code long}s and are annotated with
 * {@link discord4j.common.jackson.UnsignedJson}.
 * <ul>
 * <li>Request POJOs:
 * <ul>
 * <li>"Optional" Fields are represented with the {@link discord4j.common.jackson.Possible Possible} type.
 * <li>"Nullable" Fields are represented with literal {@code null}. The field should be annotated as Nullable.
 * <li>"Optional and Nullable" Fields are both annotated with Nullable and are of type Possible.
 * </ul>
 * <li>Response POJOs:
 * <ul>
 * <li>"Optional" Fields are represented with literal {@code null}. The field should be annotated as Nullable.
 * <li>"Nullable" Fields are represented with literal {@code null}. The field should be annotated as Nullable.
 * <li>"Optional and Nullable" Fields are represented with literal {@code null}. The field should be annotated
 * as Nullable.
 * </ul>
 * </ul>
 * <h1>Anatomy and Naming</h1>
 * A Request POJO should contain only {@code final} fields set in its single constructor. It should not contain getters
 * or setters for its fields to discourage misuse of the object. Request POJOs with many Possible fields may optionally
 * provide a Builder for construction. See {@link discord4j.common.json.request.ChannelModifyRequest
 * ChannelModifyRequest}.
 * <p>
 * A Response POJO should contain only mutable fields and <b>no</b> constructor. It should contain <b>only</b> getter
 * methods for its fields.
 * <p>
 * A POJO used both for requests and responses should contain only mutable fields with one standard constructor and one
 * explicit NO-OP default constructor as well as getters for its fields.
 * <p>
 * Field and method names should follow standard Java convention. For fields with names which do not already conform to
 * this standard, utilize the {@link com.fasterxml.jackson.annotation.JsonProperty JsonProperty} annotation in order to
 * give it a new name.
 * <p>
 * All POJOs should override toString() to aide debugging.
 */
@NonNullApi
package discord4j.rest.json;

import reactor.util.annotation.NonNullApi;
