package sx.blah.discord.example;

import org.json.simple.parser.ParseException;
import sx.blah.discord.Discord4J;
import sx.blah.discord.DiscordClient;
import sx.blah.discord.obj.Message;
import sx.blah.discord.obj.User;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author qt
 * @since 8:00 PM 16 Aug, 2015
 * Project: DiscordAPI
 * <p>
 * Responds to users that @mention you.
 */
public class ReplyBot extends DiscordClient {
    /**
     * Private cache of messages our bot has sent.
     */
    private List<Message> ourMessages = new ArrayList<>();

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
            for (Message message : ourMessages) {
                try {
                    Discord4J.debug("Attempting deletion of message " + message.getMessageID());
                    this.deleteMessage(message.getMessageID(), message.getChannelID());
                } catch (IOException e) {
                    System.err.println("Couldn't delete message " + message.getMessageID() + " (" + e.getMessage() + ").");
                }
            }
        }
    }

    /**
     * Handles us sending messages.
     * @param m The message we sent
     */
    @Override
    public void onMessageSend(Message m) {
        this.ourMessages.add(m);
    }

    /**
     * When our bot gets mentioned in a message.
     *
     * @param message Message sent
     */
    @Override
    public void onMentioned(Message message) {
        try {
            this.sendMessage("@" + message.getAuthorUsername() + ", you called?", message.getChannelID(), message.getAuthorID());
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
     * @param messageID
     * @param channelID
     */
    @Override
    public void onMessageDelete(String messageID, String channelID) {

    }

    /**
     * Starts the bot. This can be done any place you want.
     * The main method is for demonstration.
     * @param args Command line arguments passed to the program.
     * @throws ParseException
     * @throws IOException
     * @throws URISyntaxException
     */
    public static void main(String... args) throws ParseException, IOException, URISyntaxException {
        Discord4J.debug = true;
        new ReplyBot(args[0] /* email */, args[1] /* password */);
    }
}
