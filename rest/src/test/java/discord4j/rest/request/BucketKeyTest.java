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
package discord4j.rest.request;

import discord4j.rest.util.RouteUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class BucketKeyTest {

    @Test
    public void testRouteWithMajorParamSupport() {
        String template = "/channels/{channel.id}/messages";
        BucketKey key1 = BucketKey.of(template, RouteUtils.expand(template, 111111111));
        BucketKey key2 = BucketKey.of(template, RouteUtils.expand(template, 222222222));
        assertNotEquals(key1, key2);
        assertNotEquals(key1.hashCode(), key2.hashCode());
    }

    @Test
    public void testRouteWithoutMajorParamSupport() {
        String template = "/invites/{invite.code}";
        BucketKey keyA = BucketKey.of(template, RouteUtils.expand(template, "AAAAAA"));
        BucketKey keyB = BucketKey.of(template, RouteUtils.expand(template, "BBBBBB"));
        assertEquals(keyA, keyB);
        assertEquals(keyA.hashCode(), keyB.hashCode());
    }

    @Test
    public void testRouteWithModifiedTemplate() {
        String template1 = "/channels/{channel.id}/messages/{message.id}";
        String template2 = "DELETE /channels/{channel.id}/messages/{message.id}";
        BucketKey key1 = BucketKey.of(template1, RouteUtils.expand(template1, 1, 2));
        BucketKey key2 = BucketKey.of(template2, RouteUtils.expand(template2, 1, 2));
        assertNotEquals(key1, key2);
        assertNotEquals(key1.hashCode(), key2.hashCode());
    }
}
