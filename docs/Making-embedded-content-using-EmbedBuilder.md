## Embedded content

### What are embeds?

Embeds are the pretty rich text content you see that looks a little something like this:
![Baristron](http://i.imgur.com/KvJQ6Ko.png)

The above example is from the bot "Baristron" developed by chrislo27

### How do I make them?

Below is a graphical example with the partner code used to represent it. In D4J all embeds are created through the `EmbedBuilder` class. The code should be fairly self explanatory. For full details about any of the methods used to configure the builder, please check the `EmbedBuilder` javadocs.

Embed:

![Embed example](http://i.imgur.com/0zCADHo.png)

Code:

```java
    EmbedBuilder builder = new EmbedBuilder();

    builder.appendField("fieldTitleInline", "fieldContentInline", true);
    builder.appendField("fieldTitleInline2", "fieldContentInline2", true);
    builder.appendField("fieldTitleNotInline", "fieldContentNotInline", false);
    builder.appendField(":tada: fieldWithCoolThings :tada:", "[hiddenLink](http://i.imgur.com/Y9utuDe.png)", false);

    builder.withAuthorName("authorName");
    builder.withAuthorIcon("http://i.imgur.com/PB0Soqj.png");
    builder.withAuthorUrl("http://i.imgur.com/oPvYFj3.png");

    builder.withColor(255, 0, 0);
    builder.withDesc("withDesc");
    builder.withDescription("withDescription");
    builder.withTitle("withTitle");
    builder.withTimestamp(100);
    builder.withUrl("http://i.imgur.com/IrEVKQq.png");
    builder.withImage("http://i.imgur.com/agsp5Re.png");

    builder.withFooterIcon("http://i.imgur.com/Ch0wy1e.png");
    builder.withFooterText("footerText");
    builder.withFooterIcon("http://i.imgur.com/TELh8OT.png");
    builder.withThumbnail("http://i.imgur.com/7heQOCt.png");

    builder.appendDesc(" + appendDesc");
    builder.appendDescription(" + appendDescription");

    RequestBuffer.request(() -> event.getChannel().sendMessage(builder.build()));
```