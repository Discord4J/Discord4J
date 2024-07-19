package discord4j.core.object;

import discord4j.discordjson.json.ApplicationRoleConnectionMetadataData;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents metadata for an application role connection.
 *
 * @see
 * <a href="https://discord.com/developers/docs/resources/application-role-connection-metadata">Application Role Connection Metadata</a>
 */
public class ApplicationRoleConnectionMetadata {

    private final ApplicationRoleConnectionMetadataData data;

    public ApplicationRoleConnectionMetadata(ApplicationRoleConnectionMetadataData data) {
        this.data = data;
    }

    /**
     * Get the raw data for this metadata.
     *
     * @return the raw data
     */
    public ApplicationRoleConnectionMetadataData getData() {
        return this.data;
    }

    /**
     * Get the type of this metadata.
     *
     * @return the type of this metadata
     */
    public Type getType() {
        return Type.from(this.data.type());
    }

    /**
     * Get the key of this metadata.
     *
     * @return the key of this metadata
     */
    public String getKey() {
        return this.data.key();
    }

    /**
     * Get the name of this metadata.
     *
     * @return the name of this metadata
     */
    public String getName() {
        return this.data.name();
    }

    /**
     * Get the description of this metadata.
     *
     * @return the description of this metadata
     */
    public String getDescription() {
        return this.data.description();
    }

    /**
     * Get the localized names of this metadata.
     *
     * @return the localized names of this metadata
     */
    public Map<Locale, String> getLocalizedNames() {
        return data.nameLocalizations()
            .toOptional()
            .orElse(Collections.emptyMap())
            .entrySet()
            .stream()
            .collect(Collectors.toMap(entry -> new Locale.Builder().setLanguageTag(entry.getKey()).build(),
                Map.Entry::getValue));
    }

    /**
     * Get the localized descriptions of this metadata.
     *
     * @return the localized descriptions of this metadata
     */
    public Map<Locale, String> getLocalizedDescriptions() {
        return data.descriptionLocalizations()
            .toOptional()
            .orElse(Collections.emptyMap())
            .entrySet()
            .stream()
            .collect(Collectors.toMap(entry -> new Locale.Builder().setLanguageTag(entry.getKey()).build(),
                Map.Entry::getValue));
    }

    public enum Type {

        /** Unknown type. */
        UNKNOWN(-1),

        /** the metadata value (integer) is less than or equal to the guild's configured value (integer) */
        INTEGER_LESS_THAN_OR_EQUAL(1),

        /** the metadata value (integer) is greater than or equal to the guild's configured value (integer) */
        INTEGER_GREATER_THAN_OR_EQUAL(2),

        /** the metadata value (integer) is equal to the guild's configured value (integer) */
        INTEGER_EQUAL(3),

        /** the metadata value (integer) is not equal to the guild's configured value (integer) */
        INTEGER_NOT_EQUAL(4),

        /**
         * the metadata value (ISO8601 string) is less than or equal to the guild's configured value (integer; days
         * before current date)
         */
        DATETIME_LESS_THAN_OR_EQUAL(5),

        /**
         * the metadata value (ISO8601 string) is greater than or equal to the guild's configured value (integer;
         * days before current date)
         */
        DATETIME_GREATER_THAN_OR_EQUAL(6),

        /** the metadata value (integer) is equal to the guild's configured value (integer; 1) */
        BOOLEAN_EQUAL(7),

        /** the metadata value (integer) is not equal to the guild's configured value (integer; 1) */
        BOOLEAN_NOT_EQUAL(8),
        ;

        /** The internal value as represented by Discord */
        private final int value;

        Type(int value) {
            this.value = value;
        }

        /**
         * Get the internal value as represented by Discord.
         *
         * @return the internal value
         */
        public int getValue() {
            return this.value;
        }

        /**
         * Get the {@link Type} from an internal value. It is guaranteed that invoking {@link #getValue()} from the
         * returned enum will equal ({@code ==}) the supplied {@code value}.
         *
         * @param value the internal value
         * @return the {@link Type} for the internal value
         */
        public static Type from(int value) {
            switch (value) {
                case 1: return Type.INTEGER_LESS_THAN_OR_EQUAL;
                case 2: return Type.INTEGER_GREATER_THAN_OR_EQUAL;
                case 3: return Type.INTEGER_EQUAL;
                case 4: return Type.INTEGER_NOT_EQUAL;
                case 5: return Type.DATETIME_LESS_THAN_OR_EQUAL;
                case 6: return Type.DATETIME_GREATER_THAN_OR_EQUAL;
                case 7: return Type.BOOLEAN_EQUAL;
                case 8: return Type.BOOLEAN_NOT_EQUAL;
                default: return Type.UNKNOWN;
            }
        }
    }
}
