package sx.blah.discord.example;

import org.json.simple.parser.ParseException;
import sx.blah.discord.Discord4J;
import sx.blah.discord.DiscordClient;
import sx.blah.discord.obj.Invite;
import sx.blah.discord.obj.Message;
import sx.blah.discord.obj.User;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * @author qt
 * @since 8:00 PM 16 Aug, 2015
 * Project: DiscordAPI
 * <p>
 * Responds to users that @mention you.
 */
public class ReplyBot extends DiscordClient {
    /**
     * Sets up the bot.
     *
     * @param email    Discord email.
     * @param password Discord password.
     * @throws URISyntaxException
     * @throws IOException
     * @throws ParseException
     */
    public ReplyBot(String email, String password)
            throws URISyntaxException, IOException, ParseException {
        super(email, password);
    }

    /**
     * Handles message reception.
     *
     * @param m Message received.
     */
    @Override
    public void onMessageReceive(Message m) {
        if (m.getContent().startsWith(".meme")
                || m.getContent().startsWith(".nicememe")) {
            try {
                sendMessage("http://niceme.me/", m.getChannelID());
            } catch (IOException | ParseException e) {
                e.printStackTrace();
            }
        } else if (m.getContent().startsWith(".clear")) {
            for (Message message : this.getChannelByID(m.getChannelID()).getMessages()) {
                if (message.getAuthorID().equalsIgnoreCase(this.getOurUser().getId())) {
                    try {
                        Discord4J.logger.debug("Attempting deletion of message {} by \"{}\" ({})", message.getMessageID(), message.getAuthorUsername(), message.getContent());
                        this.deleteMessage(message.getMessageID(), message.getChannelID());
                    } catch (IOException e) {
                        Discord4J.logger.error("Couldn't delete message {} ({}).", message.getMessageID(), e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * Handles us sending messages.
     *
     * @param m The message we sent
     */
    @Override
    public void onMessageSend(Message m) {
    }

    /**
     * When our bot gets mentioned in a message.
     *
     * @param message Message sent
     */
    @Override
    public void onMentioned(Message message) {
        try {
            if (message.getContent().contains("https://discord.gg/")
                    || message.getContent().contains("http://discord.gg/")) {
                String invite = message.getContent().split(".gg/")[1].split(" ")[0];
                Discord4J.logger.debug("Received invite code \"{}\"", invite);
                Invite invite1 = this.acceptInvite(invite);
                if (null != invite1) {
                    this.sendMessage(String.format("Hello, %s! I was invited to the %s channel by @%s.",
                            invite1.getGuildName(), invite1.getChannelName(), invite1.getInviterUsername()), invite1.getChannelID(), invite1.getInviterID());
                }
            } else {
                this.sendMessage("@" + message.getAuthorUsername() + ", you called?", message.getChannelID(), message.getAuthorID());
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * Called when a user starts typing.
     *
     * @param userID    The user's ID.
     * @param channelID The channel they type in.
     */
    @Override
    public void onStartTyping(String userID, String channelID) {

    }

    /**
     * Called when a user changes presence
     *
     * @param user     The user whose presence changed.
     * @param presence The presence they have changed to (online/idle/offline. Not sure if there are more)
     */
    @Override
    public void onPresenceChange(User user, String presence) {

    }

    /**
     * Called when a message is edited.
     * <p>
     * It does not give us any info about the original message,
     * so you should cache all messages.
     *
     * @param message Edited message
     */
    @Override
    public void onMessageUpdate(Message message) {

    }

    /**
     * Called when a message is deleted.
     * Unfortunately not much info is given to us.
     * If you'd like to see content that was deleted,
     * I recommend you to cache all messages.
     *
     * @param messageID The message that was deleted
     * @param channelID The channel that the message was deleted from
     */
    @Override
    public void onMessageDelete(String messageID, String channelID) {

    }

    /**
     * Starts the bot. This can be done any place you want.
     * The main method is for demonstration.
     *
     * @param args Command line arguments passed to the program.
     * @throws ParseException
     * @throws IOException
     * @throws URISyntaxException
     */
    public static void main(String... args) throws ParseException, IOException, URISyntaxException {
        new ReplyBot(args[0] /* email */, args[1] /* password */);
    }
}
