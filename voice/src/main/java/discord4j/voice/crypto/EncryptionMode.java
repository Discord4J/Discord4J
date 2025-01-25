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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */

package discord4j.voice.crypto;

import java.security.Security;

public enum EncryptionMode {

    AEAD_AES256_GCM("aead_aes256_gcm_rtpsize"),
    AEAD_XCHACHA20_POLY1305("aead_xchacha20_poly1305_rtpsize");

    private final String value;

    EncryptionMode(String value) {
        this.value = value;
    }

    public boolean isAvailable() {
        switch (this) {
            case AEAD_AES256_GCM:
                return Security.getAlgorithms("Cipher").contains("AES_256/GCM/NOPADDING");
            case AEAD_XCHACHA20_POLY1305:
                return true;
            default:
                return false;
        }
    }

    public static EncryptionMode getBestMode() {
        for (EncryptionMode value : EncryptionMode.values()) {
            if (value.isAvailable()) {
                return value;
            }
        }

        return null;
    }

    public String getValue() {
        return this.value;
    }
}
