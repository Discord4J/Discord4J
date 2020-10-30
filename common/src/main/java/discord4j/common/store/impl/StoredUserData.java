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

package discord4j.common.store.impl;

import discord4j.discordjson.json.UserData;
import discord4j.discordjson.possible.Possible;

import java.util.Optional;

import static discord4j.common.store.impl.ImplUtils.*;

class StoredUserData {
    private final long id;
    private final String username;
    private final short discriminator;
    private final String avatar;
    private final boolean bot_value;
    private final boolean bot_absent;
    private final boolean system_value;
    private final boolean system_absent;
    private final boolean mfaEnabled_value;
    private final boolean mfaEnabled_absent;
    private final String locale_value;
    private final boolean locale_absent;
    private final boolean verified_value;
    private final boolean verified_absent;
    private final String email_value;
    private final boolean email_absent;
    private final int flags_value;
    private final boolean flags_absent;
    private final int premiumType_value;
    private final boolean premiumType_absent;
    private final int publicFlags_value;
    private final boolean publicFlags_absent;

    StoredUserData(UserData original) {
        this.id = toLongId(original.id());
        this.username = original.username();
        this.discriminator = Short.parseShort(original.discriminator());
        this.avatar = original.avatar().orElse(null);
        this.bot_value = original.bot().toOptional().orElse(false);
        this.bot_absent = original.bot().isAbsent();
        this.system_value = original.system().toOptional().orElse(false);
        this.system_absent = original.system().isAbsent();
        this.mfaEnabled_value = original.mfaEnabled().toOptional().orElse(false);
        this.mfaEnabled_absent = original.mfaEnabled().isAbsent();
        this.locale_value = original.locale().toOptional().orElse(null);
        this.locale_absent = original.locale().isAbsent();
        this.verified_value = original.verified().toOptional().orElse(false);
        this.verified_absent = original.verified().isAbsent();
        this.email_value = Possible.flatOpt(original.email()).orElse(null);
        this.email_absent = original.email().isAbsent();
        this.flags_value = original.flags().toOptional().orElse(-1);
        this.flags_absent = original.flags().isAbsent();
        this.premiumType_value = original.premiumType().toOptional().orElse(-1);
        this.premiumType_absent = original.premiumType().isAbsent();
        this.publicFlags_value = original.publicFlags().toOptional().orElse(-1);
        this.publicFlags_absent = original.publicFlags().isAbsent();
    }

    long longId() {
        return id;
    }

    String stringId() {
        return "" + id;
    }

    UserData toImmutable() {
        return UserData.builder()
                .id("" + id)
                .username(username)
                .discriminator("" + discriminator)
                .avatar(Optional.ofNullable(avatar))
                .bot(toPossible(bot_value, bot_absent))
                .system(toPossible(system_value, system_absent))
                .mfaEnabled(toPossible(mfaEnabled_value, mfaEnabled_absent))
                .locale(toPossible(locale_value, locale_absent))
                .verified(toPossible(verified_value, verified_absent))
                .email(toPossibleOptional(email_value, email_absent))
                .flags(toPossible(flags_value, flags_absent))
                .premiumType(toPossible(premiumType_value, premiumType_absent))
                .publicFlags(toPossible(publicFlags_value, publicFlags_absent))
                .build();
    }
}
