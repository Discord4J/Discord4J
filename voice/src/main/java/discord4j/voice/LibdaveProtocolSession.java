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

import moe.kyokobot.libdave.Codec;
import moe.kyokobot.libdave.CommitResult;
import moe.kyokobot.libdave.DaveFactory;
import moe.kyokobot.libdave.Decryptor;
import moe.kyokobot.libdave.Encryptor;
import moe.kyokobot.libdave.KeyRatchet;
import moe.kyokobot.libdave.MediaType;
import moe.kyokobot.libdave.NativeDaveFactory;
import moe.kyokobot.libdave.RosterMap;
import moe.kyokobot.libdave.Session;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

final class LibdaveProtocolSession implements DaveProtocolSession {

    private static final long MLS_NEW_GROUP_EPOCH = 1L;
    private static final int INIT_TRANSITION_ID = 0;

    private final DaveFactory factory;
    private final Session session;
    private final DaveGatewayCallbacks callbacks;
    private final long channelId;
    private final String selfUserId;
    private final Encryptor selfEncryptor;
    private final Map<Long, Decryptor> decryptors = new HashMap<Long, Decryptor>();
    private final Set<Long> recognizedUserIds = new HashSet<Long>();
    private final Set<Long> activeE2eeUserIds = new HashSet<Long>();
    private final Map<Integer, Integer> pendingTransitions = new HashMap<Integer, Integer>();
    private final int maxProtocolVersion;

    private KeyRatchet selfKeyRatchet;
    private int currentProtocolVersion;
    private boolean closed;

    LibdaveProtocolSession(long selfUserId, long channelId, String authSessionId, DaveGatewayCallbacks callbacks) {
        NativeDaveFactory.ensureAvailable();
        this.factory = new NativeDaveFactory();
        this.callbacks = callbacks;
        this.channelId = channelId;
        this.selfUserId = Long.toUnsignedString(selfUserId);
        this.session = factory.createSession("", "", (source, reason) ->
                DaveProtocolSession.LOGGER.warn("MLS failure from {}: {}", source, reason));
        this.selfEncryptor = factory.createEncryptor();
        this.selfEncryptor.setPassthroughMode(true);
        this.maxProtocolVersion = factory.maxSupportedProtocolVersion();
    }

    @Override
    public synchronized int getMaxProtocolVersion() {
        return maxProtocolVersion;
    }

    @Override
    public synchronized void assignOpusSsrc(int ssrc) {
        if (closed) {
            return;
        }
        selfEncryptor.assignSsrcToCodec(ssrc, Codec.OPUS);
    }

    @Override
    public synchronized byte[] encrypt(int ssrc, byte[] audio) {
        if (closed) {
            return null;
        }

        byte[] output = new byte[selfEncryptor.getMaxCiphertextByteSize(MediaType.AUDIO, audio.length)];
        int result = selfEncryptor.encrypt(MediaType.AUDIO, ssrc, audio, output);
        return result >= 0 ? Arrays.copyOf(output, result) : null;
    }

    @Override
    public synchronized byte[] decrypt(long userId, byte[] encryptedAudio) {
        if (closed) {
            return null;
        }

        Decryptor decryptor = decryptors.get(Long.valueOf(userId));
        if (decryptor == null) {
            return null;
        }

        byte[] output = new byte[decryptor.getMaxPlaintextByteSize(MediaType.AUDIO, encryptedAudio.length)];
        int result = decryptor.decrypt(MediaType.AUDIO, encryptedAudio, output);
        return result >= 0 ? Arrays.copyOf(output, result) : null;
    }

    @Override
    public synchronized void addUser(long userId) {
        if (closed) {
            return;
        }

        recognizedUserIds.add(Long.valueOf(userId));
        ensureDecryptor(userId).transitionToPassthroughMode(currentProtocolVersion == 0);
    }

