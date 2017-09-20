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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */
package discord4j.common.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import discord4j.common.jackson.DiscordEntityFilter;
import discord4j.common.jackson.Possible;
import discord4j.common.jackson.PossibleOptional;
import reactor.util.lang.NonNullApi;

import java.util.Optional;

@NonNullApi
@JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = DiscordEntityFilter.class)
public class Pojo {

	private String always;
	private Optional<String> nullable;
	private Possible<String> sometimes = Possible.absent();
	private PossibleOptional<String> sometimesAndNullable = PossibleOptional.absent();

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

	@Override
	public String toString() {
		return "Pojo{" +
				"always='" + always + '\'' +
				", nullable=" + nullable +
				", sometimes=" + sometimes +
				", sometimesAndNullable=" + sometimesAndNullable +
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

		Pojo pojo = (Pojo) o;

		if (!always.equals(pojo.always)) {
			return false;
		}
		if (!nullable.equals(pojo.nullable)) {
			return false;
		}
		if (!sometimes.equals(pojo.sometimes)) {
			return false;
		}
		return sometimesAndNullable.equals(pojo.sometimesAndNullable);
	}

	@Override
	public int hashCode() {
		int result = always.hashCode();
		result = 31 * result + nullable.hashCode();
		result = 31 * result + sometimes.hashCode();
		result = 31 * result + sometimesAndNullable.hashCode();
		return result;
	}
}
