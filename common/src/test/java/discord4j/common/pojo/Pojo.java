package discord4j.common.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import discord4j.common.jackson.DiscordPojoFilter;
import discord4j.common.jackson.Possible;
import discord4j.common.jackson.PossibleOptional;

import java.util.Optional;

@JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = DiscordPojoFilter.class)
public class Pojo {

	private String always;
	private Optional<String> nullable;
	private Possible<String> sometimes;
	private PossibleOptional<String> sometimesAndNullable;

	public String getAlways() {
		return always;
	}

	public void setAlways(String always) {
		this.always = always;
	}

	public Optional<String> getNullable() {
		return nullable;
	}

	public void setNullable(Optional<String> nullable) {
		this.nullable = nullable;
	}

	public Possible<String> getSometimes() {
		return sometimes;
	}

	public void setSometimes(Possible<String> sometimes) {
		this.sometimes = sometimes;
	}

	public PossibleOptional<String> getSometimesAndNullable() {
		return sometimesAndNullable;
	}

	public void setSometimesAndNullable(PossibleOptional<String> sometimesAndNullable) {
		this.sometimesAndNullable = sometimesAndNullable;
	}
}
