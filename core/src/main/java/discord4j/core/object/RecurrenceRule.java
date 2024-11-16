package discord4j.core.object;

import discord4j.discordjson.json.RecurrenceRuleData;
import discord4j.discordjson.json.RecurrenceRuleNWeekdayData;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.Month;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Represent the definition for how often an event should recur.
 *
 * @see <a href="https://discord.com/developers/docs/resources/guild-scheduled-event#guild-scheduled-event-recurrence-rule-object">Guild Scheduled Event Recurrence Rule</a>
 */
public class RecurrenceRule {

    /**
     * The raw data as represented by Discord.
     */
    private final RecurrenceRuleData data;

    /**
     * Constructs a {@code ScheduledEventRecurrenceRule} with Discord data.
     *
     * @param data The raw data as represented by Discord, must be non-null.
     */
    public RecurrenceRule(final RecurrenceRuleData data) {
        this.data = data;
    }

    /**
     * Gets the data of the recurrence rule.
     *
     * @return The data of the recurrence rule.
     */
    public RecurrenceRuleData getData() {
        return data;
    }

    /**
     * Gets the starting time of the recurrence interval.
     *
     * @return The start time of the recurrence interval.
     */
    public Instant getStartTime() {
        return data.start();
    }

    /**
     * Gets the ending time of the recurrence interval.
     *
     * @return The end time of the recurrence interval.
     */
    public Optional<Instant> getEndTime() {
        return data.end();
    }

    /**
     * Gets how often the event occurs
     *
     * @return The frequency of this recurrence rule.
     */
    public Frequency getFrequency() {
        return Frequency.of(data.frequency());
    }

    /**
     * Gets the spacing between the events, defined by frequency.
     * <br>
     * For example, frequency of WEEKLY and an interval of 2 would be "every-other week"
     *
     * @return The interval of this recurrence rule.
     */
    public int getInterval() {
        return data.interval();
    }

    /**
     * Gets a list of specific days within a week for the event to recur on.
     *
     * @return The list of specific days within a week for the event to recur on, if present.
     */
    public Optional<List<DayOfWeek>> getByWeekday() {
        // Weekday start from 0 then need handle this to the ordinal value and not week day
        return this.data.byWeekday().map(byWeekdays -> byWeekdays.stream().map(day -> DayOfWeek.values()[day]).collect(Collectors.toList()));
    }

    /**
     * Gets a list of specific days within a specific week (1-5) to recur on.
     *
     * @return The list of specific days within a specific week (1-5) to recur on, if present.
     */
    public Optional<List<NWeekday>> getByNWeekday() {
        return this.data.byNWeekday().map(byNWeekdayList -> byNWeekdayList.stream().map(NWeekday::new).collect(Collectors.toList()));
    }

    /**
     * Gets a list of specific months to recur on.
     *
     * @return The list of specific months to recur on, if present.
     */
    public Optional<List<Month>> getByMonth() {
        return this.data.byMonth().map(byMonths -> byMonths.stream().map(Month::of).collect(Collectors.toList()));
    }

    /**
     * Gets a list of specific dates within a month to recur on
     *
     * @return The list of specific dates within a month to recur on, if present.
     */
    public Optional<List<Integer>> getByMonthDay() {
        return this.data.byMonthDay();
    }

    /**
     * Gets a list of days within a year to recur on (1-364)
     *
     * @return The list of days within a year to recur on, if present.
     */
    public Optional<List<Integer>> getByYearDay() {
        return this.data.byYearDay();
    }

    /**
     * Gets the total amount of times that the event is allowed to recur before stopping.
     *
     * @return The total amount of times that the event is allowed to recur before stopping.
     */
    public Optional<Integer> getCount() {
        return this.data.count();
    }

    /**
     * Represent a specific days within a specific week (1-5) to recur on.
     *
     * @see <a href="https://discord.com/developers/docs/resources/guild-scheduled-event#guild-scheduled-event-recurrence-rule-object-guild-scheduled-event-recurrence-rule-nweekday-structure">Guild Scheduled Event Recurrence Rule - N_Weekday</a>
     */
    public static class NWeekday {

        /**
         * The raw data as represented by Discord.
         */
        private final RecurrenceRuleNWeekdayData data;

        public NWeekday(RecurrenceRuleNWeekdayData data) {
            this.data = data;
        }

        public RecurrenceRuleNWeekdayData getData() {
            return data;
        }

        /**
         * Get the week to reoccur on.
         *
         * @return The week to reoccur on. 1 - 5.
         */
        public int getN() {
            return this.data.n();
        }

        /**
         * Get the day within the week to reoccur on.
         *
         * @return The day within the week to reoccur on.
         */
        public DayOfWeek getDay() {
            return DayOfWeek.values()[this.data.day()];
        }
    }

    /**
     * Represents a recurrence rule's frequency.
     */
    public enum Frequency {
        UNKNOWN(-1),
        YEARLY(0),
        MONTHLY(1),
        WEEKLY(2),
        DAILY(3),
        ;

        private final int value;

        Frequency(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static RecurrenceRule.Frequency of(int value) {
            switch (value) {
                case 0:
                    return YEARLY;
                case 1:
                    return MONTHLY;
                case 2:
                    return WEEKLY;
                case 3:
                    return DAILY;
                default:
                    return UNKNOWN;
            }
        }
    }
}
