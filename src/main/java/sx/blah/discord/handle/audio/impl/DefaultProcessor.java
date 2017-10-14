/*
 *     This file is part of Discord4J.
 *
 *     Discord4J is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Discord4J is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */

package sx.blah.discord.handle.audio.impl;

import sx.blah.discord.handle.audio.AudioEncodingType;
import sx.blah.discord.handle.audio.IAudioProcessor;
import sx.blah.discord.handle.audio.IAudioProvider;

/**
 * The default implementation of {@link IAudioProcessor} which just relays audio data as is.
 */
public class DefaultProcessor implements IAudioProcessor {

	/**
	 * The underlying provider from which audio is pulled.
	 */
	private volatile IAudioProvider provider = new DefaultProvider();

	@Override
	public boolean setProvider(IAudioProvider provider) {
		this.provider = provider;
		return true;
	}

	@Override
	public boolean isReady() {
		return provider.isReady();
	}

	@Override
	public byte[] provide() {
		return provider.provide();
	}

	@Override
	public int getChannels() {
		return provider.getChannels();
	}

	@Override
	public AudioEncodingType getAudioEncodingType() {
		return provider.getAudioEncodingType();
	}
}
