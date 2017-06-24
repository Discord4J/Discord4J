# MessageTokenizer

### Getting started

Making a `MessageTokenizer` is quite easy. You either pass an `IDiscordClient` and a `String` (String being the content), or an `IMessage` to the constructor.

# Methods
There are a *lot* of methods in this class. All very helpful. I will be going over all of them and how they interact with each other. I will cover all of them with this format:

> Return Type: type
> 
> Throws: exceptions

Description here

### stepForward(int)
> Return Type: int
> 
> Throws: Nothing

This just steps forward in the content to the index of `(currentIndex + specifiedIndex)`, so if your currentIndex is `4` and you call `stepForward(5)`, afterwards the `currentIndex` will be `9`.

### stepTo(int)
> Return Type: int
> 
> Throws: Nothing

This just steps to the specified index.

### hasNext()
> Return Type: int
> 
> Throws: Nothing

`true` if you're not at the end of the string; otherwise `false`.

### hasNextChar()
> Return Type: boolean
> 
> Throws: Nothing

The same has hasNext()


### nextChar()
> Return Type: int
> 
> Throws: IllegalStateException

```java
    if(!this.hasNextChar()) {
      throw new IllegalStateException("Reached end of string!");
    }
```

Otherwise, return the character at `currentIndex` and step forward one (1).

### hasNextSequence(String)

> Return Type: boolean
>
> Throws: Nothing

Returns `true` if the remaining content has the specified String; otherwise `false`.

### hasNextWord()

> Return Type: boolean
> 
> Throws: Nothing

Same as `hasNext()`

### nextWord()
> Return Type: MessageTokenizer.Token
>
> Throws: IllegalStateException

```java
    if(!this.hasNextWord()) {
      throw new IllegalStateException("No more words found!");
    }
```
Otherwise, grabs the next word in the sequence.

### hasNextLine()

> Return Type: boolean
> 
> Throws: Nothing

Same as `hasNext()`


### nextLine()

> Return Type: MessageTokenizer.Token
>
> Throws: IllegalStateException

```java
  if(!this.hasNextLine()) {
      throw new IllegalStateException("No more lines found!");
    }
```

Otherwise, grabs the next line in the sequence.

### hasNextPattern(Pattern)

> Return Type: boolean
> 
> Throws: Nothing

`true` if `hasNext()` is true **and** the given pattern has a match in the remaining content.

### nextPattern(Pattern)

> Return Type: MessageTokenizer.Token
>
> Throws: IllegalStateException

```java
    if(!this.hasNextRegex(pattern)) {
      throw new IllegalStateException("No more occurrences found!");
    }
```
```java
      if(!matcher.find()) {
        throw new IllegalStateException("Couldn't find any matches!");
      }
```
 
 Otherwise, returns the regex in the content and steps to the end of it.

### hasNextInvite()

> Return Type: boolean
>
> Throws: Nothing

`true` if there's an invite in the message, otherwise `false`.

### nextInvite()

> Return Type: MessageTokenizer.InviteToken
> 
> Throws: IllegalStateException

```java
    if(!this.hasNextInvite()) {
      throw new IllegalStateException("No more invites found!");
    }
```

```java
      if(!matcher.find()) {
        throw new IllegalStateException("Couldn't find any matches!");
      }
```

Otherwise, returns the next Invite found (Example: `discord.gg/NxGAeCY`) and steps to the end of it.

### hasNextMention()

> Return Type: boolean
>
> Throws: Nothing

`true` if there is another mention (channel, user, role, etc..) in the message, otherwise `false`

### nextMention()

> Return Type: MessageTokenizer.MentionToken
>
> Throws: IllegalStateException

```java
    if(!this.hasNextMention()) {
      throw new IllegalStateException("No more mentions found!");
    }
```

```java
      } else {
        throw new IllegalStateException("Couldn't find a mention even though it was found!");
      }
```

Otherwise, returns a `MessageTokenizer.RoleMentionToken`, `MessageTokenizer.UserMentionToken`, or a `MessageTokenizer.ChannelMentionToken`.

### hasNextEmoji()

> Return Type: boolean
>
> Throws: Nothing

`true` if there is an emoji in the remaining content, otherwise `false`

### nextEmoji()

> Return Type: MessageTokenizer.CustomEmojiToken
>
> Throws: IllegalStateException

```java
    if(!this.hasNextEmoji()) {
      throw new IllegalStateException("No more custom server emojis found!");
    } 
```

Otherwise, returns the emoji in the sequence.


### hasNextUnicodeEmoji(Emoji)

> Return Type: boolean
>
> Throws: Nothing

`true` if the unicode exists in the remaining content, otherwise `false`

### nextUnicodeEmoji(Emoji)

> Return Type: MessageTokenizer.UnicodeEmojiToken
>
> Throws: Nothing

Returns the next specified unicode emoji in the sequence.

### getContent()

> Return Type: String
>
> Throws: Nothing

Returns the content of the `MessageTokenizer`.

### getClient()

> Return Type: IDiscordClient
>
> Throws: Nothing

Gets the client associated with that `MessageTokenizer`.

### getCurrentPosition()

> Return Type: int
>
> Throws: Nothing

The current position the `MessageTokenizer` is in the content

### getRemainingContent()
> Return Type: String
>
> Throws: Nothing

Gets the remaining content the `MessageTokenizer` has left.
