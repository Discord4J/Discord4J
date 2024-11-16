package discord4j.core.spec;

import discord4j.discordjson.json.RecurrenceRuleNWeekdayData;
import org.immutables.value.Value;

import java.time.DayOfWeek;

@Value.Immutable
public interface RecurrenceRuleNWeekdaySpecGenerator extends Spec<RecurrenceRuleNWeekdayData> {

    int n();

    DayOfWeek day();

    @Override
    default RecurrenceRuleNWeekdayData asRequest() {
        return RecurrenceRuleNWeekdayData.builder()
            .n(n())
            .day(day().getValue() - 1)
            .build();
    }
}
