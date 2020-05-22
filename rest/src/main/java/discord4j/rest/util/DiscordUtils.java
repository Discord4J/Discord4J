package discord4j.rest.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class DiscordUtils {

    /**
     * Extracts the bot user's ID from the token used to authenticate requests.
     *
     * @param token The bot token used to authenticate requests.
     * @return The bot user's ID.
     */
    public static long getSelfId(String token) {
        return Long.parseLong(new String(Base64.getDecoder()
                .decode(token.split("\\.")[0]), StandardCharsets.UTF_8));
    }

}
