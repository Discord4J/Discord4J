package discord4j.common.json;

import discord4j.common.jackson.Possible;

import javax.annotation.Nullable;
import java.util.Objects;

public class PossiblePojo {

    private Possible<String> string = Possible.absent();

    public PossiblePojo(@Nullable Possible<String> string) {
        this.string = string;
    }

    public PossiblePojo() {
    }

    @Nullable
    public Possible<String> getString() {
        return string;
    }

    @Override
    public String toString() {
        return "PossiblePojo{" +
                "string=" + string +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PossiblePojo that = (PossiblePojo) o;
        return Objects.equals(string, that.string);
    }
}
