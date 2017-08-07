## Reactions and how they work

### What are reactions?

Whenever a message is sent in Discord, users can add "reactions" to the message. These can be anything from a heart, the poop emoji, or any of the custom ones you can put on servers. It looks like this: 

![Reactions](https://i.imgur.com/Bh0bOLq.png)

### How do I add them?

Well users can manually add them by right clicking on a message or hitting the little face next to the message when they hover over it. But a bot needs to add them a little differently. To add an reaction there's a few ways to do it. You can either use the unicode version, or `ReactionEmoji`.


### Reaction Emoji

`ReactionEmoji` is a class made to make reactions easier. To get a `ReactionEmoji` you can use any of the three `ReactionEmoji#of` methods. You can react to any message with `IMessage#react(ReactionEmoji)`

- `ReactionEmoji.of(IEmoji)`
This is for if you have a custom emoji and want to react with it (`IEmoji` are emojis that are guild specific).

- `ReactionEmoji.of(String)`
This one is for the unicode of an emoji. (For instance, `❤` is the unicode for `:heart:`. This can be gotten on any non-custom emoji as `\:emoji:`, in this case `\:heart:`)

- `ReactionEmoji.of(String, long)`
This is for custom emojis from other servers. Since bots can do that now. I took the :eyesR: emoji from one of the servers my bot is in and used it for this. To get the name (String) and the ID (long), you just type this into your chat: `\:emoji:`, so in my case `\:eyesR:`. This gives me `<:eyesR:284174731261902850>`. It'll be different for every server. Using the following code I can achieve the following image:

![eyesR](https://i.imgur.com/A9HkAo5.gif)

Code:
```java
        ReactionEmoji reaction = ReactionEmoji.of("eyesR", 284174731261902850L);
        message.addReaction(reaction);
```


### Emoji Manager

Emoji Manager uses the [Emoji-Java](https://github.com/vdurmont/emoji-java) library. This way is really simple. Just use `EmojiManager#getForAlias(String)`. For the above result just simple do this:

Code:
```java
Emoji e = EmojiManager.getForAlias("heart");
message.addReaction(e);
```

Remember to not use colons when grabbing it by alias. Unfortunatley EmojiJava doesn't support all emojis, such as `:track_next:`.

### Final Note

EmojiManager should really only be used when you have to get the `Emoji` by shortcode (for instance, the word `heart` in `:heart:`). Ohterwise, use ReactionEmoji, it's much better.. 
