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

import com.google.crypto.tink.aead.internal.InsecureNonceAesGcmJce;

import java.security.GeneralSecurityException;

public class Aes256GcmEncryptionAdapter implements EncryptionAdapter {

    private static final int NONCE_BYTES = 12;
    private final InsecureNonceAesGcmJce cipher;

    public Aes256GcmEncryptionAdapter(byte[] secretKey) throws GeneralSecurityException {
        this.cipher = new InsecureNonceAesGcmJce(secretKey);
    }

    @Override
    public int getNonceLength() {
        return Aes256GcmEncryptionAdapter.NONCE_BYTES;
    }

    @Override
    public byte[] encrypt(byte[] header, byte[] audio, byte[] nonce) throws GeneralSecurityException {
        return this.cipher.encrypt(nonce, audio, header);
    }

    @Override
    public byte[] decrypt(byte[] header, byte[] audio, byte[] nonce) throws GeneralSecurityException {
        return this.cipher.decrypt(nonce, audio, header);
    }

}
