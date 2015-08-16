package sx.blah.discord.example;

import org.json.simple.parser.ParseException;
import sx.blah.discord.Discord4J;
import sx.blah.discord.DiscordClient;
import sx.blah.discord.obj.Message;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * @author qt
 * @since 8:00 PM 16 Aug, 2015
 * Project: DiscordAPI
 * <p>
 * Responds to users that @mention you.
 */
public class MimicBot extends DiscordClient {
    /**
     * Sets up the bot.
     *
     * @param email    Discord email.
     * @param password Discord password.
     * @throws URISyntaxException
     * @throws IOException
     * @throws ParseException
     */
    public MimicBot(String email, String password)
            throws URISyntaxException, IOException, ParseException {
        super(email, password);
    }

    /**
     * Handles message reception.
     * Will respond with "@[user], you called?"
     * when someone mentions the bot.
     *
     * @param message Message received.
     */
    @Override
    public void onMessageReceive(Message message) {
        try {
            boolean mentioned = false;
            for (String s : message.getMentionedIDs()) {
                if (s.equalsIgnoreCase(this.getOurUser().getId())) {
                    this.sendMessage("@" + message.getAuthorUsername() + ", you called?", message.getChannelID(), message.getAuthorID());
                }
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles us sending messages.
     *
     * @param m
     */
    @Override
    public void onMessageSend(Message m) {
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
        Discord4J.debug = true;
        new MimicBot(args[0] /* email */, args[1] /* password */);
    }
}
