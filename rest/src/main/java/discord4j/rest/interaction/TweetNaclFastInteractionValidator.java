package discord4j.rest.interaction;

import com.iwebpp.crypto.TweetNaclFast;
import discord4j.rest.interaction.InteractionValidator;

public class TweetNaclFastInteractionValidator implements InteractionValidator {

    private final TweetNaclFast.Signature signature;

    public TweetNaclFastInteractionValidator(String publicKey) {
        this.signature = new TweetNaclFast.Signature(hexStringToByteArray(publicKey), null);
    }

    @Override
    public boolean validateSignature(String signature, String timestamp, String body) {
        return this.signature.detached_verify((timestamp + body).getBytes(), hexStringToByteArray(signature));
    }

    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
}