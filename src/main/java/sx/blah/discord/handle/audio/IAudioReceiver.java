package sx.blah.discord.handle.audio;

import sx.blah.discord.handle.obj.IUser;

/**
 * Handles the receiving of audio from Discord.
 */
public interface IAudioReceiver {

	/**
	 * Called when audio is received.
	 *
	 * @param audio The packet of audio. Format depends on {{@link #getAudioEncodingType()}}
	 * @param user The user this audio was received from.
	 */
	void receive(byte[] audio, IUser user);

	/**
	 * This is called to determine the type of audio data provided by this receiver. This determines how the audio data
	 * is processed.
	 *
	 * @return The audio encoding type. By default this returns {@link AudioEncodingType#PCM}.
	 */
	default AudioEncodingType getAudioEncodingType() {
		return AudioEncodingType.PCM;
	}
}
