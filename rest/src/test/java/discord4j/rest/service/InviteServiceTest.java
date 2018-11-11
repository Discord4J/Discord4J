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

import com.fasterxml.jackson.databind.ObjectMapper;
import discord4j.rest.RestTests;
import discord4j.rest.json.response.InviteResponse;
import discord4j.rest.request.Router;
import org.junit.Test;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

public class InviteServiceTest {

    private static final String inviteCode = System.getenv("inviteCode");
    private static final long modifyChannel = Long.parseUnsignedLong(System.getenv("modifyChannel"));

    private InviteService inviteService = null;

    private ChannelService channelService = null;

    private InviteService getInviteService() {

        if (inviteService != null) {
            return inviteService;
        }

        String token = System.getenv("token");
        boolean ignoreUnknown = !Boolean.parseBoolean(System.getenv("failUnknown"));
        ObjectMapper mapper = RestTests.getMapper(ignoreUnknown);
        Router router = RestTests.getRouter(token, mapper);

        return inviteService = new InviteService(router);
    }

    private ChannelService getChannelService() {

        if (channelService != null) {
            return channelService;
        }

        return channelService = RestTests.getChannelService();
    }

    @Test
    public void testGetInvite() {
        getInviteService().getInvite(inviteCode).block();
    }

    @Test
    public void testDeleteInvite() {
        getChannelService().getChannelInvites(modifyChannel)
                .filter(invite -> invite.getMaxAge() != null)
                .filter(invite -> Optional.ofNullable(invite.getCreatedAt())
                        .map(this::asInstant)
                        .map(ts -> ts.plusSeconds(invite.getMaxAge()))
                        .map(ts -> ts.isBefore(Instant.now()))
                        .orElse(false))
                .map(InviteResponse::getCode)
                .flatMap(code -> getInviteService().deleteInvite(code))
                .blockLast();
    }

    private Instant asInstant(String timestamp) {
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(timestamp, Instant::from);
    }

    @Test
    public void discordTimestampToInstant() {
        String timestamp = "2018-06-01T16:40:45.991000+00:00";
        Instant expected = ZonedDateTime.of(2018, 6, 1, 16, 40, 45, 991_000_000, ZoneId.of("Z")).toInstant();
        assertEquals(expected, asInstant(timestamp));
    }

}
