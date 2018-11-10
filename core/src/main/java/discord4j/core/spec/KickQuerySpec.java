package discord4j.core.spec;

import java.util.HashMap;
import java.util.Map;

public class KickQuerySpec implements Spec<Map<String, Object>> {

    private final Map<String, Object> request = new HashMap<>(2);

    public KickQuerySpec setReason(final String reason) {
        request.put("reason", reason);
        return this;
    }

    @Override
    public Map<String, Object> asRequest() {
        return request;
    }
}
