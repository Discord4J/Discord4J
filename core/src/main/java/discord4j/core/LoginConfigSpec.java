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

package discord4j.core;

import discord4j.core.spec.Spec;
import reactor.core.scheduler.Scheduler;

public class LoginConfigSpec implements Spec<LoginConfig> {

    private final ServiceMediator serviceMediator;

    private Scheduler blockScheduler;

    LoginConfigSpec(ServiceMediator serviceMediator) {
        this.serviceMediator = serviceMediator;
    }

    /**
     * Define the {@link Scheduler} used to wait until the created gateway is disconnected.
     *
     * @param blockScheduler a {@link Scheduler}
     * @return this spec
     */
    public LoginConfigSpec setBlockUntilLogout(Scheduler blockScheduler) {
        this.blockScheduler = blockScheduler;
        return this;
    }

    @Override
    public LoginConfig asRequest() {
        return new LoginConfig(blockScheduler);
    }
}
