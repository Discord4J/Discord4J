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

package discord4j.rest.util;

import discord4j.common.json.request.MessageCreateRequest;
import reactor.ipc.netty.http.client.HttpClientRequest;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class MultipartRequest {

	private final Consumer<HttpClientRequest.Form> formConsumer;
	private final MessageCreateRequest createRequest;

	public MultipartRequest(Consumer<HttpClientRequest.Form> formConsumer, @Nullable MessageCreateRequest createRequest) {
		this.formConsumer = formConsumer;
		this.createRequest = createRequest;
	}

	public Consumer<HttpClientRequest.Form> getFormConsumer() {
		return formConsumer;
	}

	@Nullable
	public MessageCreateRequest getCreateRequest() {
		return createRequest;
	}
}
