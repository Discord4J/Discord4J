package discord4j.voice;

import reactor.core.publisher.Mono;

/**
 * Allows for manipulation of an already-established voice connection.
 */
public class VoiceConnection {

    private final VoiceGatewayClient vgw;
    private final Mono<Void> leaveChannel;

    VoiceConnection(VoiceGatewayClient vgw, Mono<Void> leaveChannel) {
        this.vgw = vgw;
        this.leaveChannel = leaveChannel;
    }

    public Mono<Void> disconnect() {
        return Mono.fromRunnable(vgw::stop).then(leaveChannel);
    }

}
