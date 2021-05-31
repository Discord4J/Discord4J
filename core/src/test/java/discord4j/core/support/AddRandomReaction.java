/*
 * This file is part of Discord4J.
 *
 * Discord4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Discord4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J. If not, see <http://www.gnu.org/licenses/>.
 */

package discord4j.core.support;

import discord4j.core.command.Command;
import discord4j.core.command.CommandRequest;
import discord4j.core.command.CommandResponse;
import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class AddRandomReaction implements Command {

    private final Random random = new Random();
    private final List<String> emoji = new ArrayList<>(Arrays.asList(
            "😀", "😬", "😂", "😄", "😅", "😇", "☺", "😋", "😘", "😚", "😜", "🤑", "😎", "🤗", "😳", "🙄", "😤",
            "😱", "😨", "😰", "😥", "🤒", "😭", "💩", "👹", "💀", "👻", "👽", "🤖", "😺", "😹", "😻", "😼", "😽",
            "🙀", "😿", "😾", "🙌", "👏", "👋", "👍", "👊", "✊", "✌", "👌", "✋", "👐", "💪", "☝", "🙏", "👆",
            "🖐", "🤘", "🖖", "✍", "💅", "👄", "👅", "👂", "👁", "👀", "👶", "👦", "👧", "👨", "👩", "👱", "👴",
            "👵", "👲", "👳", "👷", "💂", "🕵", "🎅", "👼", "👸", "👰", "🚶", "🏃", "💃", "👯", "👫", "👬", "👭",
            "🙇", "💁", "🙅", "🙆", "🙋", "🙎", "🙍", "💇", "💆", "💑", "👨‍❤️‍👨", "💏", "👩‍❤️‍💋‍👩",
            "👨‍❤️‍💋‍👨", "👩‍👩‍👦", "👨‍👨‍👦", "👮", "👚", "👕", "👖", "👔", "👗", "👙", "👘", "💄", "💋",
            "🎩", "👟", "👞", "👢", "👡", "👠", "👣", "⛑", "🎓", "👑", "🎒", "👝", "👛", "👜", "💼", "🌂", "💍",
            "🕶", "👓", "🐯", "🦁", "🐮", "🐷", "🐽", "🐸", "🐙", "🐵", "🐦", "🐧", "🐔", "🐒", "🙉", "🙈", "🐣",
            "🐥", "🐺", "🐗", "🐴", "🦄", "🐝", "🐛", "🐢", "🦀", "🦂", "🕷", "🐜", "🐞", "🐌", "🐠", "🐟", "🐡",
            "🐬", "🐋", "🐊", "🐆", "🐘", "🐫", "🐪", "🐄", "🐂", "🐃", "🐏", "🐑", "🐀", "🐁", "🐓", "🦃", "🐉",
            "🐾", "🐿", "🐇", "🐈", "🐩", "🐕", "🐲", "🌵", "🎄", "🌲", "🌴", "🌱", "🌿", "🌾", "🍁", "🍂", "🍃",
            "🎋", "🎍", "🍀", "🌺", "🌻", "🌹", "🌷", "🌼", "🌸", "💐", "🍄", "🎃", "🐚", "🌎", "🌍", "🌏", "🌕",
            "🌖", "🌗", "🌘", "🌑", "🌒", "🌓", "🌔", "🌚", "🌝", "🌛", "🌜", "🌞", "⭐", "🌟", "💫", "✨", "🌥",
            "🌦", "🌧", "⛈", "⚡", "🔥", "❄", "🌨", "☔", "☂", "🌪", "💨", "☃", "⛄", "💧", "💦", "🌊", "🍏", "🍎",
            "🍐", "🍋", "🍌", "🍉", "🍇", "🌶", "🍅", "🍍", "🍑", "🍈", "🍓", "🌽", "🍠", "🍯", "🍞", "🍗", "🧀",
            "🍖", "🍤", "🌯", "🌮", "🍝", "🍕", "🌭", "🍟", "🍔", "🍳", "🍜", "🍲", "🍥", "🍣", "🍱", "🍛", "🍙",
            "🍚", "🎂", "🍰", "🍦", "🍨", "🍧", "🍡", "🍢", "🍘", "🍮", "🍬", "🍭", "🍫", "🍿", "🍩", "🍪", "🍺",
            "☕", "🍵", "🍶", "🍹", "🍻", "🍼", "🍴", "🍷", "🍽", "⚽", "🏀", "🏈", "⚾", "🎾", "🏐", "🏉", "🎱",
            "🎿", "🏏", "🏑", "🏓", "🏌", "⛳", "⛷", "🏂", "⛸", "🏹", "🎣", "🚣", "🏊🏼", "🏄", "🏆", "🕴", "🏇",
            "🚵", "🚴", "🏋", "⛹", "🛀", "🎽", "🏅", "🎖", "🎗", "🏵", "🎫", "🎟", "🎭", "🎺", "🎷", "🎹", "🎤",
            "🎪", "🎨", "🎸", "🎻", "🎬", "🎮", "👾", "🎯", "🎲", "🎰", "🎳", "🚗", "🚕", "🚙", "🚌", "🚎", "🏎",
            "🚓", "🚒", "🚐", "🚛", "🚜", "🏍", "🚲", "🚨", "🚃", "🚟", "🚠", "🚡", "🚖", "🚘", "🚍", "🚔", "🚋",
            "🚝", "🚄", "🚅", "🚈", "🚞", "🚂", "🚆", "🛬", "🛫", "✈", "🛩", "🚁", "🚉", "🚊", "🚇", "⛵", "🛥",
            "🚤", "⛴", "🚀", "🛳", "🛰", "💺", "🏁", "🚥", "🚦", "🚏", "⛽", "🚧", "⚓", "🎡", "🎢", "🎠", "🏗",
            "🌁", "🗼", "🏭", "⛲", "⛺", "🏕", "🗾", "🌋", "🗻", "🏔", "⛰", "🎑", "🏞", "🛣", "🛤", "🌅", "🌄",
            "🏜", "🏖", "🏝", "🎇", "🌠", "🌌", "🌉", "🌃", "🏙", "🌆", "🌇", "🎆", "🌈", "🏘", "🏰", "🏯", "🏠",
            "🗽", "🏟", "🏡", "🏚", "🏢", "🏬", "🏣", "🏤", "🏥", "🏦", "🕌", "🏛", "💒", "🏩", "🏫", "🏪", "🏨",
            "🕍", "🕋", "⛩", "🕹", "💽", "💾", "💿", "📼", "📷", "📹", "🎥", "☎", "⏱", "🎙", "📻", "📺", "📠",
            "📟", "⏲", "⏰", "🕰", "⏳", "📡", "🔋", "💴", "💵", "💸", "🛢", "🔦", "💡", "💶", "💷", "💰", "💳",
            "💎", "🔨", "💣", "🔫", "🔪", "☠", "🔮", "💈", "💊", "💉", "🔖", "🚿", "🔑", "🛋", "🚪", "🛎", "🖼",
            "🎁", "🎀", "🎏", "🎈", "🛍", "⛱", "🗺", "🎊", "🎉", "🎎", "🎐", "🎌", "🏮", "📮", "📫", "📯", "📊",
            "🗃", "📇", "📅", "📉", "📈", "📰", "📕", "📙", "📒", "✂", "🖇", "📖", "📚", "📌", "📍", "🚩", "❤",
            "💔", "❣", "💕", "💓", "💗", "💖", "💘", "💝", "💠", "🔔"));

    @Override
    public Mono<Void> apply(CommandRequest request, CommandResponse response) {
        Message message = request.getMessage();
        String rawCount = request.parameters();
        int count = 1;
        if (!rawCount.isEmpty()) {
            try {
                count = Math.max(1, Math.min(20, Integer.parseInt(rawCount.trim())));
            } catch (NumberFormatException e) {
                throw Exceptions.propagate(e);
            }
        }
        return Flux.fromIterable(fetch(count))
                .flatMap(emoji -> message.addReaction(ReactionEmoji.unicode(emoji)))
                .then();
    }

    private List<String> fetch(int count) {
        List<String> reactions = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int index = random.nextInt(emoji.size());
            reactions.add(emoji.get(index));
            emoji.remove(index);
        }
        return reactions;
    }
}
