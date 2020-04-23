package discord4j.core.object.entity.channel;

import discord4j.discordjson.json.AllowedMentionsData;
import discord4j.discordjson.json.ImmutableAllowedMentionsData;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.Snowflake;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * A class for holding the allowed_mentions object with an built-in factory for default values.
 * Also this class wraps the {@link AllowedMentionsData} JSON to a Discord4J class.
 */
public class AllowedMentions {

    private static Possible<List<AllowedMentions.Type>> DEFAULT_TYPES = Possible.absent();
    private static Possible<List<Snowflake>> DEFAULT_USERS = Possible.absent();
    private static Possible<List<Snowflake>> DEFAULT_ROLES = Possible.absent();

    /**
     * Crates a builder for this {@link AllowedMentions} class
     * @return A builder class for allowed mentions
     */
    public static AllowedMentions.Builder builder() {
        return new Builder();
    }

    /**
     * Sets the default types which are parsed in allowed mentions
     * @param types the types to allow by default
     */
    public static void setDefaultParsedTypes(Possible<List<AllowedMentions.Type>> types) {
        DEFAULT_TYPES = types;
    }

    /**
     * Sets the default types which are parsed in allowed mentions
     * @param types the types to allow by default
     */
    public static void setDefaultParsedTypes(List<AllowedMentions.Type> types) {
        setDefaultParsedTypes(Possible.of(types));
    }

    /**
     * Sets the default users which are allowed in mentions
     * @param users the users to allow by default
     */
    public static void setDefaultAllowedUsers(Possible<List<Snowflake>> users) {
        DEFAULT_USERS = users;
    }

    /**
     * Sets the default users which are allowed in mentions
     * @param users the users to allow by default
     */
    public static void setDefaultAllowedUsers(List<Snowflake> users) {
        setDefaultAllowedUsers(Possible.of(users));
    }

    /**
     * Sets the default roles which are allowed in mentions
     * @param roles the roles to allow by default
     */
    public static void setDefaultAllowedRoles(Possible<List<Snowflake>> roles) {
        DEFAULT_ROLES = roles;
    }

    /**
     * Sets the default roles which are allowed in mentions
     * @param roles the roles to allow by default
     */
    public static void setDefaultAllowedRoles(List<Snowflake> roles) {
        setDefaultAllowedRoles(Possible.of(roles));
    }

    private final Possible<List<AllowedMentions.Type>> parse;
    private final Possible<List<Snowflake>> userIds;
    private final Possible<List<Snowflake>> roleIds;

    private AllowedMentions(final Possible<List<AllowedMentions.Type>> parse, final Possible<List<Snowflake>> userIds,
                            final Possible<List<Snowflake>> roleIds) {
        this.parse = parse;
        this.userIds = userIds;
        this.roleIds = roleIds;
    }

    private <T, U> List<T> mapList(final List<U> list, final Function<? super U, ? extends T> mapper) {
        final List<T> data = new ArrayList<>(list.size());
        list.forEach(u -> data.add(mapper.apply(u)));
        return data;
    }

    /**
     * Maps this {@link AllowedMentions} object to a {@link AllowedMentionsData} JSON
     * @return JSON object
     */
    public AllowedMentionsData toData() {
        final ImmutableAllowedMentionsData.Builder builder = AllowedMentionsData.builder();
        if (!parse.isAbsent()) {
            builder.parse(mapList(parse.get(), Type::getRaw));
        }
        if (!userIds.isAbsent()) {
            builder.users(mapList(userIds.get(), Snowflake::asString));
        }
        if (!roleIds.isAbsent()) {
            builder.roles(mapList(roleIds.get(), Snowflake::asString));
        }
        return builder.build();
    }

    public static class Builder {

        private Possible<List<AllowedMentions.Type>> parse = DEFAULT_TYPES;
        private Possible<List<Snowflake>> userIds = DEFAULT_USERS;
        private Possible<List<Snowflake>> roleIds = DEFAULT_ROLES;

        /**
         * Add a type to the parsed types list
         * @param type the type to parse
         * @return this builder
         */
        public Builder parseType(final AllowedMentions.Type type) {
            if (parse.isAbsent()) {
                parse = Possible.of(new ArrayList<>());
            }
            parse.get().add(type);
            return this;
        }

        /**
         * Add a user to the allowed users list
         * @param userId the user to allow
         * @return this builder
         */
        public Builder allowUser(final Snowflake userId) {
            if (userIds.isAbsent()) {
                userIds = Possible.of(new ArrayList<>());
            }
            userIds.get().add(userId);
            return this;
        }

        /**
         * Add a role to the allowed roles list
         * @param roleId the role to allow
         * @return this builder
         */
        public Builder allowRole(final Snowflake roleId) {
            if (roleIds.isAbsent()) {
                roleIds = Possible.of(new ArrayList<>());
            }
            roleIds.get().add(roleId);
            return this;
        }

        /**
         * Add types to the parsed types list
         * @param type the types to parse
         * @return this builder
         */
        public Builder parseType(final AllowedMentions.Type... type) {
            if (parse.isAbsent()) {
                parse = Possible.of(new ArrayList<>());
            }
            parse.get().addAll(Arrays.asList(type));
            return this;
        }

        /**
         * Add users to the allowed users list
         * @param userId the users to allow
         * @return this builder
         */
        public Builder allowUser(final Snowflake... userId) {
            if (userIds.isAbsent()) {
                userIds = Possible.of(new ArrayList<>());
            }
            userIds.get().addAll(Arrays.asList(userId));
            return this;
        }

        /**
         * Add roles to the allowed roles list
         * @param roleId the roles to allow
         * @return this builder
         */
        public Builder allowRole(final Snowflake... roleId) {
            if (roleIds.isAbsent()) {
                roleIds = Possible.of(new ArrayList<>());
            }
            roleIds.get().addAll(Arrays.asList(roleId));
            return this;
        }

        /**
         * Build the {@link AllowedMentions} object
         * @return the allowed mentions object
         */
        public AllowedMentions build() {
            return new AllowedMentions(parse, userIds, roleIds);
        }
    }

    public enum Type {
        ROLE("roles"),
        USER("users"),
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
