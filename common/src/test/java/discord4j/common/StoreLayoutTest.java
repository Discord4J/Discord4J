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

package discord4j.common;

import discord4j.common.store.Store;
import discord4j.common.store.action.read.ReadActions;
import discord4j.common.store.api.StoreFlag;
import discord4j.common.store.api.layout.StoreLayout;
import discord4j.common.store.impl.SelectiveStoreLayout;
import discord4j.discordjson.json.ChannelData;
import discord4j.discordjson.json.ImmutableChannelData;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.EnumSet;

public class StoreLayoutTest {

    @Test
    public void fromLayoutsMergesFirst() {
        StoreLayout layout1 = SelectiveStoreLayout.create(
                EnumSet.of(StoreFlag.GUILD, StoreFlag.CHANNEL, StoreFlag.ROLE, StoreFlag.VOICE_STATE),
                new TestStoreLayout("first") {
                    @Override
                    public Flux<ChannelData> getChannels() {
                        return Flux.just(ImmutableChannelData.builder()
                                .id(1L)
                                .type(1)
                                .name("first-channel")
                                .build());
                    }
                }
        );

        StoreLayout layout2 = SelectiveStoreLayout.create(
                EnumSet.of(StoreFlag.MEMBER, StoreFlag.USER, StoreFlag.CHANNEL),
                new TestStoreLayout("second")
        );

        Store store = Store.fromLayouts(layout1, layout2);

        StepVerifier.create(store.execute(ReadActions.getChannels()))
                .expectNextMatches(it -> it.name().get().equals("first-channel"))
                .verifyComplete();
    }
}
