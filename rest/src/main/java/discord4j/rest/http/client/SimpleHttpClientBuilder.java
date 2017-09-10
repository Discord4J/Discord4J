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
package discord4j.rest.http.client;

import discord4j.rest.http.ReaderStrategy;
import discord4j.rest.http.WriterStrategy;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import reactor.ipc.netty.http.client.HttpClient;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

class SimpleHttpClientBuilder implements SimpleHttpClient.Builder {

	private final HttpHeaders headers = new DefaultHttpHeaders();
	private final List<ReaderStrategy<?>> readerStrategies = new ArrayList<>();
	private final List<WriterStrategy<?>> writerStrategies = new ArrayList<>();
	private String baseUrl = "";

	@Override
	public SimpleHttpClient.Builder baseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
		return this;
	}

	@Override
	public SimpleHttpClient.Builder defaultHeader(String key, String value) {
		headers.add(key, value);
		return this;
	}

	@Override
	public SimpleHttpClient.Builder writerStrategy(WriterStrategy<?> strategy) {
		writerStrategies.add(strategy);
		return this;
	}

	@Override
	public SimpleHttpClient.Builder readerStrategy(ReaderStrategy<?> strategy) {
		readerStrategies.add(strategy);
		return this;
	}

	@Override
	public SimpleHttpClient build() {
		return new SimpleHttpClient(HttpClient.create(), baseUrl, headers, writerStrategies, readerStrategies);
	}
}
