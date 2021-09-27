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

import discord4j.discordjson.Id;
import discord4j.discordjson.json.PartialUserData;
import discord4j.discordjson.possible.Possible;

import java.util.Optional;

class EmptyPartialUser implements PartialUserData {

    static final EmptyPartialUser INSTANCE = new EmptyPartialUser();

    @Override
    public Id id() {
        return Id.of(0);
    }

    @Override
    public Possible<String> username() {
        return Possible.absent();
    }

    @Override
    public Possible<String> discriminator() {
        return Possible.absent();
    }

    @Override
    public Possible<Optional<String>> avatar() {
        return Possible.absent();
    }

    @Override
    public Possible<Optional<String>> banner() {
        return Possible.absent();
    }

    @Override
    public Possible<Optional<Integer>> accentColor() {
        return Possible.absent();
    }

    @Override
    public Possible<Boolean> bot() {
        return Possible.absent();
    }

    @Override
    public Possible<Boolean> system() {
        return Possible.absent();
    }

    @Override
    public Possible<Boolean> mfaEnabled() {
        return Possible.absent();
    }

    @Override
    public Possible<String> locale() {
        return Possible.absent();
    }

    @Override
    public Possible<Boolean> verified() {
        return Possible.absent();
    }

    @Override
    public Possible<String> email() {
        return Possible.absent();
    }

    @Override
    public Possible<Integer> flags() {
        return Possible.absent();
    }

    @Override
    public Possible<Integer> premiumType() {
        return Possible.absent();
    }

    @Override
    public Possible<Integer> publicFlags() {
        return Possible.absent();
    }
}
