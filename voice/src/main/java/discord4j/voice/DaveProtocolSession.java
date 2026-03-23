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

import moe.kyokobot.libdave.NativeDaveFactory;
import reactor.util.Logger;
import reactor.util.Loggers;

interface DaveProtocolSession extends AutoCloseable {

    Logger LOGGER = Loggers.getLogger(DaveProtocolSession.class);

    int getMaxProtocolVersion();

    void assignOpusSsrc(int ssrc);

    byte[] encrypt(int ssrc, byte[] audio);

    byte[] decrypt(long userId, byte[] encryptedAudio);

    void addUser(long userId);

    void removeUser(long userId);

    void onSelectProtocolAck(int protocolVersion);

    void onDaveProtocolPrepareTransition(int transitionId, int protocolVersion);

    void onDaveProtocolExecuteTransition(int transitionId);

    void onDaveProtocolPrepareEpoch(long epoch, int protocolVersion);

    void onDaveProtocolMlsExternalSenderPackage(byte[] externalSenderPackage);

    void onMlsProposals(byte[] proposals);

    void onMlsPrepareCommitTransition(int transitionId, byte[] commit);

    void onMlsWelcome(int transitionId, byte[] welcome);

    @Override
    void close();

    static int getMaxSupportedProtocolVersion() {
        try {
            NativeDaveFactory.ensureAvailable();
            return new NativeDaveFactory().maxSupportedProtocolVersion();
        } catch (Throwable t) {
            LOGGER.warn("Unable to query libdave-jvm max protocol version, disabling DAVE advertisement", t);
            return 0;
        }
    }

    static DaveProtocolSession create(long selfUserId, long channelId, DaveGatewayCallbacks callbacks) {
        return create(selfUserId, channelId, null, callbacks);
    }

    static DaveProtocolSession create(long selfUserId, long channelId, String authSessionId,
                                      DaveGatewayCallbacks callbacks) {
        try {
            return new LibdaveProtocolSession(selfUserId, channelId, authSessionId, callbacks);
        } catch (Throwable t) {
            LOGGER.warn("Unable to initialize libdave-jvm, falling back to passthrough mode. "
                    + "Voice connections that require DAVE will fail after March 1, 2026.", t);
            return new NoOpDaveProtocolSession();
        }
    }
}
