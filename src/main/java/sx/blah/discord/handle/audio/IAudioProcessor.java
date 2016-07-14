package sx.blah.discord.handle.audio;

/**
 * This represents an audio processor (something that manipulates raw audio data before it is sent to discord).
 */
public interface IAudioProcessor extends IAudioProvider {

	/**
	 * This sets the {@link IAudioProvider} this processor is wrapping and returns whether this processor is compatible
	 * with the provided provider instance.
	 *
	 * @param provider The provider to use. If a previous provider was wrapped, it should be replaced by this object.
	 * @return True if compatible, false if otherwise (in this case the processor will not be called to retrieve audio
	 * data.
	 */
	boolean setProvider(IAudioProvider provider);
}
