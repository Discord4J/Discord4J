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

package discord4j.core.object.command;

import discord4j.common.annotations.Experimental;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.DiscordObject;
import discord4j.core.object.entity.Attachment;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.User;
import discord4j.discordjson.json.ApplicationCommandInteractionResolvedData;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * An object containing resolved objects from a Discord application command interaction.
 *
 * @see
 * <a href="https://discord.com/developers/docs/interactions/slash-commands#interaction-applicationcommandinteractiondata">
 * Application Command Interaction Object</a>
 */
@Experimental
public class ApplicationCommandInteractionResolved implements DiscordObject {

    /** The gateway associated to this object. */
    private final GatewayDiscordClient gateway;

    /** The raw data as represented by Discord. */
    private final ApplicationCommandInteractionResolvedData data;

    @Nullable
    private final Long guildId;

    public ApplicationCommandInteractionResolved(GatewayDiscordClient gateway,
                                                 ApplicationCommandInteractionResolvedData data,
                                                 @Nullable Long guildId) {
        this.gateway = Objects.requireNonNull(gateway);
        this.data = Objects.requireNonNull(data);
        this.guildId = guildId;
    }

    @Override
    public GatewayDiscordClient getClient() {
        return gateway;
    }

    /**
     * Gets the raw data as represented by Discord.
     *
     * @return The raw data as represented by Discord.
     */
    public ApplicationCommandInteractionResolvedData getData() {
        return data;
    }

    /**
     * Gets the resolved channel with the given ID, if present.
     *
     * @param channelId the ID of the channel to get
     * @return the resolved channel, if present
     */
    public Optional<ResolvedChannel> getChannel(Snowflake channelId) {
        return data.channels().toOptional()
                .map(channels -> channels.get(channelId.asString()))
                .map(data -> new ResolvedChannel(gateway, data));
    }

    /**
     * Gets a map containing the resolved channels associated by their IDs
     *
     * @return the resolved channels
     */
    public Map<Snowflake, ResolvedChannel> getChannels() {
        return data.channels().toOptional()
                .map(map -> map.entrySet().stream()
                        .map(entry -> Tuples.of(
                                Snowflake.of(entry.getKey()),
                                new ResolvedChannel(gateway, entry.getValue())))
                        .collect(Collectors.toMap(Tuple2::getT1, Tuple2::getT2)))
                .orElseGet(Collections::emptyMap);
    }


    /**
     * Gets the resolved user with the given ID, if present.
     *
     * @param userId the ID of the user to get
     * @return the resolved user, if present
     */
    public Optional<User> getUser(Snowflake userId) {
        return data.users().toOptional()
                .map(users -> users.get(userId.asString()))
                .map(data -> new User(gateway, data));
    }

    /**
     * Gets a map containing the resolved users associated by their IDs
     *
     * @return the resolved users
     */
    public Map<Snowflake, User> getUsers() {
        return data.users().toOptional()
                .map(map -> map.entrySet().stream()
                        .map(entry -> Tuples.of(
                                Snowflake.of(entry.getKey()),
                                new User(gateway, entry.getValue())))
                        .collect(Collectors.toMap(Tuple2::getT1, Tuple2::getT2)))
                .orElseGet(Collections::emptyMap);
    }

    /**
     * Gets the resolved member with the given ID, if present.
     *
     * @param memberId the ID of the member to get
     * @return the resolved member, if present
     */
    public Optional<ResolvedMember> getMember(Snowflake memberId) {
        return data.members().toOptional()
                .map(members -> members.get(memberId.asString()))
                .map(memberData -> new ResolvedMember(gateway, memberData,
                        getUser(memberId).map(User::getUserData).orElseThrow(IllegalStateException::new),
                        Objects.requireNonNull(guildId)));
    }

    /**
     * Gets a map containing the resolved members associated by their IDs
     *
     * @return the resolved members
     */
    public Map<Snowflake, ResolvedMember> getMembers() {
        return data.members().toOptional()
                .map(map -> map.entrySet().stream()
                        .map(entry -> {
                            final Snowflake id = Snowflake.of(entry.getKey());
                            return Tuples.of(id, new ResolvedMember(gateway, entry.getValue(),
                                    getUser(id).map(User::getUserData).orElseThrow(IllegalStateException::new),
                                    Objects.requireNonNull(guildId)));
                        })
                        .collect(Collectors.toMap(Tuple2::getT1, Tuple2::getT2)))
                .orElseGet(Collections::emptyMap);
    }

    /**
     * Gets the resolved role with the given ID, if present.
     *
     * @param roleId the ID of the role to get
     * @return the resolved role, if present
     */
    public Optional<Role> getRole(Snowflake roleId) {
        return data.roles().toOptional()
                .map(roles -> roles.get(roleId.asString()))
                .map(data -> new Role(gateway, data, Objects.requireNonNull(guildId)));
    }

    /**
     * Gets a map containing the resolved roles associated by their IDs
     *
     * @return the resolved roles
     */
    public Map<Snowflake, Role> getRoles() {
        return data.roles().toOptional()
                .map(map -> map.entrySet().stream()
                        .map(entry -> Tuples.of(Snowflake.of(entry.getKey()), new Role(gateway, entry.getValue(),
                                Objects.requireNonNull(guildId))))
                        .collect(Collectors.toMap(Tuple2::getT1, Tuple2::getT2)))
                .orElseGet(Collections::emptyMap);
    }

    /**
     * Gets the resolved message with the given ID, if present.
     *
     * @param messageId the ID of the message to get
     * @return the resolved message, if present
     */
    public Optional<Message> getMessage(Snowflake messageId) {
        return data.messages().toOptional()
                .map(messages -> messages.get(messageId.asString()))
                .map(data -> new Message(gateway, data));
    }

    /**
     * Gets a map containing the resolved messages associated by their IDs
     *
     * @return the resolved messages
     */
    public Map<Snowflake, Message> getMessages() {
        return data.messages().toOptional()
                .map(map -> map.entrySet().stream()
                        .map(entry -> Tuples.of(Snowflake.of(entry.getKey()),
                                new Message(gateway, entry.getValue())))
                        .collect(Collectors.toMap(Tuple2::getT1, Tuple2::getT2)))
                .orElseGet(Collections::emptyMap);
    }

    /**
     * Gets the resolved attachment with the given ID, if present.
     *
     * @param attachmentId the ID of the attachment to get
     * @return the resolved attachment, if present
     */
    public Optional<Attachment> getAttachment(Snowflake attachmentId) {
        return data.attachments().toOptional()
                .map(attachments -> attachments.get(attachmentId.asString()))
                .map(data -> new Attachment(gateway, data));
    }

    /**
     * Gets a map containing the resolved attachments associated by their IDs
     *
     * @return the resolved attachments
     */
    public Map<Snowflake, Attachment> getAttachments() {
        return data.attachments().toOptional()
                .map(map -> map.entrySet().stream()
                        .map(entry -> Tuples.of(Snowflake.of(entry.getKey()),
                                new Attachment(gateway, entry.getValue())))
                        .collect(Collectors.toMap(Tuple2::getT1, Tuple2::getT2)))
                .orElseGet(Collections::emptyMap);
    }

    @Override
    public String toString() {
        return "ApplicationCommandInteractionResolved{" +
                "data=" + data +
                ", guildId=" + guildId +
                '}';
    }
}
