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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import discord4j.common.jackson.PossibleModule;
import discord4j.rest.RestTests;
import discord4j.rest.json.request.*;
import discord4j.rest.json.response.ChannelResponse;
import discord4j.rest.json.response.GuildResponse;
import discord4j.rest.request.Router;
import org.junit.Before;
import org.junit.Test;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.util.Collections;

public class GuildServiceTest {

    private static final Logger log = Loggers.getLogger(GuildServiceTest.class);

    private static final long guild = Long.parseUnsignedLong(System.getenv("guild"));
    private static final long member = Long.parseUnsignedLong(System.getenv("member"));
    private static final long permanentRole = Long.parseUnsignedLong(System.getenv("permanentRole"));
    private static final long trashCategory = Long.parseUnsignedLong(System.getenv("trashCategory"));
    private static final long bannedUser = Long.parseUnsignedLong(System.getenv("bannedUser"));

    private GuildService guildService;
    private ChannelService channelService;

    @Before
    public void setup() {
        String token = System.getenv("token");
        ObjectMapper mapper = getMapper();
        Router router = RestTests.getRouter(token, mapper);
        guildService = new GuildService(router);
        channelService = new ChannelService(router);
    }

    private ObjectMapper getMapper() {
        return new ObjectMapper()
                .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
                .registerModules(new PossibleModule(), new Jdk8Module());
    }

    private GuildService getGuildService() {
        return guildService;
    }

    private ChannelService getChannelService() {
        return channelService;
    }

    @Test
    public void testCreateGuild() {
        // TODO
    }

    @Test
    public void testGetGuild() {
        GuildResponse response = getGuildService().getGuild(guild).block();
        System.out.println(response.getId());
    }

    @Test
    public void testModifyGuild() {
        GuildModifyRequest req = GuildModifyRequest.builder().region("us-south").build();
        getGuildService().modifyGuild(guild, req).block();
    }

    @Test
    public void testDeleteGuild() {
        // TODO
    }

    @Test
    public void testGetGuildChannels() {
        getGuildService().getGuildChannels(guild).then().block();
    }

    @Test
    public void testCreateGuildChannel() {
        String randomName = Long.toHexString(Double.doubleToLongBits(Math.random()));
        ChannelCreateRequest req = ChannelCreateRequest.builder().name(randomName).parentId(trashCategory).build();
        getGuildService().createGuildChannel(guild, req).block();
    }

    @Test
    public void testDeleteGuildChannels() {
        getGuildService().getGuildChannels(guild)
                .filter(res -> res.getParentId() != null && trashCategory == res.getParentId())
                .map(ChannelResponse::getId)
                .flatMap(id -> getChannelService().deleteChannel(id))
                .then()
                .block();
    }

    @Test
    public void testModifyGuildChannelPositions() {
        // TODO
    }

    @Test
    public void testGetGuildMember() {
        getGuildService().getGuildMember(guild, member).block();
    }

    @Test
    public void testGetGuildMembers() {
        getGuildService().getGuildMembers(guild, Collections.emptyMap()).then().block();
    }

    @Test
    public void testAddGuildMember() {
        // TODO
    }

    @Test
    public void testModifyGuildMember() {
        GuildMemberModifyRequest req = GuildMemberModifyRequest.builder().nick("nickname").build();
        getGuildService().modifyGuildMember(guild, member, req).block();
    }

    @Test
    public void testModifyOwnNickname() {
        NicknameModifyRequest req = new NicknameModifyRequest("nickname");
        getGuildService().modifyOwnNickname(guild, req).block();
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
        getGuildService().getGuildBans(guild).then().block();
    }

    @Test
    public void testGetGuildBan() {
        getGuildService().getGuildBan(guild, bannedUser).block();
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
    public void testGetGuildRoles() {
        getGuildService().getGuildRoles(guild).then().block();
    }

    @Test
    public void testCreateGuildRole() {
        String randomName = "test_" + Long.toHexString(Double.doubleToLongBits(Math.random()));
        RoleCreateRequest req = new RoleCreateRequest(randomName, 0, 0, false, false);
        getGuildService().createGuildRole(guild, req).block();
    }

    @Test
    public void testModifyGuildRolePositions() {
        // TODO
    }

    @Test
    public void testModifyGuildRole() {
        RoleModifyRequest req = RoleModifyRequest.builder().permissions(0).build();
        getGuildService().modifyGuildRole(guild, permanentRole, req).block();
    }

    @Test
    public void testDeleteGuildRole() {
        getGuildService().getGuildRoles(guild)
                .filter(role -> role.getName().startsWith("test_") || role.getName().startsWith("3f"))
                .limitRequest(5)
                .flatMap(role -> getGuildService().deleteGuildRole(guild, role.getId()))
                .blockLast();
    }

    @Test
    public void testGetGuildPruneCount() {
        getGuildService().getGuildPruneCount(guild, Collections.emptyMap()).block();
    }

    @Test
    public void testBeginGuildPrune() {
        // shouldn't actually prune anyone because everyone in test server should have a role
        getGuildService().beginGuildPrune(guild, Collections.emptyMap()).block();
    }

    @Test
    public void testGetGuildVoiceRegions() {
        getGuildService().getGuildVoiceRegions(guild).then().block();
    }

    @Test
    public void testGetGuildInvites() {
        getGuildService().getGuildInvites(guild).then().block();
    }

    @Test
    public void testGetGuildIntegrations() {
        getGuildService().getGuildIntegrations(guild).then().block();
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
        getGuildService().getGuildEmbed(guild).block();
    }

    @Test
    public void testModifyGuildEmbed() {
        // TODO
    }
}
