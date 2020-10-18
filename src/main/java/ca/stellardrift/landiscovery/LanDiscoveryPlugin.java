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
package ca.stellardrift.landiscovery;

import com.google.inject.Inject;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.LinearComponents;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Server;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.event.lifecycle.StartedEngineEvent;
import org.spongepowered.api.event.lifecycle.StoppingEngineEvent;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.jvm.Plugin;

import java.util.concurrent.atomic.AtomicReference;

import static net.kyori.adventure.text.serializer.plain.PlainComponentSerializer.plain;

/**
 * A simple sponge plugin
 */
@Plugin(ProjectData.ARTIFACT_ID)
public class LanDiscoveryPlugin {

    // These are all injected on plugin load for users to work from
    private final PluginContainer container;
    private final Logger logger;
    private final Game game;

    private final AtomicReference<LanThread> lanThread = new AtomicReference<>();
    private volatile boolean muted;

    @Inject
    LanDiscoveryPlugin(final PluginContainer container, final Logger logger, final Game game) {
        this.container = container;
        this.logger = logger;
        this.game = game;
    }

    @Listener
    public void registerCommands(RegisterCommandEvent<Command.Parameterized> event) {
        event.register(this.container, Command.builder()
                .setShortDescription(Component.text("Toggle muted state of LAN discovery broadcast"))
                .setPermission("landiscovery.mute")
                .setExecutor(ctx -> {
                    setMuted(!isMuted());
                    final Component message = LinearComponents.linear(NamedTextColor.AQUA, Component.text("LAN broadcast "), muted(this.isMuted()));
                    ctx.sendMessage(Identity.nil(), message);
                    logger.info(plain().serialize(message) + " by " + ctx.getFriendlyIdentifier().orElse(ctx.getIdentifier()));
                    return CommandResult.success();
                }).build(), "lanmute");

    }

    private TextComponent muted(final boolean muted) {
        if (muted) {
            return Component.text("muted", NamedTextColor.RED);
        } else {
            return Component.text("unmuted", NamedTextColor.GREEN);
        }
    }

    @Listener
    public void onServerStarted(StartedEngineEvent<Server> event) {
        LanThread thread = new LanThread(this);
        if (lanThread.compareAndSet(null, thread)) {
            thread.start();
        }
    }

    @Listener
    public void disable(StoppingEngineEvent<Server> event) {
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
