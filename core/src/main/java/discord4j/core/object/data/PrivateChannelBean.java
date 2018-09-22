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
package discord4j.core.object.data;

import discord4j.common.json.UserResponse;
import discord4j.core.object.data.stored.MessageChannelBean;
import discord4j.gateway.json.response.GatewayChannelResponse;
import discord4j.rest.json.response.ChannelResponse;

import java.util.Arrays;
import java.util.Objects;

public final class PrivateChannelBean extends MessageChannelBean {

    private static final long serialVersionUID = -8917796342023055869L;

    private long[] recipients;

    public PrivateChannelBean(final GatewayChannelResponse response) {
        super(response);

        recipients = Arrays.stream(Objects.requireNonNull(response.getRecipients()))
                .mapToLong(UserResponse::getId)
                .toArray();
    }

    public PrivateChannelBean(final ChannelResponse response) {
        super(response);

        recipients = Arrays.stream(Objects.requireNonNull(response.getRecipients()))
                .mapToLong(UserResponse::getId)
                .toArray();
    }

    public PrivateChannelBean() {}

    public long[] getRecipients() {
        return recipients;
    }

    public void setRecipients(final long[] recipients) {
        this.recipients = recipients;
    }

    @Override
    public String toString() {
        return "PrivateChannelBean{" +
                "recipients=" + Arrays.toString(recipients) +
                "} " + super.toString();
    }
}
