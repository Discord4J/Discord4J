package discord4j.core.object.entity.channel;

import discord4j.common.annotations.Experimental;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import discord4j.core.retriever.EntityRetrievalStrategy;
import discord4j.discordjson.json.ChannelData;
import discord4j.discordjson.json.ThreadMetadata;
import discord4j.rest.util.PermissionSet;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.function.Function;

@Experimental
public final class ThreadChannel extends BaseChannel implements GuildMessageChannel {

    public ThreadChannel(GatewayDiscordClient gateway, ChannelData data) {
        super(gateway, data);
    }

    public Snowflake getStarterId() {
        return getData().ownerId().toOptional() // owner_id is repurposed for the starter
                .map(Snowflake::of)
                .orElseThrow(IllegalStateException::new); // should always be present for threads
    }

    // TODO: should this be Member? What if they're not in the guild anymore? Do we consider that anywhere else?
    public Mono<User> getStarter() {
        return getClient().getUserById(getStarterId());
    }

    public Mono<User> getStarter(EntityRetrievalStrategy retrievalStrategy) {
        return getClient().withRetrievalStrategy(retrievalStrategy).getUserById(getStarterId());
    }

    public Optional<Snowflake> getParentId() {
        return getData().parentId().toOptional()
                .flatMap(Function.identity())
                .map(Snowflake::of);
    }

    public Mono<TopLevelGuildMessageChannel> getParent() {
        return Mono.justOrEmpty(getParentId())
                .flatMap(getClient()::getChannelById)
                .cast(TopLevelGuildMessageChannel.class);
    }

    public Mono<TopLevelGuildMessageChannel> getParent(EntityRetrievalStrategy retrievalStrategy) {
        return Mono.justOrEmpty(getParentId())
                .flatMap(getClient().withRetrievalStrategy(retrievalStrategy)::getChannelById)
                .cast(TopLevelGuildMessageChannel.class);
    }

    public int getApproximateMessageCount() {
        return getData().messageCount().toOptional()
                .orElseThrow(IllegalStateException::new); // should always be present for threads
    }

    public int getApproximateMemberCount() {
        return getData().memberCount().toOptional()
                .orElseThrow(IllegalStateException::new); // should always be present for threads
    }

    public boolean isArchived() {
        return getMetadata().archived();
    }

    public Instant getArchiveTimestamp() {
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(getMetadata().archiveTimestamp(), Instant::from);
    }

    public AutoArchiveDuration getAutoArchiveDuration() {
        return AutoArchiveDuration.of(getMetadata().autoArchiveDuration());
    }

    public boolean isLocked() {
        return getMetadata().locked().toOptional().orElse(false);
    }

    public boolean isPrivate() {
        return Type.of(getData().type()) == Type.GUILD_PRIVATE_THREAD;
    }

    private ThreadMetadata getMetadata() {
        return getData().threadMetadata().toOptional()
                .orElseThrow(IllegalStateException::new); // should always be present for threads
    }

    @Override
    public Mono<PermissionSet> getEffectivePermissions(Snowflake memberId) {
        return getParent().flatMap(parent -> parent.getEffectivePermissions(memberId));
    }

    @Override
    public Mono<PermissionSet> getEffectivePermissions(Member member) {
        return getParent().flatMap(parent -> parent.getEffectivePermissions(member));
    }

    public enum AutoArchiveDuration {

        // TODO naming
        UNKNOWN(-1),
        DURATION1(60),
        DURATION2(1440),
        DURATION3(4320),
        DURATION4(10080);

        private final int value;

        AutoArchiveDuration(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public Duration asDuration() {
            return Duration.ofMinutes(value);
        }

        public static AutoArchiveDuration of(int value) {
            switch (value) {
                case 60: return DURATION1;
                case 1440: return DURATION2;
                case 4320: return DURATION3;
                case 10080: return DURATION4;
                default: return UNKNOWN;
            }
        }
    }

    @Override
    public String toString() {
        return "ThreadChannel{} " + super.toString();
    }
}
