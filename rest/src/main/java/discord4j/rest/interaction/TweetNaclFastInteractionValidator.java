/*
 * This file is part of Discord4J.
 *
 * Discord4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Discord4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J. If not, see <http://www.gnu.org/licenses/>.
 */

package discord4j.rest.interaction;

import com.iwebpp.crypto.TweetNaclFast;

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