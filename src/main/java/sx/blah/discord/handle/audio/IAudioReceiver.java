package sx.blah.discord.handle.audio;

import sx.blah.discord.handle.obj.IUser;

/**
 * This represents a class which can receive audio from discord. Use
 * {@link IAudioManager#subscribeReceiver(IAudioReceiver, IUser)} and
 * {@link IAudioManager#subscribeReceiver(IAudioReceiver)} in order to utilize a receiver.
 */
@FunctionalInterface
public interface IAudioReceiver {
	
	/**
	 * This is called whenever discord sends an audio packet.
	 *
	 * @param audio This represents the raw audio bytes provided by discord in the encoding provided by
	 * {@link #getAudioEncodingType()}. This is 20 milliseconds worth of data.
	 * @param user The user who sent this audio.
	 */
	void receive(byte[] audio, IUser user);
	
	/**
	 * This is called to determine the type of audio data provided to this receiver.
	 *
	 * @return The audio encoding type. By default this returns {@link AudioEncodingType#PCM}.
	 */
	default AudioEncodingType getAudioEncodingType() {
		return AudioEncodingType.PCM;
	}
}
