package discord4j.core.spec;

import com.fasterxml.jackson.annotation.JsonProperty;
import discord4j.core.object.RecurrenceRule;
import discord4j.discordjson.json.RecurrenceRuleData;
import org.immutables.value.Value;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.Month;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Value.Immutable
public interface RecurrenceRuleSpecGenerator extends Spec<RecurrenceRuleData> {

    Instant start();

    Optional<Instant> end();

    RecurrenceRule.Frequency frequency();

    int interval();

    Optional<List<DayOfWeek>> byWeekday();

    @JsonProperty("by_n_weekday")
    Optional<List<RecurrenceRuleNWeekdaySpec>> byNWeekday();

    @JsonProperty("by_month")
    Optional<List<Month>> byMonth();

    @JsonProperty("by_month_day")
    Optional<List<Integer>> byMonthDay();

    @JsonProperty("by_year_day")
    Optional<List<Integer>> byYearDay();

    Optional<Integer> count();

    @Override
    default RecurrenceRuleData asRequest() {
        return RecurrenceRuleData.builder()
            .start(start())
            .end(end())
            .frequency(frequency().getValue())
            .interval(interval())
            .byWeekday(byWeekday().map(dayOfWeeks -> dayOfWeeks.stream().map(dayOfWeek -> dayOfWeek.getValue() - 1).collect(Collectors.toList())))
            .byNWeekday(byNWeekday().map(nWeekdays -> nWeekdays.stream().map(RecurrenceRuleNWeekdaySpecGenerator::asRequest).collect(Collectors.toList())))
            .byMonth(byMonth().map(months -> months.stream().map(Month::getValue).collect(Collectors.toList())))
            .byMonthDay(byMonthDay())
            .byYearDay(byYearDay())
            .count(count())
            .build();
    }

}
