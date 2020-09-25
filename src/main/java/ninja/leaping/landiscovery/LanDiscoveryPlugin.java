/*
 * LanDiscovery - Broadcast the server this plugin is running on, as if it were a LAN server
 * Copyright ©2015-2020 zml
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ninja.leaping.landiscovery;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.spec.CommandSpec;

import java.util.concurrent.atomic.AtomicReference;

/**
 * A simple sponge plugin
 */
@Plugin(id = ProjectData.ARTIFACT_ID, name = ProjectData.NAME, version = ProjectData.VERSION)
public class LanDiscoveryPlugin {

    // These are all injected on plugin load for users to work from
    @Inject private Logger logger;
    @Inject private Game game;

    private final AtomicReference<LanThread> lanThread = new AtomicReference<>();
    private volatile boolean muted;

    @Listener
    public void onPreInit(GamePreInitializationEvent event) {
        game.getCommandManager().register(this, CommandSpec.builder()
                .description(Text.of("Toggle muted state of LAN discovery broadcast"))
                .permission("landiscovery.mute")
                .executor((src, args) -> {
                    setMuted(!isMuted());
                    Text message = Text.of(TextColors.AQUA, "LAN broadcast ", muted ? Text.of(TextColors.RED, "muted") : Text.of(TextColors.GREEN, "unmuted"));
                    src.sendMessage(message);
                    logger.info(message.toPlain() + " by " + src.getName());
                    return CommandResult.success();
                }).build(), "lanmute");

    }

    @Listener
    public void onServerStarted(GameStartedServerEvent event) {
        LanThread thread = new LanThread(this);
        if (lanThread.compareAndSet(null, thread)) {
            thread.start();
        }
    }

    @Listener
    public void disable(GameStoppedServerEvent event) {
        LanThread oldThread = lanThread.getAndSet(null);
        if (oldThread != null) {
            oldThread.interrupt();
        }
    }

    /**
     * Whether or not the LAN discovery broadcast is muted.
     * If this is true, broadcasts will not be sent out
     *
     * @return mute status
     */
    public boolean isMuted() {
        return this.muted;
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
    }

    Game getGame() {
        return game;
    }

    Logger getLogger() {
        return logger;
    }
}
