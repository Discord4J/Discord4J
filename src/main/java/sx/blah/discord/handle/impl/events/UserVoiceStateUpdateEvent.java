package sx.blah.discord.handle.impl.events;

import sx.blah.discord.handle.Event;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

public class UserVoiceStateUpdateEvent extends Event {
    private final IUser user;
    private final IGuild guild;

    public UserVoiceStateUpdateEvent(IUser user, IGuild guild) {
        this.user = user;
        this.guild = guild;
    }
}
