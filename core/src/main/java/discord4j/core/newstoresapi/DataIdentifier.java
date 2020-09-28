package discord4j.core.newstoresapi;

import discord4j.common.util.Snowflake;

import java.util.Objects;
import java.util.Optional;

public class DataIdentifier {

    private final DataIdentifier parent;
    private final EntityType type;
    private final Snowflake snowflake;

    private DataIdentifier(DataIdentifier parent, EntityType type, Snowflake snowflake) {
        this.parent = parent;
        this.type = type;
        this.snowflake = snowflake;
    }

    public static DataIdentifier id(EntityType type, Snowflake snowflake) {
        return new DataIdentifier(null, type, snowflake);
    }

    public static DataIdentifier idWithParent(DataIdentifier parent, EntityType type, Snowflake snowflake) {
        Objects.requireNonNull(parent);
        return new DataIdentifier(parent, type, snowflake);
    }

    public Snowflake getSnowflake() {
        return snowflake;
    }

    public Optional<DataIdentifier> getParent() {
        return Optional.ofNullable(parent);
    }
}
