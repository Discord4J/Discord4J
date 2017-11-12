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
package discord4j.common.json.payload;

import discord4j.common.json.GameEntity;

import javax.annotation.Nullable;

public class StatusUpdate implements Payload {

	@Nullable
	private Long since;
	@Nullable
	private GameEntity game;
	private String status;
	private boolean afk;

	@Nullable
	public Long getSince() {
		return since;
	}

	public void setSince(@Nullable Long since) {
		this.since = since;
	}

	@Nullable
	public GameEntity getGame() {
		return game;
	}

	public void setGame(@Nullable GameEntity game) {
		this.game = game;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public boolean isAfk() {
		return afk;
	}

	public void setAfk(boolean afk) {
		this.afk = afk;
	}
}
