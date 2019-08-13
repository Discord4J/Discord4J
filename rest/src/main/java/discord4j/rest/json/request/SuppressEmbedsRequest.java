package discord4j.rest.json.request;

import discord4j.common.jackson.PossibleJson;

@PossibleJson
public class SuppressEmbedsRequest {

    private final boolean suppressed;

    public SuppressEmbedsRequest(boolean suppresed) {
        this.suppressed = suppresed;
    }

    @Override
    public String toString() {
        return "SuppressEmbedsRequest{" +
            "suppresed=" + suppressed +
            '}';
    }

}
