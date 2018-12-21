package discord4j.voice;

public class VoiceConnection {

    private final VoiceGatewayClient vgw;

    VoiceConnection(VoiceGatewayClient vgw) {
        this.vgw = vgw;
    }

    public void disconnect() {
        System.out.println("VoiceConnection.disconnect");
        vgw.stop();
    }

}
