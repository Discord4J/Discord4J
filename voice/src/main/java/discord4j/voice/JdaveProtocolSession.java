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

import club.minnced.discord.jdave.DaveCodec;
import club.minnced.discord.jdave.DaveDecryptor;
import club.minnced.discord.jdave.DaveEncryptor;
import club.minnced.discord.jdave.DaveMediaType;
import club.minnced.discord.jdave.manager.DaveSessionManager;
import club.minnced.discord.jdave.manager.DaveSessionManagerCallbacks;

import java.nio.ByteBuffer;

final class JdaveProtocolSession implements DaveProtocolSession {

    private final DaveSessionManager manager;

    private final ResizableDirectByteBuffer encryptInput = new ResizableDirectByteBuffer(512);
    private final ResizableDirectByteBuffer encryptOutput = new ResizableDirectByteBuffer(512);
    private final ResizableDirectByteBuffer decryptInput = new ResizableDirectByteBuffer(512);
    private final ResizableDirectByteBuffer decryptOutput = new ResizableDirectByteBuffer(512);
    private final ResizableDirectByteBuffer externalSenderInput = new ResizableDirectByteBuffer(512);
    private final ResizableDirectByteBuffer proposalsInput = new ResizableDirectByteBuffer(512);
    private final ResizableDirectByteBuffer commitInput = new ResizableDirectByteBuffer(512);
    private final ResizableDirectByteBuffer welcomeInput = new ResizableDirectByteBuffer(512);

    JdaveProtocolSession(long selfUserId, long channelId, String authSessionId, DaveGatewayCallbacks callbacks) {
        DaveSessionManagerCallbacks managerCallbacks = new DaveSessionManagerCallbacks() {
            @Override
            public void sendMLSKeyPackage(ByteBuffer mlsKeyPackage) {
                callbacks.sendMlsKeyPackage(copyByteBuffer(mlsKeyPackage));
            }

            @Override
            public void sendDaveProtocolReadyForTransition(int transitionId) {
                callbacks.sendDaveProtocolReadyForTransition(transitionId);
            }

            @Override
            public void sendMLSCommitWelcome(ByteBuffer commitWelcomeMessage) {
                callbacks.sendMlsCommitWelcome(copyByteBuffer(commitWelcomeMessage));
            }

            @Override
            public void sendMLSInvalidCommitWelcome(int transitionId) {
                callbacks.sendMlsInvalidCommitWelcome(transitionId);
            }
        };

        this.manager = authSessionId != null && !authSessionId.isEmpty()
                ? DaveSessionManager.create(selfUserId, channelId, managerCallbacks, authSessionId)
                : DaveSessionManager.create(selfUserId, channelId, managerCallbacks);
    }

    @Override
    public synchronized int getMaxProtocolVersion() {
        return manager.getMaxProtocolVersion();
    }

    @Override
    public synchronized void assignOpusSsrc(int ssrc) {
        manager.assignSsrcToCodec(DaveCodec.OPUS, ssrc);
    }

    @Override
    public synchronized byte[] encrypt(int ssrc, byte[] audio) {
        ByteBuffer input = encryptInput.write(audio);
        ByteBuffer output = encryptOutput.prepare(manager.getMaxEncryptedFrameSize(DaveMediaType.AUDIO, audio.length));
        DaveEncryptor.DaveEncryptResultType result = manager.encrypt(DaveMediaType.AUDIO, ssrc, input, output);
        return result == DaveEncryptor.DaveEncryptResultType.SUCCESS ? encryptOutput.read() : null;
    }

    @Override
    public synchronized byte[] decrypt(long userId, byte[] encryptedAudio) {
        ByteBuffer input = decryptInput.write(encryptedAudio);
        ByteBuffer output = decryptOutput.prepare(
                manager.getMaxDecryptedFrameSize(DaveMediaType.AUDIO, userId, encryptedAudio.length));
        DaveDecryptor.DaveDecryptResultType result = manager.decrypt(DaveMediaType.AUDIO, userId, input, output);
        return result == DaveDecryptor.DaveDecryptResultType.SUCCESS ? decryptOutput.read() : null;
    }

    @Override
    public synchronized void addUser(long userId) {
        manager.addUser(userId);
    }

    @Override
    public synchronized void removeUser(long userId) {
        manager.removeUser(userId);
    }

    @Override
    public synchronized void onSelectProtocolAck(int protocolVersion) {
        manager.onSelectProtocolAck(protocolVersion);
    }

    @Override
    public synchronized void onDaveProtocolPrepareTransition(int transitionId, int protocolVersion) {
        manager.onDaveProtocolPrepareTransition(transitionId, protocolVersion);
    }

    @Override
    public synchronized void onDaveProtocolExecuteTransition(int transitionId) {
        manager.onDaveProtocolExecuteTransition(transitionId);
    }

    @Override
    public synchronized void onDaveProtocolPrepareEpoch(long epoch, int protocolVersion) {
        manager.onDaveProtocolPrepareEpoch(epoch, protocolVersion);
    }

    @Override
    public synchronized void onDaveProtocolMlsExternalSenderPackage(byte[] externalSenderPackage) {
        manager.onDaveProtocolMLSExternalSenderPackage(externalSenderInput.write(externalSenderPackage));
    }

    @Override
    public synchronized void onMlsProposals(byte[] proposals) {
        manager.onMLSProposals(proposalsInput.write(proposals));
    }

    @Override
    public synchronized void onMlsPrepareCommitTransition(int transitionId, byte[] commit) {
        manager.onMLSPrepareCommitTransition(transitionId, commitInput.write(commit));
    }

    @Override
    public synchronized void onMlsWelcome(int transitionId, byte[] welcome) {
        manager.onMLSWelcome(transitionId, welcomeInput.write(welcome));
    }

    @Override
    public synchronized void close() {
        manager.close();
    }

    private static byte[] copyByteBuffer(ByteBuffer buffer) {
        ByteBuffer copy = buffer.duplicate();
        byte[] bytes = new byte[copy.remaining()];
        copy.get(bytes);
        return bytes;
    }
}
