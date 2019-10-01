package discord4j.rest.json.request;


public class SuppressEmbedsRequest {

    private final boolean suppress;

    public SuppressEmbedsRequest(boolean suppress) {
        this.suppress = suppress;
    }

    @Override
    public String toString() {
        return "SuppressEmbedsRequest{" +
            "suppress=" + suppress +
            '}';
    }

}
