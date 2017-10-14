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
import sx.blah.discord.handle.audio.IAudioProvider;

/**
 * The default implementation of {@link IAudioProvider} which is NO-OP. It provides no audio data.
 */
public class DefaultProvider implements IAudioProvider {

	@Override
	public boolean isReady() {
		return false;
	}

	@Override
	public byte[] provide() {
		return new byte[0];
	}

	@Override
	public int getChannels() {
		return 0;
	}

	@Override
	public AudioEncodingType getAudioEncodingType() {
		return AudioEncodingType.OPUS; //OPUS to ensure no processing is done to it.
	}
}
