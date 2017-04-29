## Message History

### What is message history?

`MessageHistory` is Discord4J's way of grabbing the history of a channel. It grabs various amounts of messages depending on the method used.

### Getting message History

There are a lot of ways to get `MessageHistory`. All of them are in `IChannel`.

***IChannel#getMessageHistory***

This gets all the messages in the channel that are currently cached. (i.e all the bot has seen since it was booted up.)

Code:

```java
        MessageHistory history = channel.getMessageHistory();
        Iterator<IMessage> i = history.iterator();
        while(i.hasNext()) {
        	// do stuff here
        }
```


***IChannel#getMessageHistory(int)***

If the integer provided is less than or equal to the cached messages, return the cached message else retrieve messages up to the amount provided.

Code:
```java
        MessageHistory history = channel.getMessageHistory(100);
        Iterator<IMessage> i = history.iterator();
        while(i.hasNext()) {
        	// do stuff here
        }
```


***IChannel#getMessageHistoryFrom(long)***

The long represents the ID of an `IMessage`. This grabs the message specified and the ones before it.

Code:
```java
        MessageHistory history = channel.getMessageHistoryFrom(Long.parseUnsignedLong("307953094258327562"));
        Iterator<IMessage> i = history.iterator();
        while (i.hasNext()) {
        	// do stuff here
        }
```


***IChannel#getMessageHistoryFrom(long, int)***

This is the same as above, but the integer indicates the max amount of `IMessage` objects to grab.


***IChannel#getMessageHistoryFrom(LocalDateTime)***

This is essentially the same as `IChannel#getMessageHistoryFrom(long)`, except it grabs messages by their date, based against the specified LocalDateTime.

***IChannel#getMessageHistoryFrom(LocalDateTime, int)***

This is essentially the same as `IChannel#getMessageHistoryFrom(long, int)`, except it grabs messages by their date, based against the specified LocalDateTime.

***IChannel#getMessageHistoryTo(arguments)***

All of these methods act like the ones above, except for the fact that they grab the `IMessage` objects from now, to the specified arguments.

***IChannel#getFullMessageHistory()***

This gets the full history of the channel.
