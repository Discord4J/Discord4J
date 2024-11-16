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

import com.google.crypto.tink.aead.internal.InsecureNonceXChaCha20Poly1305;

import java.security.GeneralSecurityException;

public class Xchacha20Poly1305EncryptionAdapter implements EncryptionAdapter {

    private static final int NONCE_BYTES = 24;
    private final InsecureNonceXChaCha20Poly1305 cipher;

    public Xchacha20Poly1305EncryptionAdapter(byte[] secretKey) throws GeneralSecurityException {
        this.cipher = new InsecureNonceXChaCha20Poly1305(secretKey);
    }

    @Override
    public int getNonceLength() {
        return Xchacha20Poly1305EncryptionAdapter.NONCE_BYTES;
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
