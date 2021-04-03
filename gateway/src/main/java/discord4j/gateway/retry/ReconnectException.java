package discord4j.gateway.retry;

import reactor.util.context.ContextView;

/**
 * An exception class to handle gateway reconnects via reconnect opcode.
 */
public class ReconnectException extends GatewayException {

    public ReconnectException(ContextView context, String message) {
        super(context, message);
    }
}

