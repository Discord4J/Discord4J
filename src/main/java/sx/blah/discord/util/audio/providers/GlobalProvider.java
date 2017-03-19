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

package sx.blah.discord.util.audio.providers;

import sx.blah.discord.api.internal.OpusUtil;
import sx.blah.discord.handle.audio.AudioEncodingType;
import sx.blah.discord.handle.audio.IAudioProvider;
import sx.blah.discord.handle.audio.impl.DefaultProvider;
import sx.blah.discord.util.TimedValue;

/**
 * GlobalProvider allows for an {@link IAudioProvider} to provide the same audio across multiple
 * {@link sx.blah.discord.handle.audio.IAudioManager} instances.
 */
public class GlobalProvider implements IAudioProvider {

	private static volatile IAudioProvider provider;

	private static final TimedValue<Boolean> isReady = new TimedValue<>(OpusUtil.OPUS_FRAME_TIME,
			() -> provider.isReady());
	private static final TimedValue<byte[]> provide = new TimedValue<>(OpusUtil.OPUS_FRAME_TIME,
			() -> provider.provide());
	private static final TimedValue<Integer> channels = new TimedValue<>(OpusUtil.OPUS_FRAME_TIME,
			() -> provider.getChannels());
	private static final TimedValue<AudioEncodingType> audioEncodingType
			= new TimedValue<>(OpusUtil.OPUS_FRAME_TIME, () -> provider.getAudioEncodingType());

	private static final GlobalProvider instance = new GlobalProvider(); //Singleton instance

	private GlobalProvider() {} //Encourage the use of singletons

	/**
	 * Gets the singleton instance of this provider.
	 *
	 * @return The instance.
	 */
	public static GlobalProvider getInstance() {
		return instance;
	}

	/**
	 * Sets the provider to broadcast.
	 *
	 * @param provider The provider.
	 */
	public static void setProvider(IAudioProvider provider) {
		if (provider == null)
			removeProvider();
		else
			GlobalProvider.provider = provider;
	}

	/**
	 * Removes the provider, setting it to a NO-OP implementation (so no sound).
	 *
	 * @return The previous provider.
	 */
	public static IAudioProvider removeProvider() {
		IAudioProvider oldProvider = GlobalProvider.getProvider();
		GlobalProvider.provider = new DefaultProvider();
		return oldProvider;
	}

	/**
	 * Gets the current global provider.
	 *
	 * @return The provider.
	 */
	public static IAudioProvider getProvider() {
		return provider;
	}

	@Override
	public boolean isReady() {
		return isReady.get();
	}

	@Override
	public byte[] provide() {
		return provide.get();
	}

	@Override
	public int getChannels() {
		return channels.get();
	}

	@Override
	public AudioEncodingType getAudioEncodingType() {
		return audioEncodingType.get();
	}
}
