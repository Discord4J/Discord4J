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
package discord4j.core.object.entity;

import discord4j.core.spec.ApplicationEditMono;
import discord4j.core.spec.ApplicationEditSpec;
import discord4j.discordjson.json.ApplicationInfoData;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.retriever.EntityRetrievalStrategy;
import discord4j.rest.util.Image;
import discord4j.common.util.Snowflake;
import discord4j.core.util.EntityUtil;
import discord4j.core.util.ImageUtil;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.*;

/**
 * Represents the Current (typically) Application Information.
 *
 * @see <a href="https://discord.com/developers/docs/resources/application">Application Resource</a>
 */
public final class ApplicationInfo implements Entity {

    /** The path for application icon image URLs. */
    private static final String ICON_IMAGE_PATH = "app-icons/%s/%s";

    /** The gateway associated to this object. */
    private final GatewayDiscordClient gateway;

    /** The raw data as represented by Discord. */
    private final ApplicationInfoData data;

    /**
     * Constructs a {@code ApplicationInfo} with an associated {@link GatewayDiscordClient} and Discord data.
     *
     * @param gateway The {@link GatewayDiscordClient} associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    public ApplicationInfo(final GatewayDiscordClient gateway, final ApplicationInfoData data) {
        this.gateway = Objects.requireNonNull(gateway);
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public GatewayDiscordClient getClient() {
        return gateway;
    }

    @Override
    public Snowflake getId() {
        return Snowflake.of(data.id());
    }

    /**
     * Gets the data of the app.
     *
     * @return The data of the app.
     */
    public ApplicationInfoData getData() {
        return data;
    }

    /**
     * Gets the name of the app.
     *
     * @return The name of the app.
     */
    public String getName() {
        return data.name();
    }

    /**
     * Gets the icon URL of the application, if present.
     *
     * @param format The format for the URL.
     * @return The icon URL of the application, if present.
     */
    public Optional<String> getIconUrl(final Image.Format format) {
        return data.icon()
                .map(icon -> ImageUtil.getUrl(String.format(ICON_IMAGE_PATH, getId().asString(), icon), format));
    }

    /**
     * Gets the icon of the application.
     *
     * @param format The format in which to get the icon.
     * @return A {@link Mono} where, upon successful completion, emits the {@link Image icon} of the application. If an
     * error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Image> getIcon(final Image.Format format) {
        return Mono.justOrEmpty(getIconUrl(format)).flatMap(Image::ofUrl);
    }

    /**
     * Gets the description of the app.
     *
     * @return The description of the app.
     */
    public String getDescription() {
        return data.description();
    }

    /**
     * Gets whether only the app owner can join the app's bot to guilds.
     *
     * @return {@code true} if only the app owner can join the app's bot to guilds, {@code false} otherwise.
     */
    public boolean isPublic() {
        return data.botPublic();
    }

    /**
     * Gets whether the app's bot will only join upon completion of the full OAuth2 code grant flow.
     *
     * @return {@code true} if the app's bot will only join upon completion of the full OAuth2 code grant flow,
     * {@code false} otherwise.
     */
    public boolean requireCodeGrant() {
        return data.botRequireCodeGrant();
    }

    /**
     * Gets the url of the app's terms of service, if present.
     *
     * @return The url of the app's terms of service, if present.
     */
    public Optional<String> getTermsOfServiceUrl() {
        return data.termsOfServiceUrl().toOptional();
    }

    /**
     * Gets the url of the app's privacy policy, if present.
     *
     * @return The url of the app's privacy policy, if present.
     */
    public Optional<String> getPrivacyPolicyUrl() {
        return data.privacyPolicyUrl().toOptional();
    }

    /**
     * Gets the ID of the owner of the application.
     *
     * @return The ID of the owner of the application.
     */
    public Snowflake getOwnerId() {
        return Snowflake.of(data.owner().id());
    }

    /**
     * Requests to retrieve the owner of the application.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link User owner} of the application. If an
     * error is received, it is emitted through the {@code Mono}.
     */
    public Mono<User> getOwner() {
        return gateway.getUserById(getOwnerId());
    }

    /**
     * Requests to retrieve the owner of the application, using the given retrieval strategy.
     *
     * @param retrievalStrategy The strategy to use to get the owner.
     * @return A {@link Mono} where, upon successful completion, emits the {@link User owner} of the application. If an
     * error is received, it is emitted through the {@code Mono}.
     */
    public Mono<User> getOwner(EntityRetrievalStrategy retrievalStrategy) {
        return gateway.withRetrievalStrategy(retrievalStrategy).getUserById(getOwnerId());
    }

