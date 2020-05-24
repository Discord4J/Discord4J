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
package discord4j.rest.service;

import discord4j.rest.DiscordTest;
import discord4j.discordjson.json.*;
import discord4j.rest.RestTests;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import discord4j.common.util.Snowflake;

import java.util.Collections;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GuildServiceTest {

    private static final long guild = Snowflake.asLong(System.getenv("guild"));
    private static final long member = Snowflake.asLong(System.getenv("member"));
    private static final long permanentRole = Snowflake.asLong(System.getenv("permanentRole"));
    private static final long trashCategory = Snowflake.asLong(System.getenv("trashCategory"));
    private static final long bannedUser = Snowflake.asLong(System.getenv("bannedUser"));

    private GuildService guildService;
    private ChannelService channelService;

    @BeforeAll
    public void setup() {
        guildService = new GuildService(RestTests.defaultRouter());
        channelService = new ChannelService(RestTests.defaultRouter());
    }

    @DiscordTest
    public void testCreateGuild() {
        // TODO
    }

    @DiscordTest
    public void testGetGuild() {
        guildService.getGuild(guild).block();
    }

    @DiscordTest
    public void testModifyGuild() {
        GuildModifyRequest req = GuildModifyRequest.builder()
            .region("us-south")
            .build();
        guildService.modifyGuild(guild, req, null).block();
    }

    @DiscordTest
    public void testDeleteGuild() {
        // TODO
    }

    @DiscordTest
    public void testGetGuildChannels() {
        guildService.getGuildChannels(guild).then().block();
    }

    @DiscordTest
    public void testCreateGuildChannel() {
        String randomName = Long.toHexString(Double.doubleToLongBits(Math.random()));
        ChannelCreateRequest req = ChannelCreateRequest.builder()
            .name(randomName)
            .parentId(Snowflake.asString(trashCategory))
            .build();
        guildService.createGuildChannel(guild, req, null).block();
    }

    @DiscordTest
    public void testDeleteGuildChannels() {
        guildService.getGuildChannels(guild)
            .filter(data -> data.parentId().get().map(parentId -> Snowflake.asLong(parentId) == trashCategory).orElse(false))
            .map(ChannelData::id)
            .flatMap(id -> channelService.deleteChannel(Snowflake.asLong(id), null))
            .then()
            .block();
    }

    @DiscordTest
    public void testModifyGuildChannelPositions() {
        // TODO
    }

    @DiscordTest
    public void testGetGuildMember() {
        guildService.getGuildMember(guild, member).block();
    }

    @DiscordTest
    public void testGetGuildMembers() {
        guildService.getGuildMembers(guild, Collections.emptyMap()).then().block();
    }

    @DiscordTest
    public void testAddGuildMember() {
        // TODO
    }

    @DiscordTest
    public void testModifyGuildMember() {
        GuildMemberModifyRequest req = GuildMemberModifyRequest.builder()
            .nick("nickname")
            .build();
        guildService.modifyGuildMember(guild, member, req, null).block();
    }

    @DiscordTest
    public void testModifyOwnNickname() {
        NicknameModifyData req = NicknameModifyData.builder().nick("nickname").build();
        guildService.modifyOwnNickname(guild, req).block();
    }

    @DiscordTest
    public void testAddGuildMemberRole() {
        // TODO
    }

    @DiscordTest
    public void testRemoveGuildMemberRole() {
        // TODO
    }

    @DiscordTest
    public void testRemoveGuildMember() {
        // TODO
    }

    @DiscordTest
    public void testGetGuildBans() {
        guildService.getGuildBans(guild).then().block();
    }

    @DiscordTest
    public void testGetGuildBan() {
        guildService.getGuildBan(guild, bannedUser).block();
    }

    @DiscordTest
    public void testCreateGuildBan() {
        // TODO
    }

    @DiscordTest
    public void testRemoveGuildBan() {
        // TODO
    }

    @DiscordTest
    public void testGetGuildRoles() {
        guildService.getGuildRoles(guild).then().block();
    }

    @DiscordTest
    public void testCreateGuildRole() {
        String randomName = "test_" + Long.toHexString(Double.doubleToLongBits(Math.random()));
        RoleCreateRequest req = RoleCreateRequest.builder()
            .name(randomName)
            .build();
        guildService.createGuildRole(guild, req, null).block();
    }

    @DiscordTest
    public void testModifyGuildRolePositions() {
        // TODO
    }

    @DiscordTest
    public void testModifyGuildRole() {
        RoleModifyRequest req = RoleModifyRequest.builder()
            .permissions(0L)
            .build();
        guildService.modifyGuildRole(guild, permanentRole, req, null).block();
    }

    @DiscordTest
    public void testDeleteGuildRole() {
        guildService.getGuildRoles(guild)
            .filter(role -> role.name().startsWith("test_") || role.name().startsWith("3f"))
            .limitRequest(5)
            .flatMap(role -> guildService.deleteGuildRole(guild, Snowflake.asLong(role.id()), null))
            .blockLast();
    }

    @DiscordTest
    public void testGetGuildPruneCount() {
        guildService.getGuildPruneCount(guild, Collections.emptyMap()).block();
    }

    @DiscordTest
    public void testBeginGuildPrune() {
        // shouldn't actually prune anyone because everyone in test server should have a role
        guildService.beginGuildPrune(guild, Collections.emptyMap(), null).block();
    }

    @DiscordTest
    public void testGetGuildVoiceRegions() {
        guildService.getGuildVoiceRegions(guild).then().block();
    }

    @DiscordTest
    public void testGetGuildInvites() {
        guildService.getGuildInvites(guild).then().block();
    }

    @DiscordTest
    public void testGetGuildIntegrations() {
        guildService.getGuildIntegrations(guild).then().block();
    }

    @DiscordTest
    public void testCreateGuildIntegration() {
        // TODO
    }

    @DiscordTest
    public void testModifyGuildIntegration() {
        // TODO
    }

    @DiscordTest
    public void testDeleteGuildIntegration() {
        // TODO
    }

    @DiscordTest
    public void testSyncGuildIntegration() {
        // TODO
    }

    @DiscordTest
    public void testGetGuildWidget() {
        guildService.getGuildWidget(guild).block();
    }

    @DiscordTest
    public void testModifyGuildEmbed() {
        // TODO
    }
}
