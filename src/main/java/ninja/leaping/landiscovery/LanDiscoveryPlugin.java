/**
 * This file is part of LanDiscovery.
 *
 * LanDiscovery is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LanDiscovery is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with LanDiscovery.  If not, see <http://www.gnu.org/licenses/>.
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
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.command.CommandException;
import org.spongepowered.api.util.command.CommandResult;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.api.util.command.args.CommandContext;
import org.spongepowered.api.util.command.spec.CommandExecutor;
import org.spongepowered.api.util.command.spec.CommandSpec;

import java.util.concurrent.atomic.AtomicReference;

/**
 * A simple sponge plugin
 */
@Plugin(id = PomData.ARTIFACT_ID, name = PomData.NAME, version = PomData.VERSION)
public class LanDiscoveryPlugin {

    // These are all injected on plugin load for users to work from
    @Inject private Logger logger;
    @Inject private Game game;

    private final AtomicReference<LanThread> lanThread = new AtomicReference<LanThread>();
    private volatile boolean muted;

    @Listener
    private void onPreInit(GamePreInitializationEvent event) {
        game.getCommandDispatcher().register(this, CommandSpec.builder()
                .description(Texts.of("Toggle muted state of LAN discovery broadcast"))
                .permission("landiscovery.mute")
                .executor(new CommandExecutor() {
                    @Override
                    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
                        setMuted(!isMuted());
                        Text message = Texts.of(TextColors.AQUA, "LAN broadcast ", muted ? Texts.of(TextColors.RED, "muted") : Texts.of(TextColors.GREEN, "unmuted"));
                        src.sendMessage(message);
                        logger.info(Texts.toPlain(message) + " by " + src.getName());
                        return CommandResult.success();
                    }
                }).build(), "lanmute");

    }

    @Listener
    private void onServerStarted(GameStartedServerEvent event) {
        LanThread thread = new LanThread(this);
        if (lanThread.compareAndSet(null, thread)) {
            thread.start();
        }
    }

    @Listener
    private void disable(GameStoppedServerEvent event) {
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
