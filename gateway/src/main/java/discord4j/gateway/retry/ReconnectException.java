package discord4j.gateway.retry;

import reactor.util.context.Context;

/**
 * An exception class to handle gateway reconnects via reconnect opcode.
 */
public class ReconnectException extends GatewayException {

    public ReconnectException(Context context, String message) {
        super(context, message);
    }
}

