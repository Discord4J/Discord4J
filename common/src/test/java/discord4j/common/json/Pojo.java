package discord4j.common.json;

import discord4j.common.jackson.Possible;
import discord4j.common.jackson.PossibleJson;

import javax.annotation.Nullable;

@PossibleJson
public class Pojo {

	@Nullable
	private Possible<String> string;

	public Pojo(@Nullable Possible<String> string) {
		this.string = string;
	}

	@Nullable
	public Possible<String> getString() {
		return string;
	}

	public void setString(@Nullable Possible<String> string) {
		this.string = string;
	}
}
