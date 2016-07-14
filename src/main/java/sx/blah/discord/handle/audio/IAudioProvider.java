package sx.blah.discord.handle.audio;

/**
 * This represents a class which can provide audio to discord.
 */
public interface IAudioProvider {

	/**
	 * This is called to determine whether the audio provider is ready to provide audio or not. When it is not ready, no
	 * audio data queries will be attempted.
	 *
	 * @return True if ready, false if otherwise.
	 */
	boolean isReady();

	/**
	 * This is called to retrieve the actual audio data. This should attempt to return 20 ms of data. The audio must
	 * also respect the encoding type returned by {@link #getAudioEncodingType()}.
	 *
	 * @return The audio data.
	 */
	byte[] provide();

	/**
	 * Gets the number of channels in this audio. NOTE: This only matters if {@link #getAudioEncodingType()} does not
	 * return {@link AudioEncodingType#OPUS}.
	 *
	 * @return The number of channels. It returns 2 (stereo) by default.
	 */
	default int getChannels() {
		return 2;
	}

	/**
	 * This is called to determine the type of audio data provided by this provider. This determines how the audio data
	 * is processed.
	 *
	 * @return The audio encoding type. By default this returns {@link AudioEncodingType#PCM}.
	 */
	default AudioEncodingType getAudioEncodingType() {
		return AudioEncodingType.PCM;
	}

	/**
	 * The types of specific audio handling Discord4J supports.
	 */
	enum AudioEncodingType {
		/**
		 * When the audio encoding type is PCM, the audio will automatically be converted to opus.
		 */
		PCM,
		/**
		 * When the audio encoding type is opus, the audio is sent directly to discord as is.
		 */
		OPUS,
	}
}
