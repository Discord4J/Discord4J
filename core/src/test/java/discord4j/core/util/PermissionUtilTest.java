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
package discord4j.core.util;

import discord4j.core.object.PermissionOverwrite;
import discord4j.core.object.util.PermissionSet;
import discord4j.core.object.util.Snowflake;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static discord4j.core.object.util.Permission.*;
import static org.junit.Assert.assertEquals;

public class PermissionUtilTest {

    private static PermissionOverwrite overwrite(PermissionSet allowed, PermissionSet denied) {
        // create an overwrite with garbage ID and type. These are not important *for these tests*.
        return PermissionOverwrite.forMember(Snowflake.of(-1L), allowed, denied);
    }

    @Test
    public void testComputeBase() {
        PermissionSet everyonePerms = PermissionSet.of(SEND_MESSAGES);
        List<PermissionSet> rolePerms = Arrays.asList(PermissionSet.of(BAN_MEMBERS, PRIORITY_SPEAKER), PermissionSet.of(MANAGE_ROLES));

        PermissionSet actual = PermissionUtil.computeBasePermissions(everyonePerms, rolePerms);
        PermissionSet expected = PermissionSet.of(SEND_MESSAGES, BAN_MEMBERS, PRIORITY_SPEAKER, MANAGE_ROLES);

        assertEquals(expected, actual);
    }

    @Test
    public void testNoOverwritesYieldsOriginal() {
        PermissionSet base = PermissionSet.of(SEND_MESSAGES);
        List<PermissionOverwrite> roleOverwrites = Collections.emptyList();
        PermissionOverwrite memberOverwrite = null;

        PermissionSet actual = PermissionUtil.computePermissions(base, null, roleOverwrites, memberOverwrite);
        PermissionSet expected = PermissionSet.of(SEND_MESSAGES);

        assertEquals(expected, actual);
    }

    @Test
    public void testRoleOverwriteAllows() {
        PermissionSet base = PermissionSet.of(SEND_MESSAGES);
        List<PermissionOverwrite> roleOverwrites = Collections.singletonList(overwrite(PermissionSet.of(PRIORITY_SPEAKER), PermissionSet.none()));
        PermissionOverwrite memberOverwrite = null;

        PermissionSet actual = PermissionUtil.computePermissions(base, null, roleOverwrites, memberOverwrite);
        PermissionSet expected = PermissionSet.of(SEND_MESSAGES, PRIORITY_SPEAKER);

        assertEquals(expected, actual);
    }

    @Test
    public void testMemberOverwriteAllows() {
        PermissionSet base = PermissionSet.of(SEND_MESSAGES);
        List<PermissionOverwrite> roleOverwrites = Collections.emptyList();
        PermissionOverwrite memberOverwrite = overwrite(PermissionSet.of(PRIORITY_SPEAKER), PermissionSet.none());

        PermissionSet actual = PermissionUtil.computePermissions(base, null, roleOverwrites, memberOverwrite);
        PermissionSet expected = PermissionSet.of(SEND_MESSAGES, PRIORITY_SPEAKER);

        assertEquals(expected, actual);
    }

    @Test
    public void testRoleOverwriteDenies() {
        PermissionSet base = PermissionSet.of(SEND_MESSAGES);
        List<PermissionOverwrite> roleOverwrites = Collections.singletonList(overwrite(PermissionSet.none(), PermissionSet.of(SEND_MESSAGES)));
        PermissionOverwrite memberOverwrite = null;

        PermissionSet actual = PermissionUtil.computePermissions(base, null, roleOverwrites, memberOverwrite);
        PermissionSet expected = PermissionSet.none();

        assertEquals(expected, actual);
    }

    @Test
    public void testOverwriteAllowsAndDenies() {
        PermissionSet base = PermissionSet.of(SEND_MESSAGES);
        List<PermissionOverwrite> roleOverwrites = Collections.emptyList();
        PermissionOverwrite memberOverwrite = overwrite(PermissionSet.of(PRIORITY_SPEAKER), PermissionSet.of(SEND_MESSAGES));

        PermissionSet actual = PermissionUtil.computePermissions(base, null, roleOverwrites, memberOverwrite);
        PermissionSet expected = PermissionSet.of(PRIORITY_SPEAKER);

        assertEquals(expected, actual);
    }

    @Test
    public void testMemberOverwriteDenies() {
        PermissionSet base = PermissionSet.of(SEND_MESSAGES);
        List<PermissionOverwrite> roleOverwrites = Collections.emptyList();
        PermissionOverwrite memberOverwrite = overwrite(PermissionSet.none(), PermissionSet.of(SEND_MESSAGES));

        PermissionSet actual = PermissionUtil.computePermissions(base, null, roleOverwrites, memberOverwrite);
        PermissionSet expected = PermissionSet.none();

        assertEquals(expected, actual);
    }

    @Test
    public void testAdminGrantsAll() {
        PermissionSet base = PermissionSet.of(ADMINISTRATOR);
        List<PermissionOverwrite> roleOverwrites = Collections.emptyList();
        PermissionOverwrite memberOverwrite = null;

        PermissionSet actual = PermissionUtil.computePermissions(base, null, roleOverwrites, memberOverwrite);
        PermissionSet expected = PermissionSet.all();

        assertEquals(expected, actual);
    }

    @Test
    public void testAdminBypassesOverwrites() {
        PermissionSet base = PermissionSet.of(SEND_MESSAGES, ADMINISTRATOR);
        List<PermissionOverwrite> roleOverwrites = Collections.emptyList();
        PermissionOverwrite memberOverwrite = overwrite(PermissionSet.none(), PermissionSet.of(SEND_MESSAGES));

        PermissionSet actual = PermissionUtil.computePermissions(base, null, roleOverwrites, memberOverwrite);
        PermissionSet expected = PermissionSet.all();

        assertEquals(expected, actual);
    }

    @Test
    public void testIssue468() {
        PermissionSet everyonePerms = PermissionSet.of(VIEW_CHANNEL, SEND_MESSAGES);
        PermissionSet rolePerms = PermissionSet.of(VIEW_CHANNEL, SEND_MESSAGES);
        PermissionOverwrite everyoneOverwrite = overwrite(PermissionSet.none(), PermissionSet.of(SEND_MESSAGES));

        PermissionSet base = PermissionUtil.computeBasePermissions(everyonePerms, Collections.singletonList(rolePerms));

        PermissionSet actual = PermissionUtil.computePermissions(base, everyoneOverwrite, Collections.emptyList(), null);
        PermissionSet expected = PermissionSet.of(VIEW_CHANNEL);

        assertEquals(expected, actual);
    }

}
