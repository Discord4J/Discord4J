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

import discord4j.discordjson.json.*;
import discord4j.rest.RestTests;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import discord4j.common.util.Snowflake;

import java.util.Collections;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@EnabledIfEnvironmentVariable(named = "D4J_TEST_DISCORD", matches = "true")
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

    @Test
    public void testCreateGuild() {
        // TODO
    }

    @Test
    public void testGetGuild() {
        guildService.getGuild(guild).block();
    }

    @Test
    public void testModifyGuild() {
        GuildModifyRequest req = GuildModifyRequest.builder()
            .regionOrNull("us-south")
            .build();
        guildService.modifyGuild(guild, req, null).block();
    }

    @Test
    public void testDeleteGuild() {
        // TODO
    }

    @Test
    public void testGetGuildChannels() {
        guildService.getGuildChannels(guild).then().block();
    }

    @Test
    public void testCreateGuildChannel() {
        String randomName = Long.toHexString(Double.doubleToLongBits(Math.random()));
        ChannelCreateRequest req = ChannelCreateRequest.builder()
            .name(randomName)
            .parentId(Snowflake.asString(trashCategory))
            .build();
        guildService.createGuildChannel(guild, req, null).block();
    }

    @Test
    public void testDeleteGuildChannels() {
        guildService.getGuildChannels(guild)
            .filter(data -> data.parentId().get().map(parentId -> Snowflake.asLong(parentId) == trashCategory).orElse(false))
            .map(ChannelData::id)
            .flatMap(id -> channelService.deleteChannel(Snowflake.asLong(id), null))
            .then()
            .block();
    }

    @Test
    public void testModifyGuildChannelPositions() {
        // TODO
    }

    @Test
    public void testGetGuildMember() {
        guildService.getGuildMember(guild, member).block();
    }

    @Test
    public void testGetGuildMembers() {
        guildService.getGuildMembers(guild, Collections.emptyMap()).then().block();
    }

    @Test
    public void testAddGuildMember() {
        // TODO
    }

    @Test
    public void testModifyGuildMember() {
        GuildMemberModifyRequest req = GuildMemberModifyRequest.builder()
            .nickOrNull("nickname")
            .build();
        guildService.modifyGuildMember(guild, member, req, null).block();
    }

    @Test
    public void testModifyCurrentMember() {
        CurrentMemberModifyData req = CurrentMemberModifyData.builder().nick("nickname").build();
        guildService.modifyCurrentMember(guild, req).block();
    }

    @Test
    public void testAddGuildMemberRole() {
        // TODO
    }

    @Test
    public void testRemoveGuildMemberRole() {
        // TODO
    }

    @Test
    public void testRemoveGuildMember() {
        // TODO
    }

    @Test
    public void testGetGuildBans() {
        guildService.getGuildBans(guild).then().block();
    }

    @Test
    public void testGetGuildBan() {
        guildService.getGuildBan(guild, bannedUser).block();
    }

    @Test
    public void testCreateGuildBan() {
        // TODO
    }

    @Test
    public void testRemoveGuildBan() {
        // TODO
    }

    @Test
    public void testBulkGuildBan() {
        // TODO
    }

    @Test
    public void testGetGuildRoles() {
        guildService.getGuildRoles(guild).then().block();
    }

    @Test
    public void testCreateGuildRole() {
        String randomName = "test_" + Long.toHexString(Double.doubleToLongBits(Math.random()));
        RoleCreateRequest req = RoleCreateRequest.builder()
            .name(randomName)
            .build();
        guildService.createGuildRole(guild, req, null).block();
    }

    @Test
    public void testModifyGuildRolePositions() {
        // TODO
    }

    @Test
    public void testModifyGuildRole() {
        RoleModifyRequest req = RoleModifyRequest.builder()
            .permissions(0L)
            .build();
        guildService.modifyGuildRole(guild, permanentRole, req, null).block();
    }

    @Test
    public void testDeleteGuildRole() {
        guildService.getGuildRoles(guild)
            .filter(role -> role.name().startsWith("test_") || role.name().startsWith("3f"))
            .limitRequest(5)
            .flatMap(role -> guildService.deleteGuildRole(guild, Snowflake.asLong(role.id()), null))
            .blockLast();
    }

    @Test
    public void testGetGuildPruneCount() {
        guildService.getGuildPruneCount(guild, Collections.emptyMap()).block();
    }

    @Test
    public void testBeginGuildPrune() {
        // shouldn't actually prune anyone because everyone in test server should have a role
        guildService.beginGuildPrune(guild, Collections.emptyMap(), null).block();
    }

    @Test
    public void testGetGuildVoiceRegions() {
        guildService.getGuildVoiceRegions(guild).then().block();
    }

    @Test
    public void testGetGuildInvites() {
        guildService.getGuildInvites(guild).then().block();
    }

    @Test
    public void testGetGuildIntegrations() {
        guildService.getGuildIntegrations(guild).then().block();
    }

    @Test
    public void testCreateGuildIntegration() {
        // TODO
    }

    @Test
    public void testModifyGuildIntegration() {
        // TODO
    }

    @Test
    public void testDeleteGuildIntegration() {
        // TODO
    }

    @Test
    public void testSyncGuildIntegration() {
        // TODO
    }

    @Test
    public void testGetGuildEmbed() {
        guildService.getGuildWidget(guild).block();
    }

    @Test
    public void testModifyGuildEmbed() {
        // TODO
    }

    @Test
    public void testGetGuildPreview() {
        // TODO
    }
}
