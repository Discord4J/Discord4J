/*
 * Discord4J - Unofficial wrapper for Discord API
 * Copyright (c) 2015
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package sx.blah.discord;

import sx.blah.discord.handle.impl.AnnotatedEventDispatcher;
import sx.blah.discord.handle.impl.EventSubscriber;
import sx.blah.discord.handle.impl.events.MentionEvent;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;

/**
 * @author qt
 * @since 9:18 PM, 10/16/15
 * Project: Discord4J
 */
public class AnnotatedTestBot {
    AnnotatedTestBot(String username, String password) {
        try {
            DiscordClient.get().login(username, password);

            DiscordClient.get().setDispatcher(new AnnotatedEventDispatcher());
            DiscordClient.get().getDispatcher().registerListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String... args) {
        new AnnotatedTestBot(args[0], args[1]);
    }

    @EventSubscriber public void meme(MessageReceivedEvent event) {
        if(event.getMessage().getContent().startsWith(".!test")) {
            try {
                event.getMessage().reply("It works!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @EventSubscriber public void meme2(MentionEvent event) {
        try {
            event.getMessage().reply("It works!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
