package discord4j.voice;

/**
 * Allows for manipulation of an already-established voice connection.
 */
public class VoiceConnection {

    private final VoiceGatewayClient vgw;
    private final Runnable leaveChannel;

    VoiceConnection(VoiceGatewayClient vgw, Runnable leaveChannel) {
        this.vgw = vgw;
        this.leaveChannel = leaveChannel;
    }

    public void disconnect() {
        vgw.stop();
        leaveChannel.run();
    }

}
