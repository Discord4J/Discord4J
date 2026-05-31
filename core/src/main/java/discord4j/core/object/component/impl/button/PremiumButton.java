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
package discord4j.core.object.component.impl.button;

import discord4j.common.util.Snowflake;
import discord4j.core.object.component.impl.Button;
import discord4j.discordjson.json.component.ButtonComponentData;
import discord4j.discordjson.json.component.ImmutableButtonComponentData;

import java.util.function.Consumer;

/**
 * Represents a button with a style of PREMIUM.
 */
public class PremiumButton extends Button {

    public static PremiumButton of(Snowflake skuId) {
        return new PremiumButton(skuId);
    }

    private PremiumButton(Snowflake skuId) {
        this(ButtonComponentData.builder()
                .style(Style.PREMIUM.getValue())
                .skuId(skuId.asString())
                .build());
    }

    protected PremiumButton(ButtonComponentData data) {
        super(data);
    }

    public PremiumButton withComponentId(Integer componentId) {
        return this.create(builder -> builder.id(componentId));
    }

    public PremiumButton withSkuId(String skuId) {
        return this.create(builder -> builder.skuId(skuId));
    }

    private PremiumButton create(Consumer<ImmutableButtonComponentData.Builder> builderConsumer) {
        ImmutableButtonComponentData.Builder dataBuilder = ButtonComponentData.builder().from(this.getData());
        builderConsumer.accept(dataBuilder);
        return new PremiumButton(dataBuilder.build());
    }
}
