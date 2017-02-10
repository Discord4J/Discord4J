package sx.blah.discord.handle.audio;

import sx.blah.discord.handle.obj.IUser;

public interface IAudioReceiver {
	void receive(byte[] audio, IUser user);

	default AudioEncodingType getAudioEncodingType() {
		return AudioEncodingType.PCM;
	}
}
