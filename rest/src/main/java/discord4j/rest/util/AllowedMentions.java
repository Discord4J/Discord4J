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

package discord4j.rest.util;

import discord4j.common.util.Snowflake;
import discord4j.discordjson.json.AllowedMentionsData;
import discord4j.discordjson.json.ImmutableAllowedMentionsData;
import discord4j.discordjson.possible.Possible;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A class for holding the allowed_mentions object with an built-in factory for default values.
 * Also this class wraps the {@link AllowedMentionsData} JSON to a Discord4J class.
 *
 * @see <a href="https://discord.com/developers/docs/resources/channel#allowed-mentions-object">Reference</a>
 */
public class AllowedMentions {

    /**
     * Create a builder for this {@link AllowedMentions} class. Building this object without any configuration is
     * equivalent to suppressing all mentions.
     *
     * @return A builder class for allowed mentions
     */
    public static AllowedMentions.Builder builder() {
        return new Builder();
    }

    /**
     * Copy an existing {@link AllowedMentions} object to a new builder.
     *
     * @param template the allowed mentions object to copy
     * @return A builder class for allowed mentions
     */
    public static AllowedMentions.Builder builder(final AllowedMentions template) {
        return new Builder(template.parse, template.userIds, template.roleIds, template.repliedUser);
    }

    /**
     * Create an {@link AllowedMentions} object that will suppress all mentions.
     *
     * @return A suppressing allowed mentions
     */
    public static AllowedMentions suppressAll() {
        return builder().build();
    }

    /**
     * Create an {@link AllowedMentions} object that will suppress @everyone and @here mentions.
     *
     * @return A suppressing allowed mentions
     */
    public static AllowedMentions suppressEveryone() {
        return builder().parseType(Type.USER, Type.ROLE).build();
    }

    private final Possible<Set<Type>> parse;
    private final Possible<Set<Snowflake>> userIds;
    private final Possible<Set<Snowflake>> roleIds;
    private final Possible<Boolean> repliedUser;

    private AllowedMentions(final Possible<Set<AllowedMentions.Type>> parse, final Possible<Set<Snowflake>> userIds,
                            final Possible<Set<Snowflake>> roleIds, final Possible<Boolean> repliedUser) {
        this.parse = parse;
        this.userIds = userIds;
        this.roleIds = roleIds;
        this.repliedUser = repliedUser;
    }

    private <T, U> List<T> mapSetToList(final Set<U> list, final Function<? super U, ? extends T> mapper) {
        final List<T> data = new ArrayList<>(list.size());
        list.forEach(u -> data.add(mapper.apply(u)));
        return data;
    }

    /**
     * Copy this {@link AllowedMentions} object to a new builder.
     *
     * @return A builder class for allowed mentions
     */
    public AllowedMentions.Builder mutate() {
        return new Builder(parse, userIds, roleIds, repliedUser);
    }

    /**
     * Map this {@link AllowedMentions} object to a {@link AllowedMentionsData} JSON
     *
     * @return JSON object
     */
    public AllowedMentionsData toData() {
        final ImmutableAllowedMentionsData.Builder builder = AllowedMentionsData.builder();
        if (!parse.isAbsent()) {
            builder.parse(mapSetToList(parse.get(), Type::getRaw));
        }
        if (!userIds.isAbsent()) {
            builder.users(mapSetToList(userIds.get(), Snowflake::asString));
        }
        if (!roleIds.isAbsent()) {
            builder.roles(mapSetToList(roleIds.get(), Snowflake::asString));
        }
        if (parse.isAbsent() && userIds.isAbsent() && roleIds.isAbsent()) {
            builder.parse(Collections.emptyList()); // this empty list is required to work
        }
        if (!repliedUser.isAbsent()) {
            builder.repliedUser(repliedUser.get());
        }
        return builder.build();
    }

    /**
     * Maps an {@link AllowedMentionsData} to a wrapped {@link AllowedMentions} object
     *
     * @param allowedMentionsData The data to map
     * @return A wrapped {@link AllowedMentionsData}
     */
    public static AllowedMentions from(AllowedMentionsData allowedMentionsData) {
        final AllowedMentions.Builder builder = AllowedMentions.builder();
        if (!allowedMentionsData.parse().isAbsent()) {
            allowedMentionsData.parse().get().stream()
                .map(Type::valueOf) // Parse to Type object
                .forEach(builder::parseType);
        }

        if (!allowedMentionsData.users().isAbsent()) {
            allowedMentionsData.users().get().stream()
                .map(Snowflake::of)
                .forEach(builder::allowUser);
        }

        if (!allowedMentionsData.roles().isAbsent()) {
            allowedMentionsData.roles().get().stream()
                .map(Snowflake::of)
                .forEach(builder::allowRole);
        }

        if (!allowedMentionsData.repliedUser().isAbsent()) {
            builder.repliedUser(allowedMentionsData.repliedUser().get());
        }

        return builder.build();
    }

