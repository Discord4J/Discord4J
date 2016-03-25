package sx.blah.discord.handle.impl.events;

import sx.blah.discord.api.internal.AudioPacket;
import sx.blah.discord.api.Event;

/**
 * This event is dispatched when audio is received.
 */
public class AudioReceiveEvent extends Event {

    private final AudioPacket packet;

    public AudioReceiveEvent(AudioPacket packet) {
        this.packet = packet;
    }

	/**
	 * Gets the audio data received.
	 *
	 * @return The audio data.
	 */
    public byte[] getAudio(){
        return packet.getRawAudio();
    }
}
