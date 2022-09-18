/*
 * LanDiscovery - Broadcast the server this plugin is running on, as if it were a LAN server
 * Copyright ©2015-2022 zml
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

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

/**
 * This class originally from Spout's Vanilla project, adapted for Sponge and changes in Minecraft.
 *
 * <p>The original file from Spout is Copyright (c) 2011 Spout LLC <a href="http://www.spout.org/">http://www.spout.org</a>,
 * available under the terms of the MIT license.</p>
 */
final class LanThread extends Thread {
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * broadcast interval in seconds
     */
    private static final int BROADCAST_INTERVAL = 2;
    private static final InetAddress BROADCAST_ADDRESS;
    private static final int BROADCAST_PORT = 4445;

    static {
        try {
            BROADCAST_ADDRESS = InetAddress.getByName("224.0.2.60");
        } catch (final UnknownHostException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final LanDiscoveryPlugin plugin;
    private MulticastSocket socket;
    private int broadcastInterval = BROADCAST_INTERVAL;

    LanThread(final LanDiscoveryPlugin plugin) {
        super("LAN Discovery");
        this.setDaemon(true);
        this.plugin = plugin;
    }

    @Override
    public void start() {
        try {
            this.socket = new MulticastSocket();
        } catch (final IOException ex) {
            LOGGER.warn("Failed to create socket for LAN discovery", ex);
        }
        super.start();
    }

    private byte[] getContents() {
        // format: [MOTD]<motd in legacy formatting>[/MOTD][AD]<port number>[/AD]

        return String.format(
                "[MOTD]%s[/MOTD][AD]%d[/AD]",
                LegacyComponentSerializer.legacySection().serialize(this.plugin.game().server().motd()),
                this.plugin.game().server().boundAddress().map(InetSocketAddress::getPort).orElse(25565)
        ).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public void run() {
        while (!this.isInterrupted()) {
            if (!this.plugin.muted()) {
                try {
                    final byte[] contents = this.getContents();
                    this.socket.send(new DatagramPacket(contents, contents.length, BROADCAST_ADDRESS, BROADCAST_PORT));
                    this.broadcastInterval = BROADCAST_INTERVAL; // reset to default once we can successfully send a broadcast
                } catch (final IOException e) {
                    this.broadcastInterval *= 2;
                    LOGGER.error("Error sending out discovery broadcast, increasing delay to {}: {}", this.broadcastInterval, e.getLocalizedMessage(), e);
                }
            }

            try {
                Thread.sleep(this.broadcastInterval * 1000L);
            } catch (final InterruptedException e1) {
                break;
            }
        }
    }
}
