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
import discord4j.discordjson.Id;
import discord4j.discordjson.json.AutoModActionData;
import discord4j.discordjson.json.AutoModRuleModifyRequest;
import discord4j.discordjson.json.AutoModTriggerMetaData;
import discord4j.discordjson.possible.Possible;
import org.immutables.value.Value;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Value.Immutable
public interface AutoModRuleEditSpecGenerator extends AuditSpec<AutoModRuleModifyRequest> {

    String name();

    int eventType();

    Possible<AutoModTriggerMetaData> triggerMetaData();

    @Value.Default
    default List<AutoModActionData> actions() {
        return Collections.emptyList();
    }

    boolean enabled();

    @Value.Default
    default List<Snowflake> exemptRoles() {
        return Collections.emptyList();
    }

    @Value.Default
    default List<Snowflake> exemptChannels() {
        return Collections.emptyList();
    }

    @Override
    default AutoModRuleModifyRequest asRequest() {
        return AutoModRuleModifyRequest.builder()
                .name(name())
                .eventType(eventType())
                .triggerMetadata(triggerMetaData())
                .actions(actions())
                .enabled(enabled())
                .exemptRoles(exemptRoles().stream().map(Snowflake::asLong).map(Id::of).collect(Collectors.toList()))
                .exemptChannels(exemptChannels().stream().map(Snowflake::asLong).map(Id::of).collect(Collectors.toList()))
                .build();
    }

}

@SuppressWarnings("immutables:subtype")
@Value.Immutable(builder = false)
abstract class AutoModRuleEditMonoGenerator extends Mono<AutoModRule> implements AutoModRuleEditSpecGenerator {

    abstract AutoModRule autoModRule();

    @Override
    public void subscribe(CoreSubscriber<? super AutoModRule> actual) {
        autoModRule().edit(AutoModRuleEditSpec.copyOf(this)).subscribe(actual);
    }

    @Override
    public abstract String toString();
}
