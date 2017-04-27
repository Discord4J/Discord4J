## Reactions and how they work

### What are reactions?

Whenever a message is sent in Discord, users can add "reactions" to the message. These can be anything from a heart, the poop emoji, or any of the custom ones you can put on servers. It looks like this: 

![Reactions](https://i.imgur.com/Bh0bOLq.png)

### How do I add them?

Well users can manually add them by right clicking on a message or hitting the little face next to the message when they hover over it. But a bot needs to add them a little differently. To add an reaction there's a few ways to do it. You can either use the unicode version, or EmojiManager. (Custom emojis we will get to later)

### Unicode Emojis

Unicode emojis are just the unicode version of the emoji (this method doesn't work with server added emojis). To grab this all you need to do is send a message in Discord. Take the emoji name `:heart:`, and just add a backslash. Like `\:heart:`. Now copy and paste that into an IMessage#addReaction(String) and proof! Suddenly it works. Example here:

![Reaction_By_Unicode](https://i.imgur.com/pZmhITs.gif)

Code:
```java
message.addReaction("❤");
```


### Emoji Manager

Emoji Manager uses the [Emoji-Java](https://github.com/vdurmont/emoji-java) library. This way is really simple. Just use EmojiManager#getForAlias(String). For the above result just simple do this:

Code:
```java
Emoji e = EmojiManager.getForAlias("heart");
message.addReaction(e);
```

Remember to not use colons when grabbing it by alias. Unfortunatley EmojiJava doesn't support all emojis, such as `:track_next:`.

### Custom Emojis

Custom server emojis are a little different. Trying to grab the unicode version of my custom emoji `:NootLikeThis:` returns `<:NootLikeThis:306996261075288065>` so we can't use that, and EmojiManager doesn't support that. The best way to accomplish this is by using a mix of IMessage#addReaction(IEmoji) and IGuild#getEmojiByName(String). I can grab and add `:NootLikeThis:` with this:

![Custom_Emoji](https://i.imgur.com/5si0e23.gif)

Code:
```java
IEmoji e = guild.getEmojiByName("NootLikeThis"); 
message.addReaction(e);
```

### Final Note

Unicode is likely the best way to go. You can find a large list of unicode emojis [here.](http://unicode.org/emoji/charts/full-emoji-list.html)
