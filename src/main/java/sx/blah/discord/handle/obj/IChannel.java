package sx.blah.discord.handle.obj;

import sx.blah.discord.handle.impl.obj.PrivateChannel;
import sx.blah.discord.util.HTTP403Exception;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Defines a text channel in a guild/server.
 */
public interface IChannel {
    
    /**
     * Gets the name of this channel.
     * 
     * @return The channel name.
     */
    String getName();
    
    /**
     * Gets the id of this channel.
     * 
     * @return The channel id.
     */
    String getID();
    
    /**
     * Gets the messages in this channel.
     * 
     * @return The list of messages in the channel.
     */
    List<IMessage> getMessages();
    
    /**
     * Gets a specific message by its id.
     * 
     * @param messageID The message id.
     * @return The message (if found).
     */
    IMessage getMessageByID(String messageID);
    
    /**
     * Gets the guild this channel is a part of.
     * 
     * @return The guild.
     * @deprecated Use {@link #getGuild()} instead.
     */
    @Deprecated
    IGuild getParent();
    
    /**
     * Gets the guild this channel is a part of.
     * 
     * @return The guild.
     */
    IGuild getGuild();
    
    /**
     * Gets whether or not this channel is a private one–if it is a private one, this object is an instance of {@link PrivateChannel}.
     * 
     * @return True if the channel is private, false if otherwise.
     */
    boolean isPrivate();
    
    /**
     * Gets the topic for the channel.
     * 
     * @return The channel topic (null if not set).
     */
    String getTopic();
    
    /**
     * Formats a string to be able to #mention this channel.
     * 
     * @return The formatted string.
     */
    String mention();
    
    /**
     * Sends a message without tts to the desired channel.
     *
     * @param content The content of the message.
     * @return The message object representing the sent message
     */
    IMessage sendMessage(String content);
    
    /**
     * Sends a message to the desired channel.
     *
     * @param content The content of the message.
     * @param tts Whether the message should use tts or not.
     * @return The message object representing the sent message
     */
    IMessage sendMessage(String content, boolean tts);
    
    /**
     * Sends a file to the channel.
     * 
     * @param file The file to send.
     * @return The message sent.
     * 
     * @throws HTTP403Exception
     * @throws IOException
     */
    IMessage sendFile(File file) throws HTTP403Exception, IOException;
    
    /**
     * Generates an invite for this channel.
     * 
     * @param maxAge How long the invite should be valid, setting it to 0 makes it last forever.
     * @param maxUses The maximum uses for the invite, setting it to 0 makes the invite have unlimited uses.
     * @param temporary Whether users admitted with this invite are temporary.
     * @param useXkcdPass Whether to generate a human-readable code, maxAge cannot be 0 for this to work.
     * @return The newly generated invite.
	 */
    IInvite createInvite(int maxAge, int maxUses, boolean temporary, boolean useXkcdPass);
    
    /**
     * Toggles whether the bot is "typing".
     *
     */
    void toggleTypingStatus();
    
    /**
     * Gets whether the bot is "typing".
     *
     * @return True if the bot is typing, false if otherwise.
     */
    boolean getTypingStatus();
    
    /**
     * Gets the last read message id.
     * 
     * @return The message id.
     */
    String getLastReadMessageID();
    
    /**
     * Gets the last read message
     * 
     * @return The message.
     */
    IMessage getLastReadMessage();
}
