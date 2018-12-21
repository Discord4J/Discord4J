package discord4j.voice;

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
