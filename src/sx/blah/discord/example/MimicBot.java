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
 * This example bot will mimic everything anyone says if
 * the message starts with the letter 'a'.
 * Use sparingly.
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
     * Will immediately send the same content back to the channel
     * upon reception of message that starts with "a".
     *
     * @param message Message received.
     */
    @Override
    public void onMessageReceive(Message message) {
        try {
            if (message.getContent().charAt(0) == 'a'
                    || message.getContent().charAt(0) == 'A')
                this.sendMessage(message.getContent(), message.getChannel_id());
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
        new MimicBot("e-mail", "password");
    }
}
