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
