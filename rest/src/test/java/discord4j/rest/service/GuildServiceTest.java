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
import discord4j.rest.RestTests;
import discord4j.rest.json.request.*;
import discord4j.rest.json.response.ChannelResponse;
import discord4j.rest.json.response.GuildResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

import java.util.Collections;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GuildServiceTest {

    private static final long guild = Long.parseUnsignedLong(System.getenv("guild"));
    private static final long member = Long.parseUnsignedLong(System.getenv("member"));
    private static final long permanentRole = Long.parseUnsignedLong(System.getenv("permanentRole"));
    private static final long trashCategory = Long.parseUnsignedLong(System.getenv("trashCategory"));
    private static final long bannedUser = Long.parseUnsignedLong(System.getenv("bannedUser"));

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
        GuildResponse response = guildService.getGuild(guild).block();
    }

    @DiscordTest
    public void testModifyGuild() {
        GuildModifyRequest req = GuildModifyRequest.builder().region("us-south").build();
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
        ChannelCreateRequest req = ChannelCreateRequest.builder().name(randomName).parentId(trashCategory).build();
        guildService.createGuildChannel(guild, req, null).block();
    }

    @DiscordTest
    public void testDeleteGuildChannels() {
        guildService.getGuildChannels(guild)
                .filter(res -> res.getParentId() != null && trashCategory == res.getParentId())
                .map(ChannelResponse::getId)
                .flatMap(id -> channelService.deleteChannel(id, null))
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
        GuildMemberModifyRequest req = GuildMemberModifyRequest.builder().nick("nickname").build();
        guildService.modifyGuildMember(guild, member, req, null).block();
    }

    @DiscordTest
    public void testModifyOwnNickname() {
        NicknameModifyRequest req = new NicknameModifyRequest("nickname");
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
        RoleCreateRequest req = new RoleCreateRequest(randomName, 0, 0, false, false);
        guildService.createGuildRole(guild, req, null).block();
    }

    @DiscordTest
    public void testModifyGuildRolePositions() {
        // TODO
    }

    @DiscordTest
    public void testModifyGuildRole() {
        RoleModifyRequest req = RoleModifyRequest.builder().permissions(0).build();
        guildService.modifyGuildRole(guild, permanentRole, req, null).block();
    }

    @DiscordTest
    public void testDeleteGuildRole() {
        guildService.getGuildRoles(guild)
                .filter(role -> role.getName().startsWith("test_") || role.getName().startsWith("3f"))
                .limitRequest(5)
                .flatMap(role -> guildService.deleteGuildRole(guild, role.getId(), null))
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
    public void testGetGuildEmbed() {
        guildService.getGuildEmbed(guild).block();
    }

    @DiscordTest
    public void testModifyGuildEmbed() {
        // TODO
    }
}