    /**
     * Gets the members of the application team, if the application belongs to a team.
     *
     * @return The members of the application's team, if the application belongs to a team.
     */
    public Optional<ApplicationTeam> getTeam() {
        return data.team().map(data -> new ApplicationTeam(gateway, data));
    }

    /**
     * Returns the public flags of this {@link ApplicationInfo}.
     *
     * @return A {@code EnumSet} with the public flags of this application.
     */
    public EnumSet<Flag> getFlags() {
        int publicFlags = data.flags().toOptional().orElse(0);
        if (publicFlags != 0) {
            return Flag.of(publicFlags);
        }
        return EnumSet.noneOf(Flag.class);
    }

    /**
     * Requests to edit the current application. Properties specifying how to edit this application
     * can be set via the {@code withXxx} methods of the returned {@link ApplicationEditMono}.
     *
     * @return A {@link Mono} where, upon successful completion, emits the edited {@link ApplicationInfo}.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public ApplicationEditMono edit() {
        return ApplicationEditMono.of(this);
    }

    /**
     * Requests to edit this application.
     *
     * @param spec an immutable object that specifies how to edit this application
     * @return A {@link Mono} where, upon successful completion, emits the edited {@link ApplicationInfo}. If an error is received,
     * it is emitted through the {@code Mono}.
     */
    public Mono<ApplicationInfo> edit(ApplicationEditSpec spec) {
        Objects.requireNonNull(spec);
        return Mono.defer(
            () -> gateway.getRestClient().getApplicationService().modifyCurrentApplicationInfo(spec.asRequest()))
            .map(data -> new ApplicationInfo(gateway, ApplicationInfoData.builder()
                .from(this.data)
                .from(data)
                .build()));
    }

    @Override
    public boolean equals(@Nullable final Object obj) {
        return EntityUtil.equals(this, obj);
    }

    @Override
    public int hashCode() {
        return EntityUtil.hashCode(this);
    }

    /**
     * Describes the flags of an application.
     *
     * @see
     * <a href="https://discord.com/developers/docs/resources/application#application-object-application-flags">Discord</a>
     */
    public enum Flag {
        APPLICATION_AUTO_MODERATION_RULE_CREATE_BADGE(6),
        GATEWAY_PRESENCE(12),
        GATEWAY_PRESENCE_LIMITED(13),
        GATEWAY_GUILD_MEMBERS(14),
        GATEWAY_GUILD_MEMBERS_LIMITED(15),
        VERIFICATION_PENDING_GUILD_LIMIT(16),
        EMBEDDED(17),
        GATEWAY_MESSAGE_CONTENT(18),
        GATEWAY_MESSAGE_CONTENT_LIMITED(19),
        APPLICATION_COMMAND_BADGE(23);

        /** The underlying value as represented by Discord. */
        private final int value;

        /** The flag value as represented by Discord. */
        private final int flag;

        /**
         * Constructs a {@code ApplicationInfo.Flag}.
         */
        Flag(final int value) {
            this.value = value;
            this.flag = 1 << value;
        }

        /**
         * Gets the underlying value as represented by Discord.
         *
         * @return The underlying value as represented by Discord.
         */
        public int getValue() {
            return value;
        }

        /**
         * Gets the flag value as represented by Discord.
         *
         * @return The flag value as represented by Discord.
         */
        public int getFlag() {
            return flag;
        }

        /**
         * Gets the flags of an application. It is guaranteed that invoking {@link #getValue()} from the returned enum
         * will be equal ({@code ==}) to the supplied {@code value}.
         *
         * @param value The flags value as represented by Discord.
         * @return The {@link EnumSet} of flags.
         */
        public static EnumSet<Flag> of(final int value) {
            final EnumSet<Flag> flagSet = EnumSet.noneOf(Flag.class);
            for (Flag flag : Flag.values()) {
                long flagValue = flag.getFlag();
                if ((flagValue & value) == flagValue) {
                    flagSet.add(flag);
                }
            }
            return flagSet;
        }
    }

    @Override
    public String toString() {
        return "ApplicationInfo{" +
                "data=" + data +
                '}';
    }
}
