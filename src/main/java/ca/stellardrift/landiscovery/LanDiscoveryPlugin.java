/*
 * Copyright (c) 2015-2022 zml and contributors
 * This file is part of LanDiscovery.
 *
 * LanDiscovery is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LanDiscovery is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with LanDiscovery.  If not, see <https://www.gnu.org/licenses/>.
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
import org.spongepowered.plugin.builtin.jvm.Plugin;

import java.util.concurrent.atomic.AtomicReference;

import static net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText;


/**
 * A simple sponge plugin
 */
@Plugin(ProjectData.ARTIFACT_ID)
public final class LanDiscoveryPlugin {

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
    public void registerCommands(final RegisterCommandEvent<Command.Parameterized> event) {
        event.register(this.container, Command.builder()
                .shortDescription(Component.text("Toggle muted state of LAN discovery broadcast"))
                .permission("landiscovery.mute")
                .executor(ctx -> {
                    this.muted(!this.muted());
                    final Component message = LinearComponents.linear(NamedTextColor.AQUA, Component.text("LAN broadcast "), this.mutedLabel(this.muted()));
                    ctx.sendMessage(Identity.nil(), message);
                    this.logger.info("{} by {}", plainText().serialize(message), ctx.friendlyIdentifier().orElse(ctx.identifier()));
                    return CommandResult.success();
                }).build(), "lanmute");

    }

    private TextComponent mutedLabel(final boolean muted) {
        if (muted) {
            return Component.text("muted", NamedTextColor.RED);
        } else {
            return Component.text("unmuted", NamedTextColor.GREEN);
        }
    }

    @Listener
    public void onServerStarted(final StartedEngineEvent<Server> event) {
        if (event.engine().isDedicatedServer()) {
            final LanThread thread = new LanThread(this);
            if (this.lanThread.compareAndSet(null, thread)) {
                thread.start();
            }
        }
    }

    @Listener
    public void disable(final StoppingEngineEvent<Server> event) {
        final LanThread oldThread = this.lanThread.getAndSet(null);
        if (oldThread != null) {
            oldThread.interrupt();
        }
    }

    /**
     * Whether or not the LAN discovery broadcast is muted.
     *
     * <p>If this is true, broadcasts will not be sent out.</p>
     *
     * @return mute status
     */
    public boolean muted() {
        return this.muted;
    }

    public void muted(final boolean muted) {
        this.muted = muted;
    }

    Game game() {
        return this.game;
    }
}