    @Override
    public synchronized void removeUser(long userId) {
        if (closed) {
            return;
        }

        recognizedUserIds.remove(Long.valueOf(userId));
        activeE2eeUserIds.remove(Long.valueOf(userId));

        Decryptor decryptor = decryptors.remove(Long.valueOf(userId));
        if (decryptor != null) {
            decryptor.close();
        }
    }

    @Override
    public synchronized void onSelectProtocolAck(int protocolVersion) {
        daveProtocolInit(protocolVersion);
    }

    @Override
    public synchronized void onDaveProtocolPrepareTransition(int transitionId, int protocolVersion) {
        prepareRatchets(transitionId, protocolVersion);
        if (transitionId != INIT_TRANSITION_ID) {
            callbacks.sendDaveProtocolReadyForTransition(transitionId);
        }
    }

    @Override
    public synchronized void onDaveProtocolExecuteTransition(int transitionId) {
        executeTransition(transitionId);
    }

    @Override
    public synchronized void onDaveProtocolPrepareEpoch(long epoch, int protocolVersion) {
        prepareEpoch(epoch, protocolVersion);
        if (epoch == MLS_NEW_GROUP_EPOCH) {
            sendMlsKeyPackage();
        }
    }

    @Override
    public synchronized void onDaveProtocolMlsExternalSenderPackage(byte[] externalSenderPackage) {
        if (closed) {
            return;
        }
        session.setExternalSender(externalSenderPackage);
    }

    @Override
    public synchronized void onMlsProposals(byte[] proposals) {
        if (closed) {
            return;
        }

        byte[] commitWelcome = session.processProposals(proposals, recognizedUserIdArray());
        if (commitWelcome != null && commitWelcome.length > 0) {
            callbacks.sendMlsCommitWelcome(commitWelcome);
        }
    }

    @Override
    public synchronized void onMlsPrepareCommitTransition(int transitionId, byte[] commit) {
        if (closed) {
            return;
        }

        CommitResult result = session.processCommit(commit);
        if (result.isIgnored()) {
            pendingTransitions.remove(Integer.valueOf(transitionId));
            return;
        }

        if (result.isFailed()) {
            callbacks.sendMlsInvalidCommitWelcome(transitionId);
            daveProtocolInit(session.getProtocolVersion());
            return;
        }

        updateActiveUsers(result.getRosterMap());
        prepareRatchets(transitionId, session.getProtocolVersion());
        if (transitionId != INIT_TRANSITION_ID) {
            callbacks.sendDaveProtocolReadyForTransition(transitionId);
        }
    }

    @Override
    public synchronized void onMlsWelcome(int transitionId, byte[] welcome) {
        if (closed) {
            return;
        }

        RosterMap roster = session.processWelcome(welcome, recognizedUserIdArray());
        if (roster == null) {
            callbacks.sendMlsInvalidCommitWelcome(transitionId);
            sendMlsKeyPackage();
            return;
        }

        updateActiveUsers(roster);
        prepareRatchets(transitionId, session.getProtocolVersion());
        if (transitionId != INIT_TRANSITION_ID) {
            callbacks.sendDaveProtocolReadyForTransition(transitionId);
        }
    }

    @Override
    public synchronized void close() {
        if (closed) {
            return;
        }

        closed = true;
        session.close();
        if (selfKeyRatchet != null) {
            selfKeyRatchet.close();
            selfKeyRatchet = null;
        }
        selfEncryptor.close();
        for (Decryptor decryptor : decryptors.values()) {
            decryptor.close();
        }
        decryptors.clear();
        recognizedUserIds.clear();
        activeE2eeUserIds.clear();
        pendingTransitions.clear();
    }

    private void daveProtocolInit(int protocolVersion) {
        if (protocolVersion > 0) {
            prepareEpoch(MLS_NEW_GROUP_EPOCH, protocolVersion);
            sendMlsKeyPackage();
            return;
        }

        activeE2eeUserIds.clear();
        prepareRatchets(INIT_TRANSITION_ID, protocolVersion);
        executeTransition(INIT_TRANSITION_ID);
    }

