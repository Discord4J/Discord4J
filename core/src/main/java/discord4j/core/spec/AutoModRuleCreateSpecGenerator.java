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

package discord4j.core.spec;

import discord4j.common.util.Snowflake;
import discord4j.core.object.automod.AutoModRule;
import discord4j.core.object.entity.Guild;
import discord4j.discordjson.Id;
import discord4j.discordjson.json.AutoModActionData;
import discord4j.discordjson.json.AutoModRuleCreateRequest;
import discord4j.discordjson.json.AutoModTriggerMetaData;
import discord4j.discordjson.possible.Possible;
import org.immutables.value.Value;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static discord4j.core.spec.InternalSpecUtils.mapPossible;

@Value.Immutable
interface AutoModRuleCreateSpecGenerator extends AuditSpec<AutoModRuleCreateRequest> {

    String name();

    int eventType();

    int triggerType();

    Possible<AutoModTriggerMetaData> triggerMetaData();

    @Value.Default
    default List<AutoModActionData> actions() {
        return Collections.emptyList();
    }

    Possible<Boolean> enabled();

    Possible<List<Snowflake>> exemptRoles();

    Possible<List<Snowflake>> exemptChannels();

    @Override
    default AutoModRuleCreateRequest asRequest() {
        return AutoModRuleCreateRequest.builder()
                .name(name())
                .eventType(eventType())
                .triggerType(triggerType())
                .triggerMetadata(triggerMetaData())
                .actions(actions())
                .enabled(enabled())
                .exemptRoles(mapPossible(exemptRoles(), r -> r.stream().map(Snowflake::asLong).map(Id::of).collect(Collectors.toList())))
                .exemptChannels(mapPossible(exemptChannels(), r -> r.stream().map(Snowflake::asLong).map(Id::of).collect(Collectors.toList())))
                .build();
    }
}

@SuppressWarnings("immutables:subtype")
@Value.Immutable(builder = false)
abstract class AutoModRuleCreateMonoGenerator extends Mono<AutoModRule> implements AutoModRuleCreateSpecGenerator {

    abstract Guild guild();

    @Override
    public void subscribe(CoreSubscriber<? super AutoModRule> actual) {
        guild().createAutoModRule(AutoModRuleCreateSpec.copyOf(this)).subscribe(actual);
    }

    @Override
    public abstract String toString();
}
