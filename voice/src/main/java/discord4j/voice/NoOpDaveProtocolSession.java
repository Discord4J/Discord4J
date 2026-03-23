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

package discord4j.voice;

final class NoOpDaveProtocolSession implements DaveProtocolSession {

    @Override
    public int getMaxProtocolVersion() {
        return 0;
    }

    @Override
    public void assignOpusSsrc(int ssrc) {
    }

    @Override
    public byte[] encrypt(int ssrc, byte[] audio) {
        return audio;
    }

    @Override
    public byte[] decrypt(long userId, byte[] encryptedAudio) {
        return encryptedAudio;
    }

    @Override
    public void addUser(long userId) {
    }

    @Override
    public void removeUser(long userId) {
    }

    @Override
    public void onSelectProtocolAck(int protocolVersion) {
    }

    @Override
    public void onDaveProtocolPrepareTransition(int transitionId, int protocolVersion) {
    }

    @Override
    public void onDaveProtocolExecuteTransition(int transitionId) {
    }

    @Override
    public void onDaveProtocolPrepareEpoch(long epoch, int protocolVersion) {
    }

    @Override
    public void onDaveProtocolMlsExternalSenderPackage(byte[] externalSenderPackage) {
    }

    @Override
    public void onMlsProposals(byte[] proposals) {
    }

    @Override
    public void onMlsPrepareCommitTransition(int transitionId, byte[] commit) {
    }

    @Override
    public void onMlsWelcome(int transitionId, byte[] welcome) {
    }

    @Override
    public void close() {
    }
}