    private void prepareEpoch(long epoch, int protocolVersion) {
        if (closed) {
            return;
        }

        if (epoch == MLS_NEW_GROUP_EPOCH) {
            session.init(protocolVersion, channelId, selfUserId);
        }
    }

    private void prepareRatchets(int transitionId, int protocolVersion) {
        if (protocolVersion == 0) {
            for (Long userId : recognizedUserIds) {
                setupKeyRatchetForUser(Long.toUnsignedString(userId.longValue()), 0);
            }
            setupKeyRatchetForUser(selfUserId, 0);
        } else {
            for (Long userId : recognizedUserIds) {
                if (selfUserId.equals(Long.toUnsignedString(userId.longValue()))) {
                    continue;
                }

                if (activeE2eeUserIds.contains(userId)) {
                    setupKeyRatchetForUser(Long.toUnsignedString(userId.longValue()), protocolVersion);
                } else {
                    ensureDecryptor(userId.longValue()).transitionToPassthroughMode(true);
                }
            }
        }

        if (transitionId == INIT_TRANSITION_ID) {
            setupKeyRatchetForUser(selfUserId, protocolVersion);
        } else {
            pendingTransitions.put(Integer.valueOf(transitionId), Integer.valueOf(protocolVersion));
        }

        currentProtocolVersion = protocolVersion;
    }

    private void executeTransition(int transitionId) {
        Integer protocolVersion = pendingTransitions.remove(Integer.valueOf(transitionId));
        if (protocolVersion == null && transitionId != INIT_TRANSITION_ID) {
            return;
        }

        int version = protocolVersion != null ? protocolVersion.intValue() : currentProtocolVersion;
        if (version == 0) {
            session.reset();
            activeE2eeUserIds.clear();
        }

        setupKeyRatchetForUser(selfUserId, version);
    }

    private void sendMlsKeyPackage() {
        byte[] keyPackage = session.getMarshalledKeyPackage();
        if (keyPackage != null && keyPackage.length > 0) {
            callbacks.sendMlsKeyPackage(keyPackage);
        }
    }

    private void updateActiveUsers(RosterMap roster) {
        for (Map.Entry<Long, byte[]> entry : roster.entrySet()) {
            if (entry.getValue() == null || entry.getValue().length == 0) {
                activeE2eeUserIds.remove(entry.getKey());
            } else {
                activeE2eeUserIds.add(entry.getKey());
            }
        }
    }

    private void setupKeyRatchetForUser(String userId, int protocolVersion) {
        KeyRatchet keyRatchet = protocolVersion == 0 ? null : session.getKeyRatchet(userId);
        if (selfUserId.equals(userId)) {
            setSelfKeyRatchet(keyRatchet);
            return;
        }

        Decryptor decryptor = ensureDecryptor(Long.parseUnsignedLong(userId));
        if (keyRatchet != null) {
            decryptor.transitionToKeyRatchet(keyRatchet);
        } else {
            decryptor.transitionToPassthroughMode(true);
        }
    }

    private void setSelfKeyRatchet(KeyRatchet keyRatchet) {
        if (selfKeyRatchet != null) {
            selfKeyRatchet.close();
        }

        selfKeyRatchet = keyRatchet;
        if (selfKeyRatchet == null) {
            selfEncryptor.setPassthroughMode(true);
            return;
        }

        selfEncryptor.setKeyRatchet(selfKeyRatchet);
        selfEncryptor.setPassthroughMode(false);
    }

    private Decryptor ensureDecryptor(long userId) {
        Long key = Long.valueOf(userId);
        Decryptor decryptor = decryptors.get(key);
        if (decryptor != null) {
            return decryptor;
        }

        decryptor = factory.createDecryptor();
        decryptors.put(key, decryptor);
        return decryptor;
    }

    private String[] recognizedUserIdArray() {
        String[] userIds = new String[recognizedUserIds.size() + 1];
        int index = 0;
        for (Long userId : recognizedUserIds) {
            userIds[index++] = Long.toUnsignedString(userId.longValue());
        }
        userIds[index] = selfUserId;
        return userIds;
    }
}
