package discord4j.gateway.retry;

/**
 * An exception class to handle gateway reconnects via reconnect opcode
 */
public class ReconnectException extends RuntimeException {

    public ReconnectException(String message) {
        super(message);
    }
}

