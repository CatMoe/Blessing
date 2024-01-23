/*
 * Copyright (C) 2023-2024. CatMoe / Blessing Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.miaomoe.blessing.fallback.config;

import net.miaomoe.blessing.config.AbstractConfig;
import net.miaomoe.blessing.config.annotation.Description;
import net.miaomoe.blessing.config.annotation.Path;
import net.miaomoe.blessing.nbt.dimension.World;

import java.util.logging.Logger;

public class FallbackConfig extends AbstractConfig {

    // Settings for logger - not in config.
    public Logger debugLogger = null;

    @Path
    @Description(description = {
            "What brand should be provided to the client?",
            "It can be view in f3 in the client.",
            "",
            "MiniMessage is supported. But only can use legacy color."
    })
    public String brand = "Blessing";

    @Path(path = "player-name")
    @Description(description = "What should be the name of the player joining the fallback?")
    public String playerName = "Blessing";

    @Path(path = "validate-order-check")
    @Description(description = {
            "Check whether each packet is sent in order",
            "",
            "If you accidentally get disconnected.",
            "You can try disable this check and try again."
    })
    public boolean validate = true;

    @Path
    @Description(description = {
            "The world that is presented to connected clients",
            "It also involves dimension and nbt.",
            "",
            "If you are causing problems while using a particular world.",
            "Please switch to another world. Then report the issue."
    })
    public World world = World.OVERWORLD;

    @Path
    @Description(description =
            "Disconnects when the connection is idle for more than a few milliseconds"
    )
    public long timeout = 30000;

}
