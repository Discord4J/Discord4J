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

import discord4j.rest.RestTests;
import discord4j.rest.json.response.InviteResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@EnabledIfEnvironmentVariable(named = "D4J_TEST_DISCORD", matches = "true")
public class InviteServiceTest {

    private static final String inviteCode = System.getenv("inviteCode");
    private static final long modifyChannel = Long.parseUnsignedLong(System.getenv("modifyChannel"));

    private InviteService inviteService;
    private ChannelService channelService;

    @BeforeAll
    public void setup() {
        inviteService = new InviteService(RestTests.defaultRouter());
        channelService = new ChannelService(RestTests.defaultRouter());
    }

    @Test
    public void testGetInvite() {
        inviteService.getInvite(inviteCode).block();
    }

    @Test
    public void testDeleteInvite() {
        channelService.getChannelInvites(modifyChannel)
                .filter(invite -> invite.getMaxAge() != null)
                .filter(invite -> Optional.ofNullable(invite.getCreatedAt())
                        .map(this::asInstant)
                        .map(ts -> ts.plusSeconds(invite.getMaxAge()))
                        .map(ts -> ts.isBefore(Instant.now()))
                        .orElse(false))
                .map(InviteResponse::getCode)
                .flatMap(code -> inviteService.deleteInvite(code, null))
                .blockLast();
    }

    private Instant asInstant(String timestamp) {
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(timestamp, Instant::from);
    }

}