    /**
     * A builder for creating {@link AllowedMentions} objects.
     *
     * @see <a href="https://discord.com/developers/docs/resources/channel#allowed-mentions-object">Reference</a>
     */
    public static class Builder {

        private Possible<Set<AllowedMentions.Type>> parse;
        private Possible<Set<Snowflake>> userIds;
        private Possible<Set<Snowflake>> roleIds;
        private Possible<Boolean> repliedUser;

        private Builder() {
            this(
                    Possible.absent(),
                    Possible.absent(),
                    Possible.absent(),
                    Possible.absent()
            );
        }

        private Builder(final Possible<Set<Type>> parse, final Possible<Set<Snowflake>> userIds,
                        final Possible<Set<Snowflake>> roleIds, final Possible<Boolean> repliedUser) {
            this.parse = parse;
            this.userIds = userIds;
            this.roleIds = roleIds;
            this.repliedUser = repliedUser;
        }

        /**
         * Add a type to the allowed parsed types list.
         *
         * @param type the type to parse
         * @return this builder
         */
        public Builder parseType(final AllowedMentions.Type type) {
            if (parse.isAbsent()) {
                parse = Possible.of(new HashSet<>());
            }
            parse.get().add(type);
            return this;
        }

        /**
         * Add a user to the allowed users list.
         *
         * @param userId the user to allow
         * @return this builder
         */
        public Builder allowUser(final Snowflake userId) {
            if (userIds.isAbsent()) {
                userIds = Possible.of(new HashSet<>());
            }
            userIds.get().add(userId);
            return this;
        }

        /**
         * Add a role to the allowed roles list.
         *
         * @param roleId the role to allow
         * @return this builder
         */
        public Builder allowRole(final Snowflake roleId) {
            if (roleIds.isAbsent()) {
                roleIds = Possible.of(new HashSet<>());
            }
            roleIds.get().add(roleId);
            return this;
        }

        /**
         * Add types to the allowed parsed types list.
         *
         * @param type the types to parse
         * @return this builder
         */
        public Builder parseType(final AllowedMentions.Type... type) {
            if (parse.isAbsent()) {
                parse = Possible.of(new HashSet<>());
            }
            parse.get().addAll(Arrays.asList(type));
            return this;
        }

        /**
         * Add users to the allowed users list.
         *
         * @param userId the users to allow
         * @return this builder
         */
        public Builder allowUser(final Snowflake... userId) {
            if (userIds.isAbsent()) {
                userIds = Possible.of(new HashSet<>());
            }
            userIds.get().addAll(Arrays.asList(userId));
            return this;
        }

        /**
         * Add roles to the allowed roles list.
         *
         * @param roleId the roles to allow
         * @return this builder
         */
        public Builder allowRole(final Snowflake... roleId) {
            if (roleIds.isAbsent()) {
                roleIds = Possible.of(new HashSet<>());
            }
            roleIds.get().addAll(Arrays.asList(roleId));
            return this;
        }

        /**
         * Set whether to mention the author of the message being replied to.
         *
         * @param repliedUser whether to mention the author of the message being replied to.
         * @return this builder
         */
        public Builder repliedUser(final boolean repliedUser) {
            this.repliedUser = Possible.of(repliedUser);
            return this;
        }

        /**
         * Build the {@link AllowedMentions} object
         *
         * @return the allowed mentions object
         */
        public AllowedMentions build() {
            return new AllowedMentions(parse, userIds, roleIds, repliedUser);
        }
    }

    /**
     * An allowed mentions type, grouped into role mentions, user mentions and everyone mentions (includes @everyone
     * and @here).
     */
    public enum Type {
        /**
         * Control role mentions.
         */
        ROLE("roles"),
        /**
         * Control user mentions.
         */
        USER("users"),
        /**
         * Control @everyone and @here mentions.
         */
        EVERYONE_AND_HERE("everyone");

        private final String raw;

        Type(final String raw) {
            this.raw = raw;
        }

        public String getRaw() {
            return raw;
        }
    }
}
