package discord4j.core.event.domain.guild;

import discord4j.core.DiscordClient;
import discord4j.core.object.entity.Member;

import javax.annotation.Nullable;
import java.util.Optional;

public class MemberUpdateEvent extends GuildEvent {

    private final Member current;
    private final Member old;

    public MemberUpdateEvent(DiscordClient client, Member current, @Nullable Member old) {
        super(client);
        this.current = current;
        this.old = old;
    }

    public Member getCurrent() {
        return current;
    }

    public Optional<Member> getOld() {
        return Optional.ofNullable(old);
    }
}
