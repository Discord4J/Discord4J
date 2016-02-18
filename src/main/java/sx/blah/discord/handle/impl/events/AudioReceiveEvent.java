package sx.blah.discord.handle.impl.events;

import sx.blah.discord.api.internal.audio.AudioPacket;
import sx.blah.discord.handle.Event;

public class AudioReceiveEvent extends Event {
    private final AudioPacket packet;

    public AudioReceiveEvent(AudioPacket packet) {
        this.packet = packet;
    }

    public byte[] getAudio(){
        return packet.getRawAudio();
    }
}
