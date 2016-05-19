package sx.blah.discord.util.audio.processors;

import sx.blah.discord.handle.audio.IAudioProcessor;
import sx.blah.discord.handle.audio.IAudioProvider;
import sx.blah.discord.handle.audio.impl.DefaultProvider;

/**
 * This processor implementation allows for audio providers to be paused.
 */
public class PauseableProcessor implements IAudioProcessor {

	private volatile IAudioProvider provider = new DefaultProvider();
	private volatile boolean isPaused = false;

	/**
	 * This gets whether this processor is paused.
	 *
	 * @return True if paused, false if otherwise.
	 */
	public boolean isPaused() {
		return isPaused;
	}

	/**
	 * This sets whether this processor is paused.
	 *
	 * @param isPaused True to pause, false to resume.
	 */
	public void setPaused(boolean isPaused) {
		this.isPaused = isPaused;
	}

	@Override
	public boolean setProvider(IAudioProvider provider) {
		this.provider = provider;
		return true;
	}

	@Override
	public boolean isReady() {
		return provider.isReady() && !isPaused;
	}

	@Override
	public byte[] provide() {
		if (!isPaused) {
			return provider.provide();
		}
		return new byte[0];
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
